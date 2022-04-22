package com.crossbowffs.remotepreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * <p>
 * Receives requests to send the preferences and responds with
 * the full set of preferences being sent as intent.
 * </p>
 */
public abstract class IntentBridgedPreferencesRequestedReceiver extends BroadcastReceiver {
    private final String mActionName;
    private final SharedPreferences mSourcePreferences;

    /**
     * Initializes this receiver with the action name and
     * the given {@link SharedPreferences} as source for preferences.
     *
     * @param actionName The actionName of the action.
     * @param sourcePreferences The {@link SharedPreferences} used as source.
     */
    public IntentBridgedPreferencesRequestedReceiver(String actionName, SharedPreferences sourcePreferences) {
        mActionName = actionName;
        mSourcePreferences = sourcePreferences;
    }

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        Intent preferencesIntent = new Intent(mActionName + ".PREFERENCES");

        for (Map.Entry<String, ?> preference : mSourcePreferences.getAll().entrySet()) {
            IntentBridgedUtils.setAsIntentExtra(preferencesIntent, preference.getKey(), preference.getValue());
        }

        context.sendBroadcast(preferencesIntent);
    }
}
