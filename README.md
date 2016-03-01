# DIUASparser

A fast User Agent parser library, using data from [udger.com](http://udger.com/)

## Maven Dependency
```
<dependency>
  <groupId>me.atlis</groupId>
  <artifactId>decibelinsight-uasparser</artifactId>
  <version>RELEASE</version>
</dependency>
```

## Usage

Simply use DIUASparser or any of its subclasses like so:

```
UASparser parser = new UASparser();
String key = ""; // udger license key
OnlineUpdater updater = new OnlineUpdater(parser, key);
UserAgentInfo info = parser.parse("Mozilla/4.0 (compatible; MSIE 7.0;Windows NT 5.1; )");
```

This will create a new parser and initialize it with a bundled copy of the database. The
``OnlineUpdater`` will then asynchronously fetch the latest database in the
background, making it available after a few seconds and caching it locally as well. See
it's source for more on how it works.

In addition, there are a few different parser classes available:

* ``UASparser`` - Default parser, thread-safe
* ``MultithreadedUASparser`` - A faster variant of UASparser, uses a bit more memory
* ``SingleThreadedUASparser`` - Non-threadsafe variant, ideal for Hadoop and similar use cases
* ``BrowserFamilyParser`` - UASparser subclass which _only_ returns the browser family string

## Dependencies

* [JRegex](http://jregex.sourceforge.net/)

## License

LGPL. See LICENSE file for details.
