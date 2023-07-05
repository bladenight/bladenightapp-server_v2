package app.bladenight.server;

import app.bladenight.common.events.Event;
import app.bladenight.common.events.Event.EventStatus;
import app.bladenight.common.events.EventList;
import app.bladenight.common.imagesandlinks.ImageAndLink;
import app.bladenight.common.imagesandlinks.ImagesAndLinksList;
import app.bladenight.common.keyvaluestore.KeyValueStoreSingleton;
import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.persistence.ListPersistor;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.procession.tasks.ComputeScheduler;
import app.bladenight.common.procession.tasks.ParticipantCollector;
import app.bladenight.common.procession.tasks.ProcessionLogger;
import app.bladenight.common.protocol.Protocol;
import app.bladenight.common.relationships.Relationship;
import app.bladenight.common.relationships.RelationshipStore;
import app.bladenight.common.relationships.tasks.RelationshipCollector;
import app.bladenight.common.routes.RouteStore;
import app.bladenight.common.security.PasswordSafe;
import app.bladenight.wampv2.server.TextFrameEavesdropper;
import app.bladenight.wampv2.server.WampBnServer;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.UUID;

public class App {

    public static WampBnServer wampBnServer;
    static ContextHandlerCollection _mainJettyServerServerHandlers = new ContextHandlerCollection();
    static ContextHandlerCollection _auxJettyServerServerHandlers = new ContextHandlerCollection();
    //static ExecutorService watchConfigMainPathService = null;
    //static ExecutorService watchRoutesService = null;
    //static ExecutorService watchEventsService = null;

    static Thread _routeupdaterThread = null;
    static Thread _computeSchedulerThread = null;

    static Server _auxServer = null;
    static Server _mainServer = null;

