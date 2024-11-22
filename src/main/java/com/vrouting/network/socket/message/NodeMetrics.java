package com.vrouting.network.socket.message;

public class NodeMetrics {
    private double centrality;
    private int degree;
    private int clusterSize;
    private double stability;

    public NodeMetrics() {
        this.centrality = 0.0;
        this.degree = 0;
        this.clusterSize = 0;
        this.stability = 0.0;
    }

    public NodeMetrics(double centrality, int degree, int clusterSize, double stability) {
        this.centrality = centrality;
        this.degree = degree;
        this.clusterSize = clusterSize;
        this.stability = stability;
    }

    public double getCentrality() {
        return centrality;
    }

    public void setCentrality(double centrality) {
        this.centrality = centrality;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(int clusterSize) {
        this.clusterSize = clusterSize;
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    @Override
    public String toString() {
        return "NodeMetrics{" +
                "centrality=" + centrality +
                ", degree=" + degree +
                ", clusterSize=" + clusterSize +
                ", stability=" + stability +
                '}';
    }
}
