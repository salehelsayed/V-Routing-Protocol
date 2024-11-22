package com.vrouting.network.socket;

import com.vrouting.network.socket.core.Node;
import com.vrouting.network.socket.config.NetworkConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VRoutingTest {
    @Test
    public void testBasicNodeSetup() {
        NetworkConfig config = new NetworkConfig.Builder().maxNetworkDepth(5).clusterHeadThreshold(0.7).build();
        
        Node node = mock(Node.class, withSettings()
            .useConstructor("test-node-1", config)
            .defaultAnswer(CALLS_REAL_METHODS));
        
        assertNotNull(node);
        assertEquals("test-node-1", node.getNodeId());
    }
}
