import React from 'react';
import './SimulationStatus.css';

const SimulationStatus = ({ status }) => {
    if (!status) return null;

    return (
        <div className="simulation-status">
            <h3>Simulation Status</h3>
            <div className="status-content">
                <div className="status-item">
                    <label>State:</label>
                    <span className={`status-state ${status.currentState.toLowerCase()}`}>
                        {status.currentState}
                    </span>
                </div>
                <div className="status-item">
                    <label>Progress:</label>
                    <div className="progress-bar">
                        <div 
                            className="progress-fill"
                            style={{ width: `${status.completionPercentage}%` }}
                        />
                        <span className="progress-text">
                            {status.completionPercentage}%
                        </span>
                    </div>
                </div>
                {status.message && (
                    <div className="status-message">
                        {status.message}
                    </div>
                )}
            </div>
        </div>
    );
};

export default SimulationStatus;
