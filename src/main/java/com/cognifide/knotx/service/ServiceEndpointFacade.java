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
package com.cognifide.knotx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ServiceEndpointFacade {

    @Autowired
    private ServiceConfiguration serviceConfiguration;

    public Optional<? extends ServiceEndpoint> getServiceEndpoint(String path) {
        return serviceConfiguration.getServices().stream()
                .map(ServiceEndpointProvider::from)
                .filter(service -> service.support(path))
                .findFirst();
    }

}