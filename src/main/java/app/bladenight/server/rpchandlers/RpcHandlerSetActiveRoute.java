package app.bladenight.server.rpchandlers;

import app.bladenight.common.events.EventList;
import app.bladenight.common.network.BladenightError;
import app.bladenight.common.network.messages.SetActiveRouteMessage;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.routes.Route;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.wampv2.server.RpcCall;
import app.bladenight.wampv2.server.RpcHandler;
import dev.loqo71la.haversine.Coordinate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class RpcHandlerSetActiveRoute extends RpcHandler {

    private final PasswordSafe passwordSafe;

    public RpcHandlerSetActiveRoute(EventList eventList, Procession procession, RouteStore routeStore, PasswordSafe passwordSafe) {
        this.procession = procession;
        this.routeStore = routeStore;
        this.eventList = eventList;
        this.passwordSafe = passwordSafe;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        SetActiveRouteMessage msg = rpcCall.getInput(SetActiveRouteMessage.class);

        if (msg == null) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
            return;
        }
        if (!msg.verify(passwordSafe.getAdminPassword(), 12 * 3600 * 1000)) {
            rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + msg);
            return;
        }
        String newRouteName = msg.getRouteName();
        if (newRouteName == null) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Input route name = null");
            return;
        }
        Route newRoute = routeStore.getRoute(newRouteName);
        if (newRoute == null) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Unknown route: " + newRouteName);
            return;
        }

        procession.setRoute(newRoute);
        long routeLength = calcRouteLength(newRoute.getNodesLatLong());
        eventList.setNextRoute(newRouteName,routeLength);
        getLog().info("Set new route to:" + newRouteName + " by did:" + msg.did);
        try {
            eventList.write();
        } catch (IOException e) {
            getLog().error("Failed to save events: " + e);
        }
    }

    private long calcRouteLength(List<Route.LatLong> routePoints) {
        if (routePoints.size() <2) return 0;
        double sumDistance = 0;

        for (int i = 0; i < routePoints.size() - 1; i++) {
            Coordinate point1 = new Coordinate(routePoints.get(i).lat, routePoints.get(i).lon);
            Coordinate point2 = new Coordinate(routePoints.get(i + 1).lat, routePoints.get(i + 1).lon);
            sumDistance += haversine(point1.getLatitude(),point1.getLongitude(), point2.getLatitude(), point2.getLongitude());
        }

        return (long) sumDistance;
    }

    ///Calculate distance between 2 LatLng
    static double haversine(double lat1, double lon1,
                            double lat2, double lon2)
    {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c *1000;
    }

    // Driver Code
    public static void main(String[] args)
    {
        double lat1 = 51.5007;
        double lon1 = 0.1246;
        double lat2 = 40.6892;
        double lon2 = 74.0445;
        System.out.println(haversine(lat1, lon1, lat2, lon2) + " K.M.");
    }

    private static Logger log;

    public static void setLog(Logger log) {
        RpcHandlerSetActiveRoute.log = log;
    }

    protected static Logger getLog() {
        if (log == null)
            log = LogManager.getLogger(RpcHandlerSetActiveRoute.class.getName());
        return log;
    }

    private final Procession procession;
    private final RouteStore routeStore;
    private final EventList eventList;
}
