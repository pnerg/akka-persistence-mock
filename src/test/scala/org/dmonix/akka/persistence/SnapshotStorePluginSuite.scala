/**
 *  Copyright 2015 Peter Nerg
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dmonix.akka.persistence

import org.scalatest.FunSuite
import scala.concurrent.ExecutionContext
import akka.persistence.SnapshotMetadata
import akka.testkit.TestKit
import akka.actor.{ ActorSystem, Props }
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike

object SnapshotStorePluginSuiteHelper {
  val config = ConfigFactory.parseString(
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

/**
 * @author Peter Nerg
 */
class SnapshotStorePluginSuite extends TestKit(ActorSystem("SnapshotStorePluginSuite", SnapshotStorePluginSuiteHelper.config)) with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  // Makes sure that the actor system is shut down.
  override def afterAll() { system.terminate() }

  val time = System.currentTimeMillis()
  val account = system.actorOf(AccountActor.props(String.valueOf(time)), "AccountActor-"+time)

  "SnapshotStorePlugin" should {
    "Test to save snapshot" in {
        account ! Snap
    }
  }

}