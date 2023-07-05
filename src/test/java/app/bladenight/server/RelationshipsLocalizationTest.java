package app.bladenight.server;

import app.bladenight.common.events.EventList;
import app.bladenight.common.network.messages.GpsInfo;
import app.bladenight.common.network.messages.MovingPointMessage;
import app.bladenight.common.network.messages.RealTimeUpdateData;
import app.bladenight.common.network.messages.RelationshipOutputMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenight.common.routes.Route;
import app.bladenight.common.time.Sleep;
import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallResultMessage;
import app.bladenight.wampv2.server.messages.Message;
import app.bladenight.wampv2.server.messages.MessageType;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RelationshipsLocalizationTest {
    final String routeName = "Nord - kurz";
    final String path = "/routes/" + routeName + ".kml";

    @BeforeClass
    public static void beforeClass() {
        LogHelper.disableLogs();
    }

    @Before
    public void before() {
        Route.setLog(LogManager.getLogger(RelationshipsLocalizationTest.class.getName()));
        File file = FileUtils.toFile(EventList.class.getResource(path));
        assertTrue(file != null);
        Route route = new Route();
        assertTrue(route.load(file));
        assertEquals(routeName, route.getName());

        Procession procession = new Procession();
        procession.setRoute(route);
        procession.setMaxComputeAge(0);

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
                .setProcession(procession)
                .setRelationshipStore(new RelationshipStore())
                .build();

        client = new Client(server);

    }


    @Test
    public void test() throws IOException, BadArgumentException, InterruptedException {
        String deviceId1 = "user-1";
        String deviceId2 = "user-2";
        String deviceId3 = "user-3";
        String deviceId4 = "user-4";

        int friendIdFor2 = createRelationShip(deviceId1, deviceId2);
        int friendIdFor3 = createRelationShip(deviceId1, deviceId3);
        int friendIdFor4 = createRelationShip(deviceId1, deviceId4);

        RealTimeUpdateData data2 = getRealtimeUpdateFromParticipant(deviceId2, 48.143655, 11.548839, 1, 10.5);
        assertEquals(1756, data2.getUserPosition(), 1.0);
        assertEquals(true, data2.isUserOnRoute());

        RealTimeUpdateData data3 = getRealtimeUpdateFromParticipant(deviceId3, 48.143755, 11.548839, 2, 20.5);
        assertEquals(1766, data3.getUserPosition(), 1.0);
        assertEquals(true, data3.isUserOnRoute());

        RealTimeUpdateData data1 = getRealtimeUpdateFromParticipant(deviceId1, 48.139341, 11.547129, 4, 40.5);
        assertEquals(1381, data1.getUserPosition(), 1.0);
        assertEquals(true, data1.isUserOnRoute());
        assertTrue(data1.getFriends() != null);

        MovingPointMessage friend1 = data1.getFriends().get(friendIdFor2);
        assertTrue(friend1 != null);
        assertEquals(data2.getUserPosition(), friend1.getPosition(), 1.0);

        MovingPointMessage friend2 = data1.getFriends().get(friendIdFor3);
        assertTrue(friend2 != null);
        assertEquals(data3.getUserPosition(), friend2.getPosition(), 1.0);

        MovingPointMessage friend3 = data1.getFriends().get(friendIdFor4);
        assertTrue(friend3 == null);

        Sleep.sleep(10);

        int accuracy3 = 100;
        data3 = getRealtimeUpdateFromParticipant(deviceId3, 48.160027, 11.561509, accuracy3, 0, 0);
        assertEquals(4729.0, data3.getUserPosition(), 1.0);
        assertEquals(true, data3.isUserOnRoute());

        data1 = getRealtimeUpdateFromParticipant(deviceId1, 48.139341, 11.547129, 0, 0);
        friend3 = data1.getFriends().get(friendIdFor3);
        assertTrue(friend3 != null);
        assertEquals(true, friend3.isOnRoute());
        assertEquals(friend3.getPosition(), data3.getUserPosition(), 0.0);
        assertEquals(accuracy3, friend3.getAccuracy());
        assertTrue(friend3.getEstimatedTimeToArrival() > 0);
        //assertTrue(data3.getUser().getEstimatedTimeToArrival() <= friend3.getEstimatedTimeToArrival());
    }

    @Test
    public void serverShallNotSendCoordinatesForObservers() throws IOException, BadArgumentException, InterruptedException {
        String deviceId1 = "user-1";
        String deviceId2 = "user-2";

        int friendIdFor2 = createRelationShip(deviceId1, deviceId2);

        RealTimeUpdateData data2 = getRealtimeUpdateFromObserver(deviceId2, 48.143655, 11.548839, 0, 0);
        assertEquals(1756, data2.getUserPosition(), 1.0);
        assertEquals(true, data2.isUserOnRoute());

        RealTimeUpdateData data1 = getRealtimeUpdateFromParticipant(deviceId1, 48.139341, 11.547129, 0, 0);

        MovingPointMessage friend2 = data1.getFriends().get(friendIdFor2);
        assertTrue(friend2 == null);

        getRealtimeUpdateFromParticipant(deviceId2, 48.143655, 11.548839, 0, 0);
        data1 = getRealtimeUpdateFromParticipant(deviceId1, 48.139341, 11.547129, 0, 0);
        friend2 = data1.getFriends().get(friendIdFor2);

        assertTrue(friend2 != null);
        assertEquals(data2.getUserPosition(), friend2.getPosition(), 1.0);
    }

    public int createRelationShip(String deviceId1, String deviceId2) throws IOException, BadArgumentException {
        RelationshipOutputMessage output;
        output = sendAndParse(deviceId1, ++friendIdCounter, 0);
        int friendId = output.getFriendId();
        output = sendAndParse(deviceId2, ++friendIdCounter, output.getRequestId());
        return friendId;
    }

    public RelationshipOutputMessage sendAndParse(String deviceId, int friendId, long relationshipId) throws IOException, BadArgumentException {
        Message message = client.sendRelationshipRequest(deviceId, friendId, relationshipId);
        assertTrue(message.getType() == MessageType.RESULT);
        CallResultMessage callResult = (CallResultMessage) message;
        return callResult.getPayload(RelationshipOutputMessage.class);
    }

    RealTimeUpdateData getRealtimeUpdateFromObserver(String clientId, double lat, double lon, int spf, double userSpeed) throws IOException, BadArgumentException {
        return client.getRealtimeUpdate(new GpsInfo(clientId, false, lat, lon,0, spf, userSpeed, 0.0));
    }


    RealTimeUpdateData getRealtimeUpdateFromParticipant(String clientId, double lat, double lon, double acc, int spf, double userSpeed) throws IOException, BadArgumentException {
        return client.getRealtimeUpdate(new GpsInfo(clientId, true, lat, lon, (int) acc, spf, userSpeed,0.0));
    }

    RealTimeUpdateData getRealtimeUpdateFromParticipant(String clientId, double lat, double lon, int spf, double userSpeed) throws IOException, BadArgumentException {
        return getRealtimeUpdateFromParticipant(clientId, lat, lon, 0.0, spf, userSpeed);
    }

    static int friendIdCounter = 1;
    private Client client;

}
