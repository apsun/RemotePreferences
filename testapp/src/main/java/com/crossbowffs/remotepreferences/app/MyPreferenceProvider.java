package com.crossbowffs.remotepreferences.app;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class MyPreferenceProvider extends RemotePreferenceProvider {
    public MyPreferenceProvider() {
        super(Constants.AUTHORITY, new String[] {Constants.PREF_FILE});
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        if (prefKey.equals(Constants.UNREADABLE_PREF_KEY) && !write) return false;
        if (prefKey.equals(Constants.UNWRITABLE_PREF_KEY) && write) return false;
        return true;
    }
}
