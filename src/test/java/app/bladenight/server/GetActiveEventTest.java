package app.bladenight.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.common.events.Event;
import app.bladenight.common.events.Event.EventStatus;
import app.bladenight.common.events.EventList;
import app.bladenight.common.network.messages.EventMessage;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;

public class GetActiveEventTest {

    @Before
    public void init() throws ParseException {
        LogHelper.disableLogs();

        eventList = new EventList();
        eventList.addEvent(new Event.Builder()
        .setStartDate("2023-06-01T21:00")
        .setRouteName("route 1")
        .setDurationInMinutes(60)
        .setStatus(EventStatus.CANCELLED)
        .build());
        eventList.addEvent(new Event.Builder()
        .setStartDate("2023-06-08T21:00")
        .setRouteName("route 2")
        .setDurationInMinutes(120)
        .setStatus(EventStatus.CONFIRMED)
        .build());

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
            .setEventList(eventList)
            .build();

        client = new Client(server);

    }

    @Test
    public void test() throws IOException, BadArgumentException {
        EventMessage data = client.getActiveEvent();
        //if event outdate return outdateEvent as result
        assert(data.toEvent().getStatus()!=EventStatus.NOEVENTPLANNED):"testevents are too old, no new Event planned";

        assertEquals(data.toEvent(), eventList.get(0));

    }

    private EventList eventList;
    private Client client;
}
