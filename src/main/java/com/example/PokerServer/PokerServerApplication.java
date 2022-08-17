package com.example.PokerServer;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Objects.TransactionNumber;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.ArrayList;

@RestController
@SpringBootApplication
public class PokerServerApplication {


    private static String username = "admin";
    private static String password = "asik1122";

    private static String editUsername = "admin";
    private static String editPassword = "11223";

    private static String terminalPath = "terminal-10.0.0.out";
    private static String imagePath = "images/";
    private static String host = "140.82.0.55";

    private static boolean showButton = true;
    private static int version = 10;

    private static int port = 1112;

    private static int imageCount = 24;
    private static int clearTill = 34;

    private static int leastPlayerCount = 2;

    private static boolean printError = true;


//    private static String terminalPath = "Files/terminal.out";
//    private static String imagePath = "Files/images/";
//    private static String host = "localhost";

    public static void main(String[] args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder(PokerServerApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);


        int minCoinWithdraw = 5;
        double coinPricePerCrore = 26.0;
        long[] coinAmountOnBuy = { 30000000, 50000000, 100000000, 200000000, 500000000, 1000000000 };
        double[] coinPriceOnBuy = { 100, 150, 300, 600, 1480, 2950 };

        int boardTypeCount = 10;
        long minCallValue[] = {10000, 20000, 100000, 200000, 500000, 1000000, 2000000, 4000000, 10000000, 20000000};
        String boardType[] = {"board1", "board2", "board3", "board4", "board5", "board6", "board7", "board8", "board9", "board10"};
        long minEntryValue[] = {50000, 500000, 2000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000};
        long maxEntryValue[] = {1000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000, 1000000000, 2000000000};
        long mcr[] = {0, 0, 2500000, 7000000, 15000000, 40000000, 100000000, 150000000, 400000000, 1000000000};
        boolean hiddenStatus[] = {true, true, true, true, true, true, true, true, true, true};

        String[] botNames = {"Fardin Soumo", "Rony Bhuiya", "Mohsin khan", "Rakib Ali", "Jibon Bsl", "Najmul Mahbub", "Rubel Islam", "Shoisob Mahmud",
                             "Nure Alam", "Imrul kabir", "Yousuf Pathan", "Mashrafee", "Farhan ul islam", "Joy khan", "Zubaer khan", "Mehedi Hasan",
                             "Atikul Islam", "Shanjida Haque", "Farjana Yeasmin", "Sadia Afreen", "Sheikh Marjia", "Ayon Bin Kabir", "Lemon Ali",
                             "Urbi Das",
                             "Al Rabby Bsl", "Al Riad", "Md Gaddafi", "Rezvi Kabir", "Al Mamun", "Sharif Ahmed", "Milu Rahman", "Feroz Shahi",
                             "Saddam hossain", "Sayed hasan", "Loban Hussain", "Shafiqul Islam", "Wahidunnabi Sagar", "Rakibujjamam Rakib", "Al Mubin Rony",
                             "Raton khan", "Surovi Akter", "Mina Rahman", "Mukta Khatun", "Jannatul", "Redwanul Haque", "Boss Dulal",
                             "Bipasha bsl",
                             "Jibon", "Bishnu das", "Sourov", "Iqbal khan", "Mollah", "Polok", "Parvez", "Babu", "Susmoy", "Khokon",
                             "Astab", "Anamul", "Amirul", "Golam kibria", "Monju", "Shihad", "Roni khan", "Chasi karim", "Mredul", "Hridoy"};

        Server.pokerServer = new Server(boardTypeCount, boardType, minEntryValue, maxEntryValue, minCallValue, mcr, hiddenStatus,
                10000000, 100000000,
                leastPlayerCount, 5, 1, 120, port, host, 10000, 500000,
                10, 50000, 100000, 60, 2, 10,
                minCoinWithdraw, coinPricePerCrore, coinAmountOnBuy, coinPriceOnBuy, getTransactionNumbers(), 30000, 15000,
                5000, showButton, version, imageCount,
                2000, 3000, 2, 2, 2000, 10000, 5,
                botNames, 10000, printError, getBkashLinks(), getBotPercentages(), 4, 3, 3.5);
    }



    public static double[] getBotPercentages(){
        return new double[]{80, 80, 50, 80, 80, 80, 80, 80, 80, 80};
    }

    public static String[] getBkashLinks(){

        String[] ret = new String[6];

        ret[0] = "https://shop.bkash.com/ms-bhuiya-traders01914957284/pay/bdt100/JIkVJa";
        ret[1] = "https://shop.bkash.com/ms-bhuiya-traders01914957284/pay/bdt150/Hg0JPS";
        ret[2] = "https://shop.bkash.com/ms-bhuiya-traders01914957284/pay/bdt295/ttsrrp";
        ret[3] = "https://shop.bkash.com/ms-bhuiya-traders01914957284/pay/bdt590/JWs3f6";
        ret[4] = "https://shop.bkash.com/ms-bhuiya-traders01914957284/pay/bdt1480/n7kdlY";
        ret[5] = "https://shop.bkash.com/ms-bhuiya-traders01914957284/pay/bdt2950/CHS38D";

        return ret;
    }


    public static ArrayList<TransactionNumber> getTransactionNumbers(){

        ArrayList<TransactionNumber> transactionNumbers = new ArrayList<>();

        transactionNumbers.add(new TransactionNumber(transactionNumbers.size(), "Bkash", "make payment to \"01914957284\""));
        transactionNumbers.add(new TransactionNumber(transactionNumbers.size(), "Nagad", "send money to \"01798203178\""));
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

    public static int getClearTill() {
        return clearTill;
    }
}
