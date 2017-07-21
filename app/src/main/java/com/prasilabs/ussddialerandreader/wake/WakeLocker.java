package com.prasilabs.ussddialerandreader.wake;

import android.content.Context;
import android.os.PowerManager;

public abstract class WakeLocker {
    private static PowerManager.WakeLock wakeLock;

    public static void acquire(Context ctx) {
        try {
            if (wakeLock != null) {
                wakeLock.release();
            }

            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "wakeup");
            wakeLock.acquire();
        } catch (Exception e) {
        }
    }

    public static void release() {
        try {
            if (wakeLock != null) {
                wakeLock.release();
            }
            wakeLock = null;
        } catch (Exception e) {
        }
    }
}