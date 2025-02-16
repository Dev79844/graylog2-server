/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog2.bindings.providers;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.graylog2.indexer.retention.strategies.UnknownRetentionStrategyConfig;
import org.graylog2.plugin.indexer.retention.RetentionStrategyConfig;
import org.mongojack.internal.MongoJackModule;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class MongoJackObjectMapperProvider implements Provider<ObjectMapper> {
    private final ObjectMapper objectMapper;

    @Inject
    public MongoJackObjectMapperProvider(ObjectMapper objectMapper) {
        this.objectMapper = CommonMongoJackObjectMapperProvider.configure(objectMapper)
                .addHandler(new ReplaceUnknownSubtypesWithFallbackHandler())
                .setPropertyNamingStrategy(new PreserveLeadingUnderscoreStrategy());

        MongoJackModule.configure(this.objectMapper);
    }

    @Override
    public ObjectMapper get() {
        return objectMapper;
    }

    /**
     * This abomination is necessary because when using MongoJack to read "_id" object ids back from the database
     * the property name isn't correctly mapped to the POJO using the LowerCaseWithUnderscoresStrategy.
     * <p>
     * Apparently no one in the world tried to use a different naming strategy with MongoJack.
     * (one of my many useless talents is finding corner cases).
     * </p>
     */
    public static class PreserveLeadingUnderscoreStrategy extends PropertyNamingStrategies.SnakeCaseStrategy {
        @Override
        public String translate(String input) {
            String translated = super.translate(input);
            if (input.startsWith("_") && !translated.startsWith("_")) {
                translated = "_" + translated; // lol underscore
            }
            return translated;
        }
    }

    // TODO this should be pluggable to allow subsystems to specify their own fallback types instead of hardcoding it there.
    private static class ReplaceUnknownSubtypesWithFallbackHandler extends DeserializationProblemHandler {
        @Override
        public JavaType handleUnknownTypeId(DeserializationContext ctxt, JavaType baseType, String subTypeId, TypeIdResolver idResolver, String failureMsg) throws IOException {
            if (baseType.getRawClass().equals(RetentionStrategyConfig.class)) {
                return SimpleType.constructUnsafe(UnknownRetentionStrategyConfig.class);
            }
            return super.handleUnknownTypeId(ctxt, baseType, subTypeId, idResolver, failureMsg);
        }
    }
}
