package app.bladenight.server.rpchandlers;

import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.RouteMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.routes.Route;


public class RpcHandlerGetActiveRoute extends RpcHandler {

    public RpcHandlerGetActiveRoute(Procession procession) {
        this.procession = procession;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        Route route = procession.getRoute();
        if ( route != null )
            rpcCall.setOutput(new RouteMessage(route), RouteMessage.class);
        else
            rpcCall.setError(BladenightUrl.BASE+"noSuchRoute", "No active route available");
    }

    public boolean validateInput(RpcCall rpcCall, String input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }

    private Procession procession;
}
