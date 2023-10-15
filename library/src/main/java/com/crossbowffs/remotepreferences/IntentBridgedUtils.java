package com.crossbowffs.remotepreferences;

import android.content.Intent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Common utilities used to set preferences into intents
 * and extract them again.
 */
/* package */ final class IntentBridgedUtils {
    private IntentBridgedUtils() {}

    /**
     * Sets the parameter as an extra on the given intent.
     *
     * @param intent The target {@link Intent}.
     * @param key The key of the value.
     * @param value The value, as type {@link Object}.
     */
    public static void setAsIntentExtra(Intent intent, String key, Object value) {
        if (value instanceof String) {
            intent.putExtra(key, (String)value);
        } else if (value instanceof Set<?>) {
            intent.putExtra(key, ((Set<String>)value).toArray(new String[0]));
        } else if (value instanceof Integer) {
            intent.putExtra(key, (int)value);
        } else if (value instanceof Long) {
            intent.putExtra(key, (long)value);
        } else if (value instanceof Float) {
            intent.putExtra(key, (float)value);
        } else if (value instanceof Boolean) {
            intent.putExtra(key, (boolean)value);
        } else {
            intent.putExtra(key, (Serializable) null);
        }
    }
}
