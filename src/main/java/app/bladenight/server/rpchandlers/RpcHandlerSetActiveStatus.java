package app.bladenight.server.rpchandlers;

import app.bladenight.common.events.EventList;
import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.messages.EventMessage;
import app.bladenight.common.network.messages.EventMessage.EventStatus;
import app.bladenight.common.network.messages.SetActiveStatusMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RpcHandlerSetActiveStatus extends RpcHandler {

    public RpcHandlerSetActiveStatus(EventList eventList, Procession procession, PasswordSafe passwordSafe) {
        this.eventList = eventList;
        this.procession = procession;
        this.passwordSafe = passwordSafe;

    }

    @Override
    public void execute(RpcCall rpcCall) {
        SetActiveStatusMessage msg = rpcCall.getInput(SetActiveStatusMessage.class);
        if (msg == null) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
            return;
        }
        if (!msg.verify(passwordSafe.getAdminPassword(), 12 * 3600 * 1000)) {
            rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + msg.toString());
            return;
        }
        EventStatus newStatus = msg.getStatus();
        if (newStatus == null) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid status");
            return;
        }

        procession.setEventStatus(newStatus);
        eventList.setStatusOfNextEvent(EventMessage.convertStatus(newStatus));
        getLog().info("SetStatusOfNextEvent to:" + newStatus + " by did:" + msg.did);

        try {
            eventList.write();
        } catch (IOException e) {
            getLog().error("Failed to write to dir: " + e);
        }
    }

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerSetActiveStatus.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerSetActiveStatus.class.getName());
        return log;
    }


    private final EventList eventList;
    private final Procession procession;
    private final PasswordSafe passwordSafe;

}