    public static void loadLog4j2Config(String logConfigurationFile) {
        try {
            InputStream inputStream = new FileInputStream(logConfigurationFile);
            ConfigurationSource source = new ConfigurationSource(inputStream);
            Configurator.initialize(null, source);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Logger logger = null;//LogManager.getLogger(App.class);

    public static void main(String[] args) {
        //org.slf4j.impl.StaticLoggerBinder.getSingleton();
        initializeApplicationConfiguration();
        initializeLogger();
        initStoresAndComputing();
        //Watch for changes in config and Route files - update on changes without Restart
        //Error calls often and for each file
        //startRouteStoreListWatcher();
        //startEventsListWatcher();
        //startWatchConfigFile();
        logger.info("Bladenight server started successfully");
    }

    private static void initStoresAndComputing() {
        initializeApplicationConfiguration();
        RouteStore routeStore = initializeRouteStore();
        EventList eventList = initializeEventsList();
        Procession procession = initializeProcession(eventList, routeStore);
        RelationshipStore relationshipStore = initializeRelationshipStore();
        PasswordSafe passwordSafe = initializePasswordSafe();
        ImagesAndLinksList imagesAndLinksList = initializeImagesAndLinks();

        // The active route might change for different reasons, for instance if the current event is over, or
        // if the route of the next/current event is changed. To keep the procession up-to-date, start a thread
        // that checks and updates periodically. This will not win a design beauty contest but does the job.
        _routeupdaterThread = new Thread(new RouteUpdater(procession, routeStore, eventList, 5000));
        _routeupdaterThread.start();

        // Start a thread that triggers the computation of the procession regularly
        _computeSchedulerThread = new Thread(new ComputeScheduler(procession, 1000));
        _computeSchedulerThread.start();

        BladenightWampServerMain.Builder mainBuilder = new BladenightWampServerMain.Builder();
        mainBuilder.setRouteStore(routeStore)
                .setEventList(eventList)
                .setProcession(procession)
                .setRelationshipStore(relationshipStore)
                .setPasswordSafe(passwordSafe)
                .setImagesAndLinks(imagesAndLinksList);


        initializeMinClientVersion(mainBuilder);

        BladenightWampServerAux.Builder auxBuilder = new BladenightWampServerAux.Builder();
        auxBuilder.setEventList(eventList)
                .setProcession(procession)
                .setRouteStore(routeStore)
                .setImagesAndLinks(imagesAndLinksList);

        tryStartAndJoinMainServer(mainBuilder);
        tryStartAndJoinAuxMainServer(auxBuilder);
    }

    public static void tryStartAndJoinMainServer(BladenightWampServerMain.Builder mainBuilder) {
        try {
            startAndJoinMainJettyServer(mainBuilder);
        } catch (Exception e) {
            logger.error("Failed to start main server", e);
        }

    }

    public static void tryStartAndJoinAuxMainServer(BladenightWampServerAux.Builder auxBuilder) {
        try {
            startAndJoinAuxJettyServer(auxBuilder);
        } catch (Exception e) {
            logger.error("Failed to start aux server", e);
        }

    }

    public static void startAndJoinMainJettyServer(BladenightWampServerMain.Builder mainBuilder) throws Exception {
        try {
            wampBnServer = mainBuilder.build();
            initializeProtocol(wampBnServer, "bnserver.network.main.protocol.path");

            _mainServer = createMainJettyServer();

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            // Add websocket servlet
            ServletHolder servletHolder = new ServletHolder("wsbn", new BladenightSocketServletMain(wampBnServer));
            context.addServlet(servletHolder, "/ws");
            Handler staticHandler = addStaticHttpDocs("bnserver.network.main.httpdocs", "bnserver.network.main.httpdocs.landing", "bnserver.network.main.httpdocs.landingpath");
            if (staticHandler != null) {
                _mainJettyServerServerHandlers.setHandlers(new Handler[]{context, staticHandler});
            } else {
                _mainJettyServerServerHandlers.setHandlers(new Handler[]{context, new DefaultHandler()});
            }
            _mainServer.setHandler(_mainJettyServerServerHandlers);
            _mainServer.start();
            //server.join(); //blocks
            logger.info("The main server is now listening on port " + getMainPort());
            logger.info("SSL is" + (useSslForMainPort() ? " " : " not ") + "activated");
        } catch (Exception e) {
            logger.error("Failed to start main server: " + e);
            System.out.println("Failed to start main server: " + e);
            System.exit(1);
        }


    }

    public static void startAndJoinAuxJettyServer(BladenightWampServerAux.Builder auxBuilder) {
        try {
            _auxServer = createAuxJettySSLServer();
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            // Add websocket servlet
            ServletHolder wsHolder = new ServletHolder("bnws", new BladenightSocketServletAux(wampBnServer));
            context.addServlet(wsHolder, "/ws");

            Handler staticHandler = addStaticHttpDocs("bnserver.network.aux.httpdocs", "bnserver.network.main.aux.landing", "bnserver.network.main.aux.landingpath");
            if (staticHandler != null) {
                _auxJettyServerServerHandlers.setHandlers(new Handler[]{context, staticHandler});
            } else {
                _auxJettyServerServerHandlers.setHandlers(new Handler[]{context, new DefaultHandler()});

            }
            _auxServer.setHandler(_auxJettyServerServerHandlers);
            _auxServer.start();
            //server.join();
            logger.info("The aux server is now listening on port " + getAuxPort());
        } catch (Exception e) {
            logger.error("Failed to start aux server: " + e);
            System.exit(1);
        }
    }

    public static void addWebSocket(final Class<?> webSocket, String pathSpec) {
        WebSocketHandler wsHandler = new WebSocketHandler() {

            @Override
            public void configure(WebSocketServletFactory webSocketServletFactory) {
                webSocketServletFactory.register(webSocket);
            }
        };
        ContextHandler wsContextHandler = new ContextHandler();
        wsContextHandler.setHandler(wsHandler);
        wsContextHandler.setContextPath(pathSpec);  // this context path doesn't work ftm
        //webSocketHandlerList.add(wsHandler);
    }

    public static Handler addStaticHttpDocs(String httpdocsConfigKey, String landingPageConfigKey, String wwwPathConfigKey) throws Exception {
        String httpdocsPath = KeyValueStoreSingleton.getPath(httpdocsConfigKey, null);
        String landingPage = KeyValueStoreSingleton.getString(landingPageConfigKey, "index.html");
        String wwwPath = KeyValueStoreSingleton.getString(wwwPathConfigKey, "www");

        if (httpdocsPath == null) {
            logger.info("No httpdocs path has been set (" + httpdocsConfigKey + ")");
        } else if (!new File(httpdocsPath).isDirectory()) {
            logger.fatal("The provided httpdocs path is not a valid directory: " + httpdocsPath);
            System.exit(1);
        } else {
            logger.info("Setting up HTTP(s) doc handler: " + httpdocsConfigKey + "=" + httpdocsPath);
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setWelcomeFiles(new String[]{landingPage});
            resourceHandler.setDirectoriesListed(true);
            resourceHandler.setResourceBase(httpdocsPath);

            ServletContextHandler resCtxHandler = new ServletContextHandler();
            resCtxHandler.setContextPath("/" + wwwPath); // '/' not working on https
            resCtxHandler.insertHandler(resourceHandler);

            return resCtxHandler;
            //resCtx.start();
            //resourceHandler.setHandler(resourceHandler);
        }
        return null;
    }

    private static Server createAuxJettySSLServer() {
        if (!useSslForAuxPort()) {
            Server server = new Server(getAuxPort());
            server.setHandler(_auxJettyServerServerHandlers);
            return server;
        }

        Server server = new Server();

        server.setHandler(_auxJettyServerServerHandlers);
        SslContextFactory contextFactory = new SslContextFactory.Server();
        String storePath = KeyValueStoreSingleton.getPath("bnserver.network.main.ssl.keystore.path");
        contextFactory.setKeyStorePath(storePath);
        if (storePath.endsWith("p12") || storePath.endsWith("pkcs12")) {
            contextFactory.setKeyStoreType("PKCS12");
        }
        String kpwd=KeyValueStoreSingleton.getString("bnserver.network.aux.ssl.keystore.password");
        if (kpwd==null){
            throw new InvalidParameterException("Config error bnserver.network.aux.ssl.keystore.password is null or not set");
        }
        contextFactory.setKeyStorePassword(kpwd);
        contextFactory.setKeyManagerPassword(kpwd);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(contextFactory, org.eclipse.jetty.http.HttpVersion.HTTP_1_1.toString());
        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(getMainPort());
        config.setOutputBufferSize(32786);
        config.setRequestHeaderSize(8192);
        config.setResponseHeaderSize(8192);

        HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);
        config.addCustomizer(new SecureRequestCustomizer());
        ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);

        connector.setPort(getAuxPort());
        server.addConnector(connector);
        return server;
    }

