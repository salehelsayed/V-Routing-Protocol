package com.vrouting.simulation;

import java.util.Objects;

public class SimulationStatus {
    private int completionPercentage;
    private String currentState;
    private boolean isRunning;
    private String message;
    private SimulationStatus previousStatus;

    public SimulationStatus() {
        this.completionPercentage = 0;
        this.currentState = "IDLE";
        this.isRunning = false;
        this.message = "";
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean hasChanges() {
        if (previousStatus == null) {
            previousStatus = new SimulationStatus();
            previousStatus.isRunning = this.isRunning;
            previousStatus.currentState = this.currentState;
            previousStatus.message = this.message;
            previousStatus.completionPercentage = this.completionPercentage;
            return true;
        }

        boolean hasChanged = this.isRunning != previousStatus.isRunning ||
                           !Objects.equals(this.currentState, previousStatus.currentState) ||
                           !Objects.equals(this.message, previousStatus.message) ||
                           this.completionPercentage != previousStatus.completionPercentage;

        // Update previous status
        if (hasChanged) {
            previousStatus.isRunning = this.isRunning;
            previousStatus.currentState = this.currentState;
            previousStatus.message = this.message;
            previousStatus.completionPercentage = this.completionPercentage;
        }

        return hasChanged;
    }
}
