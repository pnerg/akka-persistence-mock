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

import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.mutable.HashMap
import akka.persistence.{ SelectedSnapshot, SnapshotMetadata, SnapshotSelectionCriteria }
import akka.persistence.snapshot.SnapshotStore

/**
 * Represents a stored snapshot.
 * @constructor Creates a new instance of this persisted snapshot.
 * @param sequenceNr the sequence number for when the snapshot was taken
 * @param state is data for the snapshot, can be anything.
 * @param timestamp the time stamp for when the snapshot was taken
 */
case class PersistedSnap(sequenceNr: Long, timestamp: Long, state: Any)

class SnapshotStash {
  val snapshots = HashMap[Long, PersistedSnap]()

  def add(snap: PersistedSnap) {
    println("Store:"+snap)
    snapshots.put(snap.sequenceNr, snap)
  }

  def select(c: SnapshotSelectionCriteria) = {
    snapshots.values.filter(s => inRange(s.sequenceNr, c.minSequenceNr, c.maxSequenceNr)).filter(s => inRange(s.timestamp, c.minTimestamp, c.maxTimestamp))
  }

  def delete(sequenceNr: Long) = snapshots.remove(sequenceNr) 
  
  private def inRange(value: Long, min: Long, max: Long) = value >= min && value <= max
}

class SnapshotStorage {
  /** stores persistenceId -> Snapshot*/
  val stashes = HashMap[String, SnapshotStash]()

  def add(persistenceId: String, snap: PersistedSnap) {
    stashes.get(persistenceId) match {
      case Some(stash) => stash.add(snap)
      case None => {
        val stash = new SnapshotStash
        stash.add(snap)
        stashes.put(persistenceId, stash)
      }
    }
  }

  def get(persistenceId: String) = stashes.get(persistenceId)
}

/**
 * @author Peter Nerg
 */
class SnapshotStorePlugin extends SnapshotStore {

  implicit val ec = ExecutionContext.global

  val storage = new SnapshotStorage

  def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    println("load:"+persistenceId+":"+criteria)
    Future {
      //first find if there's a storage for the provided ID
      storage.get(persistenceId).flatMap(stash => {
        val snap = stash.select(criteria).reduceLeftOption((l, r) => if (l.timestamp > r.timestamp) l else r)
        
        println("found:"+snap)
        snap.map(s => SelectedSnapshot(SnapshotMetadata(persistenceId, s.sequenceNr, s.timestamp), s.state))
      })
    }
  }

  def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    Future {
      log.debug("Save [{}] [{}]", metadata, snapshot)
      storage.add(metadata.persistenceId, PersistedSnap(metadata.sequenceNr, metadata.timestamp, snapshot))
    }
  }

  def deleteAsync(metadata: SnapshotMetadata): Future[Unit] = {
//    SnapshotSelectionCriteria(metadata.sequenceNr, metadata.timestamp, me)
    Future {
    }
  }

  def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] = {
    Future {
      storage.get(persistenceId).foreach(stash => {
        stash.select(criteria).foreach(snap => stash.delete(snap.sequenceNr))
      })
    }
  }

  private def deleteSnapshot(persistenceId: String): Future[Unit] = {
    Future {
    }
  }
}