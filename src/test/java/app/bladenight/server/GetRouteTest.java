package app.bladenight.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallResultMessage;
import app.bladenight.wampv2.server.messages.Message;
import app.bladenight.wampv2.server.messages.MessageType;
import app.bladenight.common.network.messages.LatLong;
import app.bladenight.common.network.messages.RouteMessage;
import app.bladenight.common.routes.RouteStore;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetRouteTest {
    final String routesDir = "/routes/";

    @Before
    public void init() {
        LogHelper.disableLogs();

        RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveRouteTest.class.getResource(routesDir)));

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
                .setRouteStore(routeStore)
                .build();

        client = new Client(server);
    }

    @Test
    public void test() throws IOException, BadArgumentException {
        final String routeName = "Nord - kurz";
        Message message = client.getRoute(routeName);
        assertTrue(message.getType().equals(MessageType.RESULT));
        RouteMessage routeMessage = ((CallResultMessage) message).getPayload(RouteMessage.class);
        assertTrue(routeMessage != null);
        assertEquals(routeName, routeMessage.getRouteName());
        assertEquals(12605, routeMessage.getRouteLength());
        List<LatLong> nodes = routeMessage.getNodes();
        assertTrue(nodes != null);
        assertEquals(nodes.size(), 76);
        assertEquals(nodes.get(0), new LatLong(48.13246449995051, 11.54349921573263));
        assertEquals(nodes.get(75), new LatLong(48.1325299743437, 11.54351506700966));
    }


    private Client client;

}
