package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.Objects.User;
import org.json.JSONObject;

public class BotClient extends ServerToClient {


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

    private JSONObject jsonIncoming;
    private User user;                                  //      USER, NULL IF NOT LOGGED IN
    private GameThread gameThread;                      //      GAME THREAD, NULL IF NOT IN ANY GAME

    //============================================================
    //
    //============================================================


    //=============================================================================
    //
    //          CONSTRUCTOR
    //
    //=============================================================================


    public BotClient() {
        super(null);

        this.port = super.getPort();
        this.host = super.getHost();
        this.jsonIncoming = super.getJsonIncoming();
        this.user = super.getUser();
        this.gameThread = super.getGameThread();
    }

    public BotClient(User user) {
        super(null);

        this.port = super.getPort();
        this.host = super.getHost();
        this.jsonIncoming = super.getJsonIncoming();
        this.user = user;
        this.gameThread = super.getGameThread();
    }

    public BotClient(User user, GameThread g) {
        super(null);

        this.port = super.getPort();
        this.host = super.getHost();
        this.jsonIncoming = super.getJsonIncoming();
        this.user = user;
        this.gameThread = g;
    }

    @Override
    public void run() {

        System.out. println("A bot has been created");
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

        } catch (Exception e) {
            System.out. println("Error for connection in bot -> " + user.getUsername());
            System.out. println("Error in sending data from server -> " + e);
        }
    }

    public void incomingMsg(String temp) {

        try {
            jsonIncoming = new JSONObject(temp);
        } catch (Exception e) {
            System.out.println("Error in getting json in server side\n" + e);
            jsonIncoming = null;
            return;
        }

        if (jsonIncoming.get("requestType").equals("JoinRequest")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("data");
            requestJoin(true, tempJson);
        }
        else if (jsonIncoming.get("requestType").equals("GameThread")) {

            JSONObject gameData = jsonIncoming.getJSONObject("gameData");

            gameThread.incomingMessage(temp, this);
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

    public void closeEverything() {

        try {
            System.out. println("A bot has been removed");

            Server.pokerServer.removeFromCasualConnections(this);

        } catch (Exception e) {
            System.out. println("Error in closing connection in Server side, error -> " + e);
        }
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

    public GameThread getGameThread() {
        return gameThread;
    }

    public void setGameThread(GameThread gameThread) {
        this.gameThread = gameThread;
    }


    //==============================================================================
    //
    //==============================================================================

}
