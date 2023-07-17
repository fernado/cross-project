package pr.iceworld.fernando.zk;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ZKTest {

    @Test
    public void testZk() throws IOException, InterruptedException, KeeperException {
        ZKManager zkManager = new ZKManagerImpl();
        zkManager.create("/example", "30".getBytes(StandardCharsets.UTF_8));
        Object obj1 = zkManager.getZNodeData("/example", true);
        System.out.println(obj1.toString());
        zkManager.update("/example", "40".getBytes(StandardCharsets.UTF_8));
        obj1 = zkManager.getZNodeData("/example", true);
        System.out.println(obj1.toString());
        zkManager.delete("/example");
        System.out.println("---------------");
        zkManager.closeConnection();
    }
}
