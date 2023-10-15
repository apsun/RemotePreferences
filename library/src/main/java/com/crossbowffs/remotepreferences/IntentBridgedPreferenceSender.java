package com.crossbowffs.remotepreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * <p>
 * Propagates changes in the given {@link SharedPreferences} as intents
 * to any other app on the device.
 * </p>
 *
 * <p>
 * You must extend this class and declare a 0-argument constructor which
 * calls the super constructor with the appropriate action and preferences
 * parameters.
 * </p>
 *
 * <p>
 * Remember to hold onto the created instance explicitely, e.g. through
 * a member of an activity. This registers a listener on the given
 * {@link SharedPreferences} and these are only weakly referenced.
 * </p>
 */
public class IntentBridgedPreferenceSender {
    private final String mActionName;
    private final Context mContext;
    private final SharedPreferences.OnSharedPreferenceChangeListener mListener;

    /**
     * Initializes this sender with the specified action and the given
     * {@link SharedPreferences} as sources for changes and preferences.
     *
     * @param context The {@link Context} of this app.
     * @param actionName The actionName of the action.
     * @param sourcePreferences The {@link SharedPreferences} used as source.
     */
    public IntentBridgedPreferenceSender(Context context, String actionName, SharedPreferences sourcePreferences) {
        mContext = context;
        mActionName = actionName;
        mListener = this::onPreferenceChanged;

        sourcePreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    private void onPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Intent intent = new Intent(mActionName + ".PREFERENCES");

        IntentBridgedUtils.setAsIntentExtra(intent, key, sharedPreferences.getAll().get(key));

        mContext.sendBroadcast(intent);
    }
}
