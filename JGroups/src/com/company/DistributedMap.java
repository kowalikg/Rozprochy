package com.company;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedMap extends ReceiverAdapter implements SimpleStringMap {
    private String channelName;
    private String multicastAddress;
    private JChannel channel;
    private final Map<String, String> map = new ConcurrentHashMap<>();

    public DistributedMap(String channelName, String multicastAddress) throws Exception {
        this.channelName = channelName;
        this.multicastAddress = multicastAddress;
        launchChannel();
    }

    private void launchChannel() throws Exception {
        channel = new JChannel(false);
        channel.setReceiver(this);

        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        initStack(stack);

        channel.connect(channelName);
        channel.getState(null, Values.TIMEOUT);
        showMap();
    }

    private void initStack(ProtocolStack stack) throws Exception {
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(multicastAddress)))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE());

        stack.init();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public String put(String key, String value) {
        Notification notification;
        notification = new Notification(Operation.PUT, key, value);
        try {
            channel.send(null, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return putToMap(key, value);
    }

    private String putToMap(String key, String value) {
        String response = map.put(key, value);
        return response == null ? key + " : " + value + " added successfully" : "problem with adding. Try again.";
    }

    @Override
    public String remove(String key) {
        Notification notification;
        notification = new Notification(Operation.REMOVE, key);
        try {
            channel.send(null, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removeFromMap(key);
    }

    private String removeFromMap(String key) {
        String response = map.remove(key);
        return response == null ? "no key in map" : "value: " + response + " deleted successfully";
    }

    public void showMap(){
        Set keySet = map.keySet();

        System.out.println("Map state:");
        for (Object key: keySet){
            String k = (String) key;
            System.out.println("Key: " + k + ", value: " + map.get(k));
        }
    }
    public void shutdown(){
        channel.close();
        try {
            Thread.sleep(Values.TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        System.out.println("Sending map copy...");
        Util.objectToStream(map, new ObjectOutputStream(output));
    }

    @Override
    public void setState(InputStream input) throws Exception {
        System.out.println("Receiving map copy...");
        Map<String, String> mapCopy;
        mapCopy = (ConcurrentHashMap<String, String>) Util.objectFromStream(new ObjectInputStream(input));
        map.clear();
        map.putAll(mapCopy);
    }

    @Override
    public void receive(Message msg) {
        if (!(msg.getObject() instanceof Notification)) return;
        if (channel.getAddress().equals(msg.getSrc())) return;

        Notification notification = (Notification) msg.getObject();
        String key, value;
        switch (notification.getOperation()){
            case PUT:
                key = notification.getKey();
                value = notification.getValue();
                System.out.println(putToMap(key, value));
                break;
            case REMOVE:
                key = notification.getKey();
                System.out.println(removeFromMap(key));
                break;
            default:
                break;
        }
    }

    @Override
    public void viewAccepted(View view) {
        handleView(channel, view);

    }
    private static void handleView(JChannel ch, View new_view) {
        if(new_view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(ch, (MergeView)new_view);
            // requires separate thread as we don't want to block JGroups
            handler.start();
        }
    }
    private static class ViewHandler extends Thread {
        JChannel channel;
        MergeView view;

        private ViewHandler(JChannel channel, MergeView view) {
            this.channel = channel;
            this.view = view;
        }

        public void run() {
            List<View> subgroups = view.getSubgroups();
            View tmp_view = subgroups.get(0); // picks the first
            Address local_addr = channel.getAddress();
            System.out.println("moj adress: " + channel.getAddress());
            if(!tmp_view.getMembers().contains(local_addr)) {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will re-acquire the state");
                try {
                    channel.getState(null, Values.TIMEOUT);
                }
                catch(Exception ex) {
                }
            }
            else {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will do nothing");
            }
        }
    }
}
