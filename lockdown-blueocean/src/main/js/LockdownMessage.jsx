import React, { Component, PropTypes } from 'react';
import { sseConnection, AppConfig, Fetch } from '@jenkins-cd/blueocean-core-js';

export default class LockdownMessage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            lockdownMessage: '',
        };
    }

    componentWillMount() {
        this.updateLockdownMessage();

        this.lockdownMessageEventListener = sseConnection.subscribe('lockdown', (event) => {
            if (event.jenkins_event === 'lockdown_message_changed') {
                this.updateLockdownMessage();
            }
        });
    }

    componentWillUnmount() {
        if (this.lockdownMessageEventListener) {
            sseConnection.unsubscribe(this.lockdownMessageEventListener);
        }
    }

    updateLockdownMessage() {
        this.fetchLockdownMessage().then(response => {
            this.setState({
                lockdownMessage: response.data.message,
            });
        });
    }

    lockdownMessageUrl() {
        const jenkinsUrl = AppConfig.getJenkinsRootURL();
        return `${jenkinsUrl}/lockdown/lockdownMessage`;
    }

    fetchLockdownMessage() {
        const fetchOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return Fetch.fetchJSON(this.lockdownMessageUrl(), { fetchOptions });
    }

    render() {
        /* eslint-disable react/jsx-closing-bracket-location */
        /* eslint-disable react/jsx-indent-props */

        // The lockdown message is formatted the same way as Jenkins'
        // system message, so it is already either completely escaped
        // or safe HTML.
        return this.state.lockdownMessage.length === 0 ? (null) :
            (<div className="lockdown-message"
                  dangerouslySetInnerHTML={{ __html: this.state.lockdownMessage }}>
             </div>);
    }

}

LockdownMessage.propTypes = {
    lockdownMessage: PropTypes.string,
};
