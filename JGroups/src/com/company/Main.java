package com.company;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        DistributedMap map = null;
        try {
            map = new DistributedMap(Values.CHANNEL_NAME, Values.MULTICAST_ADDRESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        String line = null;
        System.out.println("Write operation:");
        do {
            line = scanner.nextLine();
            String[] parts = line.split(":");
            switch (parts[0]){
                case "PUT":
                    if (parts.length == 3)  //PUT:key:value
                        System.out.println("Response: " + map.put(parts[1], parts[2]));
                    break;
                case "GET":
                    if (parts.length == 2)
                        System.out.println("Response: " + map.get(parts[1]));
                    break;
                case "REMOVE":
                    if (parts.length == 2)
                        System.out.println("Response: " + map.remove(parts[1]));
                    break;
                case "CONTAINS":
                    if (parts.length == 2)
                        System.out.println("Response: " + map.containsKey(parts[1]));
                    break;
                case "SHOW":
                    if (parts.length == 1)
                        map.showMap();
                    break;
                default:
                    System.out.println("Unknown command");
                    break;

            }
        }
        while(!line.equals("EXIT"));
        System.out.println("Koniec");
        map.shutdown();

    }
}
