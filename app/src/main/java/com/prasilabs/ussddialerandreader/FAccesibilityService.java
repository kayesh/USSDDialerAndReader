/*
 * @category USSD.
 * @copyright Copyright (C) 2017 Prasilabs. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.prasilabs.ussddialerandreader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Accessibility service that will receive accesibility prompt message.
 *
 * @author Prasanna Anbazhagan <praslnx8@gmail.com>
 * @version 1.0
 */
public class FAccesibilityService extends AccessibilityService {

    private static final String TAG = FAccesibilityService.class.getSimpleName();

    public static FAccesibilityService self;

    public AccessibilityEvent accessibilityEvent;

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        String text = accessibilityEvent.getText().toString();

        /**
         * Check for valid accesibility dialog.
         */
        if (accessibilityEvent.getClassName().equals("android.app.AlertDialog")) {
            if (text.contains("send") || text.contains("SEND") || text.contains("OK") || text.contains("ok")
                    || text.contains("CANCEL") || text.contains("cancel")) {
                this.accessibilityEvent = accessibilityEvent;

                if(USSDManager.accesibilityCallBack != null) {
                    USSDManager.accesibilityCallBack.received(accessibilityEvent);
                }
            }
        }
    }


    public static void closeDialog() {
        if(self != null) {
            self.closeDialogs();
        }
    }

    public void closeDialogs() {
        performGlobalAction(GLOBAL_ACTION_BACK);
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        self = null;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "service connected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }

    public static void stopItSelf() {
        try{
            if(self != null) {
                self.stopSelf();
            }
        }catch (Exception e){

        }
    }

    public interface FAccesibilityCallBack {

        void received(AccessibilityEvent accessibilityEvent);
    }
}
