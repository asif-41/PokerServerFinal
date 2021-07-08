package com.example.PokerServer.GameThread;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingRoom implements Runnable {

    private int gameId;
    private int gameCode;
    private String boardType;
    private long minEntryValue;
    private long minCallValue;

    private int playerCount;
    private int maxPlayerCount;

    private Timer startTimer;
    private Timer closeTimer;
    private boolean removingMultiple;
    private boolean closing;

    private ServerToClient owner;
    private ServerToClient seat[];

    private JSONObject jsonIncoming;


    public WaitingRoom(ServerToClient owner, int gameId, int gameCode, int maxPlayerCount, String boardType, long minEntryValue, long minCallValue) {

        startTimer = null;
        closeTimer = null;
        removingMultiple = false;
        closing = false;

        this.gameId = gameId;
        this.gameCode = gameCode;
        this.maxPlayerCount = maxPlayerCount;
        this.boardType = boardType;
        this.minEntryValue = minEntryValue;
        this.minCallValue = minCallValue;
        this.owner = owner;

        playerCount = 0;
        seat = new ServerToClient[maxPlayerCount];
    }

    @Override
    public void run() {

        askBoardCoin(owner);

        closeTimer = new Timer();
        closeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(playerCount == 0) closeEverything();
            }
        }, Server.pokerServer.waitingRoomWaitAtStart * 1000);
    }

    //==================================================================================================================
    //
    //==================================================================================================================








    //==================================================================================================================
    //
    //              COMMUNICATIONS
    //
    //==================================================================================================================

    private JSONObject initiateJson() {

        JSONObject send = new JSONObject();

        send.put("sender", "Server");
        send.put("ip", Server.pokerServer.getHost());
        send.put("port", Server.pokerServer.getPort());
        send.put("requestType", "WaitingRoom");
        if(owner != null) send.put("owner", owner.getUser().getUsername());

        return send;
    }

    private void sendMessage(int loc, String s) {

        ServerToClient temp = seat[loc];

        if (temp == null) return;
        temp.sendMessage(s);
    }

    private void sendMessage(ServerToClient ss, String s) {

        if (ss == null) return;
        ss.sendMessage(s);
    }

    private void sendMessageToAll(String s) {

        for (int i = 0; i < maxPlayerCount; i++) {
            sendMessage(i, s);
        }
    }


    //==================================================================================================================
    //
    //==================================================================================================================


    public void incomingMsg(String temp, ServerToClient from) {

        try {
            jsonIncoming = new JSONObject(temp);

        } catch (Exception e) {
            System.out.println("Error in getting json in waiting room in server side\n" + e);

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
            jsonIncoming = null;
            return;
        }

        if (jsonIncoming.get("requestType").equals("WaitingRoom")) {

            JSONObject waitingRoomData = jsonIncoming.getJSONObject("waitingRoomData");

            if(waitingRoomData.get("requestType").equals("RemoveFromWaitingRoom")){

                if(from == owner){

                    removeFromWaitingRoom(jsonIncoming);
                }
            }
            else if(waitingRoomData.get("requestType").equals("RemoveMeFromWaitingRoom")){

                int loc = waitingRoomData.getInt("seatPosition");
                removeFromWaitingRoom(loc);
            }
            else if(waitingRoomData.get("requestType").equals("EditBoardCoin")){

                editBoardCoin(jsonIncoming, from);
            }
            else if (waitingRoomData.get("requestType").equals("StartGame")) {

                startGame();
            }
        }

    }


    //==================================================================================================================
    //
    //==================================================================================================================










    //==================================================================================================================
    //
    //              NECESSARY FUNCTIONS
    //
    //==================================================================================================================

    private int getNextSeat(){

        int ret = -1;

        for(int i=0; i<maxPlayerCount; i++){
            if(seat[i] == null){
                ret = i;
                break;
            }
        }
        return ret;
    }

    //==================================================================================================================
    //
    //==================================================================================================================
















    //==================================================================================================================
    //
    //      INITIALIZING JOIN REQUESTS
    //
    //==================================================================================================================

    public void askBoardCoin(ServerToClient s){

        s.setWaitingRoom(this);
        sendAskBoardCoin(s);
    }

    private void sendAskBoardCoin(ServerToClient s){

        JSONObject send = initiateJson();
        JSONObject data = new JSONObject();

        data.put("gameId", gameId);
        data.put("gameCode", gameCode);
        data.put("boardType", boardType);
        data.put("minCallValue", minCallValue);
        data.put("minEntryValue", minEntryValue);
        data.put("requestType", "AskBoardCoin");
        data.put("dataType", "AskBoardCoin");

        send.put("waitingRoomData", data);

        s.sendMessage(send.toString());
    }




    private void editBoardCoin(JSONObject jsonObject, ServerToClient s){

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");
        long amount = waitingRoomData.getLong("amount");
        long current = s.getUser().getBoardCoin() + s.getUser().getCurrentCoin();

        if(amount > current) sendEditBoardCoinResponse(false, s);
        else{
            s.getUser().setBoardCoin(amount);
            s.getUser().setCurrentCoin(current - amount);
            sendEditBoardCoinResponse(true,  s);
            sendPlayersDataToAll();
        }
    }

    private void sendEditBoardCoinResponse(boolean success, ServerToClient s){

        JSONObject send = initiateJson();
        JSONObject data = new JSONObject();

        data.put("success", success);
        data.put("currentCoin", s.getUser().getCurrentCoin());
        data.put("boardCoin", s.getUser().getBoardCoin());
        data.put("requestType", "EditBoardCoinResponse");
        data.put("dataType", "EditBoardCoinResponse");

        send.put("waitingRoomData", data);

        s.sendMessage(send.toString());
    }




    public void joinAmountRequest(JSONObject jsonObject, ServerToClient s){

        JSONObject waitingRoomData = jsonObject.getJSONObject("waitingRoomData");
        long amount = waitingRoomData.getLong("amount");
        int seatPosition = getNextSeat();


        initiateUser(s, seatPosition, amount);
        sendInitiateUser(s, seatPosition, amount);
        sendPlayersDataToAll();
    }


    private void initiateUser(ServerToClient s, int seatPosition, long boardCoin){

        User tempUser = s.getUser();

        seat[seatPosition] = s;
        playerCount++;

        tempUser.initializeInvitationData(gameId, gameCode, maxPlayerCount, boardType, minEntryValue, minCallValue, owner.getUser().getId(), seatPosition, boardCoin);
    }

    private void sendInitiateUser(ServerToClient s, int seatPosition, long boardCoin){

        JSONObject send = initiateJson();

        JSONObject data = new JSONObject();

        data.put("gameId", gameId);
        data.put("gameCode", gameCode);
        data.put("boardType", boardType);
        data.put("minCallValue", minCallValue);
        data.put("minEntryValue", minEntryValue);
        data.put("ownerId", owner.getUser().getId());
        data.put("seatPosition", seatPosition);
        data.put("boardCoin", boardCoin);
        data.put("maxPlayerCount", maxPlayerCount);

        data.put("dataType", "InitializeUser");
        data.put("requestType", "InitializeUser");

        send.put("waitingRoomData", data);
        s.sendMessage(send.toString());
    }

    //==================================================================================================================
    //
    //==================================================================================================================







    //==================================================================================================================
    //
    //              REMOVE FROM WAITING ROOM
    //
    //==================================================================================================================

    private void removeFromWaitingRoom(JSONObject jsonObject){

        removingMultiple = true;
        JSONArray data = jsonObject.getJSONArray("data");

        for(int i=0; i<data.length(); i++) {

            sendRemoveFromWaitingRoomMessage(data.getInt(i), "Owner removed you from waiting room");
            removeFromWaitingRoom(data.getInt(i));
        }
        sendPlayersDataToAll();

        removingMultiple = false;
    }

    public void removeFromWaitingRoom(int seatPosition){

        if(seatPosition < 0) return ;
        if(seat[seatPosition] == null) return ;

        ServerToClient s = seat[seatPosition];
        s.setWaitingRoom(null);
        seat[seatPosition] = null;

        s.getUser().deInitializeInvitationData();

        owner = seat[0];
        checkIfRoomClosed();

        if(closing) return ;
        if(removingMultiple) return ;

        sendPlayersDataToAll();
    }


    private void sendRemoveFromWaitingRoomMessage(int loc, String message){

        if(seat[loc] == null) return ;

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "RemoveFromWaitingRoomResponse");
        temp.put("dataType", "RemoveFromWaitingRoomResponse");
        temp.put("message", message);

        send.put("waitingRoomData", temp);
        sendMessage(loc, send.toString());
    }


    private void checkIfRoomClosed(){

        if(owner == null && closing == false) closeEverything();
    }

    private void closeEverything() {

        if(closeTimer != null) closeTimer.cancel();
        closing = true;

        for (int i = 1; i < maxPlayerCount; i++) {

            if(seat[i] == null) continue;

            sendRemoveFromWaitingRoomMessage(i,"Owner closed the room");
            removeFromWaitingRoom(i);
        }
        removeFromWaitingRoom(0);
        Server.pokerServer.removeWaitingRoom(this);
    }

    //==================================================================================================================
    //
    //==================================================================================================================






    //==================================================================================================================
    //
    //              START GAME FROM WAITING ROOM
    //
    //==================================================================================================================

    private void startGame() {

        String msg;
        boolean willStart = false;

        if (playerCount > 1) willStart = true;

        if (!willStart) msg = "In sufficient players, player count must be greater than 1";
        else msg = "Will start game in " + Server.pokerServer.delayInStartingGame + " seconds";

        sendStartGameResponse(msg);


        if (willStart) {
            startTimer = new Timer();
            startTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    makeGame();
                }
            }, Server.pokerServer.delayInStartingGame * 1000);
        }
    }

    private void sendStartGameResponse(String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "StartGameResponse");
        temp.put("message", msg);

        send.put("waitingRoomData", temp);

        sendMessageToAll(send.toString());
    }

    private void makeGame() {

        if(startTimer != null) startTimer.cancel();

        if( ! (playerCount > 1) ) {
            sendStartGameResponse("In sufficient players, player count must be greater than 1");
            return ;
        }

        Server.pokerServer.makeGameThread(this);
    }

    //==================================================================================================================
    //
    //==================================================================================================================






    //==================================================================================================================
    //
    //              START GAME FROM WAITING ROOM
    //
    //==================================================================================================================

    private void sendPlayersDataToAll() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("requestType", "LoadPlayersData");

        send.put("waitingRoomData", tempJson);

        JSONArray array = new JSONArray();

        for (int i = 0; i < maxPlayerCount; i++) {

            if (seat[i] == null) continue;

            JSONObject temp = User.UserToJsonInGame(seat[i].getUser());
            array.put(temp);
        }
        send.put("data", array);
        sendMessageToAll(send.toString());
    }



    //==================================================================================================================
    //
    //==================================================================================================================









    //==================================================================================================================
    //
    //==================================================================================================================

    @Override
    public String toString() {
        String ret = "WaitingRoom{" +
                "gameId=" + gameId +
                ", gameCode=" + gameCode +
                ", boardType='" + boardType + '\'' +
                ", minEntryValue=" + minEntryValue +
                ", minCallValue=" + minCallValue +
                ", playerCount=" + playerCount +
                ", maxPlayerCount=" + maxPlayerCount +
                ", closing=" + closing +
                ", owner=" + owner +
                ", seat=" + Arrays.toString(seat) +
                ", jsonIncoming=" + jsonIncoming +
                '}' + "\n";

        ret += "Seat: ";
        for(int i=0; i<maxPlayerCount; i++) {
            if(seat[i] != null) ret += seat[i].getUser().getUsername() + " ";
        }
        ret += "\n";

        return ret;
    }

    public String printWaitingRoom(){

        String ret = "";

        ret += "Id: " + gameId + "\n";
        ret += "Code: " + gameCode + "\n";
        ret += "Board Type: " + boardType + "\n";
        ret += "Min Entry Value: " + ( (double) minEntryValue / 100000 ) + "lac\n";
        ret += "Min Call Value: " + ( (double) minCallValue / 100000 ) + "lac\n";
        ret += "Player Count: " + playerCount + "\n";
        ret += "Owner Username: " + owner.getUser().getUsername() + "\n";
        ret += "Players: " + "\n";


        for(int i=0; i<maxPlayerCount; i++){
            if(seat[i] == null) continue;

            User user = seat[i].getUser();

            ret += "Seat: " + i + " ";
            ret += "Username: " + user.getUsername() + " ";
            ret += "Id: " + user.getId() + " ";

            if(user.getLoginMethod().equals("facebook")) ret += "Login id: " + user.getFb_id() + " ";
            else if(user.getLoginMethod().equals("google")) ret += "Login id: " + user.getGmail_id() + " ";

            ret += "Board coin: " + ( (double) user.getBoardCoin() / 100000 ) + "lac ";
            ret += "\n";
        }
        ret += "\n";

        return ret;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getGameCode() {
        return gameCode;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public long getMinEntryValue() {
        return minEntryValue;
    }

    public void setMinEntryValue(long minEntryValue) {
        this.minEntryValue = minEntryValue;
    }

    public long getMinCallValue() {
        return minCallValue;
    }

    public void setMinCallValue(long minCallValue) {
        this.minCallValue = minCallValue;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    public ServerToClient getOwner() {
        return owner;
    }

    public void setOwner(ServerToClient owner) {
        this.owner = owner;
    }

    public ServerToClient getAtSeat(int i) {
        return seat[i];
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
}
