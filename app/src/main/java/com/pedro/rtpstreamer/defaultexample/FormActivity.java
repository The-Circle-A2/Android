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

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

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
        user_name_input     = (EditText)findViewById(R.id.user_name_input);
        private_key_input   = (EditText)findViewById(R.id.private_key_input);

        bSubmit = findViewById(R.id.submit_form);
        bSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_form:
                if(CheckPrefs()) {
                    SaveConnectionPrefs();
                    startActivity(new Intent(this, StreamActivity.class));
                }
                break;
            default:
                break;
        }
    }

    private Boolean CheckPrefs()
    {
        String user_name = user_name_input.getText().toString();
        String private_key_string = private_key_input.getText().toString();

        if(user_name.isEmpty() || private_key_string.isEmpty()) {
            Toast.makeText(this, "Not all fields are filled.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String reducedPrivateKey = private_key_string
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\n", "")
                .replaceAll("\\s+","");

        KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(reducedPrivateKey));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            keyFactory.generatePrivate(keySpec);

            return true;
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, "Private key is invalid.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void SaveConnectionPrefs()
    {
        SharedPreferences.Editor editor = getSharedPreferences(CONNECTION_PREFS, MODE_PRIVATE).edit();

        editor.putString("USERNAME", user_name_input.getText().toString());
        editor.putString("PRIVATE_KEY", private_key_input.getText().toString());
        editor.apply();
    }
}
