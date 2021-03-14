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
package org.opencypher.gremlin;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencypher.gremlin.groups.SkipWithGremlinGroovy;
import org.opencypher.gremlin.rules.GremlinServerExternalResource;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class WithTest {

    @ClassRule
    public static final GremlinServerExternalResource gremlinServer = new GremlinServerExternalResource();

    private List<Map<String, Object>> submitAndGet(String cypher) {
        return gremlinServer.cypherGremlinClient().submit(cypher).all();
    }

    @Test
    public void rename() throws Exception {
        List<Map<String, Object>> results = submitAndGet(
            "MATCH (s:software) WITH s.name AS x RETURN x"
        );

        assertThat(results)
            .extracting("x")
            .containsExactlyInAnyOrder("lop", "ripple");
    }

    @Test
    public void aliasShadow() throws Exception {
        List<Map<String, Object>> results = submitAndGet(
            "MATCH (s:software) WITH s.name AS s RETURN s"
        );

        assertThat(results)
            .extracting("s")
            .containsExactlyInAnyOrder("lop", "ripple");
    }

    @Test
    public void where() throws Exception {
        List<Map<String, Object>> results = submitAndGet(
            "MATCH (p:person) " +
                "WITH p WHERE p.age < 30 " +
                "RETURN p.name"
        );

        assertThat(results)
            .extracting("p.name")
            .containsExactlyInAnyOrder("marko", "vadas");
    }

    /**
     * Maps don't work in Gremlin Groovy translation
     */
    @Test
    @Category(SkipWithGremlinGroovy.class)
    public void withMap() throws Exception {
        assertThat(returnWith("map").toString()).isEqualTo("{name=Mats}");
        assertThat(returnWith("map.name")).isEqualTo("Mats");
        assertThat(returnWith("exists(map.name)")).isEqualTo(true);
        assertThat(returnWith("map.nonExisting")).isEqualTo(null);
        assertThat(returnWith("exists(map.nonExisting)")).isEqualTo(false);
    }

    private Object returnWith(String returnExpression) throws Exception {
        String queryTemplate = "WITH {name: 'Mats'} AS map RETURN %s AS result";
        return getResult(format(queryTemplate, returnExpression));
    }

    /**
     * Maps don't work in Gremlin Groovy translation
     */
    @Test
    @Category(SkipWithGremlinGroovy.class)
    public void withMapWithNullValue() throws Exception {
        String query = "WITH {notName: 0, notName2: null} AS map " +
            "RETURN exists(map.notName2) AS result";
        assertThat(getResult(query)).isEqualTo(false);
    }

    private Object getResult(String query) throws Exception {
        List<Map<String, Object>> results = submitAndGet(query);
        return results.iterator().next().get("result");
    }

    @Test
    public void skipAndLimit() {
        List<Map<String, Object>> results = submitAndGet(
            "UNWIND [1, 2, 3, 4, 5] AS i " +
                "WITH i SKIP 1 LIMIT 3 " +
                "RETURN i"
        );

        assertThat(results)
            .extracting("i")
            .containsExactly(2L, 3L, 4L);
    }

}
