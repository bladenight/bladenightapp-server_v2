package app.bladenight.server.rpchandlers;


import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.messages.SetMinimumLinearPosition;
import app.bladenight.common.procession.ParticipantUpdater;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RpcHandlerSetMinimumLinearPosition extends RpcHandler {

    private PasswordSafe passwordSafe;

    public RpcHandlerSetMinimumLinearPosition(PasswordSafe passwordSafe) {
        this.passwordSafe = passwordSafe;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        SetMinimumLinearPosition msg = rpcCall.getInput(SetMinimumLinearPosition.class);

        if (msg == null) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
            return;
        }
        if (!msg.verify(passwordSafe.getAdminPassword(), 12 * 3600 * 1000)) {
            rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + msg.toString());
            return;
        }
        getLog().warn("Setting ParticipantUpdater.minLinearPosition to " + msg.getValue());
        ParticipantUpdater.minLinearPosition = msg.getValue();
    }

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerSetMinimumLinearPosition.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerSetMinimumLinearPosition.class.getName());
        return log;
    }
}
