package pr.iceworld.fernando.zk;

import org.apache.zookeeper.KeeperException;

import java.io.UnsupportedEncodingException;

public interface ZKManager {
    void create(String path, byte[] data)
            throws KeeperException, InterruptedException;

    Object getZNodeData(String path, boolean watchFlag)
            throws UnsupportedEncodingException, KeeperException, InterruptedException;

    void update(String path, byte[] data)
            throws KeeperException, InterruptedException;

    void delete(String path) throws InterruptedException, KeeperException;

    void closeConnection()
            throws InterruptedException;
}