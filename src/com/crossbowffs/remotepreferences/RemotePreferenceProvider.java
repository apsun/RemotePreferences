package com.crossbowffs.remotepreferences;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes {@link SharedPreferences} to other apps running on the device.
 *
 * You must extend this class and declare a default constructor which
 * calls the super constructor with the appropriate authority and
 * preference file name parameters. Remember to add your provider to
 * your AndroidManifest.xml file and set the {@code android:exported}
 * property to true.
 *
 * To access the data from a remote process, use {@link RemotePreferences}
 * initialized with the same authority and the desired preference file name.
 *
 * For granular access control, override {@link #checkAccess(String, String, boolean)}
 * and return {@code false} to deny the operation.
 */
public abstract class RemotePreferenceProvider extends ContentProvider implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int PREFERENCES_ID = 1;
    private static final int PREFERENCE_ID = 2;

    private final Uri mBaseUri;
    private final String[] mPrefNames;
    private final Map<String, SharedPreferences> mPreferences;
    private final UriMatcher mUriMatcher;

    /**
     * Initializes the remote preference provider with the specified
     * authority and preference files. The authority must match the
     * {@code android:authorities} property defined in your manifest
     * file. Only the specified preference files will be accessible
     * through the provider.
     *
     * @param authority The authority of the provider.
     * @param prefNames The names of the preference files to expose.
     */
    public RemotePreferenceProvider(String authority, String[] prefNames) {
        mBaseUri = Uri.parse("content://" + authority);
        mPrefNames = prefNames;
        mPreferences = new HashMap<String, SharedPreferences>(prefNames.length);
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(authority, "*/", PREFERENCES_ID);
        mUriMatcher.addURI(authority, "*/*", PREFERENCE_ID);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCreate() {
        Context context = getContext();
        for (String prefName : mPrefNames) {
            SharedPreferences preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            preferences.registerOnSharedPreferenceChangeListener(this);
            mPreferences.put(prefName, preferences);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        checkAccessOrThrow(nameKeyPair.name, nameKeyPair.key, false);
        SharedPreferences preferences = getPreferencesByName(nameKeyPair.name);
        Map<String, ?> preferenceMap = preferences.getAll();
        if (projection == null) {
            projection = RemoteContract.COLUMN_ALL;
        }
        MatrixCursor cursor = new MatrixCursor(projection);
        if (nameKeyPair.key.length() == 0) {
            for (Map.Entry<String, ?> entry : preferenceMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                cursor.addRow(buildRow(projection, key, value));
            }
        } else {
            String key = nameKeyPair.key;
            Object value = preferenceMap.get(key);
            cursor.addRow(buildRow(projection, key, value));
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            return null;
        }
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        String key = getKeyFromUriOrValues(nameKeyPair, values);
        checkAccessOrThrow(nameKeyPair.name, key, true);
        SharedPreferences.Editor editor = getPreferencesByName(nameKeyPair.name).edit();
        putPreference(editor, key, values);
        editor.commit();
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        if (nameKeyPair.key.length() != 0) {
            throw new IllegalArgumentException("Cannot bulk insert with single key URI");
        }
        SharedPreferences.Editor editor = getPreferencesByName(nameKeyPair.name).edit();
        for (ContentValues value : values) {
            String key = getKeyFromValues(value);
            checkAccessOrThrow(nameKeyPair.name, key, true);
            putPreference(editor, key, value);
        }
        editor.commit();
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        checkAccessOrThrow(nameKeyPair.name, nameKeyPair.key, true);
        SharedPreferences preferences = getPreferencesByName(nameKeyPair.name);
        if (nameKeyPair.key.length() == 0) {
            preferences.edit().clear().commit();
        } else {
            preferences.edit().remove(nameKeyPair.key).commit();
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values == null) {
            delete(uri, selection, selectionArgs);
        } else {
            insert(uri, values);
        }
        return 0;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String prefName = getPreferenceName(sharedPreferences);
        Uri uri = mBaseUri.buildUpon().appendPath(prefName).appendPath(key).build();
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private PrefNameKeyPair parseUri(Uri uri) {
        int match = mUriMatcher.match(uri);
        if (match != PREFERENCE_ID && match != PREFERENCES_ID) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        List<String> pathSegments = uri.getPathSegments();
        String prefName = pathSegments.get(0);
        String prefKey = "";
        if (match == PREFERENCE_ID) {
            prefKey = pathSegments.get(1);
        }
        return new PrefNameKeyPair(prefName, prefKey);
    }

    private String getKeyFromValues(ContentValues values) {
        String key = values.getAsString(RemoteContract.COLUMN_KEY);
        if (key == null) {
            key = "";
        }
        return key;
    }

    private String getKeyFromUriOrValues(PrefNameKeyPair nameKeyPair, ContentValues values) {
        String uriKey = nameKeyPair.key;
        String valuesKey = getKeyFromValues(values);
        if (uriKey.length() != 0 && valuesKey.length() != 0) {
            // If a key is specified in both the URI and ContentValues,
            // they must match
            if (!uriKey.equals(valuesKey)) {
                throw new IllegalArgumentException("Conflicting keys specified in URI and ContentValues");
            }
            return uriKey;
        } else if (uriKey.length() != 0) {
            return uriKey;
        } else if (valuesKey.length() != 0) {
            return valuesKey;
        } else {
            return "";
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void putPreference(SharedPreferences.Editor editor, String key, ContentValues values) {
        Integer type = values.getAsInteger(RemoteContract.COLUMN_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Invalid or no preference type specified");
        }

        // deserializeInput makes sure the actual object type matches
        // the expected type, so we must perform this step before actually
        // performing any actions.
        Object rawValue = values.get(RemoteContract.COLUMN_VALUE);
        Object value = RemoteUtils.deserializeInput(rawValue, type);

        // Null keys are normalized to empty strings, so this checks both
        // null and empty cases.
        if (key.length() == 0) {
            if (type == RemoteContract.TYPE_NULL) {
                editor.clear();
                return;
            } else {
                throw new IllegalArgumentException("Attempting to insert preference with null or empty key");
            }
        }

        switch (type) {
        case RemoteContract.TYPE_NULL:
            editor.remove(key);
            break;
        case RemoteContract.TYPE_STRING:
            editor.putString(key, (String)value);
            break;
        case RemoteContract.TYPE_STRING_SET:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet(key, RemoteUtils.castStringSet(value));
            } else {
                throw new IllegalArgumentException("String set preferences not supported on API < 11");
            }
            break;
        case RemoteContract.TYPE_INT:
            editor.putInt(key, (Integer)value);
            break;
        case RemoteContract.TYPE_LONG:
            editor.putLong(key, (Long)value);
            break;
        case RemoteContract.TYPE_FLOAT:
            editor.putFloat(key, (Float)value);
            break;
        case RemoteContract.TYPE_BOOLEAN:
            editor.putBoolean(key, (Boolean)value);
            break;
        default:
            throw new IllegalArgumentException("Cannot set preference with type " + type);
        }
    }

    private Object[] buildRow(String[] projection, String key, Object value) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < row.length; ++i) {
            String col = projection[i];
            if (RemoteContract.COLUMN_KEY.equals(col)) {
                row[i] = key;
            } else if (RemoteContract.COLUMN_TYPE.equals(col)) {
                row[i] = RemoteUtils.getPreferenceType(value);
            } else if (RemoteContract.COLUMN_VALUE.equals(col)) {
                row[i] = RemoteUtils.serializeOutput(value);
            } else {
                throw new IllegalArgumentException("Invalid column name: " + col);
            }
        }
        return row;
    }

    private void checkAccessOrThrow(String prefName, String prefKey, boolean write) {
        if (!checkAccess(prefName, prefKey, write)) {
            throw new SecurityException("Insufficient permissions to access: " + prefName + "/" + prefKey);
        }
    }

    private SharedPreferences getPreferencesByName(String prefName) {
        SharedPreferences prefs = mPreferences.get(prefName);
        if (prefs == null) {
            throw new IllegalArgumentException("Unknown preference file name: " + prefName);
        }
        return prefs;
    }

    private String getPreferenceName(SharedPreferences preferences) {
        for (Map.Entry<String, SharedPreferences> entry : mPreferences.entrySet()) {
            if (entry.getValue() == preferences) {
                return entry.getKey();
            }
        }
        throw new AssertionError("Cannot find name for SharedPreferences");
    }

    /**
     * Checks whether a specific preference is accessible by clients.
     * The default implementation returns {@code true} for all accesses.
     * You may override this method to control which preferences can be
     * read or written.
     *
     * @param prefName The name of the preference file.
     * @param prefKey The preference key. This is an empty string when handling the
     *                {@link SharedPreferences#getAll()} and
     *                {@link SharedPreferences.Editor#clear()} operations.
     * @param write {@code true} for "put" operations; {@code false} for "get" operations.
     * @return {@code true} if the access is allowed; {@code false} otherwise.
     */
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        return true;
    }

    private class PrefNameKeyPair {
        private final String name;
        private final String key;

        private PrefNameKeyPair(String prefName, String prefKey) {
            name = prefName;
            key = prefKey;
        }
    }
}
