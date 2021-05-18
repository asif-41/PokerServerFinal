package com.example.PokerServer;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.TransactionMethods;
import com.example.PokerServer.Objects.TransactionNumber;
import com.example.PokerServer.Objects.User;
import com.example.PokerServer.db.DB;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

@RestController
@SpringBootApplication
public class PokerServerApplication {


    private String username = "admin";
    private String password = "PokerServerPa$$w0RD";

    public static void main(String[] args) {

        SpringApplicationBuilder builder = new SpringApplicationBuilder(PokerServerApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        int boardTypeCount = 10;
        long minCallValue[] = {10000, 20000, 100000, 200000, 500000, 1000000, 2000000, 4000000, 10000000, 20000000};
        String boardType[] = {"board1", "board2", "board3", "board4", "board5", "board6", "board7", "board8", "board9", "board10"};
        long minEntryValue[] = {50000, 500000, 2000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000};
        long maxEntryValue[] = {1000000, 5000000, 10000000, 25000000, 50000000, 100000000, 250000000, 500000000, 1000000000, 2000000000};
        long mcr[] = {0, 0, 2500000, 7000000, 15000000, 40000000, 100000000, 150000000, 400000000, 1000000000};



        Server.pokerServer = new Server(boardTypeCount, boardType, minEntryValue, maxEntryValue, minCallValue, mcr, 10000000, 100000000,
                2, 5, 1, 120, 1112, 10000,500000,
                10, 50000, 100000, 60, 10, 10, getTransactionNumbers());
    }

    private static ArrayList<TransactionNumber> getTransactionNumbers(){

        ArrayList<TransactionNumber> transactionNumbers = new ArrayList<>();

        transactionNumbers.add(new TransactionNumber("Bkash", "0111"));
        transactionNumbers.add(new TransactionNumber("Bkash", "0112"));
        transactionNumbers.add(new TransactionNumber("Nagad", "0111"));

        return transactionNumbers;
    }








    @RequestMapping(value = "/", produces = "text/plain")
    public String readIp(HttpServletRequest request, HttpServletResponse response){

        String show = "Welcom to server\n";

        show += "Server ip " + request.getLocalAddr() + "\n";
        show += "Client ip " + request.getRemoteAddr();

        Server.pokerServer.setHost(request.getLocalAddr());

        return show;
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> image() throws IOException {
        final ByteArrayResource inputStream = new ByteArrayResource(Files.readAllBytes(Paths.get(
                "././././Images/guest.png"
        )));
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(inputStream.contentLength())
                .body(inputStream);

    }









    private boolean authorizeAdmin(String username, String password){
        if(username.equals(this.username) && password.equals(this.password)) return true;
        else return false;
    }

    @RequestMapping(value = "/memory", produces = "text/plain")
    public String readMemory(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret +=  "Free memory: " + Runtime.getRuntime().freeMemory() + "\n" +
                    "Total memory: " + Runtime.getRuntime().totalMemory() + "\n" +
                    "Max memory: " + Runtime.getRuntime().maxMemory();
        }
        else ret += "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/terminal", produces = "text/plain")
    public @ResponseBody String terminal(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){
            ret += "Terminal\n\n";

            try{
                File f = new File("././././Images/terminal.out");
                Scanner sc = new Scanner(f);
                int l = 1;
                while(sc.hasNextLine()) {
                    ret += String.format("%06d", l) + ".   " + sc.nextLine() + "\n";
                    l++;
                }

            }catch (Exception e){
                ret += "File not found......\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data", produces = "text/plain")
    public @ResponseBody String dataHome(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){
            ret += "Welcome admin\n\n";

            ret += "Server host: " + Server.pokerServer.getHost() + "\n";
            ret += "Server port: " + Server.pokerServer.getPort() + "\n\n";

            ret += "Connected clients: " + Server.pokerServer.getCasualConnections().size() + "\n\n";
            ret += "Logged in users: " + Server.pokerServer.getLoggedInUsers().size() + "\n";
            ret += "Guest users: " + Server.pokerServer.getGuests().size() + "\n\n";

            ret += "Join queue size: \n";
            for(int i=0; i<Server.pokerServer.getBoardTypeCount(); i++)
                ret += "Board type: " + Server.pokerServer.getBoardType()[i] + " count: " + Server.pokerServer.getPendingQueue()[i].size() + "\n";
            ret += "\n";

            ret += "Game rooms: \n";
            for(int i=0; i<Server.pokerServer.getBoardTypeCount(); i++)
                ret += "Board type: " + Server.pokerServer.getBoardType()[i] + " count: " + Server.pokerServer.getGameThreads()[i].size() + "\n";
            ret += "\n";

            ret += "Waiting rooms: \n";
            for(int i=0; i<Server.pokerServer.getBoardTypeCount(); i++)
                ret += "Board type: " + Server.pokerServer.getBoardType()[i] + " count: " + Server.pokerServer.getWaitingRoom()[i].size() + "\n";
            ret += "\n";

        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printBasicData", produces = "text/plain")
    public @ResponseBody String printBasicData(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += "Welcome admin\n\n";
            ret += "Server info:\n\n";

            ret += "New user registration coin: " + ( (double) Server.pokerServer.initialCoin / 100000 ) + "lac\n";
            ret += "Max guest limit: " + Server.pokerServer.getMaxGuestLimit() + "\n";
            ret += "Wait in queue: " + Server.pokerServer.getQueueWaitLimit() + "seconds\n";
            ret += "Each day, free login coin: " + ( (double) Server.pokerServer.freeLoginCoin / 100000 ) + "lac\n";
            ret += "Each day, available video: " + Server.pokerServer.dailyCoinVideoCount + "\n";
            ret += "Each video ad coin: " + ( (double) Server.pokerServer.eachVideoCoin / 100000 ) + "lac\n";
            ret += "Max player in a game: " + Server.pokerServer.getMaxPlayerCount() + "\n\n";

            ret += "Board type: " + Server.pokerServer.getBoardTypeCount() + "\n\n";
            for(int i=0; i<Server.pokerServer.getBoardTypeCount(); i++){

                ret += "Board type: " + (i+1) + "\n";
                ret += "Minimum entry value: " + ( (double) Server.pokerServer.getMinEntryValue()[i] / 100000 ) + "lac\n";
                ret += "Maximum entry value: " + ( (double) Server.pokerServer.getMaxEntryValue()[i] / 100000 ) + "lac\n";
                ret += "Minimum call value: " + ( (double) Server.pokerServer.getMinCallValue()[i] / 100000 ) + "lac\n";
                ret += "Minimum coin to enter: " + ( (double) Server.pokerServer.getMcr()[i] / 100000 ) + "lac\n";
                ret += "\n";
            }
            ret += "\n";

            ret += "Withdraw price per crore: " + TransactionMethods.getCoinPricePerCrore() + "\n";
            ret += "Buy coin packages: " + TransactionMethods.getCoinAmountOnBuy().length + "\n";

            for(int i=0; i<TransactionMethods.getCoinAmountOnBuy().length; i++){
                ret += "Package " + (i+1) + ": ";
                ret += "Coin: " + ( (double) TransactionMethods.getCoinAmountOnBuy()[i] / 100000 ) + "lac ";
                ret += "Price: " + TransactionMethods.getCoinPriceOnBuy()[i] + "\n";
            }
            ret += "\n";

            ret += "Exp increase after each game: " + User.getExpIncrease() + "\n";
            ret += "User ranks: ";
            for(String x : User.rankString) ret += x + " ";
            ret += "\n\n";
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printDatabase", produces = "text/plain")
    public @ResponseBody String printDatabase(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += DB.printDatabase(Server.pokerServer.getDb()) + "\n";
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printLoggedUsers", produces = "text/plain")
    public @ResponseBody String printLoggedUsers(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += "Connected clients: " + Server.pokerServer.getCasualConnections().size() + "\n\n";
            ret += "Logged in users: " + Server.pokerServer.getLoggedInUsers().size() + "\n";
            ret += "Guest users: " + Server.pokerServer.getGuests().size() + "\n\n";

            ret += "Logged usernames: " + "\n\n";
            for(Object x : Server.pokerServer.getLoggedInUsers()){

                User user = ( (ServerToClient) x ).getUser();

                if(user == null) ret += "Pay nai ekjon!\n\n";
                else ret += user.printUser() + "\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printGuestUsers", produces = "text/plain")
    public @ResponseBody String printGuestUsers(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += "Guest limit: " + Server.pokerServer.getMaxGuestLimit() + "\n";
            ret += "Guest user count: " + Server.pokerServer.getGuests().size() + "\n";
            ret += "Guest users id: " + "\n\n";

            for(Object x : Server.pokerServer.getGuests()){

                ret += (int) x + "\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printPendingQueue", produces = "text/plain")
    public @ResponseBody String printPendingQueue(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            int cnt = Server.pokerServer.getBoardTypeCount();
            ArrayList[] pendingQueue = Server.pokerServer.getPendingQueue();
            ArrayList[] queueTimer = Server.pokerServer.getQueueTimeCount();

            ret += "Board types: " + cnt + "\n\n";

            for(int i=0; i<cnt; i++){

                ret += "Type-" + i + ": " + "\n";
                for(int j=0; j<pendingQueue[i].size(); j++){

                    ServerToClient s = (ServerToClient) pendingQueue[i].get(j);
                    User user = s.getUser();

                    ret += "Username: " + user.getUsername() + " Board Coin: " + ( (double) user.getBoardCoin() / 100000 ) + "lac " +
                            " waited: " + queueTimer[i].get(j) + " limit: " + Server.pokerServer.getQueueWaitLimit() + "\n";
                }
                ret += "\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printGameThreads", produces = "text/plain")
    public @ResponseBody String printGameThreads(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            int cnt = Server.pokerServer.getBoardTypeCount();
            ArrayList[] gameThreads = Server.pokerServer.getGameThreads();

            ret += "Game Thread types: " + cnt + "\n\n";

            for(int i=0; i<cnt; i++){

                ret += "Type-" + i + ": " + "\n\n";

                for(int j=0; j<gameThreads[i].size(); j++){

                    GameThread g = (GameThread) gameThreads[i].get(j);
                    ret += g.printGameThread() + "\n";
                }
                ret += "\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printWaitingRooms", produces = "text/plain")
    public @ResponseBody String printWaitingRooms(@RequestParam String username, @RequestParam String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            int cnt = Server.pokerServer.getBoardTypeCount();
            ArrayList[] waitingRoom = Server.pokerServer.getWaitingRoom();

            ret += "Waiting Room types: " + cnt + "\n\n";

            for(int i=0; i<cnt; i++){

                ret += "Type-" + i + ": " + "\n\n";

                for(int j=0; j<waitingRoom[i].size(); j++){

                    WaitingRoom w = (WaitingRoom) waitingRoom[i].get(j);
                    ret += w.printWaitingRoom() + "\n";
                }
                ret += "\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }



}
