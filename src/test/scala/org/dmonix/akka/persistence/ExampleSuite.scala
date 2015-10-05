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

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.Matchers
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import scala.concurrent.duration._
import akka.testkit.TestProbe
import akka.actor.PoisonPill

/**
 * @author Peter Nerg
 */
class ExampleSuite extends TestKit(ActorSystem("ExampleSuite", PersistenceSuiteTrait.config))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  // Makes sure that the actor system is shut down.
  override def afterAll() { system.terminate }

  def createAccount(name: String) = system.actorOf(AccountActor.props(name), name)
  
  "ExampleSuite" should {
    "Create and use account" in new ExampleSuite {
      val probe = TestProbe()

      val account = createAccount("Peter")

      //simulate a few transaction
      account ! Deposit(100)
      expectMsg(Balance(100))
      account ! Deposit(50)
      expectMsg(Balance(150))

      //make a snapshot with the above transactions
      account ! Snap
      
      //do some more transactions
      account ! Deposit(200)
      expectMsg(Balance(350))
      account ! Withdraw(100)
      expectMsg(Balance(250))

      //kill the actor/account
      probe watch account
      account ! PoisonPill
      probe.expectTerminated(account)
      
      //resurrect the actor/account and verify its balance
      val resurrected = createAccount("Peter")
      resurrected ! Balance
      expectMsg(Balance(250))
    }
  }

}