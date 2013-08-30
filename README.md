# Usage
You can use `LocaleFactory.get(Class<T extends LocalizableResource> cls)` to obtain `Messages`, `Constants` or `ConstantsWithLookup` both on server and client side.

### Server
Add `I18nFilter` to `web.xml` or use `ThreadLocalLocaleProvider.setLocale()` with `ThreadLocalLocaleProvider.clear()` to manually set thread-local locale.

Make sure you are executing `LocaleProxy.initialize()` before any `LocaleFactory` calls, or you will get "Messages not found" exception. An option to do this is to add `I18nInitializer` listener to your `web.xml`.

Also there are a couple of additional server-only features like `MessagesWithLookup` interface and *encoding* proxies, which allow you to use localization in a more dynamic way.

### Client
Add `<inherits name="net.lightoze.gwt.i18n.GwtI18nServer"/>` to your GWT XML module descriptor.

Call `LocaleFactory.put(MyMessages.class, GWT.<MyMessages>create(MyMessages.class))` in your `EntryPoint`.

# Maven repositories
Snapshots - <https://oss.sonatype.org/content/groups/public/>

Releases - Maven Central

Latest release (for GWT 2.5):
<pre>
    &lt;dependency&gt;
        &lt;groupId&gt;net.lightoze.gwt-i18n-server&lt;/groupId&gt;
        &lt;artifactId&gt;gwt-i18n-server&lt;/artifactId&gt;
        &lt;version&gt;0.20&lt;/version&gt;
    &lt;/dependency&gt;
</pre>

This artifact contains a copy of `com.google.gwt.i18n` package from GWT distribution, so you don't have to deploy the whole GWT library on the server.
In case you want only `gwt-i18n-server` classes, use an artifact with `original` classifier.