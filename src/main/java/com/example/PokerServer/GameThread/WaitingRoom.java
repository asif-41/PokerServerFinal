package com.example.PokerServer.GameThread;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WaitingRoom implements Runnable {

    private int gameId;
    private int gameCode;
    private String boardType;
    private int minBoardCoin;
    private int minCallValue;

    private int playerCount;
    private int maxPlayerCount;

    private ArrayList waitingForApproval;
    private ServerToClient owner;
    private ServerToClient seat[];

    private JSONObject jsonIncoming;


    public WaitingRoom(ArrayList<ServerToClient> invitedFriends, ServerToClient owner, int gameId, int gameCode, int maxPlayerCount, String boardType, int minBoardCoin, int minCallValue) {

        this.gameId = gameId;
        this.gameCode = gameCode;
        this.maxPlayerCount = maxPlayerCount;
        this.boardType = boardType;
        this.minBoardCoin = minBoardCoin;
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
        sendRoomData();

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
            }

        }

    }


    //==================================================================================================================
    //
    //==================================================================================================================


    private void setServerSideData(int k) {

        ServerToClient s = seat[k];

        s.setWaitingRoom(this);
        s.getUser().setSeatPosition(k);
    }

    private void initializeUser(int k) {

        setServerSideData(k);
        sendInitialData(k);
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
    }

    public void removeInvitedUser(ServerToClient s, boolean sendMsg, String msg) {

        int loc = -1;
        for (int i = 0; i < maxPlayerCount; i++) {
            if (seat[i] == s) {
                loc = i;
                break;
            }
        }

        System.out.println("lcc " + loc);

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

        for (int i = 1; i < maxPlayerCount; i++) {
            if (seat[i] != null) removeInvitedUser(seat[i], true, "Owner closed the room");
        }
        removeInvitedUser(owner, false, null);

        Server.pokerServer.removeWaitingRoom(this);
    }


    private void sendRoomData() {

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

    private void sendInitialData(int k) {

        JSONObject send = initiateJson();
        JSONObject data = new JSONObject();

        data.put("boardType", boardType);
        data.put("roomId", gameId);
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
            data.put("roomId", gameId);
            data.put("roomCode", gameCode);
            data.put("minCallValue", minCallValue);
            data.put("minEntryAmount", minBoardCoin);
            data.put("requestType", "JoinWaitingRoom");

            send.put("waitingRoomData", data);

            sendMessage((ServerToClient) waitingForApproval.get(i), send.toString());
        }

        waitingForApproval.clear();
    }


    public void sendRemovalMessage(ServerToClient s, String msg) {

        JSONObject send = initiateJson();

        send.put("requestType", "WaitingRoom");

        JSONObject temp = new JSONObject();

        temp.put("requestType", "RemoveFromWaitingRoomResponse");
        temp.put("roomCode", gameCode);
        temp.put("message", msg);

        send.put("waitingRoomData", temp);
        s.sendMessage(send.toString());
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
                ", minBoardCoin=" + minBoardCoin +
                ", minCallValue=" + minCallValue +
                ", maxPlayerCount=" + maxPlayerCount +
                ", owner=" + owner.getUser().getUsername() +
                '}' + "\n";

        for (int i = 0; i < maxPlayerCount; i++) {
            if (seat[i] != null) {
                ret += seat[i].getUser().getUsername() + " ";
            }
        }
        ret += "\n";

        ret += "Waiting for approval " + "\n";
        for (int i = 0; i < waitingForApproval.size(); i++)
            ret += ((ServerToClient) waitingForApproval.get(i)).getUser().getUsername() + " ";

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

    public int getMinBoardCoin() {
        return minBoardCoin;
    }

    public void setMinBoardCoin(int minBoardCoin) {
        this.minBoardCoin = minBoardCoin;
    }

    public int getMinCallValue() {
        return minCallValue;
    }

    public void setMinCallValue(int minCallValue) {
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

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
}
