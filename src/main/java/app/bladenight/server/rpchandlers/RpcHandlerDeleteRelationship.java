package app.bladenight.server.rpchandlers;

import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.RelationshipInputMessage;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RpcHandlerDeleteRelationship extends RpcHandler {

    public RpcHandlerDeleteRelationship(RelationshipStore relationshipStore) {
        this.relationshipStore = relationshipStore;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        RelationshipInputMessage input = rpcCall.getInput(RelationshipInputMessage.class);

        if ( ! validateInput(rpcCall, input) )
            return;

        if ( relationshipStore == null ) {
            rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Internal error: relationshipStore is null");
            return;
        }

        if ( input.getDeviceId() == null || input.getDeviceId().length() == 0 ) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid device id:" + input.getDeviceId() );
            return;
        }

        if ( input.getFriendId() <= 0 ) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid device id:" + input.getDeviceId() );
            return;
        }

        getLog().info("Trying to delete relationship for deviceId=" + input.getDeviceId() +  " friendId=" + input.getFriendId() );

        int hits = relationshipStore.deleteRelationship(input.getDeviceId(), input.getFriendId());

        getLog().info("Deleted " + hits + " relationship(s)" );

        try {
            relationshipStore.write();
        }
        catch(IOException e){
            getLog().error("Failed to write relationships: "  + e);
        }
    }

    public boolean validateInput(RpcCall rpcCall, RelationshipInputMessage input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }


    private final RelationshipStore relationshipStore;


    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerDeleteRelationship.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerDeleteRelationship.class.getName());
        return log;
    }
}
