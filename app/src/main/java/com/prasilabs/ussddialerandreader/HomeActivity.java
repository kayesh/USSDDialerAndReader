/*
 * @category USSD.
 * @copyright Copyright (C) 2017 Prasilabs. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.prasilabs.ussddialerandreader;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Accesibility service that will receive accesibility prompt message.
 *
 * @author Prasanna Anbazhagan <praslnx8@gmail.com>
 * @version 1.0
 */
public class HomeActivity extends AppCompatActivity {

    private static final String DIAL_NO = "*929#";

    private static final String DIAL_NO2 = "1";

    private static final String TAG = HomeActivity.class.getSimpleName();

    private Button dialButton;

    private TextView responseMessageTextView;

    private boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dialButton = (Button) findViewById(R.id.btn_dial);
        responseMessageTextView = (TextView) findViewById(R.id.response_text);

        dialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    dial(DIAL_NO);
                } else {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 21);
                }
            }
        });

        if (isAccessibilitySettingsOn(this)) {
            if (!isStarted) {
                isStarted = true;
                startService(new Intent(this, FAccesibilityService.class));
            }
        } else {
            openAccesibilitySetting();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccessibilitySettingsOn(this)) {
            if (!isStarted) {
                isStarted = true;
                startService(new Intent(this, FAccesibilityService.class));
            }
        }
    }

    private void dial(String no) {
        final USSDManager ussdManager = new USSDManager(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager
                .PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        ussdManager.call(this, "*121#", new USSDManager.USSDCallback() {
            @Override
            public void response(String response) {
                responseMessageTextView.setText(response);
                ussdManager.reply("1", "Send", new USSDManager.USSDCallback() {
                    @Override
                    public void response(String response) {
                        responseMessageTextView.setText(responseMessageTextView.getText() + response);
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 21 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dial(DIAL_NO);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FAccesibilityService.stopItSelf();
    }

    private void openAccesibilitySetting() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + FAccesibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }
}
