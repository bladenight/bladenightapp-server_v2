package app.bladenight.server;

import app.bladenight.wampv2.server.exceptions.BadArgumentException;
import app.bladenight.common.security.PasswordSafe;
import app.bladenightapp.testutils.Client;
import app.bladenightapp.testutils.LogHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class VerifyAdminPasswordTest {

    @Before
    public void init() throws IOException {
        LogHelper.disableLogs();

        passwordSafe = new PasswordSafe();
        passwordSafe.setAdminPassword(password);

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setPasswordSafe(passwordSafe)
        .build();

        client = new Client(server);

    }

    @Test
    public void verifyPassword() throws IOException, BadArgumentException {
        assertTrue(client.verifyPasswordAgainstServer(password));
        assertTrue(! client.verifyPasswordAgainstServer("invalid password"));
    }

    private Client client;
    private PasswordSafe passwordSafe;
    final static String password = UUID.randomUUID().toString();
}
