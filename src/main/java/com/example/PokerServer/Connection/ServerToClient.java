package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.Notification;
import com.example.PokerServer.Objects.TransactionMethods;
import com.example.PokerServer.Objects.TransactionNumber;
import com.example.PokerServer.Objects.User;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ServerToClient implements Runnable {


    //=====================================================
    //
    //          SERVER TO CLIENT ->  SERVER SIDE OBJECT
    //
    //=====================================================


    //==========================================================
    //
    //          INITIALIZING PARAMETERS
    //
    //
    //===========================================================


    private int port;                                   //      CONNECTION PORT
    private String host;                                //      CONNECTED IP OF SERVER
    private String webSocketKey;                        //      WEB SOCKET KEY, IDENTIFIER
    private WebSocketSession session;                   //      SESSION

    private String incoming;

    private JSONObject jsonIncoming;
    private User user;                                  //      USER, NULL IF NOT LOGGED IN
    private GameThread gameThread;                      //      GAME THREAD, NULL IF NOT IN ANY GAME
    private WaitingRoom waitingRoom;                    //      WAITING ROOM, NULL IF NOT IN ANY ROOM

    private Timer connectionChecker;                    //      CONNECTION CHECKER TIMER
    private Timer connectionChecketWait;
    private boolean isConnected;

    private int lastGameCode;
    private boolean active;

    //============================================================
    //
    //============================================================


    //=============================================================================
    //
    //          CONSTRUCTOR
    //
    //=============================================================================


    public ServerToClient(WebSocketSession session) {

        active = true;
        incoming = "";
        jsonIncoming = null;
        user = null;
        gameThread = null;
        waitingRoom = null;
        if(session != null) connectionChecker = new Timer();
        if(session != null) connectionChecketWait = new Timer();

        lastGameCode = -1;

        this.session = session;
        if(session != null) webSocketKey = session.getHandshakeHeaders().get("sec-websocket-key").get(0);
        else webSocketKey = "Empty";


        try {

            if(session != null) port = session.getRemoteAddress().getPort();
            else port = Server.pokerServer.getPort();
            host = Server.pokerServer.getHost();

        } catch (Exception e) {
            System.out. println("Exception in fetching ip -> " + e);

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
        }
    }

    @Override
    public void run() {

        System.out. println("A connection has been established");
        setupConnectionChecker();
    }

    //============================================================
    //
    //============================================================


    //================================================================================
    //
    //          SETTING UP COMMUNICATIONS
    //
    //================================================================================

    private JSONObject initiateJson() {

        JSONObject send = new JSONObject();

        send.put("sender", "Server");
        send.put("ip", host);
        send.put("port", port);

        return send;
    }

    public void sendMessage(String temp) {

        try {
            String[] splitted = FluentIterable.from(Splitter.fixedLength(4000).split(temp)).toArray(String.class);

            for(int i=0; i<splitted.length-1; i++){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("done", false);
                jsonObject.put("data", splitted[i]);

                if(session.isOpen()) session.sendMessage(new TextMessage(jsonObject.toString()));
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("done", true);
            jsonObject.put("data", splitted[splitted.length-1]);

            if(session.isOpen()) session.sendMessage(new TextMessage(jsonObject.toString()));

        } catch (Exception e) {
            System.out. println("Error for connection with key -> " + webSocketKey);
            System.out. println("Error in sending data from server -> " + e);

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
        }
    }

    public void incomingMsg(String temp) {

        setupConnectionChecker();

        try {
            jsonIncoming = new JSONObject(temp);
            incoming = "";
        } catch (Exception e) {
            System.out.println("Error in getting json in server side\n" + e);

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
            incoming = "";
            jsonIncoming = null;
            return;
        }

        if (jsonIncoming.get("requestType").equals("LoginRequest")) {

            //LOGIN REQUEST

            loginRequestResponse(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("LogoutRequest")) {

            //LOGOUT REQUEST();

            sendResponseLogout();
        }
        else if (jsonIncoming.get("requestType").equals("Transaction")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("data");

            if(tempJson.get("request").equals("BuyCoin")){

                buyCoinRequest(jsonIncoming);
            }
            else if(tempJson.get("request").equals("WithdrawCoin")){

                withdrawCoinRequest(jsonIncoming);
            }
            else if(tempJson.get("request").equals("ShowTransactions")){

                allTransactionsResponse();
            }
        }
        else if (jsonIncoming.get("requestType").equals("ShopData")){

            sendShopData();
        }
        else if (jsonIncoming.get("requestType").equals("AddCoinVideoRequest")) {

            addCoinVideoResponse(jsonIncoming.getString("requestTime"));
        }
        else if (jsonIncoming.get("requestType").equals("AddFreeCoinRequest")) {

            addFreeCoinResponse(jsonIncoming.getString("requestTime"));
        }
        else if (jsonIncoming.get("requestType").equals("NotificationRequest")){

            getNotifications();
        }
        else if (jsonIncoming.get("requestType").equals("JoinRequest")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("data");
            requestJoin(true, tempJson);
        }
        else if (jsonIncoming.get("requestType").equals("BoardData")){

            sendBoardData();
        }
        else if (jsonIncoming.get("requestType").equals("AbortRequest")) {

            requestAbort();
        }
        else if (jsonIncoming.get("requestType").equals("UpdateOwn")) {

            sendOwnData();
        }
        else if (jsonIncoming.get("requestType").equals("UpdateOwnInGame")) {

            sendOwnInGameData();
        }
        else if (jsonIncoming.get("requestType").equals("CheckConnection")) {

            connectionCheckingResponse();
        }
        else if (jsonIncoming.get("requestType").equals("GameThread")) {

            JSONObject gameData = jsonIncoming.getJSONObject("gameData");

            if(gameData.get("requestType").equals("AskJoinGameThreadByCode")){

                int code = gameData.getInt("gameCode");
                askToJoinGameThread(code);
            }
            else if(gameData.get("requestType").equals("CancelJoinRequest")){

                cancelJoiningGameRequest();
            }
            else if(gameData.get("requestType").equals("JoinAmount")){

                joinGameAmountRequest(jsonIncoming);
            }
            else gameThread.incomingMessage(temp, this);
        }
        else if (jsonIncoming.get("requestType").equals("WaitingRoom")) {

            JSONObject waitingRoomData = jsonIncoming.getJSONObject("waitingRoomData");

            if (waitingRoomData.get("requestType").equals("Create")) {

                createWaitingRoom(jsonIncoming);
                System.out.println("Created");
            }
            else if (waitingRoomData.get("requestType").equals("AskJoinWaitingRoomByCode")) {

                int code = waitingRoomData.getInt("gameCode");
                askToJoinWaitingRoom(code);
            }
            else if (waitingRoomData.get("requestType").equals("CancelJoinRequest")) {

                cancelJoiningRequest();
            }
            else if(waitingRoomData.get("requestType").equals("JoinAmount")){

                joinAmountRequest(jsonIncoming);
                System.out.println("Join result -> " + jsonIncoming);
            }
            else waitingRoom.incomingMsg(temp, this);
        }
        else if (jsonIncoming.get("requestType").equals("Version")){

            sendVersion();
        }
        else if (jsonIncoming.get("requestType").equals("ChangeImage")){

            changeImageRequest(jsonIncoming);
        }
        else if (jsonIncoming.get("requestType").equals("CurrentImage")){

            sendCurrentImage();
        }
    }

    //==============================================================================
    //
    //==============================================================================




    private void sendVersion() {

        JSONObject send = initiateJson();
        send.put("requestType", "Version");
        send.put("version", Server.pokerServer.version);

        sendMessage(send.toString());
    }

    private void changeImageRequest(JSONObject jsonObject){

        String link = jsonObject.getString("imageLink");
        user.setImageLink(link);

        Server.pokerServer.updateDatabase(user, "image");
    }

    private void sendCurrentImage(){

        JSONObject send = initiateJson();
        send.put("requestType", "CurrentImage");
        send.put("imageLink", user.getImageLink());
        sendMessage(send.toString());
    }




    //==============================================================================
    //
    //             LOGIN FUNCTIONS
    //
    //==============================================================================

    private void loginRequestResponse(JSONObject jsonObject) {

        JSONObject data = jsonObject.getJSONObject("data");

        String account_id = data.getString("account_id");
        String account_type = data.getString("account_type");
        String account_username = data.getString("account_username");
        String imageLink = data.getString("imageLink");

        user = Server.pokerServer.makeUser(account_id, account_type, account_username, imageLink);

        if (user != null) {
            user.setLoggedIn(true);
            Server.pokerServer.addInLoggedUser(this);
        }
        sendResponseLogin();
    }

    private void sendResponseLogin() {

        JSONObject send = initiateJson();
        send.put("requestType", "LoginResponse");
        send.put("transactionButtons", Server.pokerServer.showButton);

        if (user == null) {
            send.put("response", false);

            JSONObject temp = new JSONObject();
            send.put("data", temp);
        }
        else {
            send.put("response", true);

            JSONObject temp = User.UserToJson(user);
            temp.put("appData", getAppData());
            send.put("data", temp);
        }
        sendMessage(send.toString());
    }

    public void forceLogout(String msg){

        Server.pokerServer.removeFromLoggedUsers(this);
        sendForceLogout(msg);
    }

    private void sendForceLogout(String msg){

        JSONObject send = initiateJson();
        send.put("requestType", "ForceLogout");
        send.put("message", msg);
        sendMessage(send.toString());
    }

    private JSONObject getAppData(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("coinPricePerCrore", TransactionMethods.getCoinPricePerCrore());
        jsonObject.put("coinAmountOnBuy", TransactionMethods.getCoinAmountOnBuy());
        jsonObject.put("coinPriceOnBuy", TransactionMethods.getCoinPriceOnBuy());

        jsonObject.put("boardTypeCount", Server.pokerServer.getBoardTypeCount());
        jsonObject.put("minCallValue", Server.pokerServer.getMinCallValue());
        jsonObject.put("boardType", Server.pokerServer.getBoardType());
        jsonObject.put("minEntryValue", Server.pokerServer.getMinEntryValue());
        jsonObject.put("maxEntryValue", Server.pokerServer.getMaxEntryValue());
        jsonObject.put("mcr", Server.pokerServer.getMcr());

        jsonObject.put("expIncrease", User.getExpIncrease());
        jsonObject.put("rankString", User.getRankString());
        jsonObject.put("ranksValue", User.getRanksValue());

        return jsonObject;
    }

    private void sendBoardData() {
        JSONObject send = initiateJson();
        send.put("requestType", "LoadBoardData");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("boardTypeCount", Server.pokerServer.getBoardTypeCount());
        jsonObject.put("minCallValue", Server.pokerServer.getMinCallValue());
        jsonObject.put("boardType", Server.pokerServer.getBoardType());
        jsonObject.put("minEntryValue", Server.pokerServer.getMinEntryValue());
        jsonObject.put("maxEntryValue", Server.pokerServer.getMaxEntryValue());
        jsonObject.put("mcr", Server.pokerServer.getMcr());

        send.put("data", jsonObject);
        sendMessage(send.toString());
    }

    private void sendShopData(){

        JSONObject send = initiateJson();
        send.put("requestType", "LoadShopData");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("coinPricePerCrore", TransactionMethods.getCoinPricePerCrore());
        jsonObject.put("coinAmountOnBuy", TransactionMethods.getCoinAmountOnBuy());
        jsonObject.put("coinPriceOnBuy", TransactionMethods.getCoinPriceOnBuy());

        send.put("data", jsonObject);

        JSONArray array = new JSONArray();
        for(TransactionNumber x : Server.pokerServer.transactionNumbers)
            array.put(x.getJSON());

        send.put("Numbers", array);
        send.put("requestLeft", Server.pokerServer.maxPendingReq - Server.pokerServer.getDb().getPendingTransactionCount(user.getId()));
        sendMessage(send.toString());

    }

    public void sendResponseLogout() {

        JSONObject send = initiateJson();
        send.put("requestType", "LogoutResponse");

        if (user == null) send.put("success", false);
        else {
            send.put("success", true);
            Server.pokerServer.removeFromLoggedUsers(this);
        }

        sendMessage(send.toString());
    }

    private void sendOwnData() {

        JSONObject send = initiateJson();
        send.put("requestType", "UpdateOwnResponse");

        if (user == null) {
            send.put("response", false);

            JSONObject temp = new JSONObject();
            send.put("data", temp);
        } else {
            send.put("response", true);

            JSONObject temp = User.UserToJson(user);
            send.put("data", temp);
        }
        sendMessage(send.toString());
    }

    private void sendOwnInGameData() {

        JSONObject send = initiateJson();
        send.put("requestType", "UpdateOwnInGameResponse");

        if (user == null || gameThread == null) {
            send.put("response", false);

            JSONObject temp = new JSONObject();
            send.put("data", temp);
        } else {
            send.put("response", true);

            JSONObject temp = User.UserToJsonInGame(user);
            send.put("data", temp);
        }
        sendMessage(send.toString());
    }

    public void closeEverything() {

        if(! active) return ;
        active = false;

        try {
            System.out. println("A Connection got removed");

            if(connectionChecker != null) connectionChecker.cancel();
            if(connectionChecketWait != null) connectionChecketWait.cancel();

            Server.pokerServer.removeFromCasualConnections(this);
            if(session != null && session.isOpen()) session.close();

        } catch (Exception e) {
            System.out. println("Error in closing connection in Server side, error -> " + e + " in " + user.getUsername());

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
        }
    }

    //==============================================================================
    //
    //==============================================================================


    //==============================================================================
    //
    //             COIN ADDING FUNCTIONS
    //
    //      BUY COIN, ADD COIN BY WATCHING VIDEO, ADD COIN BY LOGGING IN
    //
    //==============================================================================




    private void buyCoinRequest(JSONObject temp) {

        JSONObject tempJson = temp.getJSONObject("data");

        int id = user.getId();
        int count = Server.pokerServer.getDb().getPendingTransactionCount(id);

        if(count == Server.pokerServer.maxPendingReq) {
            sendTransactionRequestResponse(false, 0, "You have reached max pending transaction request limit.");
            return ;
        }

        String type = "buy";
        String method = tempJson.getString("method");
        String transactionId = tempJson.getString("transactionId");
        long coinAmount = tempJson.getLong("coinAmount");
        Date requestTime = User.stringToDate(tempJson.getString("requestTime"));
        String sender = tempJson.getString("sender");
        TransactionNumber receiver = new TransactionNumber(new JSONObject(tempJson.getString("receiver")));
        String receiverNumber = receiver.getNumber();
        double price = TransactionMethods.getCurrencyAmount(coinAmount, "buy");

        sendTransactionRequestResponse(true, Server.pokerServer.maxPendingReq - count - 1, "Transaction request added.");
        Server.pokerServer.getDb().addPendingTransaction(id, type, method, transactionId, coinAmount, price, requestTime, sender, receiverNumber);
    }

    private void withdrawCoinRequest(JSONObject temp){

        JSONObject tempJson = temp.getJSONObject("data");

        int id = user.getId();
        int count = Server.pokerServer.getDb().getPendingTransactionCount(id);

        if(count == Server.pokerServer.maxPendingReq) {
            sendTransactionRequestResponse(false, 0, "You have reached max pending transaction request limit.");
            return ;
        }

        String type = "withdraw";
        String method = tempJson.getString("method");
        String transactionId = "";
        long coinAmount = tempJson.getLong("coinAmount");
        Date requestTime = User.stringToDate(tempJson.getString("requestTime"));
        String sender = "";
        String receiverNumber = tempJson.getString("receiver");
        double price = TransactionMethods.getCurrencyAmount(coinAmount, "withdraw");

        if(coinAmount > user.getCurrentCoin()) {
            sendTransactionRequestResponse(false, Server.pokerServer.maxPendingReq - count, "Invalid request, insufficient coin!");
        }
        else{
            user.setCurrentCoin(user.getCurrentCoin() - coinAmount);
            Server.pokerServer.getDb().updateUser(user, "removeWithdrawCoin");

            sendTransactionRequestResponse(true, Server.pokerServer.maxPendingReq - count - 1, "Transaction request added.");
            Server.pokerServer.getDb().addPendingTransaction(id, type, method, transactionId, coinAmount, price, requestTime, sender, receiverNumber);
        }
    }

    private void sendTransactionRequestResponse(boolean added, int reqLeft, String msg){
        JSONObject send = initiateJson();

        send.put("requestType", "AddTransactionResponse");
        send.put("success", added);
        send.put("requestLeft", reqLeft);
        send.put("currentCoin", user.getCurrentCoin());
        send.put("message", msg);

        sendMessage(send.toString());
    }

    private void allTransactionsResponse(){

        JSONObject send = initiateJson();

        send.put("requestType", "AllTransactionsResponse");
        send.put("data", Server.pokerServer.getAllTransactionsOfUser(user));

        sendMessage(send.toString());
    }

    private void getNotifications(){

        ArrayList list = Server.pokerServer.userUnsentNotifications(user);
        sendNotifications(list);
    }

    public void sendNotifications(ArrayList<Notification> notifications){

        JSONObject send = initiateJson();
        JSONArray array = new JSONArray();

        for(int i=0; i<notifications.size(); i++) {

            array.put(notifications.get(i).getJson());

            long addCoin = notifications.get(i).getCoinAdded();
            if(addCoin > 0){
                user.setCurrentCoin(user.getCurrentCoin() + addCoin);
                Server.pokerServer.getDb().updateUser(user, "addCoin");
            }
        }

        send.put("requestType", "ShowNotifications");
        send.put("data", array);
        sendMessage(send.toString());
    }


    private void addCoinVideoResponse(String requestTime) {

        Date rqTime = User.stringToDate(requestTime);

        int reqDay = rqTime.getDay();
        int prevDay = user.getLastCoinVideoAvailableTime().getDay();

        int reqMonth = rqTime.getMonth();
        int prevMonth = user.getLastCoinVideoAvailableTime().getMonth();

        int reqYear = rqTime.getYear();
        int prevYear = user.getLastCoinVideoAvailableTime().getYear();

        if (reqDay != prevDay || reqMonth != prevMonth || reqYear != prevYear) {
            user.setLastCoinVideoAvailableTime(rqTime);
            user.setCoinVideoCount(Server.pokerServer.dailyCoinVideoCount);
        }

        if (user.getCoinVideoCount() > 0) {
            user.setCoinVideoCount(user.getCoinVideoCount() - 1);
            user.setCurrentCoin(user.getCurrentCoin() + Server.pokerServer.eachVideoCoin);
            Server.pokerServer.updateDatabase(user, "addCoinVideo");
            sendAddCoinVideoResponse(true);
        }
        else sendAddCoinVideoResponse(false);
    }

    private void sendAddCoinVideoResponse(boolean success) {

        long v = 0;
        if (success) v = Server.pokerServer.eachVideoCoin;

        JSONObject send = initiateJson();

        send.put("requestType", "AddCoinVideoResponse");
        send.put("success", success);
        send.put("currentCoin", user.getCurrentCoin());
        send.put("lastCoinVideoAvailableTime", user.getLastCoinVideoAvailableTime());
        send.put("coinVideoCount", user.getCoinVideoCount());
        send.put("coinAdded", v);

        if (success) send.put("message", "Added coin " + Server.pokerServer.eachVideoCoin);
        else send.put("message", "Invalid request");

        sendMessage(send.toString());
    }


    private void addFreeCoinResponse(String requestTime) {

        Date rqTime = User.stringToDate(requestTime);

        int reqDay = rqTime.getDay();
        int prevDay = user.getLastFreeCoinTime().getDay();

        int reqMonth = rqTime.getMonth();
        int prevMonth = user.getLastFreeCoinTime().getMonth();

        int reqYear = rqTime.getYear();
        int prevYear = user.getLastFreeCoinTime().getYear();

        if (reqDay != prevDay || reqMonth != prevMonth || reqYear != prevYear) {
            user.setLastFreeCoinTime(rqTime);
            user.setCurrentCoin(user.getCurrentCoin() + Server.pokerServer.freeLoginCoin);
            Server.pokerServer.updateDatabase(user, "addLoginCoin");
            sendAddFreeCoinResponse(true);
        }
        else sendAddFreeCoinResponse(false);
    }

    private void sendAddFreeCoinResponse(boolean success) {

        long v = 0;
        if (success) v = Server.pokerServer.freeLoginCoin;

        JSONObject send = initiateJson();

        send.put("requestType", "AddFreeCoinResponse");
        send.put("success", success);
        send.put("currentCoin", user.getCurrentCoin());
        send.put("lastFreeCoinTime", user.getLastFreeCoinTime());
        send.put("coinAdded", v);

        if (success) send.put("message", "Added coin " + Server.pokerServer.freeLoginCoin);
        else send.put("message", "Invalid request");

        sendMessage(send.toString());
    }


    //==============================================================================
    //
    //==============================================================================






    //==============================================================================
    //
    //             JOIN A GAME REQUEST FUNCTIONS
    //
    //==============================================================================

    public void requestJoin(boolean addInQueue, JSONObject temp) {

        if (addInQueue) {

            int gameId = temp.getInt("gameId");
            int gameCode = temp.getInt("gameCode");
            String boardType = temp.getString("boardType");
            long minEntryValue = temp.getLong("minEntryValue");
            long minCallValue = temp.getLong("minCallValue");
            int owner_id = temp.getInt("owner_id");
            int seatPosition = temp.getInt("seatPosition");
            long boardCoin = temp.getLong("boardCoin");

            user.initializeGameData(gameId, gameCode, boardType, minEntryValue, minCallValue, owner_id, seatPosition, boardCoin);
            Server.pokerServer.addInPendingQueue(this);
        }

        sendJoinResponse("Join", "waiting in the queue");
    }

    private void sendJoinResponse(String type, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "JoinResponse");
        send.put("data", msg);
        send.put("dataType", "Waiting");

        sendMessage(send.toString());
    }


    public void requestAbort() {

        boolean done = Server.pokerServer.removeFromPendingQueue(this);

        if (done) {
            user.deInitializeGameData();
            sendAbortResponse("Abort request granted");
        }
    }

    public void sendAbortResponse(String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "AbortResponse");
        send.put("data", msg);
        send.put("dataType", "LeftQueue");

        sendMessage(send.toString());

    }


    public void leaveGameRoom() {

        gameThread = null;
        user.deInitializeGameData();
        Server.pokerServer.updateDatabase(user, "gameEnd");
    }

    //==============================================================================
    //
    //==============================================================================











    //==============================================================================
    //
    //          WAITING ROOM STUFF
    //
    //==============================================================================

    private void createWaitingRoom(JSONObject jsonObject) {

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");
        String boardType = waitingRoomData.getString("boardType");

        Server.pokerServer.createWaitingRoom(this, boardType);
    }

    public void sendCreateWaitingRoomResponse(int id, int code, String boardName){

        JSONObject send = initiateJson();
        send.put("requestType", "WaitingRoom");

        JSONObject waitingRoomData = new JSONObject();
        waitingRoomData.put("id", id);
        waitingRoomData.put("code", code);
        waitingRoomData.put("boardType", boardName);
        waitingRoomData.put("requestType", "CreateWaitingRoomResponse");

        send.put("waitingRoomData", waitingRoomData);
        send.put("data", "Created waiting room with id: " + id + " code " + code + " boardType " + boardName);
        send.put("dataType", "CreateWaitingRoomResponse");

        sendMessage(send.toString());
    }




    public void askToJoinWaitingRoom(int code) {

        WaitingRoom w = Server.pokerServer.findWaitingRoomByCode(code);

        if (w == null || w.getAtSeat(0) == null || w.getOwner() == null) sendAskJoinWaitingRoomByCodeResponse(false, code, "Waiting room with code " + code + " not found.");
        else if(w.getPlayerCount() == w.getMaxPlayerCount()) sendAskJoinWaitingRoomByCodeResponse(false, code, "Waiting room with code " + code + " is full.");
        else {
            sendAskJoinWaitingRoomByCodeResponse(true, code, "Joining waiting room");

            waitingRoom = w;
            waitingRoom.askBoardCoin(this);
        }
    }

    private void sendAskJoinWaitingRoomByCodeResponse(boolean success, int code, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "AskJoinWaitingRoomByCodeResponse");
        temp.put("gameCode", code);
        temp.put("message", msg);
        temp.put("dataType", "JoinByCodeResponse");
        temp.put("success", success);

        send.put("waitingRoomData", temp);
        sendMessage(send.toString());
    }

    private void cancelJoiningRequest(){

        waitingRoom = null;
    }



    public void joinAmountRequest(JSONObject jsonObject){

        if(waitingRoom == null) sendAskJoinWaitingRoomByCodeResponse(false, -1, "Waiting room with code not found.");

        int code = waitingRoom.getGameCode();
        WaitingRoom w = Server.pokerServer.findWaitingRoomByCode(code);

        if(w == null) {
            sendAskJoinWaitingRoomByCodeResponse(false, code, "Waiting room with code " + code + " not found.");
            waitingRoom = null;
        }
        else if(w.getPlayerCount() == w.getMaxPlayerCount()) {
            sendAskJoinWaitingRoomByCodeResponse(false, code, "Waiting room with code " + code + " is full.");
            waitingRoom = null;
        }
        else waitingRoom.joinAmountRequest(jsonObject, this);
    }

    //==============================================================================
    //
    //==============================================================================













    //===========================================================================================
    //
    //              JOIN GAME BY CODE
    //
    //===========================================================================================

    public void askToJoinGameThread(int code) {

        GameThread g = Server.pokerServer.findGameThreadByCode(code);

        if(g == null) sendAskJoinGameThreadByCodeResponse(false, code, "Game room with code " + code + " not found");
        else if(g.isPrivate() == false) sendAskJoinGameThreadByCodeResponse(false, code, "Game room with code " + code + " is not private");
        else if(g.getPlayerCount() == g.getMaxPlayerCount()) sendAskJoinGameThreadByCodeResponse(false, code, "Game room with code " + code + " is full");
        else {
            sendAskJoinGameThreadByCodeResponse(true, code, "Joining game room");

            gameThread = g;
            gameThread.askBoardCoin(this);
        }
    }

    private void sendAskJoinGameThreadByCodeResponse(boolean success, int code, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "GameRoom");

        JSONObject temp = new JSONObject();

        temp.put("code", code);
        temp.put("message", msg);
        temp.put("success", success);
        temp.put("dataType", "JoinByCodeResponse");
        temp.put("gameRequest", "AskJoinGameThreadByCodeResponse");

        send.put("gameData", temp);
        sendMessage(send.toString());
    }

    private void cancelJoiningGameRequest(){

        gameThread = null;
    }


    private void joinGameAmountRequest(JSONObject jsonObject){

        JSONObject gameData = jsonObject.getJSONObject("gameData");
        int code = gameData.getInt("gameCode");
        long boardCoin = gameData.getLong("amount");

        GameThread g = Server.pokerServer.findGameThreadByCode(code);

        if(g == null) {
            sendAskJoinGameThreadByCodeResponse(false, code, "Game room with code " + code + " not found");
            gameThread = null;
        }
        else if(g.getPlayerCount() == g.getMaxPlayerCount()) {
            sendAskJoinGameThreadByCodeResponse(false, code, "Game room with code " + code + " is full");
            gameThread = null;
        }
        else {
            user.initializeGameData(gameThread.getGameId(), gameThread.getGameCode(), gameThread.getBoardType(),
                    gameThread.getMinEntryValue(), gameThread.getMinCallValue(), gameThread.getOwnerId(),
                    -1, boardCoin);

            gameThread.addInGameThread(this);
        }
    }

    //===========================================================================================
    //
    //===========================================================================================







    //===========================================================================================
    //
    //              INTERNET CONNECTION CHECKING
    //
    //===========================================================================================

    private void setupConnectionChecker(){

        if(connectionChecker != null) connectionChecker.cancel();
        if(connectionChecketWait != null) connectionChecketWait.cancel();

        connectionChecker = new Timer();
        connectionChecker.schedule(new TimerTask() {
            @Override
            public void run() {
                preConnectionCheck();
                sendConnectionChecker();
            }
        }, Server.pokerServer.getConnectionCheckDelay());
    }

    private void sendConnectionChecker(){
        JSONObject send = initiateJson();
        send.put("requestType", "CheckConnection");
        sendMessage(send.toString());
    }

    private void preConnectionCheck(){

        isConnected = false;
        connectionChecketWait = new Timer();
        connectionChecketWait.schedule(new TimerTask() {
            @Override
            public void run() {
                postConnectionCheck();
            }
        }, Server.pokerServer.getConnectionResponseDelay());
    }

    private void postConnectionCheck(){

        if(!isConnected) closeEverything();
        connectionChecketWait.cancel();
    }

    private void connectionCheckingResponse(){

        isConnected = true;
    }

    //===========================================================================================
    //
    //===========================================================================================







    //===========================================================================================
    //
    //      DUMMY FOR BOT CLIENTS
    //
    //===========================================================================================

    public void increaseBoardCoin(){

    }

    //===========================================================================================
    //
    //===========================================================================================




    //===================================================================================
    //
    //              SETTER AND GETTERS
    //
    //===================================================================================


    public int getLastGameCode() {
        return lastGameCode;
    }

    public void setLastGameCode(int lastGameCode) {
        this.lastGameCode = lastGameCode;
    }

    public String getIncoming() {
        return incoming;
    }

    public void setIncoming(String incoming) {
        this.incoming = incoming;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getWebSocketKey() {
        return webSocketKey;
    }

    public GameThread getGameThread() {
        return gameThread;
    }

    public void setGameThread(GameThread gameThread) {
        this.gameThread = gameThread;
    }

    public WaitingRoom getWaitingRoom() {
        return waitingRoom;
    }

    public void setWaitingRoom(WaitingRoom waitingRoom) {
        this.waitingRoom = waitingRoom;
    }

    public JSONObject getJsonIncoming() {
        return jsonIncoming;
    }

    //==============================================================================
    //
    //==============================================================================

}
