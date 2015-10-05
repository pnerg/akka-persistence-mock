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

import scala.collection.mutable.HashMap

object Utils {
  def inRange(value: Long, min: Long, max: Long) = value >= min && value <= max
}

trait PersistedState

class Stash[S <: PersistedState] {
  private val stateStore = HashMap[Long, S]()

  def add(id: Long, state: S): Unit = stateStore.put(id, state)

  def delete(id: Long): Unit = stateStore.remove(id)
  
  def select(filter: S => Boolean) = stateStore.values.filter(filter)
  
  def ids() = stateStore.keys
}

/**
 * @author Peter Nerg
 */
/**
 * Storage for all snapshot stashes
 */
private[persistence] class Storage[T <: PersistedState] {
  /** stores persistenceId -> Snapshot*/
  val stashes = HashMap[String, Stash[T]]()

  def add(persistenceId: String, id: Long, snap: T) {
    stashes.get(persistenceId) match {
      case Some(stash) => stash.add(id, snap)
      case None => {
        val stash = new Stash[T]
        stash.add(id, snap)
        stashes.put(persistenceId, stash)
      }
    }
  }

  def get(persistenceId: String) = stashes.get(persistenceId)
}
