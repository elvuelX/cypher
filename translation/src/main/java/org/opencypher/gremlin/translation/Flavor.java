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
package org.opencypher.gremlin.translation;

import org.opencypher.gremlin.translation.string.StringPredicate;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;

public enum Flavor {
    COSMOSDB(TranslatorFactory::cosmos),
    GREMLIN(TranslatorFactory::string);

    private Supplier<Translator<String, StringPredicate>> translator;

    Flavor(Supplier<Translator<String, StringPredicate>> translator) {
        this.translator = translator;
    }

    public static Flavor getFlavor(String flavor) {
        if (flavor == null) {
            return GREMLIN;
        }
        return Stream.of(values())
            .filter(type -> type.name().equalsIgnoreCase(flavor))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                format("Translation flavor `%s` is not supported. Supported values: `%s` (case insensitive)",
                    flavor,
                    Arrays.toString(values()))
            ));
    }

    public Translator<String, StringPredicate> getTranslator() {
        return translator.get();
    }
}
