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
package org.opencypher.gremlin.translation.walker

import org.neo4j.cypher.internal.frontend.v3_2.ast._
import org.opencypher.gremlin.translation.walker.NodeUtils.expressionValue
import org.opencypher.gremlin.translation.{Tokens, TranslationBuilder}

import scala.collection.immutable.NumericRange

/**
  * AST walker that handles translation
  * of the `UNWIND` clause nodes in the Cypher AST.
  */
object UnwindWalker {

  def walkClause[T, P](context: StatementContext[T, P], g: TranslationBuilder[T, P], node: Unwind) {
    new UnwindWalker(context, g).walkClause(node)
  }
}

private class UnwindWalker[T, P](context: StatementContext[T, P], g: TranslationBuilder[T, P]) {

  private val injectHardLimit = 10000

  def walkClause(node: Unwind) {
    if (context.isFirstStatement) {
      context.markFirstStatement()
    } else {
      val p = context.dsl.predicateFactory()
      g.is(p.neq(Tokens.START))
    }

    val Unwind(expression, Variable(varName)) = node
    expression match {
      case ListLiteral(list) =>
        val values = list
          .map(expressionValue(_, context))
          .asInstanceOf[Seq[Object]]
        g.inject(values: _*).as(varName)
      case FunctionInvocation(_, FunctionName(fnName), _, args) if "range" == fnName.toLowerCase =>
        val range: NumericRange[Long] = args match {
          case Seq(start: IntegerLiteral, end: IntegerLiteral) =>
            NumericRange.inclusive(start.value, end.value, 1)
          case Seq(start: IntegerLiteral, end: IntegerLiteral, step: IntegerLiteral) =>
            NumericRange.inclusive(start.value, end.value, step.value)
        }
        context.precondition(
          range.length < injectHardLimit,
          s"Range is too big (must be less than $injectHardLimit)",
          node
        )
        g.inject(range.asInstanceOf[Seq[Object]]: _*).as(varName)
    }
  }
}
