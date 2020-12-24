package com.crossbowffs.remotepreferences.testapp;

public final class TestConstants {
    private TestConstants() {}

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".preferences";
    public static final String AUTHORITY_DISABLED = BuildConfig.APPLICATION_ID + ".preferences.disabled";
    public static final String PREF_FILE = "main_prefs";
    public static final String UNREADABLE_PREF_KEY = "cannot_read_me";
    public static final String UNWRITABLE_PREF_KEY = "cannot_write_me";
}
