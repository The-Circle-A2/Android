package com.pedro.rtmp.rtmp

import android.media.MediaCodec
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.pedro.rtmp.flv.FlvPacket
import com.pedro.rtmp.flv.FlvType
import com.pedro.rtmp.flv.SignaturePacket
import com.pedro.rtmp.flv.audio.AacPacket
import com.pedro.rtmp.flv.audio.AudioPacketCallback
import com.pedro.rtmp.flv.video.H264Packet
import com.pedro.rtmp.flv.video.ProfileIop
import com.pedro.rtmp.flv.video.VideoPacketCallback
import com.pedro.rtmp.utils.BitrateManager
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.*
import kotlin.math.min

/**
 * Created by pedro on 8/04/21.
 */
class RtmpSender(private val connectCheckerRtmp: ConnectCheckerRtmp, private val commandsManager: CommandsManager) : AudioPacketCallback, VideoPacketCallback {

    private var aacPacket = AacPacket(this)
    private var h264Packet = H264Packet(this)
    private var running = false

    @Volatile
    private var flvPacketBlockingQueue: BlockingQueue<FlvPacket> = LinkedBlockingQueue(60)
    @Volatile
    private var videoSignatureBlockingDeque: BlockingDeque<FlvPacket> = LinkedBlockingDeque(60)
    @Volatile
    private var audioSignatureBlockingDeque: BlockingDeque<FlvPacket> = LinkedBlockingDeque(60)
    private val signatureSize: Int = 5
    private var streamThread: HandlerThread? = null
    private var signatureThread: HandlerThread? = null
    private var audioFramesSent: Long = 0
    private var videoFramesSent: Long = 0
    var output: OutputStream? = null
    var droppedAudioFrames: Long = 0
    private set
    var droppedVideoFrames: Long = 0
    private set
        private val bitrateManager: BitrateManager = BitrateManager(connectCheckerRtmp)
    private var isEnableLogs = true
    private var isFirstVideoPacket = true
    private var isFirstAudioPacket = true

    companion object {
      private const val TAG = "RtmpSender"
      private const val SIG_TAG = "$TAG Signature"
    }

    fun setVideoInfo(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
      h264Packet.sendVideoInfo(sps, pps)
    }

    fun setProfileIop(profileIop: ProfileIop) {
      h264Packet.profileIop = profileIop
    }

    fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {
      aacPacket.sendAudioInfo(sampleRate, isStereo)
    }

    fun sendVideoFrame(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
      if (running) h264Packet.createFlvAudioPacket(h264Buffer, info)
    }

    fun sendAudioFrame(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
      if (running) aacPacket.createFlvAudioPacket(aacBuffer, info)
    }

    override fun onVideoFrameCreated(flvPacket: FlvPacket) {
      try {
        if (isFirstVideoPacket) {
            isFirstAudioPacket = false
        } else {
            flvPacketBlockingQueue.add(flvPacket)
            videoSignatureBlockingDeque.addLast(flvPacket)
        }
      } catch (e: IllegalStateException) {
        Log.i(TAG, "Video frame discarded")
        droppedVideoFrames++
      }
    }

    override fun onAudioFrameCreated(flvPacket: FlvPacket) {
      try {
        if (isFirstAudioPacket) {
            isFirstAudioPacket = false
        } else {
            flvPacketBlockingQueue.add(flvPacket)
            audioSignatureBlockingDeque.addLast(flvPacket)
        }
      } catch (e: IllegalStateException) {
        Log.i(TAG, "Audio frame discarded")
        droppedAudioFrames++
      }
    }

