package com.pedro.rtpstreamer.defaultexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.pedro.rtpstreamer.R;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
public class FormActivity extends AppCompatActivity
        implements View.OnClickListener {

    private Button bSubmit;
    private EditText etUrl;
    private EditText etUsername;
    private EditText etPrivateKey;

    private String currentDateAndTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_form);
        bSubmit = findViewById(R.id.submit_form);
        bSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_form:
                startActivity(new Intent(this, StartStreamActivity.class));
                break;
            default:
                break;
        }
    }
}
