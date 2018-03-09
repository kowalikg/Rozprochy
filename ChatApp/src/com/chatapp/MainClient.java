package com.company;

import com.company.ClientThings.Client;

import java.io.IOException;

public class MainClient {

    public static void main(String[] args) {
        Client client = new Client("localhost", 2000);
        try {
            client.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

