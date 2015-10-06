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

import scala.collection.immutable.Seq
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import akka.persistence.{ AtomicWrite, PersistentRepr }
import akka.persistence.journal.{ AsyncRecovery, AsyncWriteJournal }
import scala.collection.mutable.HashMap
import akka.actor.ActorLogging
import scala.collection.mutable.MutableList
import scala.util.Success
import scala.util.Failure
import java.io.NotSerializableException

/**
 * Represents a persisted journal/transaction
 * @constructor Creates a new instance of this persisted journal.
 * @param sequenceNr The sequence number for this transaction/journal
 * @param manifest The manifest for this transaction/journal
 * @param writerUuid The writer UUID for this transaction/journal
 * @param msg The actual message for this transaction/journal
 */
private[persistence] case class PersistedJournal(sequenceNr: Long, manifest: String, writerUuid: String, msg: Any) extends PersistedState

/**
 * The implementation of the journal/transaction persistence plugin. <br>
 * All journals are kept in memory and will not survive a restart.
 * @author Peter Nerg
 */
class JournalPlugin extends AsyncWriteJournal with AsyncRecovery with ActorLogging {

  private implicit val ec = ExecutionContext.global
  private val storage = new Storage[PersistedJournal]

  def asyncWriteMessages(messages: Seq[AtomicWrite]): Future[Seq[Try[Unit]]] = {
    Future {
      var response = List[Try[Unit]]()
      messages.foreach(_.payload.foreach { p =>
        log.debug("Persist event [{}]", p)
        response = response :+ Try {
          persist(p)
        }
      })
      response
    }
  }

  def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] = {
    Future {
      storage.get(persistenceId).foreach(stash => {
        stash.ids.filter(_ <= toSequenceNr).foreach(stash.delete(_))
      })
    }
  }

  def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long,
                          max: Long)(recoveryCallback: PersistentRepr ⇒ Unit): Future[Unit] = {

    val maxInt = if (max.intValue < 0) Integer.MAX_VALUE else max.intValue
    log.debug("Replay [" + persistenceId + "] from [" + fromSequenceNr + "] to [" + toSequenceNr + "] using max [" + maxInt + "]")

    // Replays the provided journal
    def replay(journal: PersistedJournal) {
      log.debug("Replay [{}] [{}]", persistenceId, journal)
      recoveryCallback(PersistentRepr(journal.msg, journal.sequenceNr, persistenceId, journal.manifest, false, null, journal.writerUuid))
    }

    Future {
      def inRange(journal: PersistedJournal) = Utils.inRange(journal.sequenceNr, fromSequenceNr, toSequenceNr)
      def sort(l: PersistedJournal, r: PersistedJournal) = l.sequenceNr < r.sequenceNr
      storage.get(persistenceId).foreach(stash => {
        stash.select(inRange(_)).toIndexedSeq.sortWith(sort).take(maxInt).foreach(j => replay(j))
      })
    }
  }

  def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    log.debug("Read highest seqNr for id [{}] starting from [{}]", persistenceId, fromSequenceNr)
    Future {
      storage.get(persistenceId).map(stash => {
        stash.ids.filter(_ >= fromSequenceNr).foldLeft(0L)((l, r) => { if (l > r) l else r })
      }).getOrElse(0)
    }
  }

  /**
   * Attempts to persist the provided data.
   */
  private def persist(p: PersistentRepr): Unit = {
    if (p.payload.isInstanceOf[Serializable] || p.payload.isInstanceOf[java.io.Serializable]) {
      storage.add(p.persistenceId, p.sequenceNr, PersistedJournal(p.sequenceNr, p.manifest, p.writerUuid, p.payload))
    } else {
      throw new NotSerializableException
    }
  }

}