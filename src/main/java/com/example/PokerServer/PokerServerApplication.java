package com.example.PokerServer;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Objects.TransactionNumber;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@SpringBootApplication
public class PokerServerApplication {


    private static String username = "admin";
    private static String password = "asik1122";

    private static String editUsername = "admin";
    private static String editPassword = "as01765330456ik";

    private static String terminalPath = "terminal-3.0.0.out";
    private static String imagePath = "guest.png";

    public static void main(String[] args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder(PokerServerApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);


        double coinPricePerCrore = 26.0;
        long[] coinAmountOnBuy = { 30000000, 50000000, 100000000, 200000000, 500000000, 1000000000 };
        double[] coinPriceOnBuy = { 100, 150, 300, 600, 1480, 2950 };

        int boardTypeCount = 10;
        long minCallValue[] = {10000, 20000, 100000, 200000, 500000, 1000000, 2000000, 4000000, 10000000, 20000000};
        String boardType[] = {"board1", "board2", "board3", "board4", "board5", "board6", "board7", "board8", "board9", "board10"};
        long minEntryValue[] = {50000, 500000, 2000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000};
        long maxEntryValue[] = {1000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000, 1000000000, 2000000000};
        long mcr[] = {0, 0, 2500000, 7000000, 15000000, 40000000, 100000000, 150000000, 400000000, 1000000000};

        int leastPlayerCount = 2;

        Server.pokerServer = new Server(boardTypeCount, boardType, minEntryValue, maxEntryValue, minCallValue, mcr, 10000000, 100000000,
                leastPlayerCount, 5, 1, 120, 1112, 10000,500000,
                10, 50000, 100000, 60, 10, 10,
                coinPricePerCrore, coinAmountOnBuy, coinPriceOnBuy, getTransactionNumbers(), 30000, 20000, 5000, 10);
    }

    public static ArrayList<TransactionNumber> getTransactionNumbers(){

        ArrayList<TransactionNumber> transactionNumbers = new ArrayList<>();

        transactionNumbers.add(new TransactionNumber(transactionNumbers.size(), "Bkash", "make payment to \"01914957284\""));
        transactionNumbers.add(new TransactionNumber(transactionNumbers.size(), "Nagad", "send money to '01798203178'"));
        transactionNumbers.add(new TransactionNumber(transactionNumbers.size(), "Rocket", "send money to \"01798203178-2\""));

        return transactionNumbers;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getEditUsername() {
        return editUsername;
    }

    public static String getEditPassword() {
        return editPassword;
    }

    public static String getTerminalPath() {
        return terminalPath;
    }

    public static String getImagePath() {
        return imagePath;
    }
}
