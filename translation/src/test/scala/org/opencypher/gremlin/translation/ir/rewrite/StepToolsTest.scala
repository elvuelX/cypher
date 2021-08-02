/*
 * Copyright (c) 2018 "Neo4j, Inc." [https://neo4j.com]
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
package org.opencypher.gremlin.translation.ir.rewrite

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.opencypher.gremlin.translation.ir._

class StepToolsTest {

  @Test
  def findOne(): Unit = {
    val seq = Vertex :: As("n") :: OutE("rel") :: As("r") :: InV :: As("m") :: Nil
    val relWithLabel: String => PartialFunction[Seq[GremlinStep], String] = (stepLabel) => {
      case OutE(edgeLabel) :: As(`stepLabel`) :: InV :: _ => edgeLabel
    }
    val found = StepTools.find(seq, relWithLabel("r"))
    val notFound = StepTools.find(seq, relWithLabel("other"))

    assertThat(found).isEqualTo(Seq("rel"))
    assertThat(notFound).isEqualTo(Nil)
  }

  @Test
  def findMultiple(): Unit = {
    val seq = Vertex :: As("n") :: OutE("rel") :: As("r") :: InV :: As("m") :: Nil
    val found = StepTools.find(seq, {
      case As(stepLabel) :: _ => stepLabel
    })

    assertThat(found).isEqualTo(Seq("n", "r", "m"))
  }

  @Test
  def replaceOne(): Unit = {
    val seq = Vertex :: As("n") :: OutE("rel") :: As("r") :: InV :: As("m") :: Nil
    val replaced = StepTools.replace(seq, {
      case OutE(edgeLabel) :: As(_) :: InV :: rest => OutE(edgeLabel) :: InV :: rest
    })

    assertThat(replaced).isEqualTo(
      Vertex :: As("n") :: OutE("rel") :: InV :: As("m") :: Nil
    )
  }

  @Test
  def replaceMultiple(): Unit = {
    val seq = Vertex :: As("n") :: OutE("rel") :: As("r") :: InV :: As("m") :: Nil
    val replaced = StepTools.replace(seq, {
      case As(stepLabel) :: rest => As(s"_$stepLabel") :: rest
    })

    assertThat(replaced).isEqualTo(
      Vertex :: As("_n") :: OutE("rel") :: As("_r") :: InV :: As("_m") :: Nil
    )
  }

}
