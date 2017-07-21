/*
 * @category USSD.
 * @copyright Copyright (C) 2017 Prasilabs. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.prasilabs.ussddialerandreader.sms;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.prasilabs.ussddialerandreader.fileIO.FFileWriterAndReader;
import com.prasilabs.ussddialerandreader.logic.USSDManager;
import com.prasilabs.ussddialerandreader.wake.WakeLocker;


/**
 * Broadcast receiver that listen to android SMS.
 *
 * @author Prasanna <praslnx8@gmail.com>
 * @version 1.0
 */
public class FSmsReceiver extends BroadcastReceiver {
    private static final String TAG = FSmsReceiver.class.getSimpleName();

    /**
     * Default constructor
     */
    public FSmsReceiver() {
        super();
    }


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "Message received");
        final Bundle bundle = intent.getExtras();
        if (bundle != null) {

            final Object[] pdusObj = (Object[]) bundle.get("pdus");

            if (pdusObj != null && pdusObj.length > 0) {
                SmsMessage currentMessage = getIncomingMessage(pdusObj[0], bundle);
                String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                String message = currentMessage.getDisplayMessageBody();
                FFileWriterAndReader.writeToFile(context, message);

                //TODO check for message contains...
                //if(message.contains(" "))
                {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                        WakeLocker.acquire(context); //wake the app
                        final USSDManager ussdManager = USSDManager.getInstance();
                        ussdManager.startListenToUSSD(context);
                        ussdManager.call(context, "*989#", new USSDManager.USSDCallback() {
                            @Override
                            public void response(String response) {
                                Log.d(TAG, "response is : " + response);
                                FFileWriterAndReader.writeToFile(context, response);

                                Intent intent = new Intent();
                                intent.setAction("MESSAGE_UPDATE");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent); // signalling view.

                                ussdManager.pressButton("ok", new USSDManager.USSDCallback() {
                                    @Override
                                    public void response(String response) {
                                        Log.d(TAG, "response is : " + response);
                                        FFileWriterAndReader.writeToFile(context, response);
                                        ussdManager.pressButton("cancel", null);
                                        ussdManager.stopListening();
                                        WakeLocker.release(); //Release the wake here.
                                    }
                                });
                            }
                        });
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
}