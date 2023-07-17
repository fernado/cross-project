package pr.iceworld.fernando.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Comparator;
import java.util.List;

public class ZooKeeperDistributedLock {
    private static final String LOCK_ROOT_PATH = "/locks";
    private final ZooKeeper zooKeeper;
    private final String lockName;
    private String lockPath;
    private volatile boolean locked = false;

    public ZooKeeperDistributedLock(ZooKeeper zooKeeper, String lockName) {
        this.zooKeeper = zooKeeper;
        this.lockName = lockName;
    }

    public void lock() throws InterruptedException, KeeperException {
        if (locked) {
            throw new RuntimeException("Lock already acquired");
        }

        String lockFullPath = LOCK_ROOT_PATH + "/" + lockName;
        if (zooKeeper.exists(lockFullPath, false) == null) {
            zooKeeper.create(lockFullPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        lockPath = zooKeeper.create(lockFullPath + "/", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        while (true) {
            List<String> children = zooKeeper.getChildren(lockFullPath, false);

            String minChild = children.stream().min(Comparator.naturalOrder()).orElse("");
            String minChildPath = lockFullPath + "/" + minChild;

            if (lockPath.equals(minChildPath)) {
                locked = true;
                return;
            } else {
                Stat stat = zooKeeper.exists(minChildPath, true);
                if (stat != null) {
                    synchronized (this) {
                        wait();
                    }
                }
            }
        }
    }

    public void unlock() throws InterruptedException, KeeperException {
        if (!locked) {
            return;
        }

        zooKeeper.delete(lockPath, -1);
        locked = false;

        synchronized (this) {
            notifyAll();
        }
    }
}
