package app.bladenight.server.rpchandlers;

import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import app.bladenight.wampv2.server.messages.SubscribeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RPCHandlerSubscribe extends RpcHandler {
    String topicUri;
    public RPCHandlerSubscribe(ArrayList<Long> subscribers,String topicUri) {
        this.topicUri = topicUri;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        SubscribeMessage input = rpcCall.getInput(SubscribeMessage.class);
    }

    private static final Logger logger = LogManager.getLogger(RPCHandlerSubscribe.class);

}
