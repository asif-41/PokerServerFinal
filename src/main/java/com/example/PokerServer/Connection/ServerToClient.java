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

        Server.pokerServer.addTextInGui("A connection has been established");
    }

    @Override
    public void run() {

    }

    //============================================================
    //
    //============================================================


    //================================================================================
    //
    //          SETTING UP COMMUNICATIONS
    //
    //================================================================================

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
            loginRequestResponse(tempJson.getString("username"), tempJson.getString("password"));

        } else if (jsonIncoming.get("requestType").equals("LogoutRequest")) {

            //LOGOUT REQUESTect();

            sendResponseLogout();
        } else if (jsonIncoming.get("requestType").equals("BuyCoin")) {

            //REQUEST BUY COIN

            buyCoinRequest(jsonIncoming);
        } else if (jsonIncoming.get("requestType").equals("FriendsListRequest")) {

            //  Send friends list

            friendListRequestResponse();
        } else if (jsonIncoming.get("requestType").equals("JoinRequest")) {

            //JOIN GAME REQUEST TO SERVER
            JSONObject tempJson = jsonIncoming.getJSONObject("data");

            if (tempJson.getBoolean("requestIn") == true) {
                requestJoin(true, tempJson);
            } else {
                requestAbort();
            }
        } else if (jsonIncoming.get("requestType").equals("GameThread")) {
            gameThread.incomingMessage(temp, this);
        } else if (jsonIncoming.get("requestType").equals("WaitingRoom")) {

            JSONObject waitingRoomData = jsonIncoming.getJSONObject("waitingRoomData");

            if (waitingRoomData.get("requestType").equals("Create")) {

                createWaitingRoom(waitingRoomData);
            } else if (waitingRoomData.get("requestType").equals("AcceptInvitation")) {

                int code = waitingRoomData.getInt("roomCode");
                joinWaitingRoomByCode(code);

            } else waitingRoom.incomingMsg(temp, this);

        }
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
            sendRoomJoinRequestResponse(false, code, msg);
        } else if (w.getMaxPlayerCount() == w.getPlayerCount()) {
            msg = "Waiting room full";
            sendRoomJoinRequestResponse(false, code, msg);
        } else {
            msg = "Joining waiting room";
            sendRoomJoinRequestResponse(true, code, msg);
            w.addInvitedUser(this);
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

    public void closeEverything() {

        try {
            Server.pokerServer.addTextInGui("A Connection got removed");
            Server.pokerServer.removeFromCasualConnections(this);
            session.close();

        } catch (Exception e) {
            Server.pokerServer.addTextInGui("Error in closing connection in Server side, error -> " + e);
        }
    }

    public void leaveGameRoom() {

        gameThread = null;
        getUser().setSeatPosition(-1);

        updateGameDataOnJoinRequest(false, null);
    }

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

    private void updateGameDataOnJoinRequest(boolean isJoin, JSONObject temp) {

        if (user == null) return;

        if (isJoin) {
            user.setRoomCode(temp.getInt("roomCode"));
            user.setRoomId(temp.getInt("roomId"));
            user.setBoardType(temp.getString("boardType"));
            user.setBoardCoin(temp.getInt("entryAmount"));
        } else {
            user.setRoomCode(-1);
            user.setRoomId(-1);
            user.setBoardType("");
            user.setBoardCoin(-1);
        }
    }






    private JSONObject initiateResponse() {

        JSONObject send = new JSONObject();

        send.put("sender", "Server");
        send.put("ip", host);
        send.put("port", port);

        return send;
    }

    private void buyCoinRequest(JSONObject temp) {

        boolean usernameMatch = false;
        String username = jsonIncoming.getString("username");

        if (username.equals(user.getUsername())) usernameMatch = true;

        JSONObject tempJson = jsonIncoming.getJSONObject("data");
        validateBuyCoinRequest(tempJson.getInt("value"), tempJson.getString("method"), tempJson.getString("transactionId"), usernameMatch);
    }

    private void validateBuyCoinRequest(int value, String method, String transactionId, boolean usernameMatch) {

        //      AUTHENTICATE HERE


        user.setCurrentCoin(user.getCurrentCoin() + value);
        sendBuyCoinResponse(true, value + " coin buy successful");
    }

    private ArrayList<ServerToClient> loadFriendsList() {

        ArrayList friends = new ArrayList<User>();
        for (int i = 0; i < Server.pokerServer.loggedInUsers.size(); i++)
            friends.add(((ServerToClient) Server.pokerServer.loggedInUsers.get(i)).getUser());

        return friends;
    }

    public void requestJoin(boolean addInQueue, JSONObject temp) {

        if (addInQueue) {

            updateGameDataOnJoinRequest(true, temp);
            Server.pokerServer.addInPendingQueue(this);
        }

        joinResponse("Join", "waiting in the queue", false);
    }

    public void requestAbort() {

        boolean done = Server.pokerServer.removeFromPendingQueue(this);

        updateGameDataOnJoinRequest(false, null);
        if (done) joinResponse("Abort", "Abort request granted", false);
    }

    private void loginRequestResponse(String username, String password) {

        /*
        user = new User(incomingMsg[1], incomingMsg[2], 100000, 10000000);
        SERVER.loggedInUsers.add(this);

        sendMessage("Login okay " + incomingMsg[1] + " " + incomingMsg[2]  + " " +  100000 + " " + 10000000);

        */
        // HERE AUTHENTICATE


        user = new User(username, password, 100000, 10000000);
        Server.pokerServer.addInLoggedUser(this);
        sendResponseLogin();
    }

    private void sendResponseLogin() {

        JSONObject send = initiateResponse();
        send.put("requestType", "LoginResponse");

        if (user == null) send.put("response", false);
        else {
            send.put("response", true);

            JSONObject temp = new JSONObject();

            temp.put("username", user.getUsername());
            temp.put("password", user.getPassword());
            temp.put("currentCoin", user.getCurrentCoin());
            temp.put("coinWon", user.getCoinWon());

            send.put("data", temp);
        }

        sendMessage(send.toString());
    }

    private void sendResponseLogout() {

        JSONObject send = initiateResponse();
        send.put("requestType", "LogoutResponse");

        if (user == null) send.put("response", false);
        else {
            send.put("response", true);
            Server.pokerServer.removeFromLoggedUsers(this);
            user = null;
        }

        sendMessage(send.toString());
    }

    private void sendBuyCoinResponse(boolean response, String msg) {

        JSONObject send = initiateResponse();

        send.put("requestType", "BuyCoin");
        send.put("response", response);
        send.put("responseMsg", msg);
        send.put("currentCoin", user.getCurrentCoin());

        sendMessage(send.toString());
    }

    private void friendListRequestResponse() {

        //FRIENDS LIST REQUEST TO SERVER
        //Get friends list here
        ArrayList friends = loadFriendsList();


        JSONObject send = initiateResponse();

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

    private void joinResponse(String type, String msg, boolean isOwner) {

        JSONObject send = initiateResponse();

        send.put("requestType", type);
        send.put("data", msg);

        sendMessage(send.toString());
    }

    private void sendRoomJoinRequestResponse(boolean joining, int code, String msg) {

        JSONObject send = initiateResponse();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "JoinWaitingRoomResponse");
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
