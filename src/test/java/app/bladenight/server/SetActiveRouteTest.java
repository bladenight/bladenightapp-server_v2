package app.bladenight.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallErrorMessage;
import app.bladenight.wampv2.server.messages.Message;
import app.bladenight.wampv2.server.messages.MessageType;
import com.google.gson.JsonSyntaxException;
import app.bladenight.common.events.Event;
import app.bladenight.common.events.EventGsonHelper;
import app.bladenight.common.events.EventList;
import app.bladenight.common.network.BladenightError;
import app.bladenight.common.persistence.InconsistencyException;
import app.bladenight.common.persistence.ListPersistor;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.routes.Route;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.common.security.PasswordSafe;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetActiveRouteTest {
    final String initialRouteName = "Nord - kurz";
    final String newRouteName = "Ost - lang";
    final String routesDir = "/routes/";
    final String adminPassword = "test1234";

    @Before
    public void init() throws IOException, InconsistencyException {
        LogHelper.disableLogs();

        RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveRouteTest.class.getResource(routesDir)));
        route = routeStore.getRoute(initialRouteName);
        assertEquals(initialRouteName, route.getName());

        File tmpFolder = createTemporaryFolder();
        persistenceFolder = new File(tmpFolder, "copy");
        File srcDir = FileUtils.toFile(EventList.class.getResource("/events/"));
        FileUtils.copyDirectory(srcDir, persistenceFolder);

        ListPersistor<Event> persistor = new ListPersistor<Event>(Event.class, persistenceFolder);

        eventList = new EventList();
        eventList.setPersistor(persistor);
        eventList.read();

        procession = new Procession();
        procession.setRoute(route);
        procession.setMaxComputeAge(0);

        passwordSafe = new PasswordSafe();
        passwordSafe.setAdminPassword(adminPassword);

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setProcession(procession)
        .setPasswordSafe(passwordSafe)
        .setEventList(eventList)
        .setRouteStore(routeStore)
        .build();

        client = new Client(server);
    }

    @Test
    public void setActiveRouteToValidRoute() throws IOException, BadArgumentException {
        Message returnMessage = client.setActiveRouteTo(newRouteName, adminPassword);
        assertTrue(returnMessage.getType() == MessageType.RESULT);
        Route newRoute = procession.getRoute();
        assertEquals(newRouteName, newRoute.getName());
        assertEquals(16727, newRoute.getLength(), 1);
        verifyPersistency(newRouteName);
    }

    @Test
    public void setActiveRouteToValidRouteWithInvalidPassword() throws IOException, BadArgumentException {
        Message returnMessage = client.setActiveRouteTo(newRouteName, adminPassword + "-invalid");
        assertTrue(returnMessage.getType() == MessageType.ERROR);
        CallErrorMessage errorMessage = (CallErrorMessage)returnMessage;
        assertEquals(BladenightError.INVALID_PASSWORD.getText(), errorMessage.getErrorUri());
        Route newRoute = procession.getRoute();
        assertEquals(initialRouteName, newRoute.getName());
        verifyPersistency(initialRouteName);
    }

    @Test
    public void setActiveRouteToUnavailableRoute() throws IOException, BadArgumentException {
        Message returnMessage = client.setActiveRouteTo(newRouteName+"-invalid", adminPassword);
        assertTrue(returnMessage.getType() == MessageType.ERROR);
        Route newRoute = procession.getRoute();
        assertEquals(initialRouteName, newRoute.getName());
        assertEquals(12605, newRoute.getLength(), 1);
    }

    @Test
    public void setActiveRouteToNullRoute() throws IOException, BadArgumentException {
        Message returnMessage = client.setActiveRouteTo(null, adminPassword);
        assertTrue(returnMessage.getType() == MessageType.ERROR);
        Route newRoute = procession.getRoute();
        assertEquals(initialRouteName, newRoute.getName());
        assertEquals(12605, newRoute.getLength(), 1);
    }


    public File createTemporaryFolder() throws IOException  {
        File file = File.createTempFile("tmpfolder", ".d");
        file.delete();
        file.mkdir();
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
        return file;
    }

    private void verifyPersistency(String routeName) throws JsonSyntaxException, IOException {
        File file = new File(persistenceFolder, "2023-06-03.per");
        Event event = EventGsonHelper.getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), Event.class);
        assertEquals(routeName, event.getRouteName());
    }




    private Route route;
    private Procession procession;
    private Client client;
    private File persistenceFolder;
    private EventList eventList;
    private PasswordSafe passwordSafe;
}
