package app.bladenight.server.rpchandlers;

import app.bladenight.common.keyvaluestore.KeyValueStore;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.RelationshipInputMessage;
import app.bladenight.common.network.messages.RelationshipOutputMessage;
import app.bladenight.common.relationships.HandshakeInfo;
import app.bladenight.common.relationships.RelationshipStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RpcHandlerCreateRelationship extends RpcHandler {

    public RpcHandlerCreateRelationship(RelationshipStore relationshipStore) {
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

        if ( input.getRequestId() <= 0 )
            handleNewRequest(rpcCall, input);
        else
            handleRequestFinalization(rpcCall, input);
    }

    public void handleNewRequest(RpcCall rpcCall, RelationshipInputMessage input) {
        relationshipStore.checkForDuplicateIds();
        HandshakeInfo handshakeInfo = relationshipStore.newRequest(input.getDeviceId(), input.getFriendId());
        rpcCall.setOutput(new RelationshipOutputMessage(handshakeInfo.getRequestId(), handshakeInfo.getFriendId()), RelationshipOutputMessage.class);
        relationshipStore.checkForDuplicateIds();
        try {
            // TODO we write all relationships on all write, this is not efficient
            relationshipStore.write();
        } catch (IOException e) {
            getLog().error("Failed to save relationships: ", e);
        }
    }

    private void handleRequestFinalization(RpcCall rpcCall, RelationshipInputMessage input) {
        HandshakeInfo handshakeInfo = new HandshakeInfo();
        try {
            handshakeInfo = relationshipStore.finalize(input.getRequestId(), input.getDeviceId(), input.getFriendId());
            // TODO we write all relationships on all write, this is not efficient
            relationshipStore.write();
        } catch (IOException e) {
            getLog().error("Failed to save relationships: ", e);
        } catch (Exception e) {
            getLog().error("Failed to finalize relationship: ", e);
            rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Failed to finalize the relationship");
            return;
        }

        rpcCall.setOutput(new RelationshipOutputMessage(input.getRequestId(), handshakeInfo.getFriendId()), RelationshipOutputMessage.class);

    }


    public boolean validateInput(RpcCall rpcCall, RelationshipInputMessage input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }


    private RelationshipStore relationshipStore;

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerCreateRelationship.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerCreateRelationship.class.getName());
        return log;
    }
}
