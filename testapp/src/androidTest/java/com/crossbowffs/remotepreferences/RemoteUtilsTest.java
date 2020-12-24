package com.crossbowffs.remotepreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class RemoteUtilsTest {
    @Test
    public void testSerializeStringSet() {
        Set<String> set = new LinkedHashSet<String>();
        set.add("foo");
        set.add("bar;");
        set.add("baz");
        set.add("");

        String serialized = RemoteUtils.serializeStringSet(set);
        Assert.assertEquals("foo;bar\\;;baz;;", serialized);
    }

    @Test
    public void testDeserializeStringSet() {
        Set<String> set = new LinkedHashSet<String>();
        set.add("foo");
        set.add("bar;");
        set.add("baz");
        set.add("");

        String serialized = RemoteUtils.serializeStringSet(set);
        Set<String> deserialized = RemoteUtils.deserializeStringSet(serialized);
        Assert.assertEquals(set, deserialized);
    }

    @Test
    public void testSerializeEmptyStringSet() {
        Assert.assertEquals("", RemoteUtils.serializeStringSet(new HashSet<String>()));
    }

    @Test
    public void testDeserializeEmptyStringSet() {
        Assert.assertEquals(new HashSet<String>(), RemoteUtils.deserializeStringSet(""));
    }

    @Test
    public void testDeserializeInvalidStringSet() {
        try {
            RemoteUtils.deserializeStringSet("foo;bar");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
