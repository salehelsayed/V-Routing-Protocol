package com.vrouting.vrouting.model;

import java.util.List;
import java.util.Map;

public class SimulationResponse {
    private String simulationId;
    private List<String> nodes;
    private Map<String, List<String>> connections;
    private boolean success;
    private String message;

    // Getters and Setters
    public String getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(String simulationId) {
        this.simulationId = simulationId;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Map<String, List<String>> getConnections() {
        return connections;
    }

    public void setConnections(Map<String, List<String>> connections) {
        this.connections = connections;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
