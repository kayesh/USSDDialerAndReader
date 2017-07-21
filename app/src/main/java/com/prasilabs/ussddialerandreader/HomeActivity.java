/*
 * @category USSD.
 * @copyright Copyright (C) 2017 Prasilabs. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.prasilabs.ussddialerandreader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prasilabs.ussddialerandreader.fileIO.FFileWriterAndReader;
import com.prasilabs.ussddialerandreader.logic.FAccesibilityService;
import com.prasilabs.ussddialerandreader.logic.USSDManager;

/**
 * Accesibility service that will receive accesibility prompt message.
 *
 * @author Prasanna Anbazhagan <praslnx8@gmail.com>
 * @version 1.0
 */
public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_CALL = 21;
    private static final int PERMISSION_SMS = 22;
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView responseMessageTextView;

    private boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        responseMessageTextView = (TextView) findViewById(R.id.response_text);

        if (USSDManager.isAccessibilitySettingsOn(this)) {
            if (!isStarted) {
                isStarted = true;
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL);
                }
                startService(new Intent(this, FAccesibilityService.class));
            }
        } else {
            openAccesibilitySetting();
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, PERMISSION_SMS);
        }

        String text = FFileWriterAndReader.getStringFromFile(this);
        if(!TextUtils.isEmpty(text)) {
            responseMessageTextView.setText(text);
        } else {
            responseMessageTextView.setText("Nothing");
        }

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String text = FFileWriterAndReader.getStringFromFile(HomeActivity.this);
                if(!TextUtils.isEmpty(text)) {
                    responseMessageTextView.setText(text);
                } else {
                    responseMessageTextView.setText("Nothing");
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MESSAGE_UPDATE");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void openAccesibilitySetting() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (USSDManager.isAccessibilitySettingsOn(this)) {
            if (!isStarted) {
                isStarted = true;
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL);
                }
                startService(new Intent(this, FAccesibilityService.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
