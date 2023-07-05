package app.bladenightapp.testutils;

import app.bladenight.common.events.EventList;
import app.bladenight.common.procession.HeadAndTailComputer;
import app.bladenight.common.procession.ParticipantUpdater;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.procession.TravelTimeComputer;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenight.common.routes.Route;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.server.BladenightWampServerMain;
import app.bladenight.server.rpchandlers.RpcHandlerCreateRelationship;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHelper {

    public static void disableLogs() {
        Logger log = LogManager.getLogger(LogHelper.class);
        BladenightWampServerMain.setLog(log);
        RelationshipStore.setLog(log);
        RpcHandlerCreateRelationship.setLog(log);
        Route.setLog(log);
        RouteStore.setLog(log);
        Procession.setLog(log);
        HeadAndTailComputer.setLog(log);
        ParticipantUpdater.setLog(log);
        TravelTimeComputer.setLog(log);
        EventList.setLog(log);
    }



}
