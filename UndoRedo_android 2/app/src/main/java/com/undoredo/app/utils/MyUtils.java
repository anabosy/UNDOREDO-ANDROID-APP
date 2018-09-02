package com.undoredo.app.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtils {

    public static final String FIRST_LAUNCH_PASSED = "first_launch_passed";

    // methode permettant de mofifier un sharedpreference
    public static Boolean getBooleanSharedPref(Context cxt, String prefName){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(cxt);
        return SP.getBoolean(prefName, false);
    }
    // methode permettant de mofifier un sharedpreference
    public static void setBooleanSharedPref(Context cxt, String prefName, boolean value){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(cxt);
        SP.edit().putBoolean(prefName, value).commit();
    }

    // methode permettant de mofifier un sharedpreference
    public static long getLongSharedPref(Context cxt, String prefName){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(cxt);
        return SP.getLong(prefName, 0);
    }
    // methode permettant de mofifier un sharedpreference
    public static void setLongSharedPref(Context cxt, String prefName, long value){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(cxt);
        SP.edit().putLong(prefName, value).commit();
    }

    public static void appendLog(String text)
    {
        File logFile = new File("sdcard/DataCard_log_1.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            // add date at the line begining
            String dateString = new SimpleDateFormat("dd-M-yyyy hh:mm:ss").format(new Date());
            buf.append(new StringBuilder(dateString).append(": ").append(text));
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * Check if an AccessibilityService is enabled
     * Based on https://stackoverflow.com/a/40568194/1993488
     * @see <a href="https://github.com/android/platform_frameworks_base/blob/d48e0d44f6676de6fd54fd8a017332edd6a9f096/packages/SettingsLib/src/com/android/settingslib/accessibility/AccessibilityUtils.java#L55">AccessibilityUtils</a>
     */
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

}
