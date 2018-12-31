package com.company;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.KeeperException.Code;
import java.util.Arrays;
import java.util.List;

public class DataMonitor implements Watcher, AsyncCallback.StatCallback {
    private ZooKeeper zooKeeper;
    private String znode;
    private Watcher chainedWatcher;
    private boolean dead;
    private DataMonitorListener listener;
    private byte prevData[];

    public DataMonitor(ZooKeeper zooKeeper, String znode, Watcher chainedWatcher,
                       DataMonitorListener listener) {
        this.zooKeeper = zooKeeper;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        zooKeeper.exists(znode, true, this, null);
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (rc) {
            case Code.Ok:
                exists = true;
                break;
            case Code.NoNode:
                exists = false;
                break;
            case Code.SessionExpired:
            case Code.NoAuth:
                dead = true;
                listener.closing(rc);
                return;
            default:
                // Retry errors
                zooKeeper.exists(znode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                b = zooKeeper.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            prevData = b;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            // We are are being told that the state of the
            // connection has changed
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(KeeperException.Code.SessionExpired);
                    break;
            }
        }
        else if (event.getType() == Event.EventType.NodeChildrenChanged){
            try {
                System.out.println("Node " + znode + " kids amount: " + childrenAmount(znode));
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            if (path != null && path.equals(znode)) {
                // Something has changed on the node, let's find out
                zooKeeper.exists(znode, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }

    private long childrenAmount(String path) throws KeeperException, InterruptedException {
        long amount = 0;
        List childList = zooKeeper.getChildren(path, true);
        for (Object s: childList){
            String childrenPath = path + "/" + s;
            amount += childrenAmount(childrenPath);
        }
        amount += childList.size();
        return amount;
    }
}
