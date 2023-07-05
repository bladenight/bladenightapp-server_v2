package app.bladenight.server;

import app.bladenight.common.events.EventList;
import app.bladenight.common.imagesandlinks.ImagesAndLinksList;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.server.rpchandlers.*;
import app.bladenight.wampv2.server.WampBnServerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Optional;

public class BladenightWampServerMain extends WampBnServerImpl {

    public RelationshipStore relationshipStore;

    private int minClientBuildNumber;
    private Procession procession;
    private EventList eventList;
    private RouteStore routeStore;
    private PasswordSafe passwordSafe;
    private ImagesAndLinksList imagesAndLinkList;
    private final ArrayList<Long> subscribers = new ArrayList<>();

    private static Logger log;

    static public class Builder {

        Builder() {
            this.bladenightWampServerMain = new BladenightWampServerMain();
        }

        public Builder setMinimumClientBuildNumber(int minClientBuildNumber) {
            bladenightWampServerMain.minClientBuildNumber = minClientBuildNumber;
            return this;
        }

        public Builder setProcession(Procession procession) {
            bladenightWampServerMain.procession = procession;
            return this;
        }

        public Builder setEventList(EventList eventList) {
            bladenightWampServerMain.eventList = eventList;
            return this;
        }

        public Builder setRouteStore(RouteStore routeStore) {
            bladenightWampServerMain.routeStore = routeStore;
            return this;
        }

        public Builder setPasswordSafe(PasswordSafe passwordSafe) {
            bladenightWampServerMain.passwordSafe = passwordSafe;
            return this;
        }

        public Builder setRelationshipStore(RelationshipStore relationshipStore) {
            bladenightWampServerMain.relationshipStore = relationshipStore;
            return this;
        }

        public void setImagesAndLinks(ImagesAndLinksList imagesAndLinkList) {
            bladenightWampServerMain.imagesAndLinkList = imagesAndLinkList;
        }


        public BladenightWampServerMain build() {
            bladenightWampServerMain.register();
            return bladenightWampServerMain;
        }


        private final BladenightWampServerMain bladenightWampServerMain;
    }

    private BladenightWampServerMain() {
    }

    public void setMinimumClientBuildNumber(int minClientBuildNumber) {
        this.minClientBuildNumber = minClientBuildNumber;
    }

    void register() {
        getLog().debug("Registering RPC handlers...");
        registerRpcHandler(BladenightUrl.GET_ACTIVE_EVENT.getText(), new RpcHandlerGetActiveEvent(eventList));
        registerRpcHandler(BladenightUrl.GET_ALL_EVENTS.getText(), new RpcHandlerGetAllEvents(eventList));
        registerRpcHandler(BladenightUrl.GET_ACTIVE_ROUTE.getText(), new RpcHandlerGetActiveRoute(procession));
        registerRpcHandler(BladenightUrl.GET_ROUTE.getText(), new RpcHandlerGetRoute(routeStore));
        registerRpcHandler(BladenightUrl.GET_ALL_PARTICIPANTS.getText(), new RpcHandlerGetAllParticipants(procession));
        registerRpcHandler(BladenightUrl.GET_REALTIME_UPDATE.getText(), new RpcHandlerGetRealtimeUpdate(procession, Optional.ofNullable(relationshipStore), true));
        registerRpcHandler(BladenightUrl.CREATE_RELATIONSHIP.getText(), new RpcHandlerCreateRelationship(relationshipStore));
        registerRpcHandler(BladenightUrl.SET_ACTIVE_ROUTE.getText(), new RpcHandlerSetActiveRoute(eventList, procession, routeStore, passwordSafe));
        registerRpcHandler(BladenightUrl.SET_ACTIVE_STATUS.getText(), new RpcHandlerSetActiveStatus(eventList,procession, passwordSafe));
        registerRpcHandler(BladenightUrl.GET_ALL_ROUTE_NAMES.getText(), new RpcHandlerGetAllRouteNames(routeStore));
        registerRpcHandler(BladenightUrl.VERIFY_ADMIN_PASSWORD.getText(), new RpcHandlerVerifyAdminPassword(passwordSafe));
        registerRpcHandler(BladenightUrl.GET_FRIENDS.getText(), new RpcHandlerGetFriends(relationshipStore, procession));
        registerRpcHandler(BladenightUrl.DELETE_RELATIONSHIP.getText(), new RpcHandlerDeleteRelationship(relationshipStore));
        registerRpcHandler(BladenightUrl.SET_MIN_POSITION.getText(), new RpcHandlerSetMinimumLinearPosition(passwordSafe));
        registerRpcHandler(BladenightUrl.KILL_SERVER.getText(), new RpcHandlerKillServer(passwordSafe));
        registerRpcHandler(BladenightUrl.SHAKE_HANDS.getText(), new RpcHandlerHandshake(minClientBuildNumber));
        registerRpcHandler(BladenightUrl.SHAKE_HANDS2.getText(), new RpcHandlerHandshake(minClientBuildNumber));
        registerRpcHandler(BladenightUrl.GET_IMAGES_AND_LINKS.getText(), new RPCHandlerGetExternalLinksAndImages(imagesAndLinkList));
        registerRpcHandler(BladenightUrl.SUBSCRIBE_MESSAGE.getText(), new RPCHandlerSubscribe(subscribers, "test"));

    }

    public static void setLog(Logger log) {
        BladenightWampServerMain.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(BladenightWampServerMain.class.getName());
        return log;
    }
}
