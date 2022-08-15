package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.Objects.Card;
import com.example.PokerServer.Objects.Randomizer;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
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


    //          AI VARIABLES

    private int roundCount;
    private int roundAt;
    private int loseAt;
    private int aggression;
    private boolean doWin;

    private int aggression1Temp;                        //      0 mane call dibe, 1 mane raise dibe
    private long raiseValue;                            //      1 mane minimum value, 2 mane mid value, 3 mane max value
    private int loseCallCount;


    //  16-08-2022
    private int cardTill = 4;

    //============================================================
    //
    //============================================================


    //=============================================================================
    //
    //          CONSTRUCTOR
    //
    //=============================================================================

    private void biasInitialization(){

        roundCount = 0;
        roundAt = 0;
        loseAt = 0;
        aggression = 0;
        doWin = false;
        aggression1Temp = 0;
        raiseValue = 0;
        loseCallCount = 0;

        //  16-08-2022
        cardTill = 4;
    }

    public BotClient() {
        super(null);
        biasInitialization();

        this.port = super.getPort();
        this.host = super.getHost();
        this.jsonIncoming = super.getJsonIncoming();
        this.user = super.getUser();
        this.gameThread = super.getGameThread();
        increaseCoinTimer = null;
    }

    public BotClient(User user) {
        super(null);
        biasInitialization();

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

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }

            return ;
        }

        //  Client er incoming msg

        if (jsonIncoming.get("requestType").equals("GameRoom")) {

            JSONObject tempJson = jsonIncoming.getJSONObject("gameData");

            if (tempJson.get("gameRequest").equals("RoundStartMessage")) {

                roundStart();
            }
            else if (tempJson.get("gameRequest").equals("EnableGameButtons")) {

                enableGameButtons(jsonIncoming);
            }
        }

    }

    public void incomingMsg(String temp) {

        try {
            jsonIncoming = new JSONObject(temp);
        } catch (Exception e) {
            System.out.println("Error in getting json in server side\n" + e);

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
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

    private int getWinner(String[] powers){

        int winner = -1;

        int maxCnt = 0;
        int maxVal;
        int maxPos;

        int curVal;
        int curPos;

        String[][] splittedPowers = new String[powers.length][];
        for(int i=0; i<powers.length; i++) splittedPowers[i] = powers[i].split("\\.");

        int[] checker = new int[powers.length];

        for(int i=0; i<checker.length; i++) {
            checker[i] = 1;
            if(powers[i].equals("")) checker[i] = 0;
        }

        for(int level=0; level<6; level++){

            curPos = -1;
            curVal = -1;
            maxVal = -1;
            maxPos = -1;

            for(int i=0; i<checker.length; i++){

                if(checker[i] == 0) continue;

                if(splittedPowers[i][level].charAt(0) == '('){

                    Card temp = new Card(splittedPowers[i][level]);

                    curVal = temp.getValue();
                    curPos = temp.getLocation();
                }
                else curVal = Integer.valueOf(splittedPowers[i][level]);

                if(curVal > maxVal){
                    maxVal = curVal;
                    maxPos = curPos;
                }
                else if(curVal == maxVal && curPos > maxPos) maxPos = curPos;
            }

            winner = -1;
            maxCnt = 0;

            for(int i=0; i<checker.length; i++){
                if(checker[i] == 0) continue;

                if(splittedPowers[i][level].charAt(0) == '('){

                    Card temp = new Card(splittedPowers[i][level]);

                    curVal = temp.getValue();
                    curPos = temp.getLocation();
                }
                else curVal = Integer.valueOf(splittedPowers[i][level]);

                if(curVal == maxVal && curPos == maxPos) {
                    maxCnt++;
                    winner = i;
                }
                else checker[i] = 0;
            }

            if(maxCnt == 1) break;
        }

        return winner;
    }

    private int getLoser(String[] powers){

        int loser = -1;

        int minCnt = 0;
        int minVal;
        int minPos;

        int curVal;
        int curPos;

        String[][] splittedPowers = new String[powers.length][];
        for(int i=0; i<powers.length; i++) splittedPowers[i] = powers[i].split("\\.");

        int[] checker = new int[powers.length];

        for(int i=0; i<checker.length; i++) {
            checker[i] = 1;
            if(powers[i].equals("")) checker[i] = 0;
        }

        for(int level=0; level<6; level++){

            curPos = -1;
            curVal = -1;
            minVal = 100;
            minPos = 100;

            for(int i=0; i<checker.length; i++){

                if(checker[i] == 0) continue;

                if(splittedPowers[i][level].charAt(0) == '('){

                    Card temp = new Card(splittedPowers[i][level]);

                    curVal = temp.getValue();
                    curPos = temp.getLocation();
                }
                else curVal = Integer.valueOf(splittedPowers[i][level]);

                if(curVal < minVal){
                    minVal = curVal;
                    minPos = curPos;
                }
                else if(curVal == minVal && curPos < minPos) minPos = curPos;
            }

            loser = -1;
            minCnt = 0;

            for(int i=0; i<checker.length; i++){
                if(checker[i] == 0) continue;

                if(splittedPowers[i][level].charAt(0) == '('){

                    Card temp = new Card(splittedPowers[i][level]);

                    curVal = temp.getValue();
                    curPos = temp.getLocation();
                }
                else curVal = Integer.valueOf(splittedPowers[i][level]);

                if(curVal == minVal && curPos == minPos) {
                    minCnt++;
                    loser = i;
                }
                else checker[i] = 0;
            }

            if(minCnt == 1) break;
        }
        return loser;
    }




    private void biasCards(){

        if(gameThread == null) return ;

        int botLoc = gameThread.getBotLoc();
        ArrayList<Card> boardCards = gameThread.getBoardCards();
        ArrayList<Card[]> playerCards = gameThread.getPlayerCards();

        ArrayList temp = new ArrayList<Card>();
        for(Card x : boardCards) temp.add(x);

        temp.add(null);
        temp.add(null);

        String[] powers = new String[playerCards.size()];
        for(int i=0; i<powers.length; i++){

            if(playerCards.get(i) == null) powers[i] = "";
            else {
                temp.set(temp.size()-2, playerCards.get(i)[0]);
                temp.set(temp.size()-1, playerCards.get(i)[1]);

                powers[i] = Card.getPower(temp);
            }
        }

        if(doWin){
            int k = getWinner(powers);
            if(k != botLoc){
                User a = gameThread.getPlayer(k);

                ArrayList tempCards = user.getPlayerCards();
                ArrayList tempCards2 = a.getPlayerCards();

                a.setPlayerCards(tempCards);
                user.setPlayerCards(tempCards2);
            }
        }
        else{
            int k = getLoser(powers);
            if(k != botLoc){
                User a = gameThread.getPlayer(k);

                ArrayList tempCards = user.getPlayerCards();
                ArrayList tempCards2 = a.getPlayerCards();

                a.setPlayerCards(tempCards);
                user.setPlayerCards(tempCards2);
            }
        }
    }

    //  cardTillPlayerWin
    private int chooseOption(JSONArray jsonArray){

        int choice = -1;

        int check = -1;
        int call = -1;
        int raise = -1;
        int fold = -1;
        int allIn = -1;

        long callCost = -1;

        for(int i=0; i<jsonArray.length(); i++){

            JSONObject j = jsonArray.getJSONObject(i);

            if(j.getString("name").equals("Call")) {
                call = i;
                callCost = j.getLong("cost");
            }
            if(j.getString("name").equals("Check")) check = i;
            if(j.getString("name").equals("Raise")) raise = i;
            if(j.getString("name").equals("Fold")) fold = i;
            if(j.getString("name").equals("AllIn")) allIn = i;
        }

        if(gameThread == null) return choice;

        if(! doWin){
            if(gameThread.getCycleCount() == 1){

                if(check != -1) choice = check;
                else if(call != -1 && gameThread.getCardShowed() <= cardTill) {
                    choice = call;
//                    if(callCost > 2*gameThread.getMinCallValue()) choice = fold;
//                    else choice = call;
                }
                else choice = fold;
            }
            else{
                if(check != -1) choice = check;
                else if(call != -1 && gameThread.getCardShowed() <= cardTill) {
                    choice = call;


//                        if(gameThread.getRoundCall() > 2*gameThread.getMinCallValue()) choice = fold;
//                        else if(loseCallCount < 1) {
//                            choice = call;
//                            loseCallCount++;
//                        }
//                        else choice = fold;

                }
                else choice = fold;
            }
        }
        else{
            if(aggression == 1){

                if(gameThread.getCycleCount() == 1){

                    if(check != -1) choice = check;
                    else if(call != -1) choice = call;
                    else if(allIn != -1) choice = allIn;
                }
                else {
                    if(check != -1) choice = check;
                    else if(call != -1 && raise != -1){

                        if(aggression1Temp == 1) {
                            choice = raise;
                            raiseValue = 1;
                        }
                        else choice = call;

                        aggression1Temp = 1-aggression1Temp;
                    }
                    else if(raise != -1){
                        choice = raise;
                        raiseValue = 1;
                        aggression1Temp = 0;
                    }
                    else if(allIn != -1) choice = allIn;
                    else choice = fold;
                }

            }
            else if(aggression == 2){

                if(gameThread.getCycleCount() == 1){

                    if(check != -1) choice = check;
                    else if(call != -1) choice = call;
                    else if(allIn != -1) choice = allIn;
                }
                else{
                    if(call != -1 && raise != -1){

                        if(aggression1Temp == 1){
                            choice = raise;
                            raiseValue = Randomizer.one(2) + 2;
                        }
                        else choice = call;

                        aggression1Temp = 1 - aggression1Temp;
                    }
                    else if(raise != -1) {
                        choice = raise;
                        raiseValue = Randomizer.one(2) + 2;
                        aggression1Temp = 0;
                    }
                    else if(allIn != -1) choice = allIn;
                    else choice = fold;
                }
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

        //cardTill = 4;
        //decideAction();
        biasCards();
    }

    private void enableGameButtons(JSONObject jsonObject){

        JSONArray temp = jsonObject.getJSONArray("data");
        int choice = chooseOption(temp);

        if(choice == -1) return;
        JSONObject option = temp.getJSONObject(choice);

        new Thread(new Runnable() {
            @Override
            public void run() {
                delayBot();

                if(gameThread == null || !gameThread.isGameRunning()) return;

                if(option.getString("name").equals("Call")) sendGameThreadCallRequest();
                else if(option.getString("name").equals("Raise")){

                    long cost;

                    if(raiseValue == 2){

                        int step;
                        long min = option.getLong("cost");
                        long max = option.getLong("maxCost");
                        long stepSize = option.getLong("stepSize");

                        if(min == max) step = 0;
                        else{
                            step = (int) ( (max - min + stepSize) / stepSize );
                        }

                        if(step == 0) cost = min;
                        else if(step == 1) cost = max;
                        else {

                            int k = Randomizer.one(step) + 1;
                            cost = Math.min( min + k*stepSize, max);
                        }
                    }
                    else if(raiseValue == 3) cost = option.getLong("maxCost");
                    else cost = option.getLong("cost");

                    sendGameThreadRaiseRequest(cost);
                }
                else if(option.getString("name").equals("AllIn")) sendGameThreadAllInRequest();
                else if(option.getString("name").equals("Check")) sendGameThreadCheckRequest();
                else if(option.getString("name").equals("Fold")) sendGameThreadFoldRequest();
            }
        }).start();
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

        if(increaseCoinTimer != null) increaseCoinTimer.cancel();

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

        int x = (int) (Server.pokerServer.botTurnDelayMin / 1000);      //2
        int y = (int) (Server.pokerServer.botTurnDelayMax / 1000);      //5

        int t = Randomizer.one(y-x+1) + x;
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

    public void setGameThread(GameThread gameThread) { this.gameThread = gameThread; }

    public int getCardTill() {
        return cardTill;
    }

    public void setCardTill(int cardTill) {
        this.cardTill = cardTill;
    }

    public boolean isDoWin() {
        return doWin;
    }

    public void setDoWin(boolean doWin) {
        this.doWin = doWin;
    }

    public int getAggression() {
        return aggression;
    }

    public void setAggression(int aggression) {
        this.aggression = aggression;
    }

    //==============================================================================
    //
    //==============================================================================

}
