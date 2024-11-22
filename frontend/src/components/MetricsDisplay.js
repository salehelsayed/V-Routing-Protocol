import React from 'react';

const MetricsDisplay = ({ metrics }) => {
    if (!metrics) return null;

    return (
        <div className="metrics-display">
            <h3>Network Metrics</h3>
            <div className="metrics-grid">
                <div className="metric-item">
                    <label>Active Nodes:</label>
                    <span>{metrics.activeNodes}</span>
                </div>
                <div className="metric-item">
                    <label>Cluster Count:</label>
                    <span>{metrics.clusterCount}</span>
                </div>
                <div className="metric-item">
                    <label>Average Path Length:</label>
                    <span>{metrics.averagePathLength !== undefined ? metrics.averagePathLength.toFixed(2) : 'N/A'}</span>
                </div>
                <div className="metric-item">
                    <label>Network Stability:</label>
                    <span>{metrics.networkStability !== undefined ? (metrics.networkStability * 100).toFixed(1) : 'N/A'}%</span>
                </div>
                <div className="metric-item">
                    <label>Message Success Rate:</label>
                    <span>{metrics.messageSuccessRate !== undefined ? (metrics.messageSuccessRate * 100).toFixed(1) : 'N/A'}%</span>
                </div>
            </div>
        </div>
    );
};

export default MetricsDisplay;
