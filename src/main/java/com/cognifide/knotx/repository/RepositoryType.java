/*
 * Knot.x - reactive microservice assembler
 *
 * Copyright (C) 2016 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.knotx.repository;

import com.cognifide.knotx.Server;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.stream.Stream;

enum RepositoryType implements RepositoryBuilder, RepositoryMetadataValidator {

    LOCAL {
        @Override
        public Repository<String, URI> create(RepositoryConfiguration.RepositoryMetadata metadata,
                                              Server server) {
            return LocalRepository.of(metadata.getPath(), metadata.getCatalogue(), server);
        }

        @Override
        public boolean validate(RepositoryConfiguration.RepositoryMetadata metadata) {
            return isNotEmpty(metadata.getPath());
        }
    },

    REMOTE {
        @Override
        public Repository<String, URI> create(RepositoryConfiguration.RepositoryMetadata metadata,
                                              Server server) {
            return RemoteRepository.of(metadata.getPath(), metadata.getDomain(), metadata.getPort(), server
                    .getVertx().createHttpClient());
        }

        @Override
        public boolean validate(RepositoryConfiguration.RepositoryMetadata metadata) {
            return isNotEmpty(metadata.getPath(), metadata.getDomain()) && metadata.getPort() != null;
        }
    };

    private static boolean isNotEmpty(String... values) {
        return !Stream.of(values).anyMatch(StringUtils::isBlank);
    }


    public abstract Repository<String, URI> create(RepositoryConfiguration.RepositoryMetadata metadata,
                                                   Server server);

    @Override
    public abstract boolean validate(RepositoryConfiguration.RepositoryMetadata metadata);
}
