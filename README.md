[![Build Status](https://travis-ci.org/pnerg/akka-persistence-mock.svg?branch=master)](https://travis-ci.org/pnerg/akka-persistence-mock) [![codecov.io](http://codecov.io/github/pnerg/akka-persistence-mock/coverage.svg?branch=master)](http://codecov.io/github/pnerg/akka-persistence-mock?branch=master)   

# Akka Persistence Mock
Mock implementation of Akka persistence suitable for testing.

## What's this
"_Akka persistence enables stateful actors to persist their internal state so that it can be recovered when an actor is started, restarted after a JVM crash or by a supervisor, or migrated in a cluster_"
/[akka persistence (Typesafe)](http://doc.akka.io/docs/akka/2.4.0-RC3/scala/persistence.html)  
Whilst the actual persistence mechanism of the Akka platfrom is generic it allows you to define which storage plugin to use, i.e. where to store the snapshot and journal/transaction data.  
  
This project implements both the _Journal_ and _SnapshotStore_ plugins as in-memory structures. I.e. no data is written/persisted anywhere. This of course means that this implementation is not suitable for production but excellent for testing. There's no dependencies to anything like disc space or external databases meaning that the plugin requires no configuration to be used. It makes it extremely efficient to instantiate and use in testing adding virtually zero overhead.

### Guaranteed compliance
This plugin is tested against Typesafes compatibility tests to guarantee its behavior is completely according to specifications.

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

## Download
The binary can be downloaded from [Maven Central](http://search.maven.org/#artifactdetails|org.dmonix.akka|akka-persistence-mock_2.11|1.0|).

sbt
```
libraryDependencies += "org.dmonix.akka" % "akka-persistence-mock_2.11" % "1.0"
```
```
libraryDependencies += "org.dmonix.akka" %% "akka-persistence-mock" % "1.0"
```

Maven
```xml
<dependency>
    <groupId>org.dmonix.akka</groupId>
    <artifactId>akka-persistence-mock_2.11</artifactId>
    <version>1.0</version>
</dependency>
```