    fun start() {
      streamThread = HandlerThread("$TAG StreamingThread")
      streamThread?.start()
      streamThread?.let {
        val h = Handler(it.looper)
        running = true
        h.post {
          while (!Thread.interrupted()) {
            try {
              val flvPacket = flvPacketBlockingQueue.poll(1, TimeUnit.SECONDS)
              if (flvPacket == null) {
                Log.i(TAG, "Skipping iteration, frame null")
                continue
              }
              var size = 0
              if (flvPacket.type == FlvType.VIDEO) {
                videoFramesSent++
                output?.let { output ->
                  size = commandsManager.sendVideoPacket(flvPacket, output)
                  if (isEnableLogs) {
                    Log.i(TAG, "wrote Video packet, size $size")
                  }
                }
              } else {
                audioFramesSent++
                output?.let { output ->
                  size = commandsManager.sendAudioPacket(flvPacket, output)
                  if (isEnableLogs) {
                    Log.i(TAG, "wrote Audio packet, size $size")
                  }
                }
              }
              //bytes to bits
              bitrateManager.calculateBitrate(size * 8L)
            } catch (e: Exception) {
              //InterruptedException is only when you disconnect manually, you don't need report it.
              if (e !is InterruptedException) {
                connectCheckerRtmp.onConnectionFailedRtmp("Error send packet, " + e.message)
                Log.e(TAG, "send error: ", e)
              }
              return@post
            }
              // Part of the loop that does the signatures
              try {
                  //Check for both video and audio
                  checkAndMakeSignature(videoSignatureBlockingDeque, FlvType.VIDEO)
                  checkAndMakeSignature(audioSignatureBlockingDeque, FlvType.AUDIO)

                  //Check if there are enough audio packets for a signature
              } catch (e: Exception) {
                  //InterruptedException is only when you disconnect manually, you don't need report it.
                  if (e !is InterruptedException) {
                      connectCheckerRtmp.onConnectionFailedRtmp("Error signing packets, ${e.message}")
                      Log.e(TAG, "send error: ", e)
                  }
              }
          }
        }
      }
    }

    fun stop(clear: Boolean = true) {
      running = false

      //Stop the stream thread
      streamThread?.looper?.thread?.interrupt()
      streamThread?.looper?.quit()
      streamThread?.quit()
      try {
        streamThread?.join(100)
      } catch (e: Exception) { }
      streamThread = null

      //Stop the signature thread
      signatureThread?.looper?.thread?.interrupt()
      signatureThread?.looper?.quit()
      signatureThread?.quit()
      try {
        signatureThread?.join(100)
      } catch (e: Exception) { }
      signatureThread = null

      flvPacketBlockingQueue.clear()
      videoSignatureBlockingDeque.clear()
      audioSignatureBlockingDeque.clear()
      aacPacket.reset()
      h264Packet.reset(clear)
      resetSentAudioFrames()
      resetSentVideoFrames()
      resetDroppedAudioFrames()
      resetDroppedVideoFrames()
    }

    fun hasCongestion(): Boolean {
      val size = flvPacketBlockingQueue.size.toFloat()
      val remaining = flvPacketBlockingQueue.remainingCapacity().toFloat()
      val capacity = size + remaining
      return size >= capacity * 0.2f //more than 20% queue used. You could have congestion
    }

    fun resizeCache(newSize: Int) {
      if (newSize < flvPacketBlockingQueue.size - flvPacketBlockingQueue.remainingCapacity()) {
        throw RuntimeException("Can't fit current cache inside new cache size")
      }
      val tempQueue: BlockingQueue<FlvPacket> = LinkedBlockingQueue(newSize)
      flvPacketBlockingQueue.drainTo(tempQueue)
      flvPacketBlockingQueue = tempQueue
    }

    fun getCacheSize(): Int {
      return flvPacketBlockingQueue.size
    }

    fun getSentAudioFrames(): Long {
      return audioFramesSent
    }

    fun getSentVideoFrames(): Long {
      return videoFramesSent
    }

    fun resetSentAudioFrames() {
      audioFramesSent = 0
    }

    fun resetSentVideoFrames() {
      videoFramesSent = 0
    }

    fun resetDroppedAudioFrames() {
      droppedAudioFrames = 0
    }

    fun resetDroppedVideoFrames() {
      droppedVideoFrames = 0
    }

    fun setLogs(enable: Boolean) {
      isEnableLogs = enable
    }

    fun ByteArray.toHexString(): String = joinToString("") { java.lang.Byte.toUnsignedInt(it).toString(radix = 16).padStart(2, '0') }

