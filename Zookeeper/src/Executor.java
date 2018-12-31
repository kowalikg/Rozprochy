package com.company;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Executor implements DataMonitorListener, Runnable, Watcher {
    private DataMonitor dataMonitor;
    private ZooKeeper zooKeeper;
    private String exec[];
    private static String znode = "/znode_testowy";

    private Process child;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("USAGE: Executor hostPort program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String exec[] = new String[args.length - 1];
        System.arraycopy(args, 1, exec, 0, exec.length);

        try {
            new Executor(hostPort, znode, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Executor(String hostPort, String znode,
                     String exec[]) throws IOException {
        this.exec = exec;
        zooKeeper = new ZooKeeper(hostPort, 3000, this);
        dataMonitor = new DataMonitor(zooKeeper, znode, null, this);
    }

    public void run() {
        printUsage();
        Scanner scanner = new Scanner(System.in);
        String line;
        while (!(line = scanner.nextLine()).equals("quit")) {
            if (line.equals("display")) {
                try {
                    displayTree(znode);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                printUsage();
            }
        }
        child.destroy();
    }

    private void printUsage() {
        System.out.println("USAGE:" );
        System.out.println("display -> displays nodes tree");
        System.out.println("quit -> terminate program");
    }


    private void displayTree(String znode) throws KeeperException, InterruptedException {
        List childList = zooKeeper.getChildren(znode, this);
        System.out.println(znode);
        for (Object child: childList){
            displayTree(znode + "/" + child);
        }

    }

    @Override
    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                System.out.println("Starting child");
                zooKeeper.getChildren(znode, dataMonitor);
                child = Runtime.getRuntime().exec(exec);

            } catch (IOException | InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        dataMonitor.process(event);
    }
}
