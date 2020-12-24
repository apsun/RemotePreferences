package com.crossbowffs.remotepreferences;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.crossbowffs.remotepreferences.testapp.TestConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

@RunWith(AndroidJUnit4.class)
public class RemotePreferenceProviderTest {
    private Context getLocalContext() {
        return InstrumentationRegistry.getInstrumentation().getContext();
    }

    private Context getRemoteContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private SharedPreferences getSharedPreferences() {
        Context context = getRemoteContext();
        return context.getSharedPreferences(TestConstants.PREF_FILE, Context.MODE_PRIVATE);
    }

    private Uri getQueryUri(String key) {
        String uri = "content://" + TestConstants.AUTHORITY + "/" + TestConstants.PREF_FILE;
        if (key != null) {
            uri += "/" + key;
        }
        return Uri.parse(uri);
    }

    @Before
    public void resetPreferences() {
        getSharedPreferences().edit().clear().commit();
    }

    @Test
    public void testQueryAllPrefs() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 1337)
            .apply();

        ContentResolver resolver = getLocalContext().getContentResolver();
        Cursor q = resolver.query(getQueryUri(null), null, null, null, null);
        Assert.assertEquals(2, q.getCount());

        int key = q.getColumnIndex(RemoteContract.COLUMN_KEY);
        int type = q.getColumnIndex(RemoteContract.COLUMN_TYPE);
        int value = q.getColumnIndex(RemoteContract.COLUMN_VALUE);

        while (q.moveToNext()) {
            if (q.getString(key).equals("string")) {
                Assert.assertEquals(RemoteContract.TYPE_STRING, q.getInt(type));
                Assert.assertEquals("foobar", q.getString(value));
            } else if (q.getString(key).equals("int")) {
                Assert.assertEquals(RemoteContract.TYPE_INT, q.getInt(type));
                Assert.assertEquals(1337, q.getInt(value));
            } else {
                Assert.fail();
            }
        }
    }

    @Test
    public void testQuerySinglePref() {
        getSharedPreferences()
            .edit()
            .putString("string", "foobar")
            .putInt("int", 1337)
            .apply();

        ContentResolver resolver = getLocalContext().getContentResolver();
        Cursor q = resolver.query(getQueryUri("string"), null, null, null, null);
        Assert.assertEquals(1, q.getCount());

        int key = q.getColumnIndex(RemoteContract.COLUMN_KEY);
        int type = q.getColumnIndex(RemoteContract.COLUMN_TYPE);
        int value = q.getColumnIndex(RemoteContract.COLUMN_VALUE);

        q.moveToFirst();
        Assert.assertEquals("string", q.getString(key));
        Assert.assertEquals(RemoteContract.TYPE_STRING, q.getInt(type));
        Assert.assertEquals("foobar", q.getString(value));
    }

    @Test
    public void testQueryFailPermissionCheck() {
        getSharedPreferences()
            .edit()
            .putString(TestConstants.UNREADABLE_PREF_KEY, "foobar")
            .apply();
        ContentResolver resolver = getLocalContext().getContentResolver();
        try {
            resolver.query(getQueryUri(TestConstants.UNREADABLE_PREF_KEY), null, null, null, null);
            Assert.fail();
        } catch (SecurityException e) {
            // Expected
        }
    }

    @Test
    public void testInsertPref() {
        ContentValues values = new ContentValues();
        values.put(RemoteContract.COLUMN_KEY, "string");
        values.put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values.put(RemoteContract.COLUMN_VALUE, "foobar");

        ContentResolver resolver = getLocalContext().getContentResolver();
        Uri uri = resolver.insert(getQueryUri(null), values);
        Assert.assertEquals(getQueryUri("string"), uri);

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("foobar", prefs.getString("string", null));
    }

    @Test
    public void testInsertOverridePref() {
        SharedPreferences prefs = getSharedPreferences();
        prefs
            .edit()
            .putString("string", "nyaa")
            .putInt("int", 1337)
            .apply();

        ContentValues values = new ContentValues();
        values.put(RemoteContract.COLUMN_KEY, "string");
        values.put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values.put(RemoteContract.COLUMN_VALUE, "foobar");

        ContentResolver resolver = getLocalContext().getContentResolver();
        Uri uri = resolver.insert(getQueryUri(null), values);
        Assert.assertEquals(getQueryUri("string"), uri);

        Assert.assertEquals("foobar", prefs.getString("string", null));
        Assert.assertEquals(1337, prefs.getInt("int", 0));
    }

    @Test
    public void testInsertPrefKeyInUri() {
        ContentValues values = new ContentValues();
        values.put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values.put(RemoteContract.COLUMN_VALUE, "foobar");

        ContentResolver resolver = getLocalContext().getContentResolver();
        Uri uri = resolver.insert(getQueryUri("string"), values);
        Assert.assertEquals(getQueryUri("string"), uri);

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("foobar", prefs.getString("string", null));
    }

    @Test
    public void testInsertPrefKeyInUriAndValues() {
        ContentValues values = new ContentValues();
        values.put(RemoteContract.COLUMN_KEY, "string");
        values.put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values.put(RemoteContract.COLUMN_VALUE, "foobar");

        ContentResolver resolver = getLocalContext().getContentResolver();
        Uri uri = resolver.insert(getQueryUri("string"), values);
        Assert.assertEquals(getQueryUri("string"), uri);

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("foobar", prefs.getString("string", null));
    }

    @Test
    public void testInsertPrefFailKeyInUriAndValuesMismatch() {
        ContentValues values = new ContentValues();
        values.put(RemoteContract.COLUMN_KEY, "string");
        values.put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values.put(RemoteContract.COLUMN_VALUE, "foobar");

        ContentResolver resolver = getLocalContext().getContentResolver();
        try {
            resolver.insert(getQueryUri("string2"), values);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("default", prefs.getString("string", "default"));
    }

    @Test
    public void testInsertMultiplePrefs() {
        ContentValues[] values = new ContentValues[2];
        values[0] = new ContentValues();
        values[0].put(RemoteContract.COLUMN_KEY, "string");
        values[0].put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values[0].put(RemoteContract.COLUMN_VALUE, "foobar");

        values[1] = new ContentValues();
        values[1].put(RemoteContract.COLUMN_KEY, "int");
        values[1].put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_INT);
        values[1].put(RemoteContract.COLUMN_VALUE, 1337);

        ContentResolver resolver = getLocalContext().getContentResolver();
        int ret = resolver.bulkInsert(getQueryUri(null), values);
        Assert.assertEquals(2, ret);

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("foobar", prefs.getString("string", null));
        Assert.assertEquals(1337, prefs.getInt("int", 0));
    }

    @Test
    public void testInsertFailPermissionCheck() {
        ContentValues[] values = new ContentValues[2];
        values[0] = new ContentValues();
        values[0].put(RemoteContract.COLUMN_KEY, "string");
        values[0].put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values[0].put(RemoteContract.COLUMN_VALUE, "foobar");

        values[1] = new ContentValues();
        values[1].put(RemoteContract.COLUMN_KEY, TestConstants.UNWRITABLE_PREF_KEY);
        values[1].put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_INT);
        values[1].put(RemoteContract.COLUMN_VALUE, 1337);

        ContentResolver resolver = getLocalContext().getContentResolver();
        try {
            resolver.bulkInsert(getQueryUri(null), values);
            Assert.fail();
        } catch (SecurityException e) {
            // Expected
        }

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("default", prefs.getString("string", "default"));
        Assert.assertEquals(0, prefs.getInt(TestConstants.UNWRITABLE_PREF_KEY, 0));
    }

    @Test
    public void testInsertMultipleFailUriContainingKey() {
        ContentValues[] values = new ContentValues[1];
        values[0] = new ContentValues();
        values[0].put(RemoteContract.COLUMN_KEY, "string");
        values[0].put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING);
        values[0].put(RemoteContract.COLUMN_VALUE, "foobar");

        ContentResolver resolver = getLocalContext().getContentResolver();
        try {
            resolver.bulkInsert(getQueryUri("key"), values);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        SharedPreferences prefs = getSharedPreferences();
        Assert.assertEquals("default", prefs.getString("string", "default"));
    }

    @Test
    public void testDeletePref() {
        SharedPreferences prefs = getSharedPreferences();
        prefs
            .edit()
            .putString("string", "nyaa")
            .apply();

        ContentResolver resolver = getLocalContext().getContentResolver();
        resolver.delete(getQueryUri("string"), null, null);

        Assert.assertEquals("default", prefs.getString("string", "default"));
    }

    @Test
    public void testDeleteUnwritablePref() {
        SharedPreferences prefs = getSharedPreferences();
        prefs
            .edit()
            .putString(TestConstants.UNWRITABLE_PREF_KEY, "nyaa")
            .apply();

        ContentResolver resolver = getLocalContext().getContentResolver();
        try {
            resolver.delete(getQueryUri(TestConstants.UNWRITABLE_PREF_KEY), null, null);
            Assert.fail();
        } catch (SecurityException e) {
            // Expected
        }

        Assert.assertEquals("nyaa", prefs.getString(TestConstants.UNWRITABLE_PREF_KEY, "default"));
    }

    @Test
    public void testReadBoolean() {
        getSharedPreferences()
            .edit()
            .putBoolean("true", true)
            .putBoolean("false", false)
            .apply();

        ContentResolver resolver = getLocalContext().getContentResolver();
        Cursor q = resolver.query(getQueryUri(null), null, null, null, null);
        Assert.assertEquals(2, q.getCount());

        int key = q.getColumnIndex(RemoteContract.COLUMN_KEY);
        int type = q.getColumnIndex(RemoteContract.COLUMN_TYPE);
        int value = q.getColumnIndex(RemoteContract.COLUMN_VALUE);

        while (q.moveToNext()) {
            if (q.getString(key).equals("true")) {
                Assert.assertEquals(RemoteContract.TYPE_BOOLEAN, q.getInt(type));
                Assert.assertEquals(1, q.getInt(value));
            } else if (q.getString(key).equals("false")) {
                Assert.assertEquals(RemoteContract.TYPE_BOOLEAN, q.getInt(type));
                Assert.assertEquals(0, q.getInt(value));
            } else {
                Assert.fail();
            }
        }
    }

    @Test
    public void testReadStringSet() {
        HashSet<String> set = new HashSet<>();
        set.add("foo");
        set.add("bar;");
        set.add("baz");
        set.add("");

        getSharedPreferences()
            .edit()
            .putStringSet("pref", set)
            .apply();

        ContentResolver resolver = getLocalContext().getContentResolver();
        Cursor q = resolver.query(getQueryUri("pref"), null, null, null, null);
        Assert.assertEquals(1, q.getCount());

        int key = q.getColumnIndex(RemoteContract.COLUMN_KEY);
        int type = q.getColumnIndex(RemoteContract.COLUMN_TYPE);
        int value = q.getColumnIndex(RemoteContract.COLUMN_VALUE);

        while (q.moveToNext()) {
            if (q.getString(key).equals("pref")) {
                Assert.assertEquals(RemoteContract.TYPE_STRING_SET, q.getInt(type));
                String serialized = q.getString(value);
                Assert.assertEquals(set, RemoteUtils.deserializeStringSet(serialized));
            } else {
                Assert.fail();
            }
        }
    }

    @Test
    public void testInsertStringSet() {
        HashSet<String> set = new HashSet<>();
        set.add("foo");
        set.add("bar;");
        set.add("baz");
        set.add("");

        ContentValues values = new ContentValues();
        values.put(RemoteContract.COLUMN_KEY, "pref");
        values.put(RemoteContract.COLUMN_TYPE, RemoteContract.TYPE_STRING_SET);
        values.put(RemoteContract.COLUMN_VALUE, RemoteUtils.serializeStringSet(set));

        ContentResolver resolver = getLocalContext().getContentResolver();
        Uri uri = resolver.insert(getQueryUri(null), values);
        Assert.assertEquals(getQueryUri("pref"), uri);

        Assert.assertEquals(set, getSharedPreferences().getStringSet("pref", null));
    }
}
