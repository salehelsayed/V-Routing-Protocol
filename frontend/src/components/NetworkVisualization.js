import React, { useEffect, useRef } from 'react';
import * as d3 from 'd3';

const NetworkVisualization = ({ networkState }) => {
    const svgRef = useRef();
    const width = 800;
    const height = 600;

    useEffect(() => {
        if (!networkState || !networkState.nodes) return;

        const svg = d3.select(svgRef.current);
        svg.selectAll("*").remove();

        // Create force simulation
        const simulation = d3.forceSimulation(networkState.nodes)
            .force("link", d3.forceLink().id(d => d.id))
            .force("charge", d3.forceManyBody().strength(-300))
            .force("center", d3.forceCenter(width / 2, height / 2));

        // Create links between nodes in the same cluster
        const links = [];
        networkState.nodes.forEach(node => {
            if (node.cluster) {
                networkState.nodes
                    .filter(n => n.cluster === node.cluster && n.id !== node.id)
                    .forEach(target => {
                        links.push({ source: node.id, target: target.id });
                    });
            }
        });

        // Draw links
        const link = svg.append("g")
            .selectAll("line")
            .data(links)
            .enter()
            .append("line")
            .attr("stroke", "#999")
            .attr("stroke-opacity", 0.6)
            .attr("stroke-width", 2);

        // Draw nodes
        const node = svg.append("g")
            .selectAll("circle")
            .data(networkState.nodes)
            .enter()
            .append("circle")
            .attr("r", 10)
            .attr("fill", d => d.isActive ? (d.isClusterHead ? "#ff4444" : "#4444ff") : "#999")
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));

        // Add node labels
        const labels = svg.append("g")
            .selectAll("text")
            .data(networkState.nodes)
            .enter()
            .append("text")
            .text(d => d.id)
            .attr("font-size", "12px")
            .attr("dx", 12)
            .attr("dy", 4);

        // Update positions on simulation tick
        simulation.on("tick", () => {
            link
                .attr("x1", d => d.source.x)
                .attr("y1", d => d.source.y)
                .attr("x2", d => d.target.x)
                .attr("y2", d => d.target.y);

            node
                .attr("cx", d => d.x)
                .attr("cy", d => d.y);

            labels
                .attr("x", d => d.x)
                .attr("y", d => d.y);
        });

        // Drag functions
        function dragstarted(event) {
            if (!event.active) simulation.alphaTarget(0.3).restart();
            event.subject.fx = event.subject.x;
            event.subject.fy = event.subject.y;
        }

        function dragged(event) {
            event.subject.fx = event.x;
            event.subject.fy = event.y;
        }

        function dragended(event) {
            if (!event.active) simulation.alphaTarget(0);
            event.subject.fx = null;
            event.subject.fy = null;
        }

        return () => {
            simulation.stop();
        };
    }, [networkState]);

    return (
        <svg ref={svgRef} width={width} height={height} style={{ border: '1px solid #ccc' }} />
    );
};

export default NetworkVisualization;
