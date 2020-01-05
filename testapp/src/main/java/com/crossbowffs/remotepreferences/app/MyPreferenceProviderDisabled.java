package com.crossbowffs.remotepreferences.app;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class MyPreferenceProviderDisabled extends RemotePreferenceProvider {
    public MyPreferenceProviderDisabled() {
        super(Constants.AUTHORITY_DISABLED, new String[] {Constants.PREF_FILE});
    }
}
