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
import Utils._

/**
 * Represents a stored snapshot.
 * @constructor Creates a new instance of this persisted snapshot.
 * @param sequenceNr the sequence number for when the snapshot was taken
 * @param state is data for the snapshot, can be anything.
 * @param timestamp the time stamp for when the snapshot was taken
 */
private[persistence] case class PersistedSnap(sequenceNr: Long, timestamp: Long, state: Any) extends PersistedState

/**
 * The implementation of the snapshot persistence plugin. <br>
 * All snapshots are kept in memory and will not survive a restart.
 * @author Peter Nerg
 */
class SnapshotStorePlugin extends SnapshotStore {

  private implicit val ec = ExecutionContext.global

  private val storage = new Storage[PersistedSnap]()

  def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    log.debug("Load [{}] [{}]", persistenceId, criteria)
      
    Future {
      //first find if there's a storage for the provided ID
      storage.get(persistenceId).flatMap(stash => {
        val snap = select(stash, criteria).reduceOption((l, r) => if (l.sequenceNr > r.sequenceNr) l else r)
        snap.map(s => SelectedSnapshot(SnapshotMetadata(persistenceId, s.sequenceNr, s.timestamp), s.state))
      })
    }
  }

  def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    log.debug("Save [{}] [{}]", metadata, snapshot)
    Future {
      storage.add(metadata.persistenceId)(metadata.sequenceNr, PersistedSnap(metadata.sequenceNr, metadata.timestamp, snapshot))
    }
  }

  def deleteAsync(metadata: SnapshotMetadata): Future[Unit] = {
    log.debug("Delete [{}]", metadata)
    val maxTs = if (metadata.timestamp == 0) Long.MaxValue else metadata.timestamp
    deleteAsync(metadata.persistenceId, SnapshotSelectionCriteria(metadata.sequenceNr, maxTs, metadata.sequenceNr, metadata.timestamp))
  }

  def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] = {
    log.debug("Delete [{}] [{}]", persistenceId, criteria)
    Future {
      storage.get(persistenceId).foreach(stash => {
        select(stash, criteria).foreach(snap => stash.delete(snap.sequenceNr))
      })
    }
  }
  
  /**
   * Selects a number of snapshots based on the provided critera.
   */
  private def select(stash: Stash[PersistedSnap], criteria: SnapshotSelectionCriteria) = {
    def seqNrInRange(seqNr: Long) = inRange(seqNr, criteria.minSequenceNr, criteria.maxSequenceNr)
    def timeRange(seqNr: Long) = inRange(seqNr, criteria.minTimestamp, criteria.maxTimestamp)
    stash.select(s => seqNrInRange(s.sequenceNr)).filter(s => timeRange(s.timestamp))
  }
}