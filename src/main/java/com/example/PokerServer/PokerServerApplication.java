package com.example.PokerServer;

import com.example.PokerServer.Connection.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PokerServerApplication {

    public static void main(String[] args) {

        Server.pokerServer = new Server(3, 120);

        SpringApplication.run(PokerServerApplication.class, args);
    }

}
