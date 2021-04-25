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


        int boardTypeCount = 5;
        long minCallValue[] = {10000, 10000, 10000, 10000, 10000};
        String boardType[] = {"board1", "board2", "board3", "board4", "board5"};
        long minEntryValue[] = {100000, 100000, 100000, 100000, 100000};

        Server.pokerServer = new Server(boardTypeCount, boardType, minEntryValue, minCallValue, 10000,
                2, 5, 1, 120, 8080, 1000,
                1000000, 10, 10000, 10000, 60, 15);

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
