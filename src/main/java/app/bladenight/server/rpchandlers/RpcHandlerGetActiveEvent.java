package app.bladenight.server.rpchandlers;

import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import app.bladenight.common.events.Event;
import app.bladenight.common.events.EventList;
import app.bladenight.common.network.messages.EventMessage;


public class RpcHandlerGetActiveEvent extends RpcHandler {

    public RpcHandlerGetActiveEvent(EventList manager) {
        this.eventManager = manager;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        Event nextEvent = eventManager.getNextEvent();
        rpcCall.setOutput(new EventMessage(nextEvent), EventMessage.class);
    }

    private final EventList eventManager;
}
