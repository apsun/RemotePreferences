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
     * @param write {@code true} for put/remove/clear operations; {@code false} for get operations.
     * @return {@code true} if the access is allowed; {@code false} otherwise.
     */
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        return true;
    }

    @Override
    public boolean onCreate() {
        // We register the shared preference listener whenever the provider
        // is created. This method is called before almost all other code in
        // the app, which ensures that we never miss a preference change.
        Context context = getContext();
        for (String prefName : mPrefNames) {
            SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            prefs.registerOnSharedPreferenceChangeListener(this);
            mPreferences.put(prefName, prefs);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        String prefName = nameKeyPair.name;
        String rawPrefKey = nameKeyPair.key;

        SharedPreferences prefs = getPreferencesOrThrow(prefName, rawPrefKey, false);
        Map<String, ?> prefMap = prefs.getAll();

        // If no projection is specified, we return all columns.
        if (projection == null) {
            projection = RemoteContract.COLUMN_ALL;
        }

        // Fill out the cursor with the preference data. If the caller
        // didn't ask for a particular preference, we return all of them.
        MatrixCursor cursor = new MatrixCursor(projection);
        if (isSingleKey(rawPrefKey)) {
            Object prefValue = prefMap.get(rawPrefKey);
            cursor.addRow(buildRow(projection, rawPrefKey, prefValue));
        } else {
            for (Map.Entry<String, ?> entry : prefMap.entrySet()) {
                String prefKey = entry.getKey();
                Object prefValue = entry.getValue();
                cursor.addRow(buildRow(projection, prefKey, prefValue));
            }
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
        String prefName = nameKeyPair.name;
        String prefKey = getKeyFromUriOrValues(nameKeyPair, values);

        SharedPreferences prefs = getPreferencesOrThrow(prefName, prefKey, true);
        SharedPreferences.Editor editor = prefs.edit();

        putPreference(editor, prefKey, values);

        if (editor.commit()) {
            return getPreferenceUri(prefName, prefKey);
        } else {
            return null;
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        String prefName = nameKeyPair.name;
        if (isSingleKey(nameKeyPair.key)) {
            throw new IllegalArgumentException("Cannot bulk insert with single key URI");
        }

        SharedPreferences prefs = getPreferencesByName(prefName);
        SharedPreferences.Editor editor = prefs.edit();

        for (ContentValues value : values) {
            String prefKey = getKeyFromValues(value);
            checkAccessOrThrow(prefName, prefKey, true);
            putPreference(editor, prefKey, value);
        }

        if (editor.commit()) {
            return values.length;
        } else {
            return 0;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        PrefNameKeyPair nameKeyPair = parseUri(uri);
        String prefName = nameKeyPair.name;
        String prefKey = nameKeyPair.key;

        SharedPreferences prefs = getPreferencesOrThrow(prefName, prefKey, true);
        SharedPreferences.Editor editor = prefs.edit();

        int count;
        if (isSingleKey(prefKey)) {
            count = 1;
            editor.remove(prefKey);
        } else {
            count = prefs.getAll().size();
            editor.clear();
        }

        if (editor.commit()) {
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values == null) {
            return delete(uri, selection, selectionArgs);
        } else {
            return insert(uri, values) != null ? 1 : 0;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String prefKey) {
        for (Map.Entry<String, SharedPreferences> entry : mPreferences.entrySet()) {
            if (entry.getValue() == prefs) {
                String prefName = entry.getKey();
                Uri uri = getPreferenceUri(prefName, prefKey);
                getContext().getContentResolver().notifyChange(uri, null);
                return;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void putPreference(SharedPreferences.Editor editor, String prefKey, ContentValues values) {
        // Get the new value type. Note that we manually check
        // for null, then unbox the Integer so we don't cause a NPE.
        Integer type = values.getAsInteger(RemoteContract.COLUMN_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Invalid or no preference type specified");
        }

        // deserializeInput makes sure the actual object type matches
        // the expected type, so we must perform this step before actually
        // performing any actions.
        Object rawValue = values.get(RemoteContract.COLUMN_VALUE);
        Object value = RemoteUtils.deserializeInput(rawValue, type);

        // If we are writing to the "directory" and the type is null,
        // then we should clear the preferences.
        if (!isSingleKey(prefKey)) {
            if (type == RemoteContract.TYPE_NULL) {
                editor.clear();
                return;
            } else {
                throw new IllegalArgumentException("Attempting to insert preference with null or empty key");
            }
        }

        switch (type) {
        case RemoteContract.TYPE_NULL:
            editor.remove(prefKey);
            break;
        case RemoteContract.TYPE_STRING:
            editor.putString(prefKey, (String)value);
            break;
        case RemoteContract.TYPE_STRING_SET:
            if (Build.VERSION.SDK_INT >= 11) {
                editor.putStringSet(prefKey, RemoteUtils.castStringSet(value));
            } else {
                throw new IllegalArgumentException("String set preferences not supported on API < 11");
            }
            break;
        case RemoteContract.TYPE_INT:
            editor.putInt(prefKey, (Integer)value);
            break;
        case RemoteContract.TYPE_LONG:
            editor.putLong(prefKey, (Long)value);
            break;
        case RemoteContract.TYPE_FLOAT:
            editor.putFloat(prefKey, (Float)value);
            break;
        case RemoteContract.TYPE_BOOLEAN:
            editor.putBoolean(prefKey, (Boolean)value);
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

    private static boolean isSingleKey(String prefKey) {
        return prefKey != null && prefKey.length() != 0;
    }

    private static String getKeyFromValues(ContentValues values) {
        String key = values.getAsString(RemoteContract.COLUMN_KEY);
        if (key == null) {
            key = "";
        }
        return key;
    }

    private static String getKeyFromUriOrValues(PrefNameKeyPair nameKeyPair, ContentValues values) {
        String uriKey = nameKeyPair.key;
        String valuesKey = getKeyFromValues(values);
        if (uriKey.length() != 0 && valuesKey.length() != 0) {
            // If a key is specified in both the URI and
            // ContentValues, they must match
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

    private SharedPreferences getPreferencesOrThrow(String prefName, String prefKey, boolean write) {
        checkAccessOrThrow(prefName, prefKey, write);
        return getPreferencesByName(prefName);
    }

    private Uri getPreferenceUri(String prefName, String prefKey) {
        return mBaseUri.buildUpon().appendPath(prefName).appendPath(prefKey).build();
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
