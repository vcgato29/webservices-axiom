/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axiom.datatype;

import java.text.ParseException;

public abstract class InvariantType<T> implements Type<T> {
    public final <S,O> T parse(String literal, ContextAccessor<S,O> contextAccessor, S contextObject, O options) throws ParseException {
        return parse(literal);
    }

    public final <S,O> String format(T value, ContextAccessor<S,O> contextAccessor, S contextObject, O options) {
        return format(value);
    }

    public abstract T parse(String literal) throws ParseException;
    public abstract String format(T value);
}