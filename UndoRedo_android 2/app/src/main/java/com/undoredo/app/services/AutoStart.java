package com.undoredo.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.undoredo.app.R;
import com.undoredo.app.utils.MyUtils;

public class AutoStart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        boolean boolValue = MyUtils.getBooleanSharedPref(context, context.getString(R.string.key_pref_enable_pinlock));

        if (boolValue) {
            Intent intent = new Intent(context, InputAccessibilityService.class);
            context.startService(intent);
        }

    }
}
