package com.crossbowffs.remotepreferences.testapp;

import android.content.SharedPreferences;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestPreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean mIsCalled;
    private String mKey;
    private final CountDownLatch mLatch;

    public TestPreferenceListener() {
        mIsCalled = false;
        mKey = null;
        mLatch = new CountDownLatch(1);
    }

    public boolean isCalled() {
        return mIsCalled;
    }

    public String getKey() {
        if (!mIsCalled) {
            throw new IllegalStateException("Listener was not called");
        }
        return mKey;
    }

    public boolean waitForChange(long seconds) {
        try {
            return mLatch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Listener wait was interrupted");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mIsCalled = true;
        mKey = key;
        mLatch.countDown();
    }
}
