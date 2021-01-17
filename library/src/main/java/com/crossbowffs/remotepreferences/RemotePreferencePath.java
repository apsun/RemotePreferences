package com.crossbowffs.remotepreferences;

/**
 * Basically just a tuple of (preference file, preference key).
 */
/* package */ class RemotePreferencePath {
    public final String fileName;
    public final String key;

    public RemotePreferencePath(String prefFileName, String prefKey) {
        fileName = prefFileName;
        key = prefKey;
    }
}