package com.pedro.rtpstreamer.defaultexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private EditText stream_uri_input;
    private EditText user_name_input;
    private EditText private_key_input;
    private EditText etUrl;
    private EditText etUsername;
    private EditText etPrivateKey;

    private String currentDateAndTime = "";
    public static final String CONNECTION_PREFS = "CONNECTION_PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_form);

        //Input fields
        stream_uri_input = (EditText)findViewById(R.id.stream_uri_input);
        user_name_input = (EditText)findViewById(R.id.user_name_input);
        private_key_input = (EditText)findViewById(R.id.private_key_input);

        bSubmit = findViewById(R.id.submit_form);
        bSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_form:
                SaveConnectionPrefs();
                startActivity(new Intent(this, StreamActivity.class));
                break;
            default:
                break;
        }
    }

    private void SaveConnectionPrefs()
    {
        SharedPreferences.Editor editor = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE).edit();

        editor.putString("stream_uri_input", stream_uri_input.getText().toString());
        editor.putString("user_name_input", user_name_input.getText().toString());
        editor.putString("private_key_input", private_key_input.getText().toString());
        editor.apply();

        SharedPreferences prefs = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE);
        //Toast.makeText(this, prefs.getString("user_name_input", "error"), Toast.LENGTH_SHORT).show();
    }
}
