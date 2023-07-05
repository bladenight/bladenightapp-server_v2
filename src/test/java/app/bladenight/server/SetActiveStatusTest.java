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
import app.bladenight.common.network.messages.EventMessage.EventStatus;
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

public class SetActiveStatusTest {
    final String initialRouteName = "Nord - kurz";
    final String newRouteName = "Ost - lang";
    final String routesDir = "/routes/";
    final String adminPassword = "test1234";

    @Before
    public void init() throws IOException, InconsistencyException {
        LogHelper.disableLogs();

        RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveStatusTest.class.getResource(routesDir)));
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
    public void setActiveStatus() throws IOException, BadArgumentException {
        Message message = client.setActiveStatusTo(EventStatus.CAN, adminPassword);
        assertEquals(MessageType.RESULT, message.getType());
        verifyPersistency(Event.EventStatus.CANCELLED);
    }

    @Test
    public void setActiveStatusWithBadPassword() throws IOException, BadArgumentException {
        Message message = client.setActiveStatusTo(EventStatus.CAN, adminPassword + "-invalid");
        assertEquals(MessageType.ERROR, message.getType());
        CallErrorMessage errorMessage = (CallErrorMessage)message;
        assertEquals(BladenightError.INVALID_PASSWORD.getText(), errorMessage.getErrorUri());
        verifyPersistency(Event.EventStatus.CONFIRMED);
    }

    private void verifyPersistency(Event.EventStatus status) throws JsonSyntaxException, IOException {
        File file = new File(persistenceFolder, "2023-06-03.per");
        Event event = EventGsonHelper.getGson().fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), Event.class);
        assertEquals(status, event.getStatus());
    }

    public File createTemporaryFolder() throws IOException  {
        File file = File.createTempFile("tmpfolder", ".d");
        file.delete();
        file.mkdir();
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
        return file;
    }

    private Client client;
    private Route route;
    private Procession procession;
    private File persistenceFolder;
    private EventList eventList;
    private PasswordSafe passwordSafe;
}
