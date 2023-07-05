package app.bladenight.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallResultMessage;
import app.bladenight.wampv2.server.messages.Message;
import app.bladenight.wampv2.server.messages.MessageType;
import app.bladenight.common.network.messages.RouteNamesMessage;
import app.bladenight.common.routes.RouteStore;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetAllRouteNamesTest {
    final Set<String> expectedNames = new HashSet<String>();
    final String newRouteName = "Ost - lang";
    final String routesDir = "/routes/";

    @Before
    public void init() {
        LogHelper.disableLogs();

        RouteStore routeStore = new RouteStore(FileUtils.toFile(GetAllRouteNamesTest.class.getResource(routesDir)));

        expectedNames.add("Nord - kurz");
        expectedNames.add("Ost - lang");

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setRouteStore(routeStore)
        .build();

        client = new Client(server);
    }

    @Test
    public void test() throws IOException, BadArgumentException {
        Message returnMessage = client.getAllRouteNames();
        assertTrue(returnMessage.getType() == MessageType.RESULT);
        RouteNamesMessage routeNamesMessage = ((CallResultMessage)returnMessage).getPayload(RouteNamesMessage.class);
        assertEquals(2, routeNamesMessage.rna.length);
        assertEquals("Ost - lang", routeNamesMessage.rna[0]);
        assertEquals("Nord - kurz", routeNamesMessage.rna[1]);

    }


    private Client client;
}
