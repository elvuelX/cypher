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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.opencypher.gremlin.client.CypherGremlinClient;

public class EmbeddedTinkerGraphTest {

    private TinkerGraph graph;

    @Before
    public void setUp() {
        graph = TinkerGraph.open();
    }

    @Test
    public void createAndMatch() {
        CypherGremlinClient client = CypherGremlinClient.inMemory(graph.traversal());
        client.submit("CREATE (:L {foo: 'bar'})");
        List<Map<String, Object>> results = client.submit("MATCH (n:L) return n.foo").all();

        assertThat(results)
            .extracting("n.foo")
            .containsExactly("bar");
    }
}
