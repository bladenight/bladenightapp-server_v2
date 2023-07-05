package app.bladenight.server.rpchandlers;


import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.messages.SetMinimumLinearPosition;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static app.bladenight.common.events.EventGsonHelper.getGson;

public class RpcHandlerKillServer extends RpcHandler {

    private PasswordSafe passwordSafe;

    public RpcHandlerKillServer(PasswordSafe passwordSafe) {
        this.passwordSafe = passwordSafe;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        SetMinimumLinearPosition msg = rpcCall.getInput(SetMinimumLinearPosition.class);

        if ( msg == null ) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
            getLog().warn("Could not parse the input:"+ msg.did);
            return;
        }
        if ( ! msg.verify(passwordSafe.getAdminPassword(), 12*3600*1000)) {
            rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + msg.toString());
            getLog().warn("Verification for admin message failed:"+ msg.did);
            return;
        }
        getLog().warn("Killing server on client request by client:"+ msg.did);
        System.exit(0);
    }

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerKillServer.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerKillServer.class.getName());
        return log;
    }
}
