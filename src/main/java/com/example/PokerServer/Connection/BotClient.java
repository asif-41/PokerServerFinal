package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.Objects.Randomizer;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.temporal.ChronoUnit.SECONDS;

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

    private Timer increaseCoinTimer;
    private long increaseAmount;

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
        increaseCoinTimer = null;
    }

    public BotClient(User user) {
        super(null);

        this.port = super.getPort();
        this.host = super.getHost();
        this.jsonIncoming = super.getJsonIncoming();
        this.user = user;
        this.gameThread = super.getGameThread();
        increaseCoinTimer = null;
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
            jsonIncoming = new JSONObject(temp);

        } catch (Exception e) {
            System.out. println("Error for send message in bot -> " + user.getUsername());
            System.out. println("Error in sending data from server -> " + e);

            return ;
        }

        if (jsonIncoming.get("requestType").equals("GameRoom")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("gameData");

            if (tempJson.get("gameRequest").equals("RoundStartMessage")) {

                roundStart();
            }
            else if (tempJson.get("gameRequest").equals("EnableGameButtons")) {

                enableGameButtons(jsonIncoming);
            }


        }





//            else if (tempJson.get("gameRequest").equals("LeaveGame")) {
//
//                leaveGameRoom();
//
//                disableGameButtons();
//                exitButton.setEnabled(false);
//                joinGame.setText("Join");
//                addTextInGui("Leaving game");
//            }

    }

    public void incomingMsg(String temp) {

        try {
            jsonIncoming = new JSONObject(temp);
        } catch (Exception e) {
            System.out.println("Error in getting json in server side\n" + e);
            jsonIncoming = null;
            return;
        }

        if (jsonIncoming.get("requestType").equals("GameThread")) {
            gameThread.incomingMessage(temp, this);
        }
    }

    //==============================================================================
    //
    //==============================================================================


















    //==============================================================================
    //
    //             LGOUT FUNCTIONS
    //
    //==============================================================================

    public void forceLogout(String msg){

        Server.pokerServer.removeFromBotClients(this);
        System.out. println("A bot has been removed");
    }

    //==============================================================================
    //
    //==============================================================================























    //==============================================================================
    //
    //             AI
    //
    //==============================================================================

    private void biasCards(){

    }

    public int chooseOption(JSONArray jsonArray){

        int choice = -1;

        for(int i=0; i<jsonArray.length(); i++){

            JSONObject j = jsonArray.getJSONObject(i);

            if(j.getString("name").equals("Call")) {
                choice = i;
                break;
            }
            if(j.getString("name").equals("Check")) {
                choice = i;
                break;
            }
            if(j.getString("name").equals("Raise")) {
                choice = i;
                break;
            }
            if(j.getString("name").equals("Fold")) {
                choice = i;
                break;
            }
        }

        return choice;
    }

    //==============================================================================
    //
    //==============================================================================















    //==============================================================================
    //
    //      INCREASE BOARD COIN
    //
    //==============================================================================

    private long getIncreaseAmount(){

        int boardLoc = Server.pokerServer.getBoardKey(user.getBoardType());

        if(boardLoc < 0) return 0;
        long max = Server.pokerServer.getMaxEntryValue()[boardLoc];
        long min = Server.pokerServer.getMinEntryValue()[boardLoc];

        if(user.getCurrentCoin() >= 2*min){

            long k = user.getCurrentCoin() / min;        // greater than or equal 2
            long kk = k/2;                              //  greater than or equal 1

            long amount = Math.min(kk*min, max-min);

            user.setBoardCoin(user.getBoardCoin() + amount);
            user.setCurrentCoin(user.getCurrentCoin() - amount);

            return 0;
        }
        else{
            long k = (max / min) - 1;
            int kk = Randomizer.one((int)(k-1)) + 1 + 1;

            long amount = kk*min;
            return amount;
        }

    }

    public void increaseBoardCoin(){

        increaseAmount = getIncreaseAmount();
        long delay = ((long) Randomizer.one(7) + 3) * 1000;

        if(increaseCoinTimer != null) return;

        increaseCoinTimer = new Timer();
        increaseCoinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                increaseBoardCoinDone();
            }
        }, delay);
    }

    private void increaseBoardCoinDone(){

        if(increaseCoinTimer != null) {
            increaseCoinTimer.cancel();
            increaseCoinTimer = null;
        }
        if(user == null) return ;

        double x = ( (double) (Randomizer.one(10) + 1) ) / 10;
        long inc = (long) (x * increaseAmount);

        user.setBoardCoin(user.getBoardCoin() + increaseAmount);
        user.setCurrentCoin(user.getCurrentCoin() + inc );

        if(user.isInGame() && !gameThread.isGameRunning()) sendTryStartCurrentGameRequest();
    }

    private void sendTryStartCurrentGameRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameThread.getGameId());
        tempJson.put("gameCode", gameThread.getGameCode());
        tempJson.put("requestType", "StartNewRound");

        send.put("gameData", tempJson);

        incomingMsg(send.toString());
    }

    //==============================================================================
    //
    //==============================================================================











    //==============================================================================
    //
    //          GAME FUNCTIONS
    //
    //==============================================================================

    private void roundStart(){

        //System.out.println("Bias the cards according to AI");
        biasCards();
    }

    private void enableGameButtons(JSONObject jsonObject){

        JSONArray temp = jsonObject.getJSONArray("data");
        int choice = chooseOption(temp);

        JSONObject option = temp.getJSONObject(choice);


        //System.out.println("Game buttons: -> " + jsonObject);
        //System.out.println("option -> " + option);

        delayBot();
        //System.out.println("Sending option from bot");

        if(option.getString("name").equals("Call")) sendGameThreadCallRequest();
        else if(option.getString("name").equals("Raise")){
            long cost = option.getLong("cost");
            sendGameThreadRaiseRequest(cost);
        }
        else if(option.getString("name").equals("AllIn")) sendGameThreadAllInRequest();
        else if(option.getString("name").equals("Check")) sendGameThreadCheckRequest();
        else if(option.getString("name").equals("Fold")) sendGameThreadFoldRequest();
        //System.out.println("Sending done");
    }




    //==============================================================================
    //
    //==============================================================================








    //==============================================================================
    //
    //          GAME CALL FUNCTIONS
    //
    //==============================================================================

    private void sendGameThreadCallRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Call");

        send.put("gameData", tempJson);
        incomingMsg(send.toString());
    }

    private void sendGameThreadRaiseRequest(long value) {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();

        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Raise");
        tempJson.put("cost", value);

        send.put("gameData", tempJson);
        incomingMsg(send.toString());
    }

    private void sendGameThreadAllInRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "AllIn");

        send.put("gameData", tempJson);
        incomingMsg(send.toString());
    }

    private void sendGameThreadCheckRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Check");

        send.put("gameData", tempJson);
        incomingMsg(send.toString());
    }

    private void sendGameThreadFoldRequest() {

        JSONObject send = initiateJson();

        send.put("username", user.getUsername());
        send.put("requestType", "GameThread");

        JSONObject tempJson = new JSONObject();
        tempJson.put("requestType", "GameCall");
        tempJson.put("call", "Fold");

        send.put("gameData", tempJson);
        incomingMsg(send.toString());
    }

    //==============================================================================
    //
    //==============================================================================














    //==============================================================================
    //
    //             JOIN/LEAVE A GAME REQUEST FUNCTIONS
    //
    //==============================================================================

    public void leaveGameRoom() {

        gameThread = null;
        user.deInitializeGameData();
        Server.pokerServer.removeFromBotClients(this);
    }

    //==============================================================================
    //
    //==============================================================================





    //===================================================================================
    //
    //              SETTER AND GETTERS
    //
    //===================================================================================

    private void delayBot(){

        int x = (int) (Server.pokerServer.botTurnDelayMin / 1000);      //5
        int y = (int) (Server.pokerServer.botTurnDelayMax / 1000);      //10

        int t = Randomizer.one(y-x+1) + 5;
        waitBot(t);
    }

    private void waitBot(double sec){

        Long secMilli = (long) (sec * 1000);

        LocalTime start = LocalTime.now();

        while(true){

            LocalTime cur = LocalTime.now();
            long diff = SECONDS.between(start, cur) * 1000;

            if(diff >= secMilli) break;
        }
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
