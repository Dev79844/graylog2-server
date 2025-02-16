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
package org.graylog.plugins.views.storage.migration.state;

import org.assertj.core.api.Assertions;
import org.graylog.plugins.views.storage.migration.state.actions.MigrationActions;
import org.graylog.plugins.views.storage.migration.state.machine.MigrationActionsAdapter;
import org.graylog.plugins.views.storage.migration.state.machine.MigrationState;
import org.graylog.plugins.views.storage.migration.state.machine.MigrationStateMachine;
import org.graylog.plugins.views.storage.migration.state.machine.MigrationStateMachineProvider;
import org.graylog.plugins.views.storage.migration.state.machine.MigrationStep;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


class MigrationStateMachineTest {

    @Test
    void testPersistence() {
        final InMemoryStateMachinePersistence persistence = new InMemoryStateMachinePersistence();


        final AtomicReference<Map<String, Object>> providedArgs = new AtomicReference<>();

        final MigrationActionsAdapter migrationActions = new MigrationActionsAdapter() {
            @Override
            public void rollingUpgradeSelected() {
                providedArgs.set(args());
            }
        };

        final MigrationStateMachine migrationStateMachine = new MigrationStateMachineProvider(persistence, migrationActions).get();
        migrationStateMachine.trigger(MigrationStep.SELECT_ROLLING_UPGRADE_MIGRATION, Collections.singletonMap("foo", "bar"));

        Assertions.assertThat(persistence.getConfiguration())
                .isPresent()
                .hasValueSatisfying(configuration -> {
                    Assertions.assertThat(configuration.currentState()).isEqualTo(MigrationState.ROLLING_UPGRADE_MIGRATION_WELCOME);
                });

        Assertions.assertThat(providedArgs.get())
                .isNotNull()
                .containsEntry("foo", "bar");
    }
}
