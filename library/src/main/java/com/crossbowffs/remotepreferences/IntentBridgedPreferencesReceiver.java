package com.crossbowffs.remotepreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import java.util.Arrays;
import java.util.HashSet;

/**
 * <p>
 * Receives preferences as intents and sets the new value(s) into
 * the given {@link SharedPreferences} insance.
 * </p>
 */
/* package */ class IntentBridgedPreferencesReceiver extends BroadcastReceiver {
    private final SharedPreferences mSharedPreferences;

    public IntentBridgedPreferencesReceiver(SharedPreferences targetPreferences) {
        mSharedPreferences = targetPreferences;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();

            for (String key : extras.keySet()) {
                Object value = extras.get(key);

                if (value instanceof String[]) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        editor.putStringSet(key, new HashSet<>(Arrays.asList((String[]) value)));
                    }
                } else if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (long) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (int) value);
                } else if (value instanceof Float) {
                    editor.putFloat(key, (float) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (boolean) value);
                } else if (value == null) {
                    editor.putString(key, (String) null);
                }
            }

            editor.commit();
        }
    }
}
