package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

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


    private JSONObject jsonIncoming;
    private User user;                                  //      USER, NULL IF NOT LOGGED IN
    private GameThread gameThread;                      //      GAME THREAD, NULL IF NOT IN ANY GAME
    private WaitingRoom waitingRoom;

    //============================================================
    //
    //============================================================


    //=============================================================================
    //
    //          CONSTRUCTOR
    //
    //=============================================================================


    public ServerToClient(WebSocketSession session) {

        jsonIncoming = null;
        user = null;
        gameThread = null;
        waitingRoom = null;

        this.session = session;
        webSocketKey = session.getHandshakeHeaders().get("sec-websocket-key").get(0);
        try {

            port = session.getRemoteAddress().getPort();
            host = InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            Server.pokerServer.addTextInGui("Exception in fetching ip -> " + e);
        }

    }

    @Override
    public void run() {

        Server.pokerServer.addTextInGui("A connection has been established");
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

        System.out.println("sending -> " + temp);

        try {
            session.sendMessage(new TextMessage(temp));

        } catch (Exception e) {

            Server.pokerServer.addTextInGui("Error for connection with key -> " + webSocketKey);
            Server.pokerServer.addTextInGui("Error in sending data from server -> " + e);
        }

    }

    public void incomingMsg(String temp) {

        try {
            jsonIncoming = new JSONObject(temp);
            System.out.println(jsonIncoming);

        } catch (Exception e) {
            System.out.println("Error in getting json in server side\n" + e);
            jsonIncoming = null;
            return;
        }


        if (jsonIncoming.get("requestType").equals("LoginRequest")) {

            //LOGIN REQUEST

            JSONObject tempJson = jsonIncoming.getJSONObject("data");
            loginRequestResponse(tempJson.getString("account_id"), tempJson.getString("account_type"), tempJson.getString("account_username"));

        } else if (jsonIncoming.get("requestType").equals("LogoutRequest")) {

            //LOGOUT REQUEST();

            sendResponseLogout();
        } else if (jsonIncoming.get("requestType").equals("BuyCoinRequest")) {

            //REQUEST BUY COIN

            buyCoinRequest(jsonIncoming);
        } else if (jsonIncoming.get("requestType").equals("AddCoinVideoRequest")) {

            addCoinVideoResponse(jsonIncoming.getString("requestTime"));
        } else if (jsonIncoming.get("requestType").equals("AddFreeCoinRequest")) {

            addFreeCoinResponse(jsonIncoming.getString("requestTime"));
        } else if (jsonIncoming.get("requestType").equals("FriendsListRequest")) {

            //  Send friends list

            friendListRequestResponse();
        } else if (jsonIncoming.get("requestType").equals("JoinRequest")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("data");
            requestJoin(true, tempJson);
        } else if (jsonIncoming.get("requestType").equals("AbortRequest")) {

            requestAbort();
        } else if (jsonIncoming.get("requestType").equals("UpdateOwn")) {

            sendOwnData();
        } else if (jsonIncoming.get("requestType").equals("UpdateOwnInGame")) {

            sendOwnInGameData();
        } else if (jsonIncoming.get("requestType").equals("GameThread")) {
            gameThread.incomingMessage(temp, this);
        } else if (jsonIncoming.get("requestType").equals("WaitingRoom")) {

            JSONObject waitingRoomData = jsonIncoming.getJSONObject("waitingRoomData");

            if (waitingRoomData.get("requestType").equals("Create")) {

                createWaitingRoom(waitingRoomData);
            } else if (waitingRoomData.get("requestType").equals("AcceptInvitation")) {

                int code = waitingRoomData.getInt("roomCode");
                joinWaitingRoomByCode(code);

            } else if (waitingRoomData.get("requestType").equals("AskJoinWaitingRoomByCode")) {

                int code = waitingRoomData.getInt("roomCode");
                askToJoinWaitingRoom(code);
            } else waitingRoom.incomingMsg(temp, this);

        }
    }

    //==============================================================================
    //
    //==============================================================================




    
    //==============================================================================
    //
    //             LOGIN FUNCTIONS
    //
    //==============================================================================

    private void loginRequestResponse(String account_id, String account_type, String username) {

        user = Server.pokerServer.makeUser(account_id, account_type, username);

        if (user != null) {
            user.setLoggedIn(true);
            Server.pokerServer.addInLoggedUser(this);
        }
        sendResponseLogin();
    }

    private void sendResponseLogin() {

        JSONObject send = initiateJson();
        send.put("requestType", "LoginResponse");

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

    private void sendResponseLogout() {

        JSONObject send = initiateJson();
        send.put("requestType", "LogoutResponse");

        if (user == null) send.put("success", false);
        else {
            send.put("success", true);

            Server.pokerServer.removeFromLoggedUsers(this);
            user = null;
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

        try {

            Server.pokerServer.addTextInGui("A Connection got removed");
            Server.pokerServer.removeFromCasualConnections(this);
            session.close();

        } catch (Exception e) {
            Server.pokerServer.addTextInGui("Error in closing connection in Server side, error -> " + e);
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

    private boolean validateBuyCoinRequest(long value, String method, String transactionId) {

        //      AUTHENTICATE HERE

        return true;

    }

    private void buyCoinRequest(JSONObject temp) {

        JSONObject tempJson = jsonIncoming.getJSONObject("data");

        long value = tempJson.getLong("value");
        String method = tempJson.getString("method");
        String tr_id = tempJson.getString("transactionId");

        boolean validation = validateBuyCoinRequest(value, method, tr_id);
        String msg;

        if (!validation) msg = "Authentication failed";
        else {
            msg = "Coin buy successful";
            user.setCurrentCoin(user.getCurrentCoin() + value);
        }

        sendBuyCoinResponse(validation, msg);
    }

    private void sendBuyCoinResponse(boolean success, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "BuyCoinResponse");
        send.put("success", success);
        send.put("message", msg);
        send.put("currentCoin", user.getCurrentCoin());

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
            sendAddCoinVideoResponse(true);
        } else sendAddCoinVideoResponse(false);
    }

    private void sendAddCoinVideoResponse(boolean success) {

        long v = 0;
        if (success) v = Server.pokerServer.eachVideoCoin;

        JSONObject send = initiateJson();

        send.put("requestType", "AddCoinVideoResponse");
        send.put("success", success);
        send.put("currentCoin", user.getCurrentCoin());
        send.put("lastCoinVideoAvailableTime", user.getLastCoinVideoAvailableTime());
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
            sendAddFreeCoinResponse(true);
        } else sendAddFreeCoinResponse(false);
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

        System.out.println(user);
    }

    //==============================================================================
    //
    //==============================================================================











    //==============================================================================
    //
    //          WAITING ROOM STUFF
    //
    //==============================================================================

    private void createWaitingRoom(JSONObject waitingRoomData) {

        String boardType = waitingRoomData.getString("boardType");
        JSONArray usernames = waitingRoomData.getJSONArray("roomData");

        Server.pokerServer.createWaitingRoom(usernames, this, boardType);

    }

    private void joinWaitingRoomByCode(int code) {

        WaitingRoom w = Server.pokerServer.findWaitingRoomByCode(code);

        String msg = "";

        if (w == null) {
            msg = "Could not find waiting room";
            sendWaitingRoomJoinRequestResponse(false, code, msg);
        } else if (w.getMaxPlayerCount() == w.getPlayerCount()) {
            msg = "Waiting room full";
            sendWaitingRoomJoinRequestResponse(false, code, msg);
        } else {
            msg = "Joining waiting room";
            sendWaitingRoomJoinRequestResponse(true, code, msg);
            w.addInvitedUser(this);
        }
    }

    private void askToJoinWaitingRoom(int code) {

        WaitingRoom w = Server.pokerServer.findWaitingRoomByCode(code);

        if (w == null)
            sendAskJoinWaitingRoomByCodeResponse(false, code, "Waiting room with code " + code + " not found");
        else {

            sendAskJoinWaitingRoomByCodeResponse(true, code, "Waiting for owner's permission");
            w.sendAskOwnerForApproval(this);
        }

    }



    //==============================================================================
    //
    //==============================================================================


    //===========================================================================================
    //
    //              CLOSING CONNECTIONS
    //
    //===========================================================================================

    //===========================================================================================
    //
    //===========================================================================================


    //===================================================================================
    //
    //              SETTER AND GETTERS
    //
    //===================================================================================

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

    //==============================================================================
    //
    //==============================================================================


    //===================================================================================
    //
    //              JSON CODE
    //
    //===================================================================================







    private ArrayList<ServerToClient> loadFriendsList() {

        ArrayList loggedUsers = Server.pokerServer.getLoggedInUsers();
        ArrayList friends = new ArrayList<User>();

        for (int i = 0; i < loggedUsers.size(); i++)
            friends.add(((ServerToClient) loggedUsers.get(i)).getUser());

        return friends;
    }

    private void friendListRequestResponse() {

        //FRIENDS LIST REQUEST TO SERVER
        //Get friends list here
        ArrayList friends = loadFriendsList();


        JSONObject send = initiateJson();

        send.put("requestType", "FriendsList");

        JSONArray array = new JSONArray();

        for (int i = 0; i < friends.size(); i++) {

            User temp = (User) friends.get(i);
            JSONObject tempJson = new JSONObject();

            tempJson.put("username", temp.getUsername());
            tempJson.put("currentCoin", temp.getCurrentCoin());
            tempJson.put("coinWon", temp.getCoinWon());
            tempJson.put("level", temp.getLevel());

            array.put(tempJson);
        }

        send.put("data", array);
        sendMessage(send.toString());
    }

    private void sendWaitingRoomJoinRequestResponse(boolean joining, int code, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "JoinWaitingRoomResponse");
        temp.put("roomCode", code);
        temp.put("success", joining);
        temp.put("message", msg);

        send.put("waitingRoomData", temp);
        sendMessage(send.toString());
    }

    private void sendAskJoinWaitingRoomByCodeResponse(boolean joining, int code, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "AskJoinWaitingRoomByCodeResponse");
        temp.put("roomCode", code);
        temp.put("success", joining);
        temp.put("message", msg);

        send.put("waitingRoomData", temp);
        sendMessage(send.toString());
    }

/*
    private void sendCloseResponse(){

        JSONObject send = new JSONObject();

        send.put("sender", "Server");
        send.put("ip", SERVER.getIp());
        send.put("port", SERVER.getPort());
        send.put("requestType", "Close");
        send.put("response", true);
        send.put("responseMsg", "Closing Connection");

        sendMessage(send.toString());
        closeEverything();

    }
*/

    //==============================================================================
    //
    //==============================================================================
}
