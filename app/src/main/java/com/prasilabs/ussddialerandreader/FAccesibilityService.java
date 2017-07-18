package com.prasilabs.ussddialerandreader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by prasilabs on 5/7/17.
 */

public class FAccesibilityService extends AccessibilityService {

    private static final String TAG = FAccesibilityService.class.getSimpleName();

    public static FAccesibilityService self;

    /*@Override
    public void onCreate() {
        super.onCreate();
        self = this;
    }*/

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d(TAG, "accesibility received");
        String text = accessibilityEvent.getText().toString();

        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source != null) {
            //capture the EditText simply by using FOCUS_INPUT (since the EditText has the focus), you can probably find it with the viewId input_field
            AccessibilityNodeInfo inputNode = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (inputNode != null) {//prepare you text then fill it using ACTION_SET_TEXT
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,"1");
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

                List<AccessibilityNodeInfo> list = inputNode.findAccessibilityNodeInfosByText("Send");
                for (AccessibilityNodeInfo node : list) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

                AccessibilityNodeInfo nodeInput = inputNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                Bundle bundle = new Bundle();
                bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,"1");
                nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,bundle);
                nodeInput.refresh();
            } else {
                inputNode = source.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);

                if (inputNode != null) {//prepare you text then fill it using ACTION_SET_TEXT
                    inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    List<AccessibilityNodeInfo> list = inputNode.findAccessibilityNodeInfosByText("Ok");
                    for (AccessibilityNodeInfo node : list) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }

                    AccessibilityNodeInfo nodeInput = inputNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,"1");
                    nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,bundle);
                    nodeInput.refresh();
                }
            }
            //"Click" the Send button
            List<AccessibilityNodeInfo> list = source.findAccessibilityNodeInfosByText("Send");
            for (AccessibilityNodeInfo node : list) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }


            list = source.findAccessibilityNodeInfosByText("Ok");
            for (AccessibilityNodeInfo node : list) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

        if (accessibilityEvent.getClassName().equals("android.app.AlertDialog")) {

            if(text.contains("send") || text.contains("SEND") || text.contains("OK") || text.contains("ok")
                    || text.contains("CANCEL") || text.contains("cancel")) {
                Intent intent = new Intent();
                intent.setAction("FACC");
                intent.putExtra("TEXT", text);
                sendBroadcast(intent);
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
}
