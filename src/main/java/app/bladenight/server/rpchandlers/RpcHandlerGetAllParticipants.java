package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.messages.LatLong;
import app.bladenight.common.procession.Participant;
import app.bladenight.common.procession.Procession;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;

import java.util.List;

public class RpcHandlerGetAllParticipants extends RpcHandler {

    public RpcHandlerGetAllParticipants(Procession procession) {
        this.procession = procession;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        List<Participant> participants = procession.getParticipants();
        LatLong[] coordinates = new LatLong[participants.size()];
        int i = 0;
        for (Participant p : participants) {
            coordinates[i++] = new LatLong(p.getLatitude(), p.getLongitude());
        }
        rpcCall.setOutput(coordinates, LatLong[].class);
    }

    private final Procession procession;
}
