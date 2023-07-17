package pr.iceworld.fernando.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperLock implements Watcher {

    private static final String ZOOKEEPER_CONNECTION_STRING = "192.168.79.177:2181";
    private static final int SESSION_TIMEOUT = 5000;
    private static final String LOCK_ROOT_PATH = "/locks";
    private static final String LOCK_NAME = "overselling_lock_";
    private ZooKeeper zooKeeper;
    private String lockPath;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public ZooKeeperLock() throws IOException, InterruptedException, KeeperException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_CONNECTION_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        Stat lockRootStat = zooKeeper.exists(LOCK_ROOT_PATH, false);
        if (lockRootStat == null) {
            zooKeeper.create(LOCK_ROOT_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }

    public void lock() throws KeeperException, InterruptedException {
        String lockName = LOCK_NAME;
        while (true) {
            String lockPath = zooKeeper.create(LOCK_ROOT_PATH + "/" + lockName, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> lockNodes = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
            Collections.sort(lockNodes);
            int index = lockNodes.indexOf(lockPath.substring(LOCK_ROOT_PATH.length() + 1));
            if (index == 0) {
                return;
            }
            String previousLockPath = lockNodes.get(index - 1);
            final CountDownLatch latch = new CountDownLatch(1);
            Stat previousLockStat = zooKeeper.exists(LOCK_ROOT_PATH + "/" + previousLockPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            });
            if (previousLockStat != null) {
                latch.await();
            }
        }
    }

    public void unlock() throws KeeperException, InterruptedException {
        zooKeeper.delete(lockPath, -1);
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

}
