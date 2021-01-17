package com.crossbowffs.remotepreferences;

import android.content.UriMatcher;
import android.net.Uri;

import java.util.List;

/**
 * Decodes URIs passed between {@link RemotePreferences} and {@link RemotePreferenceProvider}.
 */
/* package */ class RemotePreferenceUriParser {
    private static final int PREFERENCES_ID = 1;
    private static final int PREFERENCE_ID = 2;

    private final UriMatcher mUriMatcher;

    public RemotePreferenceUriParser(String authority) {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(authority, "*/", PREFERENCES_ID);
        mUriMatcher.addURI(authority, "*/*", PREFERENCE_ID);
    }

    /**
     * Parses the preference file and key from a query URI. If the key
     * is not specified, the returned path will contain {@code null} as the key.
     *
     * @param uri The URI to parse.
     * @return A path object containing the preference file name and key.
     */
    public RemotePreferencePath parse(Uri uri) {
        int match = mUriMatcher.match(uri);
        if (match != PREFERENCE_ID && match != PREFERENCES_ID) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        // The URI must fall under one of these patterns:
        //
        //   content://authority/prefFileName/prefKey
        //   content://authority/prefFileName/
        //   content://authority/prefFileName
        //
        // The match ID will be PREFERENCE_ID under the first case,
        // and PREFERENCES_ID under the second and third cases
        // (UriMatcher ignores trailing slashes).
        List<String> pathSegments = uri.getPathSegments();
        String prefFileName = pathSegments.get(0);
        String prefKey = null;
        if (match == PREFERENCE_ID) {
            prefKey = pathSegments.get(1);
        }
        return new RemotePreferencePath(prefFileName, prefKey);
    }
}
