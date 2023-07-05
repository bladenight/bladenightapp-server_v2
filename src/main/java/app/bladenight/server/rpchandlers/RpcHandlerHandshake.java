package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.HandShakeResultMessage;
import app.bladenight.common.network.messages.HandshakeClientMessage;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RpcHandlerHandshake extends RpcHandler {

    public RpcHandlerHandshake(int minClientBuild) {
        this.minClientBuild = minClientBuild;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        HandshakeClientMessage input = rpcCall.getInput(HandshakeClientMessage.class);

        if (!validateInput(rpcCall, input)) return;

        if (minClientBuild > 0 && input.getClientBuildNumber() > 0) {
            if (input.getClientBuildNumber() < minClientBuild) {
                rpcCall.setOutput(new HandShakeResultMessage(false, minClientBuild), HandShakeResultMessage.class);
                getLog().warn("Session:"+ rpcCall.getOriginatingSession()+" should have minClientBuild=" + minClientBuild+"-"+" has clientbuild=" + input.getClientBuildNumber());

                //rpcCall.setError(BladenightError.OUTDATED_CLIENT.getText(), "Please update your client to version " + minClientBuild + " or greater");
            } else {
                rpcCall.setOutput(new HandShakeResultMessage(true, minClientBuild), HandShakeResultMessage.class);
            }
        }
    }

    public boolean validateInput(RpcCall rpcCall, HandshakeClientMessage input) {
        if (input == null) {
            rpcCall.setError(BladenightUrl.BASE + " invalidInput ", "Invalid input: " + input);
            return false;
        }
        return true;
    }

    private int minClientBuild;
    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerHandshake.log = log;
    }

    protected static Logger getLog() {
        if (log == null) log = LogManager.getLogger(RpcHandlerHandshake.class.getName());
        return log;
    }
}
