# Usage
You can use `LocaleFactory.get(Class<T extends LocalizableResource> cls)` to obtain `Messages`, `Constants` or `ConstantsWithLookup` both on server and client-side.

### Server
Add `I18nFilter` to `web.xml` or use `LocaleProxy.setLocale()` with `LocaleProxy.clear()` to manually set thread-local locale.

Make sure you are executing `LocaleProxy.initialize()` before any `LocaleFactory` calls, or you will get "Messages not found" exception. An option to do this is to add `I18nInitializer` listener to your `web.xml`.

### Client
Add `<inherits name="com.teklabs.gwt_i18n_server.GwtI18nServer"/>` to your GWT XML module descriptor.

Call `LocaleFactory.put(MyMessages.class, GWT.<MyMessages>create(MyMessages.class))` in your `EntryPoint`.

# Maven repositories
Snapshots - <https://service.teklabs.com/nexus/content/repositories/public-snapshots/>

Releases - <https://service.teklabs.com/nexus/content/repositories/public-releases/>

Latest release:
<pre>
    &lt;dependency&gt;
        &lt;groupId&gt;com.teklabs.gwt-i18n-server&lt;/groupId&gt;
        &lt;artifactId&gt;gwt-i18n-server&lt;/artifactId&gt;
        &lt;version&gt;0.3&lt;/version&gt;
    &lt;/dependency&gt;
</pre>
