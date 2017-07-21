/*
 * @category USSD.
 * @copyright Copyright (C) 2017 Prasilabs. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.prasilabs.ussddialerandreader.fileIO;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class handles writing and reading to and from files respectively.
 *
 * @author Prasanna <praslnx8@gmail.com>
 * @version 1.0
 */
public class FFileWriterAndReader {

    private static final String FILE_NAME = "file1";

    /**
     * This method is used to write message to the file.
     *
     * @param context Context to access android components.
     * @param message Message to write.
     */
    public static void writeToFile(Context context, String message)
    {
        FileWriter fileWriter = null;
        try
        {
            if (context != null) {
                File file = new File(context.getFilesDir(), FILE_NAME);

                fileWriter = new FileWriter(file, true);
                fileWriter.write(message);
                fileWriter.write("\n"); //Add next line.
                fileWriter.flush();
                fileWriter.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * This method read the string from file.
     *
     * @param context Context to access android components.
     * @return String of message.
     */
    public static String getStringFromFile (Context context) {
        try {
            File fl = new File(context.getFilesDir(), FILE_NAME);
            FileInputStream fin = new FileInputStream(fl);
            String ret = convertStreamToString(fin);
            fin.close();
            return ret;
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

}
