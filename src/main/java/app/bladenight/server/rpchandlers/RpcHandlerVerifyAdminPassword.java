package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.messages.AdminMessage;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RpcHandlerVerifyAdminPassword extends RpcHandler {

    public RpcHandlerVerifyAdminPassword(PasswordSafe passwordSafe) {
        this.passwordSafe = passwordSafe;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        AdminMessage message = rpcCall.getInput(AdminMessage.class);
        // TODO put maxAge in the configuration file
        if ( ! message.verify(passwordSafe.getAdminPassword(), 12*3600*1000) ) {
            rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + message.toString());
            return;
        }
        rpcCall.setOutput("OK", String.class);
    }

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerVerifyAdminPassword.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerVerifyAdminPassword.class.getName());
        return log;
    }

    private PasswordSafe passwordSafe;

}
