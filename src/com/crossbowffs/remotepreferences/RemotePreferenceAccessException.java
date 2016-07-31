package com.crossbowffs.remotepreferences;

/**
 * Thrown if the preferences could not be loaded. This is commonly
 * thrown under these conditions:
 * <ul>
 *     <li>Preference provider component is disabled</li>
 *     <li>Preference provider denied access via {@link RemotePreferenceProvider#checkAccess(String, String, boolean)}</li>
 *     <li>Insufficient permissions to access provider (via AndroidManifest.xml)</li>
 *     <li>Incorrect provider authority passed to constructor</li>
 *     <li>Accessing string set preference on pre-API11 devices</li>
 * </ul>
 */
public class RemotePreferenceAccessException extends RuntimeException {
    public RemotePreferenceAccessException() {

    }

    public RemotePreferenceAccessException(String detailMessage) {
        super(detailMessage);
    }

    public RemotePreferenceAccessException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RemotePreferenceAccessException(Throwable throwable) {
        super(throwable);
    }
}
