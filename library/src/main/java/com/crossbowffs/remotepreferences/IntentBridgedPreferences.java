package com.crossbowffs.remotepreferences;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Provides a {@link SharedPreferences} compatible API to
 * {@link IntentBridgedPreferenceSender} {@link IntentBridgedPreferencesRequestedReceiver}.
 * See both classes for more information.
 * </p>
 *
 * <p>
 * If you are reading preferences from the same context as the
 * provider, you should not use this class; just access the
 * {@link SharedPreferences} API as you would normally.
 * </p>
 */
public class IntentBridgedPreferences implements SharedPreferences {
    private final SharedPreferences mCachedPreferences;
    private final IntentFilter mPreferencesIntentFilter;
    private final IntentBridgedPreferencesReceiver mPreferencesReceiver;

    /**
     * <p>
     * Initializes the intent bridged preferences with the specified
     * authority. The authority must match the action tag defined in
     * your manifest file. Only the specified preferences will be
     * accessible through the provider.
     * </p>
     *
     * <p>
     * As intents are asynchronous, this instance keeps a local cache
     * of the last known values of the preferences. Upon creation
     * a request to refresh this cache will be send, so the (old) cached
     * values will be used until the new set of preferences is received.
     * </p>
     *
     * @param context The {@link Context} of this app.
     * @param actionName The actionName of the action.
     */
    public IntentBridgedPreferences(Context context, String actionName) {
        mCachedPreferences = context.getSharedPreferences(actionName, Context.MODE_PRIVATE);
        mPreferencesIntentFilter = new IntentFilter(actionName + ".PREFERENCES");
        mPreferencesReceiver = new IntentBridgedPreferencesReceiver(mCachedPreferences);

        context.registerReceiver(mPreferencesReceiver, mPreferencesIntentFilter);

        Intent intent = new Intent(actionName + ".PREFERENCES_REQUESTED")
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            intent = intent.setPackage(actionName);
        }

        context.sendBroadcast(intent);
    }

    @Override
    public Map<String, ?> getAll() {
        return mCachedPreferences.getAll();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return mCachedPreferences.getString(key, defaultValue);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return mCachedPreferences.getStringSet(key, defaultValue);
        } else {
            return null;
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return mCachedPreferences.getInt(key, defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return mCachedPreferences.getLong(key, defaultValue);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return mCachedPreferences.getFloat(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return mCachedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    public boolean contains(String key) {
        return mCachedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        return mCachedPreferences.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        mCachedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        mCachedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
}
