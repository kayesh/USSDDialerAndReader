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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * USSD manager helps you call ussd and interact with them.
 * It implements {@link com.prasilabs.ussddialerandreader.FAccesibilityService.FAccesibilityCallBack}
 * to receive callback from {@link FAccesibilityService}.
 *
 * @author Prasanna Anbazhagan <praslnx8@gmail.com>
 * @version 1.0
 */
public class USSDManager implements FAccesibilityService.FAccesibilityCallBack {

    private USSDCallback ussdCallback;

    public static FAccesibilityService.FAccesibilityCallBack accesibilityCallBack;

    private AccessibilityEvent currentAccesiblityEvent;

    public USSDManager() {
        accesibilityCallBack = this;
    }

    /**
     * Call USSD with this method.
     *
     * @param context Context to access android components for calling.
     * @param ussd USSD no to call. Eg. *123#
     * @param ussdCallback Callback interface to get the response.
     */
    @RequiresPermission(allOf = Manifest.permission.CALL_PHONE)
    public void call(Context context, String ussd, USSDCallback ussdCallback) {
        this.ussdCallback = ussdCallback;
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(ussd)));
            context.startActivity(intent);
        }
    }

    /**
     * Reply to the USSD dialog by entering value and press the button.
     *
     * @param reply Reply message like 1 or 2.
     * @param buttonName Button name like Send, Ok, Cancel.
     * @param ussdCallback CallBack interface to get the response.
     */
    public void reply(String reply, String buttonName, USSDCallback ussdCallback) {
        this.ussdCallback = ussdCallback;

        AccessibilityNodeInfo source = currentAccesiblityEvent.getSource();
        if (source != null) {
            //capture the EditText simply by using FOCUS_INPUT (since the EditText has the focus), you can probably find it with the viewId input_field
            AccessibilityNodeInfo inputNode = source.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (inputNode != null) {//prepare you text then fill it using ACTION_SET_TEXT
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,reply);
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }
            //"Click" the Send button
            List<AccessibilityNodeInfo> list = source.findAccessibilityNodeInfosByText(buttonName);
            for (AccessibilityNodeInfo node : list) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * Press the button of the accessibility dialog.
     *
     * @param name Name of the button.
     * @param ussdCallback Response after callback.
     */
    public void pressButton(String name, USSDCallback ussdCallback) {
        this.ussdCallback = ussdCallback;

        if(currentAccesiblityEvent != null) {
            AccessibilityNodeInfo source = currentAccesiblityEvent.getSource();

            if (source != null) {
                List<AccessibilityNodeInfo> list = source.findAccessibilityNodeInfosByText(name);
                for (AccessibilityNodeInfo node : list) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    @Override
    public void received(AccessibilityEvent accessibilityEvent) {
        if(ussdCallback != null) {
            currentAccesiblityEvent = accessibilityEvent;
            if(currentAccesiblityEvent != null) {
                String text = currentAccesiblityEvent.getText().toString();

                ussdCallback.response(text);
            }
        }
    }

    /**
     * CAllback interface to communicate.
     */
    public interface USSDCallback {

        /**
         * Get the response in the USSD dialog.
         *
         * @param response Response string.
         */
        void response(String response);
    }
}
