# RemotePreferences

A drop-in solution for global (inter-app) access to `SharedPreferences`.

A word of warning: this library is currently under development and may
not be suitable for usage in a production environment. Use at your own risk!


## Changelog

0.2

- Fixed catastrophic security bug allowing anyone to write to preferences
- Added strict mode to distinguish between "cannot access provider" vs. "key doesn't exist"
- Keys can no longer be `null` or `""`, values can no longer be `null`

0.1

- Initial release.


## Installing

1\. Add the dependency to your `build.gradle` file:

```
repositories {
    jcenter()
}

dependencies {
    compile 'com.crossbowffs.remotepreferences:remotepreferences:0.2'
}
```

2\. Subclass `RemotePreferenceProvider` and implement a default
constructor which calls the super constructor with an authority
(pick one, e.g. `"com.example.app.preferences"`) and an array of
preference files to expose:

```Java
public class MyPreferenceProvider extends RemotePreferenceProvider {
    public MyPreferenceProvider() {
        super("com.example.app.preferences", new String[] {"main_prefs"});
    }
}
```

3\. Add the corresponding entry to `AndroidManifest.xml`, with
`android:authorities` equal to the value you picked in step 2:

```XML
<provider
    android:authorities="com.example.app.preferences"
    android:name=".MyPreferenceProvider"
    android:exported="true"/>
```

4\. You're all set! To access your preferences, create a new
instance of `RemotePreferences` with the authority value you
picked earlier and the name of the preference file:

```Java
SharedPreferences prefs = new RemotePreferences(context, "com.example.app.preferences", "main_prefs");
```

Note that you can (and should) still use `getSharedPreferences("main_prefs", MODE_PRIVATE)`
if your code is executing within the app that owns the preferences. Only use
`RemotePreferences` when accessing preferences from the context of another app.


## Compatibility

`RemotePreferences` is fully compatible with the `SharedPreferences`
API, with these minor exceptions:

- String set operations are only supported on Android API 11 or higher
- `apply()` is executed synchronously (i.e. it's equivalent to `commit()`)
- Keys cannot be empty (`null` or `""`), values cannot be `null`


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
