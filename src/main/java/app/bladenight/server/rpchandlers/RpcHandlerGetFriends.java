package app.bladenight.server.rpchandlers;

import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.messages.FriendMessage;
import app.bladenight.common.network.messages.FriendsMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.relationships.RelationshipMember;
import app.bladenight.common.relationships.RelationshipStore;

import java.util.List;

public class RpcHandlerGetFriends extends RpcHandler {
    public RpcHandlerGetFriends(RelationshipStore relationshipStore, Procession procession) {
        this.relationshipStore = relationshipStore;
        this.procession = procession;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        String deviceId = rpcCall.getInput(String.class);
        if ( deviceId == null || deviceId.length() == 0 ) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid deviceId: " + deviceId);
            return;
        }
        List<RelationshipMember> relationshipMembers = relationshipStore.getAllRelationships(deviceId);
        FriendsMessage friends = new FriendsMessage();
        for ( RelationshipMember member : relationshipMembers) {
            FriendMessage friend = new FriendMessage();
            friend.setFriendId(member.getFriendId());
            friend.setRequestId(member.getRequestId());
            friends.put(friend.getFriendId(),friend);
            if ( member.getDeviceId() != null)
                friend.isOnline(procession.getParticipant(member.getDeviceId()) != null);
        }
        rpcCall.setOutput(friends, FriendsMessage.class);
    }

    private RelationshipStore relationshipStore;
    private Procession procession;

}
