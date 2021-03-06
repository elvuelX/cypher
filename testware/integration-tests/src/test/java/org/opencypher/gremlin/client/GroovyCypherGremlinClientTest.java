/*
 * Copyright (c) 2018-2019 "Neo4j, Inc." [https://neo4j.com]
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
package org.opencypher.gremlin.client;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opencypher.gremlin.rules.GremlinServerExternalResource;
import org.opencypher.gremlin.test.TestCommons;
import org.opencypher.gremlin.translation.translator.Translator;

@SuppressWarnings("Duplicates")
public class GroovyCypherGremlinClientTest {

    @ClassRule
    public static final GremlinServerExternalResource gremlinServer = new GremlinServerExternalResource(TestCommons::modernGraph);

    private GroovyCypherGremlinClient client;

    @Before
    public void setUp() {
        client = new GroovyCypherGremlinClient(
            gremlinServer.gremlinClient(),
            () -> Translator.builder()
                .gremlinGroovy()
                .build()
        );
    }

    @Test
    public void submit() {
        String cypher = "MATCH (p:person) RETURN p.name AS name";
        List<Map<String, Object>> results = client.submit(cypher).all();

        assertThat(results)
            .extracting("name")
            .containsExactlyInAnyOrder("marko", "vadas", "josh", "peter");
    }

    @Test
    public void submitExtractedParameters() {
        String cypher = "MATCH (p:person) WHERE 27 <= p.age < 32 RETURN p.name AS name";
        List<Map<String, Object>> results = client.submit(cypher).all();

        assertThat(results)
            .extracting("name")
            .containsExactlyInAnyOrder("marko", "vadas");
    }

    @Test
    public void submitExplicitParameters() {
        String cypher = "MATCH (p:person) WHERE $low <= p.age < 32 RETURN p.name AS name";
        Map<String, ?> parameters = singletonMap("low", 29);
        List<Map<String, Object>> results = client.submit(cypher, parameters).all();

        assertThat(results)
            .extracting("name")
            .containsExactlyInAnyOrder("marko");
    }

    @Test
    public void invalidSyntax() {
        CypherResultSet resultSet = client.submit("INVALID");
        Throwable throwable = catchThrowable(resultSet::all);

        assertThat(throwable)
            .hasMessageContaining("Invalid input");
    }

    @Test
    public void invalidParameter() {
        String cypher = "RETURN $`🐼`";
        Map<String, ?> parameters = singletonMap("🐼", 0);
        CypherResultSet resultSet = client.submit(cypher, parameters);
        Throwable throwable = catchThrowable(resultSet::all);

        assertThat(throwable)
            .hasMessageContaining("Invalid parameter name: 🐼");
    }

    @Test
    public void groovyKeywordParameter() {
        String cypher = "RETURN $goto";
        Map<String, ?> parameters = singletonMap("goto", 0);
        CypherResultSet resultSet = client.submit(cypher, parameters);
        Throwable throwable = catchThrowable(resultSet::all);

        assertThat(throwable)
            .hasMessageContaining("Invalid parameter name: goto");
    }

    @Test
    public void multiple() {
        List<Map<String, Object>> result1 = client.submit("RETURN 1").all();
        List<Map<String, Object>> result2 = client.submit("RETURN 1").all();
        List<Map<String, Object>> result3 = client.submit("RETURN 1").all();

        assertThat(result1)
            .isEqualTo(result2)
            .isEqualTo(result3)
            .extracting("1")
            .containsExactly(1L);
    }
}
