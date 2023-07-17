package pr.iceworld.fernando.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperWatcherExample implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "192.168.79.177:2181";
    private static final int SESSION_TIMEOUT = 5000;
    private static final String NODE_PATH = "/test";

    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    public ZookeeperWatcherExample() throws IOException, InterruptedException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        connectedSignal.await();
    }

    public void createNode(String path, String data) throws KeeperException, InterruptedException {
        byte[] bytes = data.getBytes();
        this.zooKeeper.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public String getNodeData(String path) throws KeeperException, InterruptedException {
        System.out.println(">>> check exists -- 1");
        Stat stat = this.zooKeeper.exists(path, this);
        if (stat != null) {
            byte[] data = this.zooKeeper.getData(path, this, stat);
            return new String(data);
        }
        return null;
    }

    public void updateNodeData(String path, String data) throws KeeperException, InterruptedException {
        byte[] bytes = data.getBytes();
        Stat stat = this.zooKeeper.setData(path, bytes, -1);
        System.out.println(">>> Node data updated, new version: " + stat.getVersion());
    }

    public void deleteNode(String path) throws KeeperException, InterruptedException {
        System.out.println(">>> check exists -- 2");
        Stat stat = this.zooKeeper.exists(path, this);
        if (stat != null) {
            this.zooKeeper.delete(path, -1);
            System.out.println(">>> Node deleted");
        }
    }

    @Override
    public void process(WatchedEvent event) {

        System.out.println(">>> Event Received: " + event.getType());

        switch (event.getType()) {
            case None:
                System.out.println(">>> None Event");
                break;
            case NodeCreated:
                System.out.println(">>> Node Created Event");
                break;
            case NodeDeleted:
                System.out.println(">>> Node Deleted Event");
                break;
            case NodeDataChanged:
                System.out.println(">>> Node Data Changed Event");
                break;
            case NodeChildrenChanged:
                System.out.println(">>> Node Children Changed Event");
                break;
            default:
                break;
        }

        if (event.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
            System.out.println(">>> Connected to Zookeeper");
        } else if (event.getType() == Event.EventType.NodeDataChanged) {
            String path = event.getPath();
            try {
                String data = getNodeData(path);
                System.out.println(">>> Node data changed, new data: " + data);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZookeeperWatcherExample example = new ZookeeperWatcherExample();

        System.out.println(">>> createNode");
        // Create node
        example.createNode(NODE_PATH, "hello");
        System.out.println(">>> afterCreatedNode");

        System.out.println(">>> getNode");
        // Get node data
        String data = example.getNodeData(NODE_PATH);
        System.out.println(">>> Node data: " + data);
        System.out.println(">>> updateNodeData");

        // Update node data
        example.updateNodeData(NODE_PATH, "world");
        System.out.println(">>> updatedNodeData");

        System.out.println(">>> deleteNode");
        // Delete node
        example.deleteNode(NODE_PATH);
        System.out.println(">>> deletedNode");

        example.zooKeeper.close();
    }
}
