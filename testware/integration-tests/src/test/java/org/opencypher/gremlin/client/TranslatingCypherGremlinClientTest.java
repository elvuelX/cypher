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
package org.opencypher.gremlin.client;

import org.junit.ClassRule;
import org.junit.Test;
import org.opencypher.gremlin.rules.GremlinServerExternalResource;
import org.opencypher.gremlin.translation.Flavor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TranslatingCypherGremlinClientTest {

    @ClassRule
    public static final GremlinServerExternalResource gremlinServer = new GremlinServerExternalResource();

    @Test
    public void submitToDefaultGraph() {
        TranslatingCypherGremlinClient client =
            new TranslatingCypherGremlinClient(gremlinServer.gremlinClient(), Flavor.GREMLIN);
        String cypher = "MATCH (p:person) RETURN p.name AS name";
        List<Map<String, Object>> results = client.submit(cypher);

        assertThat(results)
            .extracting("name")
            .containsExactlyInAnyOrder("marko", "vadas", "josh", "peter");
    }
}