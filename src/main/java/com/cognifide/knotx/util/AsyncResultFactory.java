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
package com.cognifide.knotx.util;

import io.vertx.core.AsyncResult;

public class AsyncResultFactory {

    public static <T> AsyncResult<T> createSuccess(T result) {
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return result;
            }

            @Override
            public Throwable cause() {
                return null;
            }

            @Override
            public boolean succeeded() {
                return true;
            }

            @Override
            public boolean failed() {
                return false;
            }
        };
    }

    public static <T> AsyncResult<T> createFailure(Throwable t) {
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return null;
            }

            @Override
            public Throwable cause() {
                return t;
            }

            @Override
            public boolean succeeded() {
                return false;
            }

            @Override
            public boolean failed() {
                return true;
            }
        };
    }

    public static <T> AsyncResult<T> createFailure() {
        return createFailure(null);
    }

}
