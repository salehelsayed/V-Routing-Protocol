package com.vrouting.network.socket.core;

public class PayloadWrapper {
    private String routeId;
    private RoutingManager.Route route;

    public PayloadWrapper(String routeId, RoutingManager.Route route) {
        this.routeId = routeId;
        this.route = route;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public RoutingManager.Route getRoute() {
        return route;
    }

    public void setRoute(RoutingManager.Route route) {
        this.route = route;
    }
}