    // Function for checking if there are enough frames to make a signature
    // Makes a signature and puts it in a data message if possible
    private fun checkAndMakeSignature(signatureBlockingDeque: BlockingDeque<FlvPacket>, flvType: FlvType) {
        if (signatureBlockingDeque.size >= signatureSize) {
            val byteArrayList: MutableList<ByteArray> = LinkedList()
            val backupList: MutableList<FlvPacket> = LinkedList()
            for (i in 1..signatureSize) {
                val packet = signatureBlockingDeque.pollFirst(10, TimeUnit.MILLISECONDS)
                backupList.add(packet)
                byteArrayList.add(packet.buffer)
                byteArrayList.add(packet.timeStamp.toBytes())
            }

            try {
                // If there somehow are not exactly 5 frames, put all of them back in the que (in the right order) and try again next loop
                if (byteArrayList.size != signatureSize * 2) {
                    throw Exception()
                }


                // Concatenate all ByteArrays into one single instance
                val byteArray: ByteArray = byteArrayListToByteArray(byteArrayList)

                val signature = signByteArray(byteArray)
                val timestamps: List<Double> = backupList.map { it.timeStamp.toDouble() }
                val signaturePacket = SignaturePacket(signature, timestamps, flvType)
                Log.e(SIG_TAG, "Signature timestamps: $timestamps")
                Log.e(SIG_TAG, "Signature Hash: ${MessageDigest.getInstance("SHA-256").digest(byteArray).toHexString()}")
                Log.e(SIG_TAG, "Signature: ${signature.toHexString()}")

//                val result = verifyByteArray(byteArray, signature)
//                Log.e(SIG_TAG, "Verify: $result")

                var size = 0
                output?.let { output ->
                    size = commandsManager.sendSignature(signaturePacket, flvType, output)
                    if (isEnableLogs) {
                        Log.i(SIG_TAG, "wrote $flvType signature packet, size $size")
                    }
                }
                bitrateManager.calculateBitrate(size * 8L)
            } catch (e: Exception) {
                Log.e(TAG, "Error Found: $e")

                backupList.reverse()
                backupList .forEach { signatureBlockingDeque.addFirst(it) }
            }
        }
    }

    private fun Long.toBytes(): ByteArray {
        val buffer: ByteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES)
        buffer.putLong(this)
        return buffer.array()
    }

    private fun byteArrayListToByteArray(byteArrayList: MutableList<ByteArray>): ByteArray {
        var byteArray: ByteArray = byteArrayList.removeFirst()
        for (byteElement in byteArrayList) {
            byteArray = byteArray.plus(byteElement)
        }
        return byteArray
    }

    private val PRIVATE_KEY: String = "MIICWgIBAAKBgHcyTFikOhMTDuiisl6kwRpSBmrEstw1+gYboOQtugugpYVHcSwI" +
            "UM9lFfiGN5zn6++bU8DDQScnIU4D7Zg6S3/h1dyqyHjIzSD9fvCcbaJlFC32mrNO" +
            "SPhbF+irpHaIbS4e2V8qd6RdWerqJaXM7OsFEKydGzDW7G8lr5jIrAddAgMBAAEC" +
            "gYADPqNFZnMOQd6OBp/EY8e95621ClW0GOQNdoMSswv1dRIMZr117WQFwUKv2Td6" +
            "VfXeN+Q3wxjq7+3AKes10aBseQmXB68iJ7e78LjsaU4f02j5WQOPTQRy2f4H/Cgm" +
            "dZplSuyuWUQUL0UM2CH4bhXPKLDXkfPhuLwPxl7tD83SAQJBANcqZg6rQAM22DVI" +
            "AW0kUJjUDCrHq0m8sIR2dSEfqoxVegDSdXZ5/hdgkS5Ly61p4BwfQ1fbrajkjFZi" +
            "oZQXftECQQCN0VZIkzMyX83h65QsoGdjq1wO2d/BbU3NUfTy9zg3qjrQ1DgPeyU4" +
            "BWNSaB7+eRsyaelxhij5iq7NMOvoZlrNAkBi/hbGWPOyhuEiYmaFmFeceLLAW+zq" +
            "l+1+hCGPg8orlofzKODyCV5l0v/4lNa4iiWZyqhpG6DiO4R1mhtMzyKBAkAWk4m5" +
            "2f0fetLqsTcQd6Sd4EyybIrLXxwwoGhvOV3wtp/QWMhn5oHBTlJGbx7oAd2LhALO" +
            "uL3TI/m53pzfjVPNAkBZfx4rEHrucEcfgoQjU5PDUDkATBHLa7juPc4hEzViHcRi" +
            "1oz0hdGXB1kSldcK9ejqbMuNvm905jFkaauqYwv+"
    private val PUBLIC_KEY: String = "MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgHcyTFikOhMTDuiisl6kwRpSBmrE" +
            "stw1+gYboOQtugugpYVHcSwIUM9lFfiGN5zn6++bU8DDQScnIU4D7Zg6S3/h1dyq" +
            "yHjIzSD9fvCcbaJlFC32mrNOSPhbF+irpHaIbS4e2V8qd6RdWerqJaXM7OsFEKyd" +
            "GzDW7G8lr5jIrAddAgMBAAE="

    private fun signByteArray(data: ByteArray): ByteArray {
        val privateKeyBytes = Base64.getDecoder().decode(PRIVATE_KEY)
        val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)

        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data)

        return signature.sign()
    }
}