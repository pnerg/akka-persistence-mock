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

import akka.persistence.PersistentActor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.persistence.SnapshotOffer

case class Deposit(sum: Integer)
case class Withdraw(sum: Integer)
case class Balance(sum: Integer = 0)
case object Snap

object Account {
  def apply(balance: Integer) = new Account(balance)
}

class Account(balance: Integer) {

  def deposit(sum: Integer) = {
    Account(balance + sum)
  }

  def withdraw(sum: Integer) = {
    Account(balance - sum)
  }

  def balance(): Integer = balance

  override def toString = String.valueOf(balance)
}

/**
 * @author Peter Nerg
 */
object AccountActor {
  def props(id: String) = Props(new AccountActor(id))
}

/**
 * @author Peter Nerg
 */
class AccountActor(id: String) extends PersistentActor with ActorLogging {
  override def persistenceId = id
  var account = Account(0)

  private def deposit(evt: Deposit) {
    account = account.deposit(evt.sum)
  }

  private def withdraw(evt: Withdraw) {
    account = account.withdraw(evt.sum)
  }

  val receiveRecover: Receive = {
    case evt: Deposit  => deposit(evt)
    case evt: Withdraw => withdraw(evt)
    case SnapshotOffer(_, snapshot: Account) => {
      log.debug("Restoring snapshot [{}] for [{}]", snapshot, persistenceId)
      account = snapshot
    }
    case x: Any => log.debug("Got message [{}]", x)
  }

  val receiveCommand: Receive = {
    case evt: Deposit => persist(evt) { evt =>
        deposit(evt)
        sender ! Balance(account.balance())
      }

    case evt: Withdraw => {
      persist(evt) { evt =>
        withdraw(evt)
        sender ! Balance(account.balance())
      }
    }
    case Balance => sender ! Balance(account.balance())
    case Snap    => saveSnapshot(account)
    case x: Any  => log.debug("Got message [{}]", x)
  }
}