    private static Server createMainJettyServer() {
        if (!useSslForMainPort()) {
            Server server = new Server(getMainPort());
            server.setHandler(_mainJettyServerServerHandlers);
            return server;
        }

        // solution works for  wss://localhost:xxxx/ws
        Server server = new Server();

        server.setHandler(_mainJettyServerServerHandlers);
        SslContextFactory contextFactory = new SslContextFactory.Server();
        String storePath = KeyValueStoreSingleton.getPath("bnserver.network.main.ssl.keystore.path");
        contextFactory.setKeyStorePath(storePath);
        if (storePath.endsWith("p12") || storePath.endsWith("pkcs12")) {
            contextFactory.setKeyStoreType("PKCS12");
        }
        String kpwd=KeyValueStoreSingleton.getString("bnserver.network.main.ssl.keystore.password");
        if (kpwd==null){
            throw new InvalidParameterException("Config error bnserver.network.main.ssl.keystore.password is null");
        }
        contextFactory.setKeyStorePassword(kpwd);
        contextFactory.setKeyManagerPassword(kpwd);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(contextFactory, org.eclipse.jetty.http.HttpVersion.HTTP_1_1.toString());
        /*unsafe
        try {
            FileWatcher.onFileChange(Paths.get(URI.create(contextFactory.getKeyStorePath())), () -> contextFactory.reload(scf -> logger.info("Reloaded MainSSLContext keySore")));
        } catch (IOException e) {
            logger.error("can't Reload MainSSLContext");
        }*/
        HttpConfiguration config = new HttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(getMainPort());
        config.setOutputBufferSize(32786);
        config.setRequestHeaderSize(8192);
        config.setResponseHeaderSize(8192);

        HttpConfiguration sslConfiguration = new HttpConfiguration(config);
        sslConfiguration.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);
        config.addCustomizer(new SecureRequestCustomizer());
        ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);

        connector.setPort(getMainPort());
        server.addConnector(connector);
        return server;
    }

    private static boolean useSslForMainPort() {
        return KeyValueStoreSingleton.getInt("bnserver.network.main.ssl.enable", 0) != 0;
    }

    private static boolean useSslForAuxPort() {
        return KeyValueStoreSingleton.getInt("bnserver.network.aux.ssl.enable", 0) != 0;
    }

    public static void initializeApplicationConfiguration() {
        String propertyName = "bnserver.configuration";
        String path = System.getProperty(propertyName);
        if (path == null) {
            System.err.println("fatal error: please provide the path of the configuration file as Java system property (-D" + propertyName + "=/path/...)");
            System.exit(1);
        }

        if (!KeyValueStoreSingleton.readFromFile(path)) {
            System.err.println("fatal error: Unable to read the configuration file at:\n" + path + "\n");
            System.exit(1);
        }
    }

    private static Integer getMainPort() {
        String key = "bnserver.network.main.port";
        int port = 0;

        try {
            port = Integer.parseInt(KeyValueStoreSingleton.getString(key));
        } catch (Exception e) {
            logger.error(e);
        }
        if (port == 0) {
            logger.error("Please provide a TCP port to bind the main server (" + key + ")");
            System.exit(1);
        }
        return port;
    }

    private static Integer getAuxPort() {
        String key = "bnserver.network.aux.port";
        int port = 0;

        try {
            port = Integer.parseInt(KeyValueStoreSingleton.getString(key));
        } catch (Exception e) {
            logger.error(e);
        }
        if (port == 0) {
            logger.error("Please provide a TCP port to bind the aux server (" + key + ")");
            System.exit(1);
        }
        return port;
    }

    private static void initializeLogger() {
        String log4jConfiguration = System.getProperty("log4j.configurationFile");
        //final String property = System.getProperty("java.class.path");
        if (log4jConfiguration == null || logger == null) {
            log4jConfiguration = KeyValueStoreSingleton.getPath("bnserver.log4j2.configurationpath", "log4j2.xml");
            System.setProperty("log4j.configurationFile", log4jConfiguration);
            logger = LogManager.getLogger(App.class.getName());
        }
        logger.info("config: logger initialized, log4j.properties=" + log4jConfiguration);
    }

    private static void startRouteStoreListWatcher() {
        String configurationKey = "bnserver.routes.path";
        String asString = KeyValueStoreSingleton.getPath(configurationKey);
        File asFile = new File(asString);
        if (!asFile.isDirectory()) {
            logger.error("Invalid directory for the routes: " + configurationKey + "=" + asString);
            return;
        }
        //watchRoutesService = new WatchDirectoryExecutor().startFolderWatcher(Paths.get(asString), () -> restartServer());
    }

    public static void startWatchConfigFile() {
        String asString = KeyValueStoreSingleton.getCurrentStore().getBasePath();
        File asFile = new File(asString);
        if (!asFile.isDirectory()) {
            logger.error("Invalid main directory for app: " + asString);
            return;
        }
        // watchConfigMainPathService = new WatchDirectoryExecutor().startFolderWatcher(Paths.get(asString), () -> restartServer());

    }

    private static void restartServer() {
        try {
            _auxServer.stop();
            _mainServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        _computeSchedulerThread.interrupt();
        _routeupdaterThread.interrupt();
        _auxServer = null;
        _mainServer = null;
        initStoresAndComputing();
    }

    private static RouteStore initializeRouteStore() {
        String configurationKey = "bnserver.routes.path";
        String asString = KeyValueStoreSingleton.getPath(configurationKey);
        File asFile = new File(asString);
        if (!asFile.isDirectory()) {
            logger.error("Invalid path for route files: " + configurationKey + "=" + asString);
        }
        RouteStore routeStore = new RouteStore(asFile);
        logger.info("Config: routeStorePath=" + asString);
        logger.info("Route store initialized, there are " + routeStore.getAvailableRoutes().size() + " different app.bladenight.routes available.");
        return routeStore;
    }

    private static void startEventsListWatcher() {
        String configurationKey = "bnserver.events.path";
        String asString = KeyValueStoreSingleton.getPath(configurationKey);
        File asFile = new File(asString);
        if (!asFile.isDirectory()) {
            logger.error("Invalid directory for the events: " + configurationKey + "=" + asString);
            return;
        }
        //watchEventsService = new WatchDirectoryExecutor().startFolderWatcher(Paths.get(asString), () -> restartServer());
    }

    private static EventList initializeEventsList() {
        String configurationKey = "bnserver.events.path";
        String asString = KeyValueStoreSingleton.getPath(configurationKey);
        File asFile = new File(asString);
        if (!asFile.isDirectory()) {
            logger.error("Invalid directory for the events: " + configurationKey + "=" + asString);
        }
        logger.info("Config: eventStorePath=" + asString);

        ListPersistor<Event> persistor = new ListPersistor<Event>(Event.class, asFile);

        EventList eventList = new EventList();
        eventList.setPersistor(persistor);
        try {
            eventList.read();
        } catch (Exception e) {
            logger.error("Failed to read events: " + e);
            System.exit(1);
        }
        logger.info("Events list initialized with " + eventList.size() + " events.");


        String routeToScheduleNow = KeyValueStoreSingleton.getString("bnserver.events.now.route");
        if (routeToScheduleNow != null) {
            Event event = new Event();
            event.setDuration(new Duration(120 * 60 * 1000));
            event.setRouteName(routeToScheduleNow);
            event.setStartDate(new DateTime());
            event.setStatus(EventStatus.CONFIRMED);

            for (Event eventFromList: eventList
                 ) {
                if(eventFromList.getStartDate().equals(eventFromList.getStartDate())){
                    //throws Exception in ListPersistor when event on same day
                    logger.warn("Could not add immediate event with route: " + routeToScheduleNow);

                    return eventList;
                }
            }

            eventList.addEvent(event);
            logger.info("Added immediate event with route: " + routeToScheduleNow);
        }

        return eventList;
    }

    private static Procession initializeProcession(EventList eventList, RouteStore routeStore) {
        Procession procession = new Procession();

        double smoothingFactor = KeyValueStoreSingleton.getDouble("bnserver.procession.smoothing", 0.0);
        procession.setUpdateSmoothingFactor(smoothingFactor);
        logger.info("Config: Procession smoothingFactor=" + smoothingFactor);

        double greediness = KeyValueStoreSingleton.getDouble("bnserver.procession.greediness", 5.0);
        procession.setProcessionGreediness(greediness);
        logger.info("Config: Procession greediness=" + greediness);

        initializeProcessionLogger(procession);

        initializeParticipantCollector(procession);

        return procession;
    }

    private static void initializeProcessionLogger(Procession procession) {
        String configurationKey = "bnserver.procession.logfile";
        String processionLogPath = KeyValueStoreSingleton.getPath(configurationKey);
        logger.info("Config: Procession log file=" + processionLogPath);
        if (processionLogPath != null)
            new Thread(new ProcessionLogger(new File(processionLogPath), procession, 1000)).start();
    }

    private static void initializeParticipantCollector(Procession procession) {
        long maxAbsoluteAge = KeyValueStoreSingleton.getLong("bnserver.procession.collector.absolute", 30000);
        double maxRelativeAgeFactor = KeyValueStoreSingleton.getDouble("bnserver.procession.collector.relative", 5.0);
        long period = KeyValueStoreSingleton.getLong("bnserver.procession.collector.period", 1000);

        logger.info("Config: Procession collector maxAbsoluteAge=" + maxAbsoluteAge);
        logger.info("Config: Procession collector maxRelativeAgeFactor=" + maxRelativeAgeFactor);
        logger.info("Config: Procession collector period=" + period);

        ParticipantCollector collector = new ParticipantCollector(procession);
        collector.setPeriod(period);
        collector.setMaxAbsoluteAge(maxAbsoluteAge);
        collector.setMaxRelativeAgeFactor(maxRelativeAgeFactor);

        new Thread(collector).start();
    }

    private static RelationshipStore initializeRelationshipStore() {
        RelationshipStore relationshipStore = new RelationshipStore();
        String configurationKey = "bnserver.relationships.path";
        String path = KeyValueStoreSingleton.getPath(configurationKey);
        if (path == null || !new File(path).isDirectory()) {
            logger.error(configurationKey + " in the configuration file needs to point to a valid directory: " + path);
            System.exit(1);
        }
        logger.info("Config: relationshipStorePath=" + path);
        ListPersistor<Relationship> persistor = new ListPersistor<>(Relationship.class, new File(path));
        relationshipStore.setPersistor(persistor);
        try {
            relationshipStore.read();
        } catch (Exception e) {
            logger.error("Failed to read relationships from " + path);
            System.exit(1);
        }

        long maxAge = KeyValueStoreSingleton.getLong("bnserver.relationships.collector.maxage", 3600 * 1000);
        long period = KeyValueStoreSingleton.getLong("bnserver.relationships.collector.period", 60 * 1000);

        logger.info("Config: Relationship collector: maxAge=" + maxAge);
        logger.info("Config: Relationship collector: period=" + period);

        RelationshipCollector collector = new RelationshipCollector(relationshipStore, period, maxAge);
        new Thread(collector).start();
        return relationshipStore;
    }

    private static PasswordSafe initializePasswordSafe() {
        PasswordSafe passwordSafe = new PasswordSafe();
        String configurationKey = "bnserver.admin.password";
        String password = KeyValueStoreSingleton.getString(configurationKey);
        if (password == null) {
            logger.warn(configurationKey + " is not set in the configuraiton file, defaulting to a random but safe value");
            password = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        }
        passwordSafe.setAdminPassword(password);
        return passwordSafe;
    }

    private static void initializeProtocol(WampBnServer server, String propertyKey) {
        try {
            initializeProtocolWithException(server, propertyKey);
        } catch (IOException e) {
            logger.error("Failed to open protocol file: " + e);
            System.exit(1);
        }
    }

    private static void initializeProtocolWithException(WampBnServer server, String propertyKey) throws IOException {
        String path = KeyValueStoreSingleton.getPath(propertyKey);
        if (path == null)
            return;
        File file = new File(path);
        FileUtils.forceMkdir(file.getParentFile());
        final Protocol protocol = new Protocol(file);
        final String configurationKey = "bnserver.client.privacy.improve";
        final boolean improvePrivacy = (KeyValueStoreSingleton.getLong(configurationKey, 0) > 0);
        logger.info("Config: Protocol: improve privacy=" + improvePrivacy);
        TextFrameEavesdropper incomingEavesdropper = new TextFrameEavesdropper() {
            @Override
            public void handler(String session, String frame) {
                if (improvePrivacy)
                    frame = removeCoordinates(frame);
                protocol.write("WAMPIN", session, frame);
            }

            private String removeCoordinates(String frame) {
                // If the user is participating in the event, we log the coordinates to be able
                // to replay the event afterwards.
                // If not, we discard them to improve his privacy
                if (!frame.contains(BladenightUrl.GET_REALTIME_UPDATE.getText()))
                    return frame;
                if (!frame.contains("\"par\":false"))
                    return frame;
                frame = frame.replaceAll("\"la\":[\\d.]+", "\"la\":-999");
                frame = frame.replaceAll("\"lo\":[\\d.]+", "\"lo\":-999");
                return frame;
            }
        };
        server.addIncomingFramesEavesdropper(incomingEavesdropper);
    }

    private static void initializeMinClientVersion(BladenightWampServerMain.Builder mainBuilder) {
        String configurationKey = "bnserver.client.build.min";
        int minClientBuild = KeyValueStoreSingleton.getInt(configurationKey, 0);
        if (minClientBuild > 0)
            mainBuilder.setMinimumClientBuildNumber(minClientBuild);
        logger.info("Config: minClientBuild=" + minClientBuild);
    }

    private static ImagesAndLinksList initializeImagesAndLinks() {
        try {
            String imagesAndLinksKey = "bnserver.client.imagesAndLinks";
            String imagesAndLinksPath = KeyValueStoreSingleton.getPath(imagesAndLinksKey, "");
            File ialFile = new File(imagesAndLinksPath.trim());
            if (!ialFile.canRead()) {
                logger.warn("No file not readable " + imagesAndLinksPath + "  configkey bnserver.client.imagesAndLinks were set or invalid!");
                return new ImagesAndLinksList();
            }
            String fileContent = FileUtils.readFileToString(ialFile, StandardCharsets.UTF_8);
            ImagesAndLinksList imagesAndLinksList = new ImagesAndLinksList();
            Gson gson = new Gson();

            ImageAndLink[] imageAndLinkArray = gson.fromJson(fileContent, ImageAndLink[].class);

            if (imageAndLinkArray.length == 0) {
                logger.warn("No images and links in  configkey bnserver.client.imagesAndLinks were set or invalid!");
            }
            for (ImageAndLink ial : imageAndLinkArray) {
                if (ial == null) continue;
                imagesAndLinksList.addImageAndLink(ial);
            }
            return imagesAndLinksList;

        } catch (Exception e) {
            logger.error("Failed to read images and links from  configkey bnserver.client.imagesAndLinks" + e.getLocalizedMessage());
            System.exit(1);
        }
        return new ImagesAndLinksList();
    }

}
