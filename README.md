# Usage
You can use `LocaleFactory.getMessages(Class<T extends Messages> cls)` to obtain Messages both on server and client-side.

### Server
Add `I18nFilter` to `web.xml` or use `MessagesProxy.setLocale()` with `MessagesProxy.clear()` to manually set thread-local locale.

Make sure you are executing  `MessagesProxy.initialize()` before any `LocaleFactory` calls, or you will get "Messages not found" exception. An option to do this is to add `I18nInitializer` listener to your `web.xml`.

### Client
Call `LocaleFactory.putMessages(MyMessages.class, GWT.create(MyMessages.class))` in your `EntryPoint`.

# Maven repositories
Snapshots - https://service.teklabs.com/nexus/content/repositories/public-snapshots/

Releases - https://service.teklabs.com/nexus/content/repositories/public-releases/
