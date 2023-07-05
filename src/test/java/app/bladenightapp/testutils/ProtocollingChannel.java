package app.bladenightapp.testutils;

import app.bladenight.wampv2.server.common.Channel;

import java.util.ArrayList;
import java.util.List;

public class ProtocollingChannel implements Channel {
    public List<String> handledMessages = new ArrayList<String>();

    @Override
    public void handle(String message) {
        // System.out.println("Channel:handle " + message);
        handledMessages.add(message);
    }

    public String lastMessage() {
        return handledMessages.get(handledMessages.size()-1);
    }

}