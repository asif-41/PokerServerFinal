package com.example.PokerServer.controller;

import com.example.PokerServer.Connection.BotClient;
import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.TransactionMethods;
import com.example.PokerServer.Objects.TransactionNumber;
import com.example.PokerServer.Objects.User;
import com.example.PokerServer.PokerServerApplication;
import com.example.PokerServer.db.DB;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@Controller
public class RawDataController {






    @RequestMapping(value = "/", produces = "text/plain")
    public @ResponseBody String readIp(HttpServletRequest request, HttpServletResponse response){

        String show = "Welcome to server\n";

        show += "Server ip " + request.getLocalAddr() + "\n";
        show += "Client ip " + request.getRemoteAddr();

        return show;
    }

    @RequestMapping(value = "/allOkay", produces = "text/plain")
    public @ResponseBody String allOkay(){
        return "allOkay";
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody ResponseEntity<Resource> image(@RequestParam String id) throws IOException {

        int index = Integer.parseInt(id);

        final ByteArrayResource inputStream = new ByteArrayResource(Files.readAllBytes(Paths.get(
                PokerServerApplication.getImagePath() + id + ".JPG"
        )));
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(inputStream.contentLength())
                .body(inputStream);

    }




    @RequestMapping(value = "/pages")
    public String pageLinks(@RequestParam(required = false) String username, @RequestParam(required = false) String password, Model model){

        String ret;

        if(authorizeAdmin(username, password)) {
            ret = "PageLinks";
            model.addAttribute("username", PokerServerApplication.getUsername());
            model.addAttribute("password", PokerServerApplication.getPassword());
        }
        else ret = "Forbidden";

        return ret;
    }


    @RequestMapping("/images")
    public String showImages(Model model){

        model.addAttribute("imageCount", Server.pokerServer.getImageCount());
        model.addAttribute("location", "http://" + Server.pokerServer.getHost() + ":" + Server.pokerServer.getPort() + "/image?id=");

        return "Images";
    }




    private boolean authorizeAdmin(String username, String password){

        if(username == null || password == null) return false;
        
        if(username.equals(PokerServerApplication.getUsername()) && password.equals(PokerServerApplication.getPassword())) return true;
        else return false;
    }

    @RequestMapping(value = "/memory", produces = "text/plain")
    public @ResponseBody String readMemory(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret +=  "Free memory: " + Runtime.getRuntime().freeMemory() + "\n" +
                    "Total memory: " + Runtime.getRuntime().totalMemory() + "\n" +
                    "Max memory: " + Runtime.getRuntime().maxMemory();
        }
        else ret += "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data", produces = "text/plain")
    public @ResponseBody String dataHome(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

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
    public @ResponseBody String printBasicData(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += "Welcome admin\n\n";
            ret += "Server info:\n\n";

            ret += "Thread count: " + Thread.activeCount() + "\n\n";



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

            ArrayList<TransactionNumber> transactionNumbers = PokerServerApplication.getTransactionNumbers();

            for(int i=0; i<transactionNumbers.size(); i++){
                ret += transactionNumbers.get(i).toString2() + "\n";
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

    @RequestMapping(value = "/data/printLoggedUsers", produces = "text/plain")
    public @ResponseBody String printLoggedUsers(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

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
    public @ResponseBody String printGuestUsers(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

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

    @RequestMapping(value = "/data/printBotUsers", produces = "text/plain")
    public @ResponseBody String printBotUsers(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += "Max bot limit: " + Server.pokerServer.getMaxBotLimit() + "\n";
            ret += "Bots user count: " + Server.pokerServer.getBotClients().size() + "\n";
            ret += "Bot users: " + "\n\n";

            for(Object x : Server.pokerServer.getBotClients()) {
                ret += ((BotClient) x).getUser().getId() + " ";
                ret += ((BotClient) x).getUser().getUsername() + "\n";
            }
            ret += "\n\n";

            ret += "Bots:\n\n";
            for(Object x : Server.pokerServer.getBotClients()){

                User temp = ((BotClient) x).getUser();
                ret += temp.printUser() + "\n";
            }
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printPendingQueue", produces = "text/plain")
    public @ResponseBody String printPendingQueue(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

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
    public @ResponseBody String printGameThreads(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

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
    public @ResponseBody String printWaitingRooms(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

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




    @RequestMapping(value = "/data/printDatabase", produces = "text/plain")
    public @ResponseBody String printDatabase(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += DB.printDatabase(Server.pokerServer.getDb()) + "\n";
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printAccounts", produces = "text/plain")
    public @ResponseBody String printAccounts(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){
            ret += DB.printAccounts(Server.pokerServer.getDb());
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printPendingTransactions", produces = "text/plain")
    public @ResponseBody String printPendingTransactions(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){
            ret += DB.printPendingTransactions(Server.pokerServer.getDb());
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printTransactions", produces = "text/plain")
    public @ResponseBody String printTransactions(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){
            ret += DB.printTransactions(Server.pokerServer.getDb());
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printPendingRefunds", produces = "text/plain")
    public @ResponseBody String printPendingRefunds(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){
            ret += DB.printPendingRefunds(Server.pokerServer.getDb());
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printRefunds", produces = "text/plain")
    public @ResponseBody String printRefunds(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += DB.printRefunds(Server.pokerServer.getDb());
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }

    @RequestMapping(value = "/data/printBannedAccounts", produces = "text/plain")
    public @ResponseBody String printBannedAccounts(@RequestParam(required = false) String username, @RequestParam(required = false) String password){

        String ret = "";

        if(authorizeAdmin(username, password)){

            ret += DB.printBannedAccounts(Server.pokerServer.getDb());
        }
        else ret = "Unauthorized access request: failed";

        return ret;
    }
}
