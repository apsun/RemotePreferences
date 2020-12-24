package com.crossbowffs.remotepreferences.testapp;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class TestPreferenceProvider extends RemotePreferenceProvider {
    public TestPreferenceProvider() {
        super(TestConstants.AUTHORITY, new String[] {TestConstants.PREF_FILE});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        if (prefKey.equals(TestConstants.UNREADABLE_PREF_KEY) && !write) return false;
        if (prefKey.equals(TestConstants.UNWRITABLE_PREF_KEY) && write) return false;
        return true;
    }
}
