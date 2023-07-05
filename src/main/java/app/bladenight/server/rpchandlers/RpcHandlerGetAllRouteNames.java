package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.messages.RouteNamesMessage;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;

public class RpcHandlerGetAllRouteNames extends RpcHandler {

    public RpcHandlerGetAllRouteNames(RouteStore routeStore) {
        this.routeStore = routeStore;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        rpcCall.setOutput(RouteNamesMessage.newFromRouteNameList(routeStore.getAvailableRoutes()), RouteNamesMessage.class);
    }

    private RouteStore routeStore;
}
