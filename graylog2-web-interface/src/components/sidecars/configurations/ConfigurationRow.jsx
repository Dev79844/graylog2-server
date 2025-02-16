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
import PropTypes from 'prop-types';
import React from 'react';

import { LinkContainer } from 'components/common/router';
import { Button, ButtonToolbar, DropdownButton, MenuItem } from 'components/bootstrap';
import Routes from 'routing/Routes';
import CollectorIndicator from 'components/sidecars/common/CollectorIndicator';
import ColorLabel from 'components/sidecars/common/ColorLabel';
import withTelemetry from 'logic/telemetry/withTelemetry';
import { TELEMETRY_EVENT_TYPE } from 'logic/telemetry/Constants';

import CopyConfigurationModal from './CopyConfigurationModal';
import styles from './ConfigurationRow.css';

class ConfigurationRow extends React.Component {
  static propTypes = {
    configuration: PropTypes.object.isRequired,
    collector: PropTypes.object,
    onCopy: PropTypes.func.isRequired,
    validateConfiguration: PropTypes.func.isRequired,
    onDelete: PropTypes.func.isRequired,
    sendTelemetry: PropTypes.func,
  };

  static defaultProps = {
    collector: {},
    sendTelemetry: () => {},
  };

  _handleDelete = async () => {
    const { configuration, onDelete, sendTelemetry } = this.props;

    sendTelemetry(TELEMETRY_EVENT_TYPE.SIDECARS.CONFIGURATION_DELETED, {
      app_pathname: 'sidecars',
      app_section: 'configuration',
    });

    // eslint-disable-next-line no-alert
    if (window.confirm(`You are about to delete configuration "${configuration.name}". Are you sure?`)) {
      await onDelete(configuration);
    }
  };

  render() {
    const { collector, configuration, validateConfiguration, onCopy } = this.props;

    return (
      <tr>
        <td className={styles.name}>{configuration.name}</td>
        <td className={styles.color}><ColorLabel color={configuration.color} size="small" /></td>
        <td>
          <CollectorIndicator collector={collector.name || 'Unknown collector'}
                              operatingSystem={collector.node_operating_system} />
        </td>
        <td className={styles.actions}>
          <ButtonToolbar>
            <LinkContainer to={Routes.SYSTEM.SIDECARS.EDIT_CONFIGURATION(configuration.id)}>
              <Button onClick={this.openModal} bsStyle="info" bsSize="xsmall">Edit</Button>
            </LinkContainer>
            <DropdownButton id={`more-actions-${configuration.id}`}
                            title="More actions"
                            bsSize="xsmall"
                            pullRight>
              <CopyConfigurationModal configuration={configuration}
                                      validateConfiguration={validateConfiguration}
                                      copyConfiguration={onCopy} />
              <MenuItem divider />
              <MenuItem onSelect={this._handleDelete}>Delete</MenuItem>
            </DropdownButton>
          </ButtonToolbar>
        </td>
      </tr>
    );
  }
}

export default withTelemetry(ConfigurationRow);
