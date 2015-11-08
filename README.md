[![Build Status](https://travis-ci.org/pnerg/akka-persistence-mock.svg?branch=master)](https://travis-ci.org/pnerg/akka-persistence-mock) [![codecov.io](http://codecov.io/github/pnerg/akka-persistence-mock/coverage.svg?branch=master)](http://codecov.io/github/pnerg/akka-persistence-mock?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dmonix.akka/akka-persistence-mock_2.11/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/org.dmonix.akka/akka-persistence-mock_2.11)
[![Scaladoc](http://javadoc-badge.appspot.com/org.dmonix.akka/akka-persistence-mock_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.dmonix.akka/akka-persistence-mock_2.11)  
  

# Akka Persistence Mock
Mock/in-memory implementation of Akka persistence suitable for testing your persistent Actors.  
This plugin implement both the Journal as well as the SnapshotStore.

## What's this
"_Akka persistence enables stateful actors to persist their internal state so that it can be recovered when an actor is started, restarted after a JVM crash or by a supervisor, or migrated in a cluster_"
/[akka persistence (Typesafe)](http://doc.akka.io/docs/akka/2.4.0/scala/persistence.html)  
Whilst the actual persistence mechanism of the Akka platform is generic it allows you to define which storage plugin to use, i.e. where to store the snapshot and journal/transaction data.  
  
This project implements both the _Journal_ and _SnapshotStore_ plugins as in-memory structures. I.e. no data is written/persisted anywhere. This of course means that this implementation is not suitable for production but excellent for testing. There's no dependencies to anything like disc space or external databases meaning that the plugin requires no configuration to be used. It makes it extremely efficient to instantiate and use in testing adding virtually zero overhead.

### Guaranteed compliance
This plugin is tested against [Typesafes compatibility tests](http://doc.akka.io/docs/akka/2.4.0/scala/persistence.html#Plugin_TCK) to guarantee its behavior is completely according to specifications.

## When to use
When you have persistent actors you need to test for proper behavior.  
To properly test your actors you need a storage plugin for the Akka persistence framework to work properly.  
It's now you probably want a lightweight in-memory implementation of the persistence plugin.

## How to use
Since testing is the primary use case there's a few ways to configure the plugin.  
Basically what you need is to add the plugin configuration to the actor system.
```
akka.persistence.journal.plugin = "dummy-journal"
akka.persistence.snapshot-store.plugin = "dummy-snapshot-store"

dummy-journal {
  class = "org.dmonix.akka.persistence.JournalPlugin"
  plugin-dispatcher = "akka.actor.default-dispatcher"
}

dummy-snapshot-store {
  class = "org.dmonix.akka.persistence.SnapshotStorePlugin"
  plugin-dispatcher = "akka.persistence.dispatchers.default-plugin-dispatcher"
}
```
One can either add the above configuration to the _resources.conf_ file.  
Place the file in _src/main/resources_ or _src/test/resources_ depending on which scope you need.  

But if it's for pure test cases Id' suggest to configure the actor system for the testkit.  
E.g. 

```scala
object PersistenceSuite {
  
  def journalId() = "dummy-journal"
  def snapStoreId() = "dummy-snapshot-store"
  
  def config() = ConfigFactory.parseString(
    """akka.loggers = [akka.testkit.TestEventListener] # makes both log-snooping and logging work
         akka.loglevel = "DEBUG"
         akka.persistence.journal.plugin = "dummy-journal"
         akka.persistence.snapshot-store.plugin = "dummy-snapshot-store"
         dummy-journal {
           class = "org.dmonix.akka.persistence.JournalPlugin"
           plugin-dispatcher = "akka.actor.default-dispatcher"
         }
        dummy-snapshot-store {
          class = "org.dmonix.akka.persistence.SnapshotStorePlugin"
          plugin-dispatcher = "akka.persistence.dispatchers.default-plugin-dispatcher"
         }          
         akka.actor.debug.receive = on""")
}
```
Then use the config for all your test cases.
```scala
class ExampleSuite extends TestKit(ActorSystem("ExampleSuite", PersistenceSuite.config))
```
Check out the [Example suite class](https://github.com/pnerg/akka-persistence-mock/blob/master/src/test/scala/org/dmonix/akka/persistence/ExampleSuite.scala)

## LICENSE

Copyright 2015 Peter Nerg.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Download
The binary can be downloaded from [Maven Central](http://search.maven.org/#artifactdetails|org.dmonix.akka|akka-persistence-mock_2.11|1.1.1|).

sbt
```
libraryDependencies += "org.dmonix.akka" % "akka-persistence-mock_2.11" % "1.1.1"
```
```
libraryDependencies += "org.dmonix.akka" %% "akka-persistence-mock" % "1.1.1"
```

Maven
```xml
<dependency>
    <groupId>org.dmonix.akka</groupId>
    <artifactId>akka-persistence-mock_2.11</artifactId>
    <version>1.1.1</version>
</dependency>
```

