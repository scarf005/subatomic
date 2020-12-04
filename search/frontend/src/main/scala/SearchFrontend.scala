/*
 * Copyright 2020 Anton Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package subatomic
package search

import scala.scalajs.js

import com.raquo.laminar.api.L._
import upickle.default._

import SearchIndex._

class SearchFrontend private (idx: SearchIndex[String]) {
  val search           = new Search(idx)
  def query(s: String) = search.string(s)

  val node = {
    val ip = input()
    val stream = ip
      .events(onInput)
      .mapTo(ip.ref.value.trim())
      .startWith("")

    val results = stream.map(query)

    div(
      ip,
      ul(
        child.text <-- results.map(_.toString)
      )
    )
  }
}

object SearchFrontend extends LaminarApp("searchContainer") {
  def load(s: String) = new SearchFrontend(read[SearchIndex[String]](s))

  def app = {
    val frontend = load(js.Dynamic.global.SearchIndexText.asInstanceOf[String])

    div(
      frontend.node
    )
  }
}
