import React, { useState } from 'react';

const SimulationControls = ({ onStart, onStop }) => {
    const [nodeCount, setNodeCount] = useState(10);
    const [failureProbability, setFailureProbability] = useState(0.1);
    const [recoveryProbability, setRecoveryProbability] = useState(0.2);

    const handleStart = () => {
        onStart({
            nodeCount,
            failureProbability,
            recoveryProbability
        });
    };

    return (
        <div className="simulation-controls">
            <div className="control-group">
                <label>Number of Nodes:</label>
                <input
                    type="number"
                    value={nodeCount}
                    onChange={(e) => setNodeCount(parseInt(e.target.value))}
                    min="2"
                    max="100"
                />
            </div>
            <div className="control-group">
                <label>Failure Probability:</label>
                <input
                    type="number"
                    value={failureProbability}
                    onChange={(e) => setFailureProbability(parseFloat(e.target.value))}
                    step="0.1"
                    min="0"
                    max="1"
                />
            </div>
            <div className="control-group">
                <label>Recovery Probability:</label>
                <input
                    type="number"
                    value={recoveryProbability}
                    onChange={(e) => setRecoveryProbability(parseFloat(e.target.value))}
                    step="0.1"
                    min="0"
                    max="1"
                />
            </div>
            <div className="button-group">
                <button onClick={handleStart} className="start-button">Start Simulation</button>
                <button onClick={onStop} className="stop-button">Stop Simulation</button>
            </div>
        </div>
    );
};

export default SimulationControls;
