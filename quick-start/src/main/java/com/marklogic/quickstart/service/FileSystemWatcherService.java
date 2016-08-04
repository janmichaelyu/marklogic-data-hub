package com.marklogic.quickstart.service;

import com.marklogic.client.helper.LoggingObject;
import com.marklogic.hub.HubConfig;
import com.marklogic.quickstart.model.EnvironmentConfig;
import com.sun.nio.file.SensitivityWatchEventModifier;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Service
@Scope(proxyMode=ScopedProxyMode.TARGET_CLASS, value="session")
public class FileSystemWatcherService extends LoggingObject implements DisposableBean {

    @Autowired
    EnvironmentConfig envConfig;

    private WatchService watcher;
    private Thread watcherThread;
    private Map<WatchKey,Path> keys = new HashMap<>();

    private List<FileSystemEventListener> listeners = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    protected synchronized void onPostConstruct() throws Exception {
        watcher = FileSystems.getDefault().newWatchService();

        watcherThread = new DirectoryWatcherThread("directory-watcher-thread", envConfig.getMlSettings());
        watcherThread.start();
    }

    /**
     * Recursively listen for file system events in the specified path name.
     *
     * @param pathName
     * @throws IOException
     */
    public synchronized void unwatch(String pathName) throws IOException {
        unregisterAll(Paths.get(pathName));
    }

    public synchronized void watch(String pathName) throws IOException {
        registerAll(Paths.get(pathName));
    }

    public void addListener(FileSystemEventListener listener) {
        listeners.add(listener);
    }

    public boolean hasListener(FileSystemEventListener listener) {
        return listeners.contains(listener);
    }

    public void removeListener(FileSystemEventListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(HubConfig hubConfig, Path path, WatchEvent<Path> event) {
        // notify global listeners
        synchronized (listeners) {
            for (FileSystemEventListener listener : listeners) {
                try {
                    listener.onWatchEvent(hubConfig, path, event);
                }
                catch (Exception e) {
                    logger.error("Exception occured on listener", e);
                }
            }
        }
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
        if (logger.isInfoEnabled()) {
            Path prev = keys.get(key);
            if (prev == null) {
                logger.info("register: {}", dir);
            } else {
                if (!dir.equals(prev)) {
                    logger.info("update: {} -> {}", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    private void unregister(Path dir) throws IOException {
        Iterator<Map.Entry<WatchKey, Path>> it = keys.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<WatchKey, Path> entry = it.next();
            WatchKey key = entry.getKey();
            if (key.equals(dir)) {
                logger.info("unregister: {}", dir);
                key.cancel();
                it.remove();
            }
        }
    }

    /**
     * Register the given directory and all its sub-directories with the WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void unregisterAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                unregister(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public synchronized void destroy() throws Exception {
        watcher.close();
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private class DirectoryWatcherThread extends Thread {

        private HubConfig hubConfig;
        public DirectoryWatcherThread(String name, HubConfig hubConfig) {
            super(name);
            this.hubConfig = hubConfig;
        }

        @Override
        public void run() {
            for (;;) {
                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException | ClosedWatchServiceException e ) {
                    return;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    logger.info("WatchKey not recognized!!");
                    continue;
                }

                for (WatchEvent<?> event: key.pollEvents()) {
                    Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // Context for directory entry event is the file name of entry
                    WatchEvent<Path> ev = cast(event);
                    Path context = ev.context();
                    Path child = dir.resolve(context);

                    // print out event
                    logger.info("Event received: {} for: {}", event.kind().name(), child);

                    // notify listeners
                    notifyListeners(hubConfig, dir, ev);

                    // if directory is created, then register it and its sub-directories
                    // we are always listening recursively
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            logger.error("Cannot watch newly created directory: " + child.getFileName().toAbsolutePath(), x);
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }
}