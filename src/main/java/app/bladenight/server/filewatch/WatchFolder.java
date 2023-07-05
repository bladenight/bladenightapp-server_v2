package app.bladenight.server.filewatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class WatchFolder {
    private static final Logger logger = LogManager.getLogger(WatchFolder.class);

    public static void watchFolder(Path directory, FileWatcherCallback fileWatcherCallback) {

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            while (true) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        fileWatcherCallback.fileWatcherCallback();
                        logger.info("A new file is created : " + fileName);
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        fileWatcherCallback.fileWatcherCallback();
                        logger.info("A file has been deleted: " + fileName);
                    }
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        fileWatcherCallback.fileWatcherCallback();
                        logger.info("A file has been modified: " + fileName);
                    }

                }

                boolean valid = watchKey.reset();
                if (!valid) {
                    break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
