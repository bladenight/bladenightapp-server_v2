package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.RouteMessage;
import app.bladenight.common.routes.Route;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RpcHandlerGetRoute extends RpcHandler {

    public RpcHandlerGetRoute(RouteStore routeStore) {
        this.routeStore = routeStore;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        String input = rpcCall.getInput(String.class);
        if ( ! validateInput(rpcCall, input) )
            return;
        getLog().trace("Got request for route: " + input);
        Route route = routeStore.getRoute(input);
        if ( route != null )
            rpcCall.setOutput(new RouteMessage(route), RouteMessage.class);
        else
            rpcCall.setError(BladenightUrl.BASE+"noSuchRoute", "Could not load route named "+ input);
    }

    public boolean validateInput(RpcCall rpcCall, String input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerGetRoute.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerGetRoute.class.getName());
        return log;
    }

    private RouteStore routeStore;
}
