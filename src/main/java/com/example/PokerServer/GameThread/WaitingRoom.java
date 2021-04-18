package com.example.PokerServer.GameThread;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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

    private boolean closing;

    private ArrayList waitingForApproval;
    private ServerToClient owner;
    private ServerToClient seat[];

    private JSONObject jsonIncoming;


    public WaitingRoom(ArrayList<ServerToClient> invitedFriends, ServerToClient owner, int gameId, int gameCode, int maxPlayerCount, String boardType, long minEntryValue, long minCallValue) {

        closing = false;

        this.gameId = gameId;
        this.gameCode = gameCode;
        this.maxPlayerCount = maxPlayerCount;
        this.boardType = boardType;
        this.minEntryValue = minEntryValue;
        this.minCallValue = minCallValue;

        waitingForApproval = new ArrayList<ServerToClient>();
        seat = new ServerToClient[maxPlayerCount];

        this.owner = owner;
        playerCount = 1;
        seat[0] = owner;
        waitingForApproval = invitedFriends;
    }

    @Override
    public void run() {


        initializeUser(0);
        sendInvitation();

    }


    //==================================================================================================================
    //
    //==================================================================================================================


    private void sendMessage(int loc, String s) {

        ServerToClient temp = seat[loc];

        if (temp == null) return;
        temp.sendMessage(s);
    }

    private void sendMessage(ServerToClient ss, String s) {

        if (ss == null) return;
        ss.sendMessage(s);
    }

    private void sendMessageToAllExceptOne(String s, int loc, String exceptionMsg) {

        if (!exceptionMsg.equals("")) sendMessage(loc, exceptionMsg);

        for (int i = 0; i < maxPlayerCount; i++) {

            if (i == loc) continue;

            sendMessage(i, s);
        }

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

        System.out.println("Waiting room msg -> " + temp);

        try {
            jsonIncoming = new JSONObject(temp);
            System.out.println(jsonIncoming);

        } catch (Exception e) {
            System.out.println("Error in getting json in waiting room in server side\n" + e);
            jsonIncoming = null;
            return;
        }

        if (jsonIncoming.get("requestType").equals("WaitingRoom")) {

            JSONObject waitingRoomData = jsonIncoming.getJSONObject("waitingRoomData");

            if (waitingRoomData.get("requestType").equals("AddInWaitingRoom")) {

                if (from == owner) {

                    JSONArray usernames = waitingRoomData.getJSONArray("roomData");
                    sendInvitationByUsername(usernames);
                }
            } else if (waitingRoomData.get("requestType").equals("RemoveFromWaitingRoom")) {

                if (from == owner) {

                    JSONArray usernames = waitingRoomData.getJSONArray("roomData");
                    removeByUsername(usernames, "You were removed by room owner");
                }
            } else if (waitingRoomData.get("requestType").equals("RemoveMeFromWaitingRoom")) {

                exitRequestResponse(from, false, null);
            } else if (waitingRoomData.get("requestType").equals("StartGame")) {

                startGame();
            }

        }

    }


    //==================================================================================================================
    //
    //==================================================================================================================

    private void startGame() {

        String msg;
        boolean willStart = false;

        if (playerCount > 1) willStart = true;

        if (willStart == false) msg = "In sufficient players, player count must be greater than 1";
        else msg = "Will start game in 10 seconds";

        sendStartGameResponse(willStart, msg);

        if (willStart) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    makeGame();
                }
            }, 15000);
        }
    }

    private void makeGame() {
        Server.pokerServer.makeGameThread(this);
    }



    private void setServerSideData(int k) {

        ServerToClient s = seat[k];

        s.setWaitingRoom(this);
        s.getUser().setSeatPosition(k);
    }

    private void initializeUser(int k) {

        setServerSideData(k);
        sendInitialData(k);

        sendRoomData();
    }

    public void addInvitedUser(ServerToClient s) {

        int loc = -1;

        for (int i = 0; i < maxPlayerCount; i++) {
            if (seat[i] == null) {
                loc = i;
                break;
            }
        }

        seat[loc] = s;
        playerCount++;
        initializeUser(loc);
    }

    private void sendInvitationByUsername(JSONArray usernames) {

        for (int i = 0; i < usernames.length(); i++) {

            String temp = usernames.getString(i);
            ServerToClient s = Server.pokerServer.getConnectionThroughUsername(temp);

            if (s != null) waitingForApproval.add(s);
        }
        sendInvitation();
    }


    private void removeServerSideData(int k) {

        ServerToClient s = seat[k];

        seat[k] = null;

        playerCount--;
        s.setWaitingRoom(null);
        if (s.getUser() != null) s.getUser().setSeatPosition(-1);

        if (closing == false) sendRoomData();
    }

    public void removeInvitedUser(ServerToClient s, boolean sendMsg, String msg) {

        int loc = -1;
        for (int i = 0; i < maxPlayerCount; i++) {
            if (seat[i] == s) {
                loc = i;
                break;
            }
        }

        if (loc != -1) removeServerSideData(loc);
        if (sendMsg) sendRemovalMessage(s, msg);
    }

    private void removeByUsername(JSONArray usernames, String msg) {

        for (int i = 0; i < usernames.length(); i++) {

            String tempUsername = usernames.getString(i);
            ServerToClient s = Server.pokerServer.getConnectionThroughUsername(tempUsername);

            removeInvitedUser(s, true, msg);
        }
    }

    public void exitRequestResponse(ServerToClient from, boolean sendMsg, String msg) {

        if (from != owner) removeInvitedUser(from, false, null);
        else closeEverything();
    }

    private void closeEverything() {

        closing = true;

        for (int i = 1; i < maxPlayerCount; i++) {
            if (seat[i] != null) removeInvitedUser(seat[i], true, "Owner closed the room");
        }
        removeInvitedUser(owner, false, null);
        Server.pokerServer.removeWaitingRoom(this);
    }


    //==================================================================================================================
    //
    //==================================================================================================================

    private JSONObject initiateJson() {

        JSONObject send = new JSONObject();

        send.put("sender", "Server");
        send.put("ip", Server.pokerServer.getHost());
        send.put("port", Server.pokerServer.getPort());
        send.put("requestType", "WaitingRoom");
        send.put("owner", owner.getUser().getUsername());

        return send;
    }

    private void sendRoomData() {

        JSONObject send = initiateJson();
        JSONObject data = new JSONObject();

        data.put("boardType", boardType);
        data.put("minCallValue", minCallValue);
        data.put("minEntryValue", minEntryValue);
        data.put("gameId", gameId);
        data.put("roomCode", gameCode);
        data.put("requestType", "AllRoomData");

        JSONArray info = new JSONArray();

        for (int i = 0; i < maxPlayerCount; i++) {

            if (seat[i] == null) continue;

            User s = seat[i].getUser();
            JSONObject temp = new JSONObject();

            temp.put("username", s.getUsername());
            temp.put("seatPosition", s.getSeatPosition());
            temp.put("currentCoin", s.getCurrentCoin());
            temp.put("boardCoin", s.getBoardCoin());

            info.put(temp);
        }


        data.put("roomData", info);
        send.put("waitingRoomData", data);
        sendMessageToAll(send.toString());
    }

    private void sendInitialData(int k) {

        JSONObject send = initiateJson();
        JSONObject data = new JSONObject();

        data.put("boardType", boardType);
        data.put("gameId", gameId);
        data.put("roomCode", gameCode);
        data.put("userEntryAmount", seat[k].getUser().getCurrentCoin());
        data.put("seatPosition", seat[k].getUser().getSeatPosition());
        data.put("requestType", "SetWaitingRoomData");

        send.put("waitingRoomData", data);
        sendMessage(k, send.toString());
    }

    private void sendInvitation() {

        for (int i = 0; i < waitingForApproval.size(); i++) {

            JSONObject send = initiateJson();
            JSONObject data = new JSONObject();

            data.put("boardType", boardType);
            data.put("gameId", gameId);
            data.put("roomCode", gameCode);
            data.put("minCallValue", minCallValue);
            data.put("minEntryAmount", minEntryValue);
            data.put("requestType", "JoinWaitingRoom");

            send.put("waitingRoomData", data);

            sendMessage((ServerToClient) waitingForApproval.get(i), send.toString());
        }

        waitingForApproval.clear();
    }

    public void sendAskOwnerForApproval(ServerToClient s) {

        JSONObject send = initiateJson();
        JSONObject data = new JSONObject();

        String message = s.getUser().getUsername() + " wants your approval to join waiting room";

        data.put("boardType", boardType);
        data.put("gameId", gameId);
        data.put("roomCode", gameCode);
        data.put("username", s.getUser().getUsername());
        data.put("requestType", "ApproveJoinByCodeRequest");
        data.put("message", message);

        send.put("waitingRoomData", data);
        sendMessage(owner, send.toString());
    }

    private void sendRemovalMessage(ServerToClient s, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "RemoveFromWaitingRoomResponse");
        temp.put("roomCode", gameCode);
        temp.put("message", msg);

        send.put("waitingRoomData", temp);
        s.sendMessage(send.toString());
    }

    private void sendStartGameResponse(boolean start, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "StartGameResponse");
        temp.put("gameId", gameId);
        temp.put("roomCode", gameCode);
        temp.put("success", start);
        temp.put("message", msg);

        send.put("waitingRoomData", temp);

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
        return "WaitingRoom{" +
                "gameId=" + gameId +
                ", gameCode=" + gameCode +
                ", boardType='" + boardType + '\'' +
                ", minEntryValue=" + minEntryValue +
                ", minCallValue=" + minCallValue +
                ", playerCount=" + playerCount +
                ", maxPlayerCount=" + maxPlayerCount +
                ", closing=" + closing +
                ", waitingForApproval=" + waitingForApproval +
                ", owner=" + owner +
                ", seat=" + Arrays.toString(seat) +
                ", jsonIncoming=" + jsonIncoming +
                '}' + "\n";
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
