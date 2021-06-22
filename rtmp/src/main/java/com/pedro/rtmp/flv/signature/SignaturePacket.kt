package com.pedro.rtmp.flv.signature

import com.pedro.rtmp.flv.FlvType

/**
 * Created by Maxime on 21/06/2021
 */
data class SignaturePacket(val signature: ByteArray = byteArrayOf(), val timestamps: List<Double> = listOf(), val type: FlvType = FlvType.AUDIO)
