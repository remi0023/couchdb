# nouveau
Lucene 9 + DropWizard = Maybe a good search option for Apache CouchDB?

Nouveau is an experimental search extension for CouchDB 3.x.

## What works?

* you can define a default analyzer and different analyzers by field name.
* sorting on text and numbers
* classic lucene query syntax
* count and range facets
* cursor support for paginating efficiently through large results sets
* indexes automatically deleted if database is deleted (as long as nouveau is running!)
* integration with ken
* update=false
* support for stale=ok
* integration with mango

## What doesn't work yet?

* include_docs=true
* No support for results grouping
* No support to configure stop words for analyzers

## Why is this better than dreyfus/clouseau?

* No scalang (or Scala!)
* Supports any version of Java that Lucene 9 supports
* memory-mapped I/O for performance
* direct I/O used for segment merging (so we don't evict useful data from disk cache)
* It's new and shiny.

## Erlang side

You'll need to run a fork of couchdb: https://github.com/rnewson/couchdb-nouveau

## Getting started

Build Nouveau with;

`mvn package`

Run Nouvea with;

`java -jar target/nouveau-*.jar server nouveau.yaml`

Now run CouchDB using the 'nouveau' branch of my fork at https://github.com/rnewson/couchdb-nouveau;

`make && dev/run --admin=foo:bar`

Make a database with some data and an index definition;

```
#!/bin/sh

URL="http://foo:bar@127.0.0.1:15984/foo"

curl -X DELETE "$URL"
curl -X PUT "$URL?n=3&q=16"

curl -X PUT "$URL/_design/foo" -d '{"nouveau":{"bar":{"default_analyzer":"standard", "field_analyzers":{"foo":"english"}, "index":"function(doc) { index(\"foo\", \"bar\", \"string\"); index(\"foo\", \"bar\", \"stored_string\"); }"}}}'

# curl "$URL/_index" -Hcontent-type:application/json -d '{"type":"nouveau", "index": {"fields": [{"name": "bar", "type":"number"}]}}'

for I in {1..100}; do
    DOCID=$RANDOM
    DOCID=$[ $DOCID % 100000 ]
    BAR=$RANDOM
    BAR=$[ $BAR % 100000 ]
    curl -X PUT "$URL/doc$DOCID" -d "{\"bar\": $BAR}"
done
```

In order not to collide with `dreyfus` I've hooked Nouveau in with some uglier paths for now;

`curl 'foo:bar@localhost:15984/foo/_design/foo/_nouveau/bar?q=*:*'`

This will cause Nouveau to build indexes for each copy (N) and each
shard range (Q) and then perform a search and return the results. Lots
of query syntax is working as is sorting on strings and numbers
(`sort=["fieldnamehere&lt;string&gt;"] or sort=["fieldnamehere&lt;number&gt;"],
defaulting to number).

Facet support

Counts of string fields and Ranges for numeric fields;

```
curl 'foo:bar@localhost:15984/foo/_design/foo/_nouveau/bar?q=*:*&limit=1&ranges={"bar":[{"label":"cheap","min":0,"max":100}]}&counts=["foo"]' -g
```

## Index function

To ease migration nouveau functions can use the 'index' function exactly as it exists in dreyfus, but the function also supports a new style.

| Arguments                                          | Effect
| :------------------------------------------------- | :-----
| index("foo", "bar");                               | adds a TextField.
| index("foo", "bar", {"store":true});               | adds a TextField and a StoredField.
| index("foo", "bar", {"store":true, "facet":true}); | adds a TextField, a StoredField and a SortedSetDocValuesField.
| index("foo", "bar", "text");                       | adds a TextField.
| index("foo", "bar", "string");                     | adds a StringField.
| index("foo", "bar", "stored");                     | adds a StoredField.
| index("foo", "bar", "sorted_set_dv");              | adds a SortedSetDocValuesField.
| index("foo", "bar", "string", true);               | adds a TextField with Store.YES


## Architecture

Nouveau consists of two main components;

### Shaded Lucene Libraries

In order to support index migration we've chosen to include multiple
versions of Lucene at the same time and treat each major release as
independent of the others. You upgrade a nouveau index by rebuilding
from scratch on the new version.

To accomplish this we relocate the lucene classes so that the package
names are distinct, using the Maven Shade Plugin.

Nouveau only supports one release of each supported major version and
will update those versions as part of its own release cycle. An
exception is made for Lucene 4.6.1 which is included to aid index
migration from Clouseau. We will not upgrade the version of Lucene 4
in future releases, users must migrate to Lucene 9 or higher.

### Dropwizard Server

Nouveau uses the dropwizard framework to provide an HTTP interface to
core Lucene functionality.

Within this project are the following components;

#### api

The api package (and its subpackages) contains classes that represents
the request and response bodies for all HTTP requests. Like CouchDB,
these are represented in JSON on the wire.

This package does not depend on any version of Lucene.

#### core

This package contains the main code, independent of Lucene
version. Subpackages provide Lucene-version specific implementations
of interfaces or abstract classes from the core package.

#### health

This package contains dropwizard health check code that verifies the
correct working of Nouveau.

#### resources

This package (and its subpackages) contains the classes for the
dropwizard-enabled endpoints themselves.

These are specific to a Lucene major release.
