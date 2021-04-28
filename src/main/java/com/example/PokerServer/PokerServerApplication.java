package com.example.PokerServer;

import com.example.PokerServer.Connection.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@SpringBootApplication
public class PokerServerApplication {

    public static void main(String[] args) {


        int boardTypeCount = 10;
        long minCallValue[] = {10000, 20000, 100000, 200000, 500000, 1000000, 2000000, 4000000, 10000000, 20000000};
        String boardType[] = {"board1", "board2", "board3", "board4", "board5", "board6", "board7", "board8", "board9", "board10"};
        long minEntryValue[] = {50000, 500000, 2000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000};
        long maxEntryValue[] = {1000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000, 1000000000, 2000000000};
        long mcr[] = {0, 0, 2500000, 7000000, 15000000, 40000000, 100000000, 150000000, 400000000, 1000000000};

        Server.pokerServer = new Server(boardTypeCount, boardType, minEntryValue, minCallValue, 10000000, 100000000,
                2, 5, 1, 120, 8080, 1000,500000,
                10, 50000, 100000, 60, 15);

        SpringApplication.run(PokerServerApplication.class, args);
    }



    @RequestMapping("/")
    public String readIp(HttpServletRequest request, HttpServletResponse response){

        String show = "Welcom to server\n";

        show += "Server ip " + request.getLocalAddr() + "\n";
        show += "Client ip " + request.getRemoteAddr();

        return show;
    }

}
