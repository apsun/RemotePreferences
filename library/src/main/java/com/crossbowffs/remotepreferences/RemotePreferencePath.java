package com.crossbowffs.remotepreferences;

/**
 * A path consists of a preference file name and optionally a key within
 * the preference file. The key will be set for operations that involve
 * a single preference (e.g. {@code getInt}), and {@code null} for operations
 * on an entire preference file (e.g. {@code getAll}).
 */
/* package */ class RemotePreferencePath {
    public final String fileName;
    public final String key;

    public RemotePreferencePath(String prefFileName, String prefKey) {
        this.fileName = prefFileName;
        this.key = prefKey;
    }

    public RemotePreferencePath withKey(String prefKey) {
        if (this.key != null) {
            throw new IllegalArgumentException("Path already has a key");
        }
        return new RemotePreferencePath(this.fileName, prefKey);
    }

    @Override
    public String toString() {
        String ret = "file:" + this.fileName;
        if (this.key != null) {
            ret += "/key:" + this.key;
        }
        return ret;
    }
}
