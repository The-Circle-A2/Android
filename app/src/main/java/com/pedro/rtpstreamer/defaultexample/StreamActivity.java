package com.pedro.rtpstreamer.defaultexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pedro.MainRecycleViewAdapter;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.model.Message;
import com.pedro.rtmp.flv.signature.PrivateKeyGetter;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtpstreamer.MainActivity;
import com.pedro.rtpstreamer.R;
import com.pedro.rtpstreamer.utils.PathUtils;
import com.pedro.tasks.GetItemAsyncTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
public class StreamActivity extends AppCompatActivity
    implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback, PrivateKeyGetter {

  private RtmpCamera1 rtmpCamera1;
  private Button stopStreamButton;
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private String currentDateAndTime = "";
  private File folder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_stream);
    folder = PathUtils.getRecordPath(this);
    SurfaceView surfaceView = findViewById(R.id.surfaceView);
    stopStreamButton = findViewById(R.id.b_start_stop);
    stopStreamButton.setOnClickListener(this);
    rtmpCamera1 = new RtmpCamera1(surfaceView, this, this);
    rtmpCamera1.setReTries(10);
    surfaceView.getHolder().addCallback(this);

    //obtain a handle to the object
    mRecyclerView = findViewById(R.id.recycler_view_activity_list);
    // use a linear layout manager
    mLayoutManager = new LinearLayoutManager(this);
    //connect it to a layout manager
    mRecyclerView.setLayoutManager(mLayoutManager);

    //Init features array
    ArrayList<Message> mMessages = getMessages();

    if(savedInstanceState == null) {
      //Show toast
      Toast.makeText(this, "Aantal opgehaalde items: " + mMessages.size(), Toast.LENGTH_LONG).show();
    }

    // specify an adapter
    setAdapter(mMessages);
  }

  private void setAdapter(ArrayList<Message> mMessages){
    mAdapter = new MainRecycleViewAdapter(mMessages);
    mRecyclerView.setAdapter(mAdapter);

    Log.d(StreamActivity.class.getSimpleName(), "Adapter has been set.");
  }

  private ArrayList<Message> getMessages(){
    ArrayList<Message> messages = new ArrayList<>();

    try {
      messages = new GetItemAsyncTask().execute().get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }

    Log.d(MainActivity.class.getSimpleName(), "Messages has been retrieved.");
    return messages;
  }

  @Override
  public void onConnectionStartedRtmp(String rtmpUrl) {
  }

  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (rtmpCamera1.reTry(5000, reason)) {
          Toast.makeText(StreamActivity.this, "Retry", Toast.LENGTH_SHORT)
              .show();
        } else {
          Toast.makeText(StreamActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
              .show();
          rtmpCamera1.stopStream();
          stopStreamButton.setText(R.string.start_button);
        }
      }
    });
  }

  @Override
  public void onNewBitrateRtmp(long bitrate) {

  }

  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.b_start_stop:
        try {
          if (!rtmpCamera1.isStreaming()) {
            if (rtmpCamera1.isRecording()
                    || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
              rtmpCamera1.stopPreview();

              SharedPreferences prefs = getSharedPreferences(FormActivity.CONNECTION_PREFS, MODE_PRIVATE);
              if(prefs.getString("USERNAME", null).isEmpty())
              {
                Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT);
                return;
              }
              rtmpCamera1.startStream(prefs.getString("USERNAME", null));
              stopStreamButton.setText(R.string.stop_button);
            } else {
              Toast.makeText(this, "Error preparing stream, This device cant do it",
                      Toast.LENGTH_SHORT).show();
            }
          } else {
            stopStreamButton.setText(R.string.start_button);
            rtmpCamera1.stopStream();
          }
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.switch_camera:
        try {
          rtmpCamera1.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    rtmpCamera1.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
      rtmpCamera1.stopRecord();
      Toast.makeText(this,
          "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
          Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }
    if (rtmpCamera1.isStreaming()) {
      rtmpCamera1.stopStream();
    }
    rtmpCamera1.stopPreview();
  }

  @NotNull
  @Override
  public PrivateKey getPrivateKey() {
    SharedPreferences sharedPreferences = getSharedPreferences(FormActivity.CONNECTION_PREFS, MODE_PRIVATE);
    String privateKeyPKCS8 = sharedPreferences.getString("PRIVATE_KEY", null);

    if (privateKeyPKCS8.isEmpty()) {
      throw new IllegalStateException("PRIVATE_KEY can not be gotten when it's not stored. User shouldn't be in this screen yet!");
    }

    String reducedPrivateKey = privateKeyPKCS8
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\n", "")
            .replaceAll("\\s+","");

    KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(reducedPrivateKey));

    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePrivate(keySpec);
    } catch (GeneralSecurityException e) {
      // TODO remove exception before done!
      throw new RuntimeException("Something went wrong while parsing the user's private key!", e);
    }
  }
}
