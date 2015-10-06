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

/**
 * Companion object to Account.
 */
object Account {
  def apply(balance: Integer) = new Account(balance)
}

/**
 * Represents an account.
 * @constructor Creates an account instance
 * @param balance The initial balance of the account.
 */
class Account(balance: Integer) {

  /**
   * Deposits a sum to the account.
   * @param The sum to deposit
   * @return A new account instance holding the new balance
   */
  def deposit(sum: Integer) = {
    Account(balance + sum)
  }

  /**
   * Withdraws a sum from the account.
   * @param The sum to withdraw
   * @return A new account instance holding the new balance
   */
  def withdraw(sum: Integer) = {
    Account(balance - sum)
  }

  /**
   * Requests the balance from the account.
   * @return The current balance
   */
  def balance(): Integer = balance

  override def toString = String.valueOf(balance)
}

/**
 * Compantion object to AccountActor
 * @author Peter Nerg
 */
object AccountActor {
  def props(name: String) = Props(new AccountActor(name))
}

/**
 * Exemplifies a persistent actor by mimicking a bank account. <br>
 * Each actor instance represents an account for a person.
 * @author Peter Nerg
 * @constructor Creates the account instance
 * @param The name of the person holding the account
 */
class AccountActor(name: String) extends PersistentActor with ActorLogging {
  override def persistenceId = name
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