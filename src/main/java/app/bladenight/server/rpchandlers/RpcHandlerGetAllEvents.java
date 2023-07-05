package app.bladenight.server.rpchandlers;

import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import app.bladenight.common.events.EventList;
import app.bladenight.common.network.messages.EventListMessage;


public class RpcHandlerGetAllEvents extends RpcHandler {

    public RpcHandlerGetAllEvents(EventList eventsList) {
        this.eventsList = eventsList;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        rpcCall.setOutput(EventListMessage.newFromEventsList(eventsList), EventListMessage.class);
    }

    private final EventList eventsList;
}
