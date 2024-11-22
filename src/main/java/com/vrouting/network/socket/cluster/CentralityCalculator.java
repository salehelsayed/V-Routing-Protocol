package com.vrouting.network.socket.cluster;

import com.vrouting.network.Node;
import com.vrouting.network.socket.core.PeerDirectory;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.alg.scoring.EigenvectorCentrality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Calculates network centrality metrics for nodes in the network.
 */
public class CentralityCalculator {
    private static final Logger logger = LoggerFactory.getLogger(CentralityCalculator.class);
    private final Node node;
    private final PeerDirectory peerDirectory;
    private double currentCentrality;
    
    // Constants for centrality calculation
    private static final double CONVERGENCE_THRESHOLD = 1e-6;
    private static final int MAX_ITERATIONS = 100;
    private static final double MIN_CENTRALITY_THRESHOLD = 0.7; // Threshold for cluster head eligibility
    
    public CentralityCalculator(Node node, PeerDirectory peerDirectory) {
        this.node = node;
        this.peerDirectory = peerDirectory;
        this.currentCentrality = 0.0;
    }
    
    public void recalculateCentrality() {
        this.currentCentrality = calculateEigenvectorCentrality();
        logger.info("Recalculated centrality for node {}: {}", node.getNodeId(), currentCentrality);
    }
    
    public boolean isEligibleForClusterHead() {
        return currentCentrality >= MIN_CENTRALITY_THRESHOLD;
    }
    
    private double calculateEigenvectorCentrality() {
        double[][] adjacencyMatrix = peerDirectory.getAdjacencyMatrixAsArray();
        if (adjacencyMatrix.length == 0) {
            return 0.0;
        }
        
        // Convert adjacency matrix to JGraphT graph
        Graph<String, DefaultEdge> graph = buildGraph();
        if (graph.vertexSet().isEmpty()) {
            return 0.0;
        }
        
        try {
            EigenvectorCentrality<String, DefaultEdge> centrality = 
                new EigenvectorCentrality<>(graph, MAX_ITERATIONS, CONVERGENCE_THRESHOLD);
            
            return centrality.getVertexScore(node.getNodeId());
        } catch (Exception e) {
            logger.warn("Failed to calculate centrality using JGraphT, falling back to power iteration", e);
            // Fall back to power iteration method if JGraphT fails
            return calculateCentralityUsingPowerIteration(adjacencyMatrix);
        }
    }
    
    private Graph<String, DefaultEdge> buildGraph() {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        // Add vertices
        Set<String> peers = peerDirectory.getAllPeerIds();
        peers.add(node.getNodeId());
        peers.forEach(graph::addVertex);
        
        // Add edges
        for (String source : peers) {
            for (String target : peerDirectory.getConnectedPeers(source)) {
                if (!graph.containsEdge(source, target)) {
                    graph.addEdge(source, target);
                }
            }
        }
        
        return graph;
    }
    
    private double calculateCentralityUsingPowerIteration(double[][] adjacencyMatrix) {
        int n = adjacencyMatrix.length;
        double[] eigenvector = new double[n];
        Arrays.fill(eigenvector, 1.0 / n);
        
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            double[] newEigenvector = new double[n];
            double norm = 0.0;
            
            // Matrix multiplication
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    newEigenvector[i] += adjacencyMatrix[i][j] * eigenvector[j];
                }
                norm += newEigenvector[i] * newEigenvector[i];
            }
            
            // Normalize
            norm = Math.sqrt(norm);
            for (int i = 0; i < n; i++) {
                newEigenvector[i] /= norm;
            }
            
            // Check convergence
            double diff = 0.0;
            for (int i = 0; i < n; i++) {
                diff += Math.abs(newEigenvector[i] - eigenvector[i]);
            }
            
            if (diff < CONVERGENCE_THRESHOLD) {
                return newEigenvector[peerDirectory.getNodeIndex(node.getNodeId())];
            }
            
            eigenvector = newEigenvector;
        }
        
        logger.warn("Power iteration did not converge after {} iterations", MAX_ITERATIONS);
        return eigenvector[peerDirectory.getNodeIndex(node.getNodeId())];
    }
}
