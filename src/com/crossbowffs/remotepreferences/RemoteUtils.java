package com.crossbowffs.remotepreferences;

import java.util.HashSet;
import java.util.Set;

/* package */ class RemoteUtils {
    @SuppressWarnings("unchecked")
    public static Set<String> castStringSet(Object value) {
        // This is just to centralize the unchecked type cast warning.
        // Since all sets we are dealing with are string sets, this
        // cast should always work as expected.
        return (Set<String>)value;
    }

    public static int getPreferenceType(Object value) {
        if (value == null) return RemoteContract.TYPE_NULL;
        if (value instanceof String) return RemoteContract.TYPE_STRING;
        if (value instanceof Set<?>) return RemoteContract.TYPE_STRING_SET;
        if (value instanceof Integer) return RemoteContract.TYPE_INT;
        if (value instanceof Long) return RemoteContract.TYPE_LONG;
        if (value instanceof Float) return RemoteContract.TYPE_FLOAT;
        if (value instanceof Boolean) return RemoteContract.TYPE_BOOLEAN;
        throw new AssertionError("Unknown preference type: " + value.getClass());
    }

    public static Object serializeOutput(Object value) {
        if (value instanceof Boolean) {
            return serializeBoolean((Boolean)value);
        } else if (value instanceof Set<?>) {
            return serializeStringSet(castStringSet(value));
        } else {
            return value;
        }
    }

    @SuppressWarnings("RedundantCast")
    public static Object deserializeInput(Object value, int expectedType) {
        if (expectedType == RemoteContract.TYPE_NULL) {
            if (value != null) {
                throw new IllegalArgumentException("Expected null, got non-null value");
            } else {
                return null;
            }
        }
        try {
            switch (expectedType) {
            case RemoteContract.TYPE_STRING:
                return (String)value;
            case RemoteContract.TYPE_STRING_SET:
                return deserializeStringSet((String)value);
            case RemoteContract.TYPE_INT:
                return (Integer)value;
            case RemoteContract.TYPE_LONG:
                return (Long)value;
            case RemoteContract.TYPE_FLOAT:
                return (Float)value;
            case RemoteContract.TYPE_BOOLEAN:
                return deserializeBoolean(value);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected type " + expectedType + ", got " + value.getClass(), e);
        }
        throw new IllegalArgumentException("Unknown type: " + expectedType);
    }

    public static Integer serializeBoolean(Boolean value) {
        if (value == null) {
            return null;
        } else {
            return value ? 1 : 0;
        }
    }

    public static Boolean deserializeBoolean(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            return (Integer)value != 0;
        }
    }

    public static String serializeStringSet(Set<String> stringSet) {
        if (stringSet == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : stringSet) {
            sb.append(s.replace("\\", "\\\\").replace(";", "\\;"));
            sb.append(';');
        }
        return sb.toString();
    }

    public static Set<String> deserializeStringSet(String serializedString) {
        if (serializedString == null) {
            return null;
        }
        HashSet<String> stringSet = new HashSet<String>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < serializedString.length(); ++i) {
            char c = serializedString.charAt(i);
            if (c == '\\') {
                char next = serializedString.charAt(++i);
                sb.append(next);
            } else if (c == ';') {
                stringSet.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        return stringSet;
    }
}
