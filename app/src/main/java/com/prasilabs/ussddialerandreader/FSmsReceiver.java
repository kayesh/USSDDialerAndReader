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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.util.Log;


/**
 * Broadcast receiver that listen to android SMS.
 * It uses {@link SmsMessageReceivedCallback} to send the received sms.
 *
 * @author Prasanna <praslnx8@gmail.com>
 * @version 1.0
 */
public class FSmsReceiver extends BroadcastReceiver {
    private static final String TAG = FSmsReceiver.class.getSimpleName();

    private SmsMessageReceivedCallback smsMessageReceivedCallback;

    /**
     * Default constructor
     */
    public FSmsReceiver() {
        super();
    }

    /**
     * Constructor with callback for SMS brodcast receiver.
     *
     * @param smsMessageReceivedCallback Callback for communicating back.
     */
    public FSmsReceiver(SmsMessageReceivedCallback smsMessageReceivedCallback) {
        super();
        this.smsMessageReceivedCallback = smsMessageReceivedCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Message received");
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {

            final Object[] pdusObj = (Object[]) bundle.get("pdus");

            if (pdusObj != null && pdusObj.length > 0) {
                SmsMessage currentMessage = getIncomingMessage(pdusObj[0], bundle);
                String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                String message = currentMessage.getDisplayMessageBody();

                if (smsMessageReceivedCallback != null) {
                    smsMessageReceivedCallback.messageReceived(phoneNumber, message);
                } else {
                    if(message.contains(" ")) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                            final USSDManager ussdManager = new USSDManager();
                            ussdManager.call(context, "*989#", new USSDManager.USSDCallback() {
                                @Override
                                public void response(String response) {
                                    Log.d(TAG, "response is : " + response);
                                    ussdManager.reply("1", "Send", new USSDManager.USSDCallback() {
                                        @Override
                                        public void response(String response) {
                                            Log.d(TAG, "response is : " + response);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }

        return currentSMS;
    }

    /**
     * Callback interface for {@link FSmsReceiver} to communicate.
     */
    public interface SmsMessageReceivedCallback {

        /**
         * Method to send the OTP message.
         *
         * @param from    Sender address.
         * @param message OTP message.
         */
        void messageReceived(String from, String message);
    }
}