package app.bladenight.server.rpchandlers;

import app.bladenight.common.keyvaluestore.KeyValueStore;
import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.HandshakeClientMessage;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RpcHandlerAuthenticate extends RpcHandler {

    public RpcHandlerAuthenticate(int minClientBuild) {
        this.minClientBuild = minClientBuild;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        HandshakeClientMessage input = rpcCall.getInput(HandshakeClientMessage.class);

        if ( ! validateInput(rpcCall, input) )
            return;

        getLog().info("minClientBuild="+minClientBuild);
        getLog().info("clientbuild="+input.getClientBuildNumber());
        if ( minClientBuild > 0 && input.getClientBuildNumber() > 0  ) {
            if ( input.getClientBuildNumber() < minClientBuild ) {
                rpcCall.setError(BladenightError.OUTDATED_CLIENT.getText(), "Please update your client to version " + minClientBuild + " or greater");
                return;
            }

        }
    }

    public boolean validateInput(RpcCall rpcCall, HandshakeClientMessage input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }

    private int minClientBuild;
    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerAuthenticate.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerAuthenticate.class.getName());
        return log;
    }
}
