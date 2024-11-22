import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import NetworkVisualization from './components/NetworkVisualization';
import SimulationControls from './components/SimulationControls';
import MetricsDisplay from './components/MetricsDisplay';
import SimulationStatus from './components/SimulationStatus';
import './App.css';

const API_BASE_URL = 'http://localhost:8080';
const WEBSOCKET_URL = `${API_BASE_URL}/vrouting-websocket`;

function App() {
    const [networkState, setNetworkState] = useState(null);
    const [stompClient, setStompClient] = useState(null);
    const [simulationStatus, setSimulationStatus] = useState(null);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS(WEBSOCKET_URL),
            onConnect: () => {
                console.log('Connected to WebSocket');
                client.subscribe('/topic/network-state', message => {
                    const newState = JSON.parse(message.body);
                    console.log('Received new network state:', newState);
                    setNetworkState(newState);
                });
                client.subscribe('/topic/simulation-status', message => {
                    const status = JSON.parse(message.body);
                    console.log('Received simulation status:', status);
                    setSimulationStatus(status);
                });
            },
            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
                setSimulationStatus(null);
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
            }
        });

        client.activate();
        setStompClient(client);

        return () => {
            if (client.connected) {
                client.deactivate();
            }
        };
    }, []);

    const handleStartSimulation = async (params) => {
        console.log('Start Simulation clicked with params:', params);
        try {
            const response = await fetch(`${API_BASE_URL}/api/simulation/start`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(params),
            });
            console.log('Simulation start response:', response);
            if (!response.ok) {
                console.error('Failed to start simulation:', response.statusText);
            }
        } catch (error) {
            console.error('Failed to start simulation:', error);
        }
    };

    const handleStopSimulation = async () => {
        try {
            await fetch(`${API_BASE_URL}/api/simulation/stop`, {
                method: 'POST',
            });
            setNetworkState(null);
            setSimulationStatus(null);
        } catch (error) {
            console.error('Failed to stop simulation:', error);
        }
    };

    return (
        <div className="app">
            <header className="app-header">
                <h1>V-Routing Protocol Visualization</h1>
            </header>
            <main className="app-main">
                <div className="control-panel">
                    <SimulationControls
                        onStart={handleStartSimulation}
                        onStop={handleStopSimulation}
                    />
                    <SimulationStatus status={simulationStatus} />
                    <MetricsDisplay metrics={networkState?.metrics} />
                </div>
                <div className="visualization-panel">
                    <NetworkVisualization networkState={networkState} />
                </div>
            </main>
        </div>
    );
}

export default App;
