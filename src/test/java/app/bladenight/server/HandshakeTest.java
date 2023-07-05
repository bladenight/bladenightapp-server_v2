package app.bladenight.server;

import app.bladenight.common.network.messages.HandShakeResultMessage;
import app.bladenight.common.network.messages.RouteMessage;
import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.wampv2.server.messages.CallErrorMessage;
import app.bladenight.wampv2.server.messages.CallResultMessage;
import app.bladenight.wampv2.server.messages.Message;
import app.bladenight.common.network.BladenightError;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import app.bladenight.wampv2.server.messages.MessageType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HandshakeTest {

    @Before
    public void init() throws ParseException {
        LogHelper.disableLogs();
        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
                .setMinimumClientBuildNumber(minClientBuildNumber)
                .build();
        client = new Client(server);
    }

    @Test
    public void validBuildNumber() throws IOException, BadArgumentException {
        Message returnedMessage = client.shakeHands("deviceid", minClientBuildNumber, "manufacturer", "model", "4.0.0");
        assertTrue(MessageType.RESULT.equals(returnedMessage.getType()));
    }

    @Test
    public void outdatedBuildNumber() throws IOException, BadArgumentException {
        Message returnedMessage = client.shakeHands("deviceid", minClientBuildNumber - 1, "manufacturer", "model", "4.0.0");
        assertEquals(MessageType.RESULT, returnedMessage.getType());
        HandShakeResultMessage callresultMessage = ((CallResultMessage) returnedMessage).getPayload(HandShakeResultMessage.class);
        assert (callresultMessage != null);
        assertEquals(false, callresultMessage.sta);

    }

    private Client client;
    private final int minClientBuildNumber = 2301;
}
