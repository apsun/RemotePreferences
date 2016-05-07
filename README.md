# RemotePreferences

A drop-in solution for global (inter-app) access to `SharedPreferences`.

A word of warning: this library is currently under development and may
not be suitable for usage in a production environment. Use at your own risk!


## Adding the library

Simply add the following line to your `build.gradle` depdencies section:

`compile 'com.crossbowffs.remotepreferences:remotepreferences:0.1'`


## Usage

First, subclass `RemotePreferenceProvider` (remember to add
the corresponding entry in your `AndroidManifest.xml`) and
implement the default constructor, which should call the super
constructor with the appropriate `authority` and `prefNames`
parameters. `authority` should have the same value as the
`android:authorities` attribute in `AndroidManifest.xml`, and
`prefNames` contains the names of the preference files you wish
to export. For example:

MyPreferenceProvider.java
```Java
public class MyPreferenceProvider extends RemotePreferenceProvider {
    public MyPreferenceProvider() {
        super("com.example.myapp.preferences", new String[] {"main_prefs"});
    }
}
```

AndroidManifest.xml
```XML
<provider
    android:authorities="com.example.myapp.preferences"
    android:name=".MyPreferenceProvider"
    android:exported="true"/>
```

Now, you can use `RemotePreferences` just like you would `SharedPreferences`.
To create a new instance, just pass in the authority and the preference file name,
like so:

```Java
SharedPreferences prefs = new RemotePreferences(context, "com.example.myapp.preferences", "main_prefs");
```

That's it! Simple, right?

Note that you can (and should) still use `getSharedPreferences(prefName, MODE_PRIVATE)`
if your code is executing within the app that owns the preferences. Only use
`RemotePreferences` when accessing preferences from the context of another app.


## Features

`RemotePreferences` is fully compatible with the `SharedPreferences` API!
If you need a feature support table to convince you, look no further:

| Method                                                                                  | Supported |
|-----------------------------------------------------------------------------------------|-----------|
| `getAll()`                                                                              | YES       |
| `getString(String key, String defValue)`                                                | YES       |
| `getStringSet(String key, Set<String> defValues)`                                       | YES*      |
| `getInt(String key, int defValue)`                                                      | YES       |
| `getLong(String key, long defValue)`                                                    | YES       |
| `getFloat(String key, float defValue)`                                                  | YES       |
| `getBoolean(String key, boolean defValue)`                                              | YES       |
| `contains(String key)`                                                                  | YES       |
| `edit()`                                                                                | YES       |
| `registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)`   | YES       |
| `unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)` | YES       |

But wait, what about preference editing?

| Method                                          | Supported |
|-------------------------------------------------|-----------|
| `putString(String key, String value)`           | YES       |
| `putStringSet(String key, Set<String> value)`   | YES*      |
| `putInt(String key, int value)`                 | YES       |
| `putLong(String key, long value)`               | YES       |
| `putFloat(String key, float value)`             | YES       |
| `putBoolean(String key, boolean value)`         | YES       |
| `remove(String key)`                            | YES       |
| `clear()`                                       | YES       |
| `commit()`                                      | YES       |
| `apply()`                                       | YES**     |

\* String set operations are only supported on Android API 11 or higher  
\*\* `apply()` is executed synchronously; use `AsyncTask` if performance is an issue


## Why would I need this?

This library was developed to simplify Xposed module preference access.
`XSharedPreferences` [has been known to silently fail on some devices]
(https://github.com/rovo89/XposedBridge/issues/74), and does not support
remote write access or value changed listeners. And thus, RemotePreferences
was born.

Of course, feel free to use this library anywhere you like; it's not
limited to Xposed at all! :-)


## How does it work?

To achieve true inter-process `SharedPreferences` access, all requests
are proxied through a `ContentProvider`. Preference change callbacks are
implemented using `ContentObserver`.

This solution does **not** use `MODE_WORLD_WRITEABLE` (which was
deprecated in Android 4.2) or any other file permission hacks.


## License

Distributed under the [MIT License](http://opensource.org/licenses/MIT).
