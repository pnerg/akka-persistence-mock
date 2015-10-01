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

/**
 * @author Peter Nerg
 */
class SnapshotStorePlugin extends SnapshotStore {

  implicit val ec = ExecutionContext.global

  /** stores persistenceId -> Snapshot*/
  val snapshots = HashMap[String, PersistedSnap]()

  def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    ???
  }

  def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    Future {
      log.debug("Save [{}] [{}]", metadata, snapshot)
      //simply overwrites any snapshot for the persistenceId/company
      snapshots.put(metadata.persistenceId, PersistedSnap(metadata.sequenceNr, metadata.timestamp, snapshot))
    }
  }

  def deleteAsync(metadata: SnapshotMetadata): Future[Unit] = {
    ???
  }

  def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] = {
    ???
  }

  private def deleteSnapshot(persistenceId: String): Future[Unit] = {
    ???
  }
}