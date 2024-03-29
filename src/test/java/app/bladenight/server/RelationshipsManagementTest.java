package app.bladenight.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallResultMessage;
import app.bladenight.wampv2.server.messages.Message;
import app.bladenight.wampv2.server.messages.MessageType;
import app.bladenight.common.network.messages.FriendsMessage;
import app.bladenight.common.network.messages.RelationshipOutputMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;

public class RelationshipsManagementTest {

    @BeforeClass
    public static void beforeClass() {
        LogHelper.disableLogs();
    }

    @Before
    public void before() {
        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setProcession(new Procession())
        .setRelationshipStore(new RelationshipStore())
        .build();

        client = new Client(server);
    }

    @Test
    public void simpleRelationship() throws IOException, BadArgumentException {
        String deviceId1 = UUID.randomUUID().toString();
        String deviceId2 = UUID.randomUUID().toString();

        // Create request from deviceId1
        RelationshipOutputMessage output = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
        final int friendId1 = friendIdCounter;
        assertTrue(output.getRequestId() > 0);
        assertEquals(friendIdCounter, output.getFriendId());

        // Make sure the relationship is pending, with the right data
        FriendsMessage friends = getAndParseFriends(deviceId1);
        assertTrue(friends.get(friendId1) != null);
        assertTrue(friends.get(friendId1).isRelationshipPending());
        assertEquals(output.getRequestId(), friends.get(friendId1).getRequestId());

        // Accept the request from deviceId2
        output = sendAndParseRequest(deviceId2, ++friendIdCounter, output.getRequestId());
        int friendId2 = friendIdCounter;
        assertTrue(output.getRequestId() > 0);
        assertEquals(friendIdCounter, output.getFriendId());

        // From deviceId1's perspective: Make sure the relationship is not pending anymore, and has the right data
        friends = getAndParseFriends(deviceId1);
        assertTrue(friends.get(friendId1) != null);
        assertTrue(! friends.get(friendId1).isRelationshipPending());
        // The request ID shall have been reset:
        assertEquals(0, friends.get(friendId1).getRequestId());

        // Same from from deviceId2's perspective
        friends = getAndParseFriends(deviceId1);
        assertTrue(friends.get(friendId1) != null);
        assertTrue(! friends.get(friendId1).isRelationshipPending());
        // The request ID shall have been reset:
        assertEquals(0, friends.get(friendId1).getRequestId());

        // Request deletion of the relation ship from deviceId1:
        sendDeletionAndCheckForSuccess(deviceId1, friendId1);

        // From deviceId1's perspective: make sure the relationship is deleted
        friends = getAndParseFriends(deviceId1);
        assertTrue(friends.get(friendId1) == null);

        // From deviceId2's perspective: make sure the relationship is deleted
        friends = getAndParseFriends(deviceId2);
        assertTrue(friends.get(friendId2) == null);
    }

    @Test
    public void openMultipleRequests() throws IOException, BadArgumentException {
        String deviceId1 = UUID.randomUUID().toString();

        RelationshipOutputMessage message1 = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
        assertTrue(message1.getRequestId() > 0);
        assertEquals(friendIdCounter, message1.getFriendId());

        RelationshipOutputMessage message2 = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
        assertTrue(message2.getRequestId() > 0);
        assertTrue(message1.getRequestId() != message2.getRequestId());
        assertEquals(friendIdCounter, message2.getFriendId());
    }


    @Test
    public void invalidRequest1() throws IOException, BadArgumentException {
        Message message = client.sendRelationshipRequest(null, ++friendIdCounter, 0);
        assertSame(message.getType(), MessageType.ERROR);
    }

    @Test
    public void invalidRequest2() throws IOException, BadArgumentException {
        RelationshipOutputMessage output = sendAndParseRequest(UUID.randomUUID().toString(), ++friendIdCounter, 0);
        assertTrue(output.getRequestId() > 0);
        Message message = client.sendRelationshipRequest(null, ++friendIdCounter, output.getRequestId());
        assertTrue(message.getType() == MessageType.ERROR);
    }

    @Test
    public void selfRelationship() throws IOException, BadArgumentException {
        String deviceId = UUID.randomUUID().toString();
        RelationshipOutputMessage output = sendAndParseRequest(deviceId, ++friendIdCounter, 0);
        assertTrue(output.getRequestId() > 0);
        Message message = client.sendRelationshipRequest(deviceId, ++friendIdCounter, output.getRequestId());
        assertTrue(message.getType() == MessageType.ERROR);
    }


    public long createRelationShip(String deviceId1, String deviceId2) throws IOException, BadArgumentException {
        RelationshipOutputMessage output;
        output = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
        long friendId = friendIdCounter;
        output = sendAndParseRequest(deviceId2, ++friendIdCounter, output.getRequestId());
        return friendId;
    }

    public void sendDeletionAndCheckForSuccess(String deviceId, int friendId) throws IOException, BadArgumentException  {
        Message message = client.deleteReleationship(deviceId, friendId);
        assertTrue(message.getType() == MessageType.RESULT);
    }

    public void sendDeletionAndCheckForFailure(String deviceId, int friendId) throws IOException, BadArgumentException  {
        Message message = client.deleteReleationship(deviceId, friendId);
        assertTrue(message.getType() == MessageType.ERROR);
    }


    public RelationshipOutputMessage sendAndParseRequest(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
        Message message = client.sendRelationshipRequest(deviceId, friendId, requestId);
        assertTrue(message.getType() == MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(RelationshipOutputMessage.class);
    }

    public FriendsMessage getAndParseFriends(String deviceId) throws IOException, BadArgumentException {
        Message message = client.getFriends(deviceId);
        assertTrue(message.getType() == MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(FriendsMessage.class);
    }


    private Client client;
    static int friendIdCounter = 1;
}
