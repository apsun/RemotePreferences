# RemotePreferences

A drop-in solution for global (inter-app) access to `SharedPreferences`.


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
    android:name=".MyPreferenceProvider"/>
```

Next, replace all uses of `SharedPreferences` with `RemotePreferences`.
For example, if this was your original code:

```Java
SharedPreferences prefs = context.getSharedPreferences("main_prefs", MODE_PRIVATE);
```

Simply change it to:

```Java
SharedPreferences prefs = new RemotePreferences(context, "com.example.myapp.preferences", "main_prefs");
```

That's it! Simple, right?


## Features

`RemotePreferences` is fully compatible with the `SharedPreferences` API!
If you need a feature support table to convince you, look no further:

| Method                                                                                | Working |
|---------------------------------------------------------------------------------------|---------|
| getAll()                                                                              | YES     |
| getString(String key, String defValue)                                                | YES     |
| getStringSet(String key, Set defValues)                                               | YES     |
| getInt(String key, int defValue)                                                      | YES     |
| getLong(String key, long defValue)                                                    | YES     |
| getFloat(String key, float defValue)                                                  | YES     |
| getBoolean(String key, boolean defValue)                                              | YES     |
| contains(String key)                                                                  | YES     |
| edit()                                                                                | YES     |
| registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)   | YES     |
| unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) | YES     |

But wait, what about preference editing?

| Method                                | Working |
|---------------------------------------|---------|
| putString(String key, String value)   | YES     |
| putStringSet(String key, Set value)   | YES     |
| putInt(String key, int value)         | YES     |
| putLong(String key, long value)       | YES     |
| putFloat(String key, float value)     | YES     |
| putBoolean(String key, boolean value) | YES     |
| remove(String key)                    | YES     |
| clear()                               | YES     |
| commit()                              | YES     |
| apply()                               | YES     |

All API's are implemented exactly according to spec, so you should
not notice any differences (other than a slight performance drop) between
`SharedPreferences` and `RemotePreferences`.


## But how?

To achieve true inter-process `SharedPreferences` access, all requests
are proxied through a `ContentProvider`. Preference change callbacks are
implemented using `ContentObserver`.

This solution does **not** use `MODE_WORLD_WRITEABLE` (which was
deprecated in Android 4.2) or any other file permission hacks.


## License

Distributed under the [MIT License](http://opensource.org/licenses/MIT).
