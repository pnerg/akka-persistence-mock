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
import Utils.inRange
case class MockPersistedState(id: Long, msg: String) extends PersistedState

/**
 * @author Peter Nerg
 */
class StorageSuite extends FunSuite {
  
  val storage = new Storage[MockPersistedState]
  
  test("inRange - is in range") {
    assert(inRange(7, 5, 10))
  }

  test("inRange - is in range, upper border") {
    assert(inRange(10, 5, 10))
  }

    test("inRange - is in range, lower border") {
    assert(inRange(5, 5, 10))
  }

  test("inRange - below range") {
    assertResult(false)(inRange(0, 5, 10))
  }

  test("inRange - beyond range") {
    assertResult(false)(inRange(69, 5, 10))
  }
  
  test("Test add with one") {
    storage.add("ID1", 1, MockPersistedState(1, "Hello"))
    assert(storage.get("ID1").isDefined)
  }

  test("Get none existing") {
    assertResult(false)(storage.get("NO-SUCH").isDefined)
  }

}