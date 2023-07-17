package pr.iceworld.fernando.zk;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class OversellingService {

    private int inventory;
    private ZooKeeperLock lock;

    public OversellingService(int inventory) throws IOException, InterruptedException, KeeperException {
        this.inventory = inventory;
        this.lock = new ZooKeeperLock();
    }

    public boolean sell(int count) throws KeeperException, InterruptedException {
        lock.lock();
        if (inventory >= count) {
            inventory -= count;
            lock.unlock();
            return true;
        }
        lock.unlock();
        return false;
    }

    public void close() throws InterruptedException, KeeperException {
        lock.close();
    }

}
