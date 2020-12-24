package com.crossbowffs.remotepreferences.testapp;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class TestPreferenceProviderDisabled extends RemotePreferenceProvider {
    public TestPreferenceProviderDisabled() {
        super(TestConstants.AUTHORITY_DISABLED, new String[] {TestConstants.PREF_FILE});
    }
}
