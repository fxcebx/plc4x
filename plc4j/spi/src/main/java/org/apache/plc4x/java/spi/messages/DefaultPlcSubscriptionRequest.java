/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.plc4x.java.spi.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.types.PlcSubscriptionType;
import org.apache.plc4x.java.spi.connection.PlcFieldHandler;
import org.apache.plc4x.java.spi.model.SubscriptionPlcField;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "className")
public class DefaultPlcSubscriptionRequest implements InternalPlcSubscriptionRequest, InternalPlcFieldRequest {

    private final PlcSubscriber subscriber;

    private LinkedHashMap<String, SubscriptionPlcField> fields;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DefaultPlcSubscriptionRequest(@JsonProperty("subscriber") PlcSubscriber subscriber,
                                         @JsonProperty("fields") LinkedHashMap<String, SubscriptionPlcField> fields) {
        this.subscriber = subscriber;
        this.fields = fields;
    }

    @Override
    @JsonIgnore
    public CompletableFuture<PlcSubscriptionResponse> execute() {
        return subscriber.subscribe(this);
    }

    @Override
    @JsonIgnore
    public int getNumberOfFields() {
        return fields.size();
    }

    @Override
    @JsonIgnore
    public LinkedHashSet<String> getFieldNames() {
        return new LinkedHashSet<>(fields.keySet());
    }

    @Override
    @JsonIgnore
    public PlcField getField(String name) {
        SubscriptionPlcField subscriptionPlcField = fields.get(name);
        if (subscriptionPlcField == null) {
            return null;
        }
        return subscriptionPlcField.getPlcField();
    }

    @Override
    @JsonIgnore
    public List<PlcField> getFields() {
        return fields.values().stream().map(SubscriptionPlcField::getPlcField).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @JsonIgnore
    public List<SubscriptionPlcField> getSubscriptionFields() {
        return new LinkedList<>(fields.values());
    }

    @Override
    public Map<String, SubscriptionPlcField> getSubscriptionPlcFieldMap() {
        return fields;
    }

    @Override
    @JsonIgnore
    public List<Pair<String, PlcField>> getNamedFields() {
        return fields.entrySet()
            .stream()
            .map(stringPlcFieldEntry -> Pair.of(stringPlcFieldEntry.getKey(), stringPlcFieldEntry.getValue().getPlcField()))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @JsonIgnore
    public List<Pair<String, SubscriptionPlcField>> getNamedSubscriptionFields() {
        return fields.entrySet()
            .stream()
            .map(stringPlcFieldEntry -> Pair.of(stringPlcFieldEntry.getKey(), stringPlcFieldEntry.getValue()))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public PlcSubscriber getSubscriber() {
        return subscriber;
    }

    public static class Builder implements PlcSubscriptionRequest.Builder {

        private final PlcSubscriber subscriber;
        private final PlcFieldHandler fieldHandler;
        private final Map<String, BuilderItem> fields;

        public Builder(PlcSubscriber subscriber, PlcFieldHandler fieldHandler) {
            this.subscriber = subscriber;
            this.fieldHandler = fieldHandler;
            fields = new TreeMap<>();
        }

        @Override
        public PlcSubscriptionRequest.Builder addCyclicField(String name, String fieldQuery, Duration pollingInterval) {
            fields.put(name, new BuilderItem(() -> fieldHandler.createField(fieldQuery), PlcSubscriptionType.CYCLIC, pollingInterval));
            return this;
        }

        @Override
        public PlcSubscriptionRequest.Builder addCyclicField(String name, PlcField fieldQuery, Duration pollingInterval) {
            fields.put(name, new BuilderItem(() -> fieldQuery, PlcSubscriptionType.CYCLIC, pollingInterval));
            return this;
        }

        @Override
        public PlcSubscriptionRequest.Builder addChangeOfStateField(String name, String fieldQuery) {
            fields.put(name, new BuilderItem(() -> fieldHandler.createField(fieldQuery), PlcSubscriptionType.CHANGE_OF_STATE));
            return this;
        }

        @Override
        public PlcSubscriptionRequest.Builder addChangeOfStateField(String name, PlcField fieldQuery) {
            fields.put(name, new BuilderItem(() -> fieldQuery, PlcSubscriptionType.CHANGE_OF_STATE));
            return this;
        }

        @Override
        public PlcSubscriptionRequest.Builder addEventField(String name, String fieldQuery) {
            if (fields.containsKey(name)) {
                throw new PlcRuntimeException("Duplicate field definition '" + name + "'");
            }
            fields.put(name, new BuilderItem(() -> fieldHandler.createField(fieldQuery), PlcSubscriptionType.EVENT));
            return this;
        }

        @Override
        public PlcSubscriptionRequest.Builder addEventField(String name, PlcField fieldQuery) {
            if (fields.containsKey(name)) {
                throw new PlcRuntimeException("Duplicate field definition '" + name + "'");
            }
            fields.put(name, new BuilderItem(() -> fieldQuery, PlcSubscriptionType.EVENT));
            return this;
        }

        @Override
        public PlcSubscriptionRequest build() {
            LinkedHashMap<String, SubscriptionPlcField> parsedFields = new LinkedHashMap<>();

            fields.forEach((name, builderItem) -> {
                PlcField parsedField = builderItem.fieldQuery.get();
                parsedFields.put(name, new SubscriptionPlcField(builderItem.plcSubscriptionType, parsedField, builderItem.duration));
            });
            return new DefaultPlcSubscriptionRequest(subscriber, parsedFields);
        }

        private static class BuilderItem {
            private final Supplier<PlcField> fieldQuery;
            private final PlcSubscriptionType plcSubscriptionType;
            private final Duration duration;

            private BuilderItem(Supplier<PlcField> fieldQuery, PlcSubscriptionType plcSubscriptionType) {
                this(fieldQuery, plcSubscriptionType, null);
            }

            private BuilderItem(Supplier<PlcField> fieldQuery, PlcSubscriptionType plcSubscriptionType, Duration duration) {
                this.fieldQuery = fieldQuery;
                this.plcSubscriptionType = plcSubscriptionType;
                this.duration = duration;
            }

        }

    }

}
