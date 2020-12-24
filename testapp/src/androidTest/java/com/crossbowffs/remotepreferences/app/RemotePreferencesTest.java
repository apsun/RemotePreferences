package com.crossbowffs.remotepreferences.app;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;
import com.crossbowffs.remotepreferences.RemotePreferences;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class RemotePreferencesTest {
    private Context getLocalContext() {
        return InstrumentationRegistry.getInstrumentation().getContext();
    }

    private Context getRemoteContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private SharedPreferences getSharedPreferences() {
        Context context = getRemoteContext();
        return context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
    }

    private RemotePreferences getRemotePreferences(boolean strictMode) {
        // This is not a typo! We are using the LOCAL context to initialize a REMOTE prefs
        // instance. This is the whole point of RemotePreferences!
        Context context = getLocalContext();
        return new RemotePreferences(context, Constants.AUTHORITY, Constants.PREF_FILE, strictMode);
    }

    private RemotePreferences getDisabledRemotePreferences(boolean strictMode) {
        Context context = getLocalContext();
        return new RemotePreferences(context, Constants.AUTHORITY_DISABLED, Constants.PREF_FILE, strictMode);
    }

    @Before
    public void resetPreferences() {
        getSharedPreferences().edit().clear().commit();
    }

    @Test
    public void testBasicRead() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .putFloat("float", 3.14f)
            .putBoolean("bool", true)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        Assert.assertEquals("foobar", remotePrefs.getString("string", null));
        Assert.assertEquals(0xeceb3026, remotePrefs.getInt("int", 0));
        Assert.assertEquals(3.14f, remotePrefs.getFloat("float", 0f), 0.0);
        Assert.assertEquals(true, remotePrefs.getBoolean("bool", false));
    }

    @Test
    public void testStringSetRead() {
        HashSet<String> set = new HashSet<>();
        set.add("Chocola");
        set.add("Vanilla");
        set.add("Coconut");
        set.add("Azuki");
        set.add("Maple");
        set.add("Cinnamon");

        getSharedPreferences()
            .edit()
            .putStringSet("pref", set)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        Assert.assertEquals(set, remotePrefs.getStringSet("pref", null));
    }

    @Test
    public void testBasicWrite() {
        getRemotePreferences(true)
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .putFloat("float", 3.14f)
            .putBoolean("bool", true)
            .apply();

        SharedPreferences sharedPrefs = getSharedPreferences();
        Assert.assertEquals("foobar", sharedPrefs.getString("string", null));
        Assert.assertEquals(0xeceb3026, sharedPrefs.getInt("int", 0));
        Assert.assertEquals(3.14f, sharedPrefs.getFloat("float", 0f), 0.0);
        Assert.assertEquals(true, sharedPrefs.getBoolean("bool", false));
    }

    @Test
    public void testGetAll() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .putFloat("float", 3.14f)
            .putBoolean("bool", true)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        Map<String, ?> prefs = remotePrefs.getAll();
        Assert.assertEquals("foobar", prefs.get("string"));
        Assert.assertEquals(0xeceb3026, prefs.get("int"));
        Assert.assertEquals(3.14f, prefs.get("float"));
        Assert.assertEquals(true, prefs.get("bool"));
    }

    @Test
    public void testContains() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .putFloat("float", 3.14f)
            .putBoolean("bool", true)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        Assert.assertTrue(remotePrefs.contains("string"));
        Assert.assertTrue(remotePrefs.contains("int"));
        Assert.assertFalse(remotePrefs.contains("nonexistent"));
    }

    @Test
    public void testClear() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .putFloat("float", 3.14f)
            .putBoolean("bool", true)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        remotePrefs.edit().clear().apply();
        Assert.assertEquals(0, getSharedPreferences().getAll().size());
    }

    @Test
    public void testStringSetWrite() {
        HashSet<String> set = new HashSet<>();
        set.add("Chocola");
        set.add("Vanilla");
        set.add("Coconut");
        set.add("Azuki");
        set.add("Maple");
        set.add("Cinnamon");

        getRemotePreferences(true)
            .edit()
            .putStringSet("pref", set)
            .apply();

        SharedPreferences sharedPrefs = getSharedPreferences();
        Assert.assertEquals(set, sharedPrefs.getStringSet("pref", null));
    }

    @Test
    public void testEmptyStringSetRead() {
        HashSet<String> set = new HashSet<>();

        getSharedPreferences()
            .edit()
            .putStringSet("pref", set)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        Assert.assertEquals(set, remotePrefs.getStringSet("pref", null));
    }

    @Test
    public void testEmptyStringSetWrite() {
        HashSet<String> set = new HashSet<>();

        getRemotePreferences(true)
            .edit()
            .putStringSet("pref", set)
            .apply();

        SharedPreferences sharedPrefs = getSharedPreferences();
        Assert.assertEquals(set, sharedPrefs.getStringSet("pref", null));
    }

    @Test
    public void testSetContainingEmptyStringRead() {
        HashSet<String> set = new HashSet<>();
        set.add("");

        getSharedPreferences()
            .edit()
            .putStringSet("pref", set)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(true);
        Assert.assertEquals(set, remotePrefs.getStringSet("pref", null));
    }

    @Test
    public void testSetContainingEmptyStringWrite() {
        HashSet<String> set = new HashSet<>();
        set.add("");

        getRemotePreferences(true)
            .edit()
            .putStringSet("pref", set)
            .apply();

        SharedPreferences sharedPrefs = getSharedPreferences();
        Assert.assertEquals(set, sharedPrefs.getStringSet("pref", null));
    }

    @Test
    public void testReadNonexistentPref() {
        RemotePreferences remotePrefs = getRemotePreferences(true);
        Assert.assertEquals("default", remotePrefs.getString("nonexistent_string", "default"));
        Assert.assertEquals(1337, remotePrefs.getInt("nonexistent_int", 1337));
    }

    @Test
    public void testInvalidAuthorityStrictMode() {
        Context context = getLocalContext();
        RemotePreferences remotePrefs = new RemotePreferences(context, "foo", "bar", true);
        try {
            remotePrefs.getString("pref", null);
            Assert.fail();
        } catch (RemotePreferenceAccessException e) {
            // Expected
        }
    }

    @Test
    public void testInvalidAuthorityNonStrictMode() {
        Context context = getLocalContext();
        RemotePreferences remotePrefs = new RemotePreferences(context, "foo", "bar", false);
        Assert.assertEquals("default", remotePrefs.getString("pref", "default"));
    }

    @Test
    public void testDisabledProviderStrictMode() {
        RemotePreferences remotePrefs = getDisabledRemotePreferences(true);
        try {
            remotePrefs.getString("pref", null);
            Assert.fail();
        } catch (RemotePreferenceAccessException e) {
            // Expected
        }
    }

    @Test
    public void testDisabledProviderNonStrictMode() {
        RemotePreferences remotePrefs = getDisabledRemotePreferences(false);
        Assert.assertEquals("default", remotePrefs.getString("pref", "default"));
    }

    @Test
    public void testUnreadablePrefStrictMode() {
        RemotePreferences remotePrefs = getRemotePreferences(true);
        try {
            remotePrefs.getString(Constants.UNREADABLE_PREF_KEY, null);
            Assert.fail();
        } catch (RemotePreferenceAccessException e) {
            // Expected
        }
    }

    @Test
    public void testUnreadablePrefNonStrictMode() {
        RemotePreferences remotePrefs = getRemotePreferences(false);
        Assert.assertEquals("default", remotePrefs.getString(Constants.UNREADABLE_PREF_KEY, "default"));
    }

    @Test
    public void testUnwritablePrefStrictMode() {
        RemotePreferences remotePrefs = getRemotePreferences(true);
        try {
            remotePrefs
                .edit()
                .putString(Constants.UNWRITABLE_PREF_KEY, "foobar")
                .apply();
            Assert.fail();
        } catch (RemotePreferenceAccessException e) {
            // Expected
        }
    }

    @Test
    public void testUnwritablePrefNonStrictMode() {
        RemotePreferences remotePrefs = getRemotePreferences(false);
        Assert.assertFalse(
            remotePrefs
                .edit()
                .putString(Constants.UNWRITABLE_PREF_KEY, "foobar")
                .commit()
        );
    }

    @Test
    public void testRemovePref() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        remotePrefs.edit().remove("string").apply();

        Assert.assertEquals("default", remotePrefs.getString("string", "default"));
        Assert.assertEquals(0xeceb3026, remotePrefs.getInt("int", 0));
    }

    @Test
    public void testRemoveAll() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 0xeceb3026)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        remotePrefs.edit().clear().apply();

        Assert.assertEquals("default", remotePrefs.getString("string", "default"));
        Assert.assertEquals(0, remotePrefs.getInt("int", 0));
    }

    @Test
    public void testRemoveUnwritablePref() {
        getSharedPreferences()
            .edit()
            .putString(Constants.UNWRITABLE_PREF_KEY, "foobar")
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        Assert.assertFalse(remotePrefs.edit().remove(Constants.UNWRITABLE_PREF_KEY).commit());

        Assert.assertEquals("foobar", remotePrefs.getString(Constants.UNWRITABLE_PREF_KEY, "default"));
    }

    @Test
    public void testReadStringAsStringSetFail() {
        getSharedPreferences()
            .edit()
            .putString("pref", "foo;bar;")
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        try {
            remotePrefs.getStringSet("pref", null);
            Assert.fail();
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test
    public void testReadStringSetAsStringFail() {
        HashSet<String> set = new HashSet<>();
        set.add("foo");
        set.add("bar");

        getSharedPreferences()
            .edit()
            .putStringSet("pref", set)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        try {
            remotePrefs.getString("pref", null);
            Assert.fail();
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test
    public void testReadBooleanAsIntFail() {
        getSharedPreferences()
            .edit()
            .putBoolean("pref", true)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        try {
            remotePrefs.getInt("pref", 0);
            Assert.fail();
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @Test
    public void testReadIntAsBooleanFail() {
        getSharedPreferences()
            .edit()
            .putInt("pref", 42)
            .apply();

        RemotePreferences remotePrefs = getRemotePreferences(false);
        try {
            remotePrefs.getBoolean("pref", false);
            Assert.fail();
        } catch (ClassCastException e) {
            // Expected
        }
    }
}
