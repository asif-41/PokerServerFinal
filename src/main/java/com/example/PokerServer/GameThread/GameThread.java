package com.example.PokerServer.GameThread;

import com.example.PokerServer.Connection.BotClient;
import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.Objects.Card;
import com.example.PokerServer.Objects.Randomizer;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static java.time.temporal.ChronoUnit.SECONDS;

public class GameThread implements Runnable, Comparable {


    //=======================================================================================================================
    //
    //
    //                          GAME THREAD
    //
    //          HAS A GAME ID
    //                GAME CODE TO MAKE PRIVATE GAME ROOM
    //
    //
    //          CONSISTS OF ROUNDS
    //                      IN EACH ROUND
    //                      PLAYERS HAVE TURNS
    //                      AND THEN THEY CALL
    //
    //          AT THE START OF A ROUND, 2 PLAYER CARDS ARE SENT TO EACH PLAYER
    //          NO BOARD CARDS ARE SENT AT FIRST.
    //
    //          THEN AT FIRST THREE BOARD CARDS ARE SENT TO PLAYERS AND SHOWED ON BOARD
    //          THEN ANOTHER ONE, AND ANOTHER ONE..........
    //
    //=======================================================================================================================


    //==================================================================================================================
    //
    //                      INITIALIZING PARAMETERS
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //                          GAME DATA
    //
    //==================================================================================================================

    private int gameCode;                               //  GAME CODE TO JOIN BY CODE
    private int gameId;                                 //  GAME ID, PRIMARY KEY OF DATABASE
    private String boardType;                           //  TYPE OF BOARD
    private long minCallValue;                           //  MINIMUM CALL VALUE FOR THIS TYPE OF BOARD
    private long minEntryValue;                          //  MINIMUM ENTRY COIN

    private int playerCount;                            //  PLAYER COUNT IN GAME ROOM, SIZE OF inGamePlayers
    private int maxPlayerCount;                         //  MAX PLAYER IN THIS ROOM
    private boolean isPrivate;                          //  INDICATOR IF THIS ROOM IS PRIVATE
    private ServerToClient owner;                       //  OWNER IF THE GAME IS PRIVATE

    private ServerToClient[] inGamePlayers;             //  IN GAME PLAYERS ACCORDING TO SEAT SEQUENCE
    private long[] playerCallValues;                     //  CALL VALUES OF ALL PLAYERS
    private long[] playerTotalCallValues;               //  TOTAL CALL VALUES IN A ROUND
    private String[] playerCalls;                       //  CALLS OF PLAYERS

    // inGamePlayers[i] != null                         //  IS PLAYER IN ROOM
    private boolean[] isActiveInRoom;                   //  IS PLAYER ACTIVE IN ROOM
    private boolean[] isPlaying;                        //  IS PLAYER PLAYING IN CURRENT ROUND
    private boolean[] isActiveInRound;                  //  IS PLAYER ACTIVE IN ROUND

    private JSONObject jsonIncoming;                    //  INCOMING MSGS

    private int checker[];                              //  checker[i] = 0 IF i-th PLAYER FOLDED OR DID NOT HAVE
    //  ENOUGH COIN TO PLAY IN THIS ROUND
    //  OR JUST ENTERED THE GAME


    //  PLAYER CALLS ->

    //  "Fold"      "Call"   "Raise"   "Check"   "AllIn"
    //  "Low"   ->      IN GAME ROOM
    //                  BUT DOES NOT HAVE ENOUGH COIN
    //  "New"   ->      MATRO DHUKSE, NEXT ROUND THEKE KHELBE

    private boolean waitingToClose;                     //  IF WAITING TO CLOSE GAMEROOM
    private boolean gameRunning;                        //  IF GAME RUNNING

    private Timer closeTimer;

    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //                          ROUND DATA
    //
    //==================================================================================================================


    private ArrayList boardCards;                       //  BOARD CARDS, INITIALIZED AT THE START OF A ROUND
    //  NOT SENT ALL AT FIRST, SENT TO PLAYERS EVENTUALLY

    private int roundCount;                             //  ROUND COUNT IN CURRENT GAME ROOM
    private int cycleCount;                             //  CYCLE COUNT
    private int turnCount;                              //  TURN COUNTS IN CURRENT ROUND
    private long roundCoins;                             //  ROUND COINS GIVEN IN THE BOARD
    private long roundCall;                              //  MINIMUM CALL VALUE OF CURRENT ROUND
    private String roundIteratorName;                   //  NAME OF WHICH PLAYER IS TAKING TURN
    private int roundIteratorSeat;                      //  CURRENT PLAYER ER SEAT
    private int roundStarterSeat;                       //  SEAT OF ROUND STARTER
    //  CURRENT ROUND

    private int cardShowed;                             //  THE NUMBER OF CARDS SENT TO PLAYERS
    //  0, THEN 3, THEN 4, THEN 5

    private boolean hasAnyoneRaised;
    private boolean hasAnyoneCalled;                    //  HAS ANYONE CALLED IN CURRENT ROUND
    private int activeCountInRound;                     //  ACTIVE PLAYERS IN CURRENT ROUND
    //  PLAYER COUNT - ( LOW PLAYERS + FOLD PLAYERS)

    private int allInCountInRound;                      //  NUMBER OF PLAYERS THAT GAVE ALL IN IN CURRENT ROUND

    private int smallBlindSeat;                         //  LOCATION OF PLAYER WHO WILL HAVE SMALL BLIND
    private int bigBlindSeat;                           //  LOCATION OF PLAYER WHO WILL HAVE BIG BLIND

    private long foldCost;                               //  FOLD COST FOR BLINDINGS
    private boolean showAllCardsAtEnd;                  //  DO WE NEED TO SHOW ALL, EVERYONES CARDS AT THE END OF THE ROUND
    //  IF GAME ENDS BEFORE cardShowed = 5, THEN WE DONT HAVE TO

    private int previousCycleStarterSeat;               //  Last round e kaar theke start kora hoisilo

    private int roundWinnerCount;                       //  WINNER COUNT
    private int[] roundWinnersSeat;                 //  INTEGER ARRAYLIST, LOCATES WINNER LOCATIONS IN inGamePlayers
    private long[] roundWinnersAmount;
    private long[] roundPlayersCoinBack;
    private String[] roundResultPower;                    //  POWER STRING FOR CARDS OF WINNERS
    private ArrayList[] resultFoundAtLevel;                     //  RESULT FOUND AT THIS LEVEL WHILE CHECKING POWER STRING
    private ArrayList[] winnerLevel;                     //  RESULT FOUND AT THIS LEVEL WHILE CHECKING POWER STRING
    private ArrayList[] winnerLevelAmount;                     //  RESULT FOUND AT THIS LEVEL WHILE CHECKING POWER STRING

    private int maxWinLevelCount;
    private boolean hasBot;
    private int botLoc;
    private int deletingBot;

    private int deductingBlinds;
    //  INDICATES IF WE NEED A KICKER OR NOT

    //==================================================================================================================
    //
    //==================================================================================================================







    //==================================================================================================================
    //
    //                  INITIALIZING GAME THREAD
    //
    //==================================================================================================================

    public GameThread(ArrayList<ServerToClient> clients, boolean isPrivate, ServerToClient owner, int gameId, int gameCode, int maxPlayerCount, String boardType, long minEntryValue,
                      long minCallValue) {


        this.isPrivate = isPrivate;
        this.owner = owner;
        this.gameCode = gameCode;
        this.gameId = gameId;
        this.minCallValue = minCallValue;
        this.minEntryValue = minEntryValue;
        this.boardType = boardType;
        this.maxPlayerCount = maxPlayerCount;

        hasBot = false;
        botLoc = -1;
        deletingBot = -1;
        initializeGameThread(clients);

        closeTimer = null;
        jsonIncoming = null;
        waitingToClose = false;
        gameRunning = false;
    }

    private void initializeGameThread(ArrayList<ServerToClient> s) {

        checker = new int[maxPlayerCount];
        inGamePlayers = new ServerToClient[maxPlayerCount];
        playerCalls = new String[maxPlayerCount];
        playerCallValues = new long[maxPlayerCount];
        playerTotalCallValues = new long[maxPlayerCount];
        isActiveInRoom = new boolean[maxPlayerCount];
        isPlaying = new boolean[maxPlayerCount];
        isActiveInRound = new boolean[maxPlayerCount];

        for (int i = 0; i < maxPlayerCount; i++) {
            checker[i] = -1;
            inGamePlayers[i] = null;
            playerCalls[i] = "Empty";
            playerCallValues[i] = -1;
            playerTotalCallValues[i] = -1;

            isActiveInRound[i] = false;
            isPlaying[i] = false;
            isActiveInRoom[i] = false;
        }

        boardCards = new ArrayList<Card>();
        for (int i = 0; i < s.size(); i++) {
            inGamePlayers[i] = s.get(i);

            inGamePlayers[i].setGameThread(this);
            inGamePlayers[i].setWaitingRoom(null);
            inGamePlayers[i].setLastGameCode(gameCode);

            hasBot |= inGamePlayers[i].getUser().isBot();
            if(inGamePlayers[i].getUser().isBot()) botLoc = i;
        }
        playerCount = s.size();

        roundWinnerCount = 0;
        roundWinnersSeat = null;
        roundWinnersAmount = null;
        roundPlayersCoinBack = null;
        roundResultPower = null;
        resultFoundAtLevel = null;
        winnerLevel = null;
        winnerLevelAmount = null;
        maxWinLevelCount = 0;
    }

    @Override
    public void run() {

        //WELCOME MESSAGE

        initializePlayers();
        sendPlayersDataToAll();
        sendWelcomeGameMsgToAll();

        roundCount = 0;
        previousCycleStarterSeat = -1;
        startNewRound();
    }

    //==================================================================================================================
    //
    //==================================================================================================================






    //==================================================================================================================
    //
    //      COMMUNICATIONS
    //
    //==================================================================================================================

    private JSONObject initiateJson() {

        JSONObject temp = new JSONObject();

        temp.put("sender", "Server");
        temp.put("ip", Server.pokerServer.getHost());
        temp.put("port", Server.pokerServer.getPort());
        temp.put("requestType", "GameRoom");

        return temp;
    }

    private void sendMessage(int loc, String s) {

        ServerToClient temp = inGamePlayers[loc];

        if (temp == null) return;
        temp.sendMessage(s);
    }

    private void sendMessageToAll(String s) {

        for (int i = 0; i < maxPlayerCount; i++) sendMessage(i, s);
    }

    public void incomingMessage(String tempp, ServerToClient serverToClient) {

        JSONObject gameData;

        try {
            jsonIncoming = new JSONObject(tempp);
            gameData = jsonIncoming.getJSONObject("gameData");

        } catch (Exception e) {
            System.out.println("Error in getting json in gameThread\n" + e);

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
            jsonIncoming = null;
            return;
        }

        //CHECKING IF USERNAME MATCHES CURRENT USERNAME
        String username = jsonIncoming.getString("username");

        if (gameData.getString("requestType").equals("GameCall")) {

            if (gameData.getString("call").equals("Call")) {

                handleUserCallRequest(username);

            }
            else if (gameData.getString("call").equals("Fold")) {

                handleUserFoldRequest(username);

            }
            else if (gameData.getString("call").equals("Raise")) {

                long value = gameData.getLong("cost");
                handleUserRaiseRequest(username, value);

            }
            else if (gameData.getString("call").equals("Check")) {

                handleUserCheckRequest(username);

            }
            else if (gameData.getString("call").equals("AllIn")) {

                handleUserAllInRequest(username);

            }
        }
        else if (gameData.getString("requestType").equals("AddBoardCoin")) {

            requestAddBoardCoin(serverToClient, jsonIncoming);
        }
        else if (gameData.getString("requestType").equals("StartNewRound")) {

            startNewRound();
        }
        else if (gameData.getString("requestType").equals("ExitGame")) {

            exitGameResponse(serverToClient);
        }
    }

    //==================================================================================================================
    //
    //==================================================================================================================






    //==================================================================================================================
    //
    //          SEND INITIAL DATA
    //
    //==================================================================================================================

    private void initializePlayer(int i) {

        if (inGamePlayers[i] == null) return;

        int ownerId = -1;
        if (owner != null) ownerId = owner.getUser().getId();


        inGamePlayers[i].getUser().joinedAGame(gameId, gameCode, ownerId, i, maxPlayerCount, playerCount);
        sendInitialData(gameId, gameCode, ownerId, i, maxPlayerCount, playerCount);
    }

    private void initializePlayers() {

        for (int i = 0; i < maxPlayerCount; i++) initializePlayer(i);
    }

    private void sendInitialData(int gameId, int gameCode, int ownerId, int loc, int maxPlayerCount, int playerCount) {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameId);
        tempJson.put("gameCode", gameCode);
        tempJson.put("ownerId", ownerId);
        tempJson.put("seatPosition", loc);
        tempJson.put("playerCount", playerCount);
        tempJson.put("maxPlayerCount", maxPlayerCount);
        tempJson.put("gameRequest", "InitializeGameData");

        send.put("gameData", tempJson);

        sendMessage(loc, send.toString());
    }


    private void sendPlayersDataToAll() {

        loadGameRoomDataInAll();

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "LoadPlayersData");

        send.put("gameData", tempJson);

        JSONArray array = new JSONArray();

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            JSONObject temp = User.UserToJsonInGame(inGamePlayers[i].getUser());
            array.put(temp);
        }
        send.put("data", array);
        sendMessageToAll(send.toString());
    }

    private JSONObject gameRoomDataInitiate() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "LoadGameRoomData");

        send.put("gameData", tempJson);

        JSONObject temp = new JSONObject();

        temp.put("gameRunning", gameRunning);
        temp.put("roundCount", roundCount);
        temp.put("cycleCount", cycleCount);
        temp.put("turnCount", turnCount);
        temp.put("roundCall", roundCall);
        temp.put("roundCoins", roundCoins);
        temp.put("roundIteratorSeat", roundIteratorSeat);
        temp.put("roundStarterSeat", roundStarterSeat);
        temp.put("smallBlindSeat", smallBlindSeat);
        temp.put("bigBlindSeat", bigBlindSeat);
        temp.put("foldCost", foldCost);

        send.put("data", temp);
        return send;
    }

    private void loadGameRoomDataInAll() {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            JSONObject send = gameRoomDataInitiate();

            JSONObject temp = send.getJSONObject("data");

            temp.put("call", playerCalls[i]);
            temp.put("callValue", playerCallValues[i]);
            temp.put("totalCallValue", playerTotalCallValues[i]);

            User.loadGameRoomData(inGamePlayers[i].getUser(), send);
        }
    }




    private void sendWelcomeGameMsg(int loc) {

        if (inGamePlayers[loc] == null) return;

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "WelcomeGameMessage");
        tempJson.put("message", "Welcome to game room, id: " + gameId + " gameCode: " + gameCode);

        send.put("gameData", tempJson);
        sendMessage(loc, send.toString());

    }

    private void sendWelcomeGameMsgToAll() {

        for (int i = 0; i < maxPlayerCount; i++) sendWelcomeGameMsg(i);
    }

    //==================================================================================================================
    //
    //==================================================================================================================








    //==================================================================================================================
    //
    //                  ADD OR REMOVE FROM GAME
    //
    //==================================================================================================================

    private synchronized void tryAddBot(){

        if(isPrivate) return ;
        if(hasBot || gameRunning || activeCountInRound >= Server.pokerServer.getLeastPlayerCount()) return;
        if(playerCount == maxPlayerCount) return ;
        if(Server.pokerServer.getMaxBotLimit() <= Server.pokerServer.botIds.size()) return ;

        int key = Server.pokerServer.getBoardKey(boardType);
        BotClient b = Server.pokerServer.makeBotClient(key);
        addInGameThread(b);
    }

    private void tryRemoveBot(){

        if(hasBot){

            if(activeCountInRound>Server.pokerServer.getLeastPlayerCount()
                || (activeCountInRound == Server.pokerServer.getLeastPlayerCount() && !isActiveInRoom[botLoc])){

                if(deletingBot == -1) deletingBot = Randomizer.one(Server.pokerServer.deleteBotBeforeRound) + 1;
                else deletingBot--;

                if(deletingBot == 0) exitGameResponse(inGamePlayers[botLoc]);
            }
        }
        else deletingBot = -1;
    }


    public synchronized void addInGameThread(ServerToClient s) {

        int loc = -1;

        for (int i = 0; i < maxPlayerCount; i++) {
            if (inGamePlayers[i] == null) {
                loc = i;
                break;
            }
        }

        if(loc == -1) {
            if(s.getUser().isBot())
                s.leaveGameRoom();

            return ;
        }

        if(s.getUser().isBot()){
            hasBot = true;
            botLoc = loc;
        }

        playerCalls[loc] = "New";
        playerCallValues[loc] = 0;
        playerTotalCallValues[loc] = 0;
        isActiveInRoom[loc] = false;
        isPlaying[loc] = false;
        isActiveInRound[loc] = false;

        playerCount++;

        inGamePlayers[loc] = s;
        s.setGameThread(this);
        s.setLastGameCode(gameCode);

        Server.pokerServer.sortGameThreads(boardType);

        initializePlayer(loc);

        sendPlayersDataToAll();
        sendWelcomeGameMsg(loc);

        if (gameRunning == false) startNewRound();
        else {

            if(s.getUser().isBot()) return ;

            sendShowCards();
            sendShowBoardInfo();

            sendShowNextTurnInfo();
            disableButtons(loc);
        }
    }



    private void setOwner() {

        if (isPrivate == false) return;

        owner = null;

        for (int i = 0; i < maxPlayerCount; i++)
            if (inGamePlayers[i] != null) {
                owner = inGamePlayers[i];
                return;
            }
    }

    private int getNextActivePlayer(int seatStarter, int seatEnder) {

        int ret = -1;
        if (seatEnder < seatStarter) seatEnder += maxPlayerCount;

        for (int i = seatStarter; i < seatEnder; i++) {

            int k = i % maxPlayerCount;

            if (inGamePlayers[k] == null) continue;
            if (isActiveInRoom[k] == false) continue;
            if (isPlaying[k] == false) continue;
            if (isActiveInRoom[k] == false) continue;

            ret = k;
            break;
        }
        return ret;
    }



    public synchronized void exitGameResponse(ServerToClient s) {

        if(s.getUser().isBot()){
            hasBot = false;
            botLoc = -1;
            deletingBot = -1;
        }

        int seatPosition = s.getUser().getSeatPosition();
        User tempUser = s.getUser();

        if (gameRunning) {
            tempUser.setTotalCallCount(tempUser.getTotalCallCount() + 1);
            tempUser.setFoldCount(tempUser.getFoldCount() + 1);

            tempUser.setRoundsPlayed(tempUser.getRoundsPlayed() + 1);
            tempUser.setCoinLost(tempUser.getCoinLost() + playerTotalCallValues[seatPosition]);
        }
        s.leaveGameRoom();
        kickFromRoomRequest(seatPosition);

        if (isActiveInRound[seatPosition]) activeCountInRound--;
        if (playerCalls[seatPosition].equals("AllIn")) allInCountInRound--;

        playerCount--;
        inGamePlayers[seatPosition] = null;
        playerCalls[seatPosition] = "Empty";
        playerCallValues[seatPosition] = -1;
        playerTotalCallValues[seatPosition] = -1;

        isActiveInRoom[seatPosition] = false;
        isPlaying[seatPosition] = false;
        isActiveInRound[seatPosition] = false;

        Server.pokerServer.sortGameThreads(boardType);

        setOwner();
        sendPlayersDataToAll();

        if (!gameRunning) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitGame(Server.pokerServer.addBotDelay);
                    tryAddBot();
                }
            }).start();
            return;
        }

        if (seatPosition == roundIteratorSeat) {
            playRound();
            return;
        } else if (seatPosition == roundStarterSeat) {

            roundStarterSeat = getNextActivePlayer(roundStarterSeat, roundIteratorSeat);
        }
        if (activeCountInRound == 1 || activeCountInRound == 0) {

            sendShowBoardInfo();
            endRound();
        }
    }


    public void closeRoom() {

        if(closeTimer != null) closeTimer.cancel();

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            ServerToClient s = inGamePlayers[i];

            boolean send = ! s.getUser().isBot();

            s.leaveGameRoom();
            if(send) kickFromRoomRequest(i);
        }
        Server.pokerServer.removeGameThread(this);
    }

    private void kickFromRoomRequest(int player) {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "LeaveGame");

        send.put("gameData", tempJson);

        sendMessage(player, send.toString());
    }

    //==================================================================================================================
    //
    //==================================================================================================================











    //==================================================================================================================
    //
    //      GAME THREAD FUNCTIONALITIES
    //
    //==================================================================================================================

    private int findNextAvailableSeatFromThisPosition(int position) {

        int k, ret = -1;
        int loc = position + 1;

        for (int i = 0; i < maxPlayerCount; i++) {
            k = (loc + i) % maxPlayerCount;

            if (inGamePlayers[k] != null && isActiveInRoom[k] != false) {
                ret = k;
                break;
            }
        }
        return ret;
    }

    private int findPrevAvailableSeatFromThisPosition(int position) {

        int k, ret = -1;
        int loc = position - 1;

        for (int i = 0; i < maxPlayerCount; i++) {
            k = (loc - i);
            if(k < 0) k += maxPlayerCount;

            if (inGamePlayers[k] != null && isActiveInRoom[k] != false) {
                ret = k;
                break;
            }
        }
        return ret;
    }


    private void deductBlindCoins(){

        User user;

        user = inGamePlayers[smallBlindSeat].getUser();
        user.setBoardCoin(user.getBoardCoin() - (minCallValue/2));
        roundCoins += (minCallValue/2);
        playerCallValues[smallBlindSeat] = (minCallValue/2);
        playerTotalCallValues[smallBlindSeat] += (minCallValue/2);

        user = inGamePlayers[bigBlindSeat].getUser();
        user.setBoardCoin(user.getBoardCoin() - minCallValue);
        roundCoins += minCallValue;
        playerCallValues[bigBlindSeat] = minCallValue;
        playerTotalCallValues[bigBlindSeat] += minCallValue;

        sendDeductBlindCoins();
        sendPlayersDataToAll();
    }

    private void sendDeductBlindCoins(){

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "DeductBlindCoins");

        send.put("gameData", tempJson);

        JSONArray array = new JSONArray();

        JSONObject temp = new JSONObject();
        temp.put("seatPosition", smallBlindSeat);
        temp.put("amount", (minCallValue/2));
        array.put(temp);

        temp = new JSONObject();
        temp.put("seatPosition", bigBlindSeat);
        temp.put("amount", minCallValue);
        array.put(temp);

        send.put("data", array);
        send.put("roundCoins", roundCoins);
        send.put("message", "Deducting blind coins");
        sendMessageToAll(send.toString());

        waitGame(2);
    }


    private void setNextPlayer() {

        int newIterator = -1, k;

        for (int i = 1; i <= maxPlayerCount; i++) {
            k = (roundIteratorSeat + i) % maxPlayerCount;

            if (inGamePlayers[k] == null) continue;
            if (isActiveInRoom[k] == false) continue;
            if (isPlaying[k] == false) continue;
            if (isActiveInRound[k] == false) continue;

            newIterator = 1;
            roundIteratorSeat = k;
            break;
        }
        if (newIterator == -1) {
            roundIteratorSeat = -1;
        }
    }

    private void setupForNextTurn() {

        //SETTING UP FOR NEXT TURN

        turnCount++;

        //TURN COUNT 1 MANE ZERO BAR OR EKBAR GHURE ASHCHE
        //EKHON NOTUN KORE GHURA LAGBE

        if (turnCount == 1) {
            hasAnyoneCalled = false;
            hasAnyoneRaised = false;

            if (roundStarterSeat == -1) {

                previousCycleStarterSeat = findNextAvailableSeatFromThisPosition(previousCycleStarterSeat);
                roundIteratorSeat = previousCycleStarterSeat;
                bigBlindSeat = findPrevAvailableSeatFromThisPosition(roundIteratorSeat);
                smallBlindSeat = findPrevAvailableSeatFromThisPosition(bigBlindSeat);

                deductingBlinds = 1;
            } else {
                roundIteratorSeat = roundStarterSeat;
                roundStarterSeat = -1;

                //===========================================================================
                //
                //  IMPORTANT
                //  EXAMPLE:      CALL 10000 -> ALL IN 5000
                //                THEN ALL OKAY WILL GO IN HERE, AND END THE ROUND
                //
                //                CALL 10000 -> ALL IN 15000
                //                IF THIS CONDITION WAS IN PLAY ROUND(), THEN WILL
                //                NOT ASK THE CALLER TO CALL AGAIN AS THE CONDITION
                //                IS ALREADY MATCHED UP.
                //
                //===========================================================================

                if (activeCountInRound == allInCountInRound + 1) roundStarterSeat = roundIteratorSeat;
            }
        } else setNextPlayer();

        if (roundIteratorSeat != -1) {

            if( ! (roundIteratorSeat >= 0 && roundIteratorSeat < maxPlayerCount) ) return;
            else if (inGamePlayers[roundIteratorSeat] == null) return ;

            String username = inGamePlayers[roundIteratorSeat].getUser().getUsername();
            roundIteratorName = username;
        }
    }

    private void playRound() {

        //EKJON BAADE SHOBAI FOLD NAILE LOW

        if (activeCountInRound == 1 || activeCountInRound == 0) {

            sendShowBoardInfo();
            sendPlayersDataToAll();

            endRound();
            return;
        }

        //SHOBAI ALL IN DISE
        //ALL IN DIYE EKJON BAKI THAKLE OITA NEXT TURN ER FUNCTION E HANDLE HOBE

        if (allInCountInRound == activeCountInRound) {

            sendShowCards();
            sendShowBoardInfo();
            sendPlayersDataToAll();

            endRound();
            return;
        }

        deductingBlinds = 0;
        setupForNextTurn();

        new Thread(new Runnable() {
            @Override
            public void run() {

                if(deductingBlinds == 1) deductBlindCoins();

                //ROUND E EKBAR TOTAL GHURA HOISE

                if (roundIteratorSeat == roundStarterSeat) {

                    sendShowBoardInfo();
                    sendPlayersDataToAll();

                    endRound();
                    return;
                }
                sendShowCards();
                sendShowBoardInfo();

                sendShowNextTurnInfo();
                JSONObject j = enableGameButtons();

                sendPlayersDataToAll();
                sendEnableGameButtons(j);
            }
        }).start();
    }

    private void startNewRound() {

        //player enough thakle bot re bair koro

        activeCountInRound = 0;
        roundCall = minCallValue;

        //HERE PLAYER DATA MUST COME FROM DATABASE

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

            playerCalls[i] = "";
            playerCallValues[i] = 0;
            playerTotalCallValues[i] = 0;

            User tempUser = inGamePlayers[i].getUser();

            if (tempUser.getBoardCoin() < minCallValue) {

                playerCalls[i] = "Low";

                isActiveInRoom[i] = false;
                isPlaying[i] = false;
                isActiveInRound[i] = false;

                if(tempUser.isBot()) inGamePlayers[i].increaseBoardCoin();

            } else {
                isActiveInRoom[i] = true;
                isPlaying[i] = true;
                isActiveInRound[i] = true;

                activeCountInRound++;
            }
        }

        if (activeCountInRound < Server.pokerServer.getLeastPlayerCount()) {

            gameRunning = false;
            if(waitingToClose) return;

            waitForPlayersToBuyMsg();
            waitingToClose = true;

            closeTimer = new Timer();
            closeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    closeRoom();
                }
            }, Server.pokerServer.waitingToCloseDelay);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitGame(Server.pokerServer.addBotDelay);
                    tryAddBot();
                }
            }).start();

            return;
        }

        if (closeTimer != null) closeTimer.cancel();
        tryRemoveBot();         //activeCountInRound >= 2

        cycleCount = 1;
        turnCount = 0;
        roundStarterSeat = -1;
        roundIteratorSeat = -1;
        roundIteratorName = "";
        roundCoins = 0;
        roundCount++;
        cardShowed = 0;
        allInCountInRound = 0;
        foldCost = 0;

        waitingToClose = false;
        gameRunning = true;

        sendPlayersDataToAll();

        loadCards();
        roundStartMsg();

        sendPlayerCardsToClient();
        sendBoardCardsToClient(new int[]{});

        playRound();
    }



    //==================================================================================================================
    //
    //      LOAD CARDS IN SERVER SIDE, AT THE START OF EACH ROUND
    //
    //==================================================================================================================

    private void loadCards() {

        int cardCount = 5 + 2 * activeCountInRound;

        ArrayList tempCards = Randomizer.randCardArray(cardCount);
        int[] assign = Randomizer.randSingleValueArray(cardCount, cardCount);

        boardCards.clear();

        for (int i = 0; i < 5; i++) {

            ((Card) tempCards.get(assign[i])).setLocation(0);
            boardCards.add(tempCards.get(assign[i]));
        }

        for (int i = 0, k = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;
            if (isActiveInRoom[i] == false) continue;

            ServerToClient temp = inGamePlayers[i];

            User tempUser = temp.getUser();

            Card a = (Card) tempCards.get(assign[2 * k + 5]);
            Card b = (Card) tempCards.get(assign[2 * k + 1 + 5]);

            k++;

            a.setLocation(1);
            b.setLocation(1);

            tempUser.getBoardCards().clear();
            tempUser.getPlayerCards().clear();

            tempUser.getPlayerCards().add(a);
            tempUser.getPlayerCards().add(b);
        }
    }

    private void showMoreCards() {
        if (cardShowed == 0) {
            cardShowed = 3;
            sendBoardCardsToClient(new int[]{0, 1, 2});
        } else if (cardShowed == 3 || cardShowed == 4) {
            sendBoardCardsToClient(new int[]{cardShowed});
            cardShowed++;
        }
    }

    //==================================================================================================================
    //
    //==================================================================================================================








    //==================================================================================================================
    //
    //              ROUND OPERATING JSON SEND DATA
    //
    //==================================================================================================================

    private void roundStartMsg() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "RoundStartMessage");

        send.put("gameData", tempJson);

        send.put("dataType", "RoundStart");
        send.put("message", "Round " + roundCount + " start");
        sendMessageToAll(send.toString());
    }


    private void sendPlayerCardsToClient() {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;
            if (isActiveInRoom[i] == false) continue;

            JSONObject send = initiateJson();

            JSONObject tempJson = new JSONObject();

            tempJson.put("id", gameId);
            tempJson.put("code", gameCode);
            tempJson.put("gameRequest", "LoadPlayerCards");

            send.put("gameData", tempJson);

            JSONArray array = new JSONArray();
            User tempUser = inGamePlayers[i].getUser();
            ArrayList userCards = tempUser.getPlayerCards();

            array.put(((Card) userCards.get(0)).toString2());
            array.put(((Card) userCards.get(1)).toString2());

            send.put("data", array);

            sendMessage(i, send.toString());
        }
    }

    private void sendBoardCardsToClient(int loc[]) {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "LoadBoardCards");

        send.put("gameData", tempJson);

        JSONArray array = new JSONArray();
        for (int i = 0; i < loc.length; i++) array.put(((Card) boardCards.get(loc[i])).toString2());

        send.put("data", array);

        sendMessageToAll(send.toString());
    }


    private void sendShowNextTurnInfo() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "ShowNextTurnInfo");

        send.put("gameData", tempJson);

        JSONObject temp = new JSONObject();

        temp.put("username", roundIteratorName);
        temp.put("seatPosition", roundIteratorSeat);

        send.put("dataType", "ShowNextTurnInfo");
        send.put("data", temp);

        sendMessageToAll(send.toString());
    }

    private void sendShowBoardInfo() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "ShowBoardInfo");

        send.put("gameData", tempJson);


        JSONObject temp = new JSONObject();

        temp.put("roundCount", roundCount);
        temp.put("roundCoins", roundCoins);
        temp.put("turnCount", turnCount);
        temp.put("cycleCount", cycleCount);
        temp.put("roundCall", roundCall);

        send.put("dataType", "ShowBoardInfo");
        send.put("data", temp);

        sendMessageToAll(send.toString());
    }

    private void sendShowCards() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "ShowCards");

        send.put("gameData", tempJson);

        sendMessageToAll(send.toString());
    }




    //==================================================================================================================
    //
    //              SENDING BUTTON INFO
    //
    //==================================================================================================================

    private long getMaxRaiseCost(){

        long ret = 0;

        for(int i=0; i<maxPlayerCount; i++){

            if (inGamePlayers[i] == null) continue;
            if(inGamePlayers[i].getUser() == null) continue;
            if(i == roundIteratorSeat) continue;

            if(! isActiveInRoom[i] || !isPlaying[i] || !isActiveInRound[i]) continue;

            long v = inGamePlayers[i].getUser().getBoardCoin() + playerCallValues[i];
            v -= playerCallValues[roundIteratorSeat];

            ret = max(ret, v);
        }

        return ret;
    }

    private void disableButtons(int loc){

        if (inGamePlayers[loc] == null) return;

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "DisableGameButtons");

        send.put("gameData", tempJson);
        send.put("dataType", "DisableButtons");

        sendMessage(loc, send.toString());
    }

    private void disableButtonsExcept(int j) {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;
            if (i == j) continue;

            JSONObject send = initiateJson();

            JSONObject tempJson = new JSONObject();

            tempJson.put("id", gameId);
            tempJson.put("code", gameCode);
            tempJson.put("gameRequest", "DisableGameButtons");

            send.put("gameData", tempJson);
            send.put("dataType", "DisableButtons");

            sendMessage(i, send.toString());
        }
    }

    private JSONObject enableGameButtons() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "EnableGameButtons");

        send.put("dataType", "EnableButtons");
        send.put("gameData", tempJson);

        JSONArray array = new JSONArray();
        JSONObject temp;
        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        if(tempUser == null) return send;

        //      FOLD BUTTON
        temp = new JSONObject();
        temp.put("name", "Fold");
        temp.put("cost", 0);
        array.put(temp);


        //Call or raise button

        long incr = roundCall;
        if(incr == 0) incr = minCallValue;

        long match = roundCall - playerCallValues[roundIteratorSeat];

        if (match < tempUser.getBoardCoin()) {
            temp = new JSONObject();
            temp.put("name", "Call");
            temp.put("cost", match);

            if(cycleCount == 1){
                if(roundIteratorSeat == smallBlindSeat) array.put(temp);
                else if(roundIteratorSeat == bigBlindSeat && hasAnyoneRaised) array.put(temp);
                else if(roundIteratorSeat != bigBlindSeat) array.put(temp);
            }
            else if(hasAnyoneRaised) array.put(temp);

            temp = new JSONObject();
            temp.put("name", "Raise");

            long min = match + incr;
            long max = min(getMaxRaiseCost() , tempUser.getBoardCoin());

            if(min > max) min = max;

            temp.put("cost", min);
            temp.put("maxCost", max);
            temp.put("stepSize", incr);

            array.put(temp);
        }
        else {
            temp = new JSONObject();
            temp.put("name", "AllIn");
            temp.put("cost", tempUser.getBoardCoin());

            array.put(temp);
        }

        if ((cycleCount == 1 && roundIteratorSeat == bigBlindSeat && !hasAnyoneRaised) || (cycleCount > 1 && !hasAnyoneCalled)){

            temp = new JSONObject();
            temp.put("name", "Check");
            temp.put("cost", 0);

            array.put(temp);
        }

        send.put("data", array);

        return send;
    }

    private void sendEnableGameButtons(JSONObject jsonObject) {

        disableButtonsExcept(roundIteratorSeat);
        sendMessage(roundIteratorSeat, jsonObject.toString());
    }

    //==================================================================================================================
    //
    //==================================================================================================================






    //==================================================================================================================
    //
    //              HANDLE USER CALL REQUESTS
    //
    //
    //==================================================================================================================

    private void handleUserCallRequest(String username) {


        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        foldCost = 0;
        hasAnyoneCalled = true;

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        tempUser.setTotalCallCount(tempUser.getTotalCallCount() + 1);
        tempUser.setCallCount(tempUser.getCallCount() + 1);

        long match = roundCall - playerCallValues[roundIteratorSeat];

        tempUser.setBoardCoin(tempUser.getBoardCoin() - match);
        playerCalls[roundIteratorSeat] = "Call";
        playerCallValues[roundIteratorSeat] = roundCall;
        playerTotalCallValues[roundIteratorSeat] += match;
        roundCoins += match;

        if (tempUser.getBoardCoin() == 0) {
            isActiveInRound[roundIteratorSeat] = false;
            allInCountInRound++;

        } else if (roundStarterSeat == -1) roundStarterSeat = roundIteratorSeat;


        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();
    }

    private void handleUserFoldRequest(String username) {

        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        tempUser.setTotalCallCount(tempUser.getTotalCallCount() + 1);
        tempUser.setFoldCount(tempUser.getFoldCount() + 1);
        playerCalls[roundIteratorSeat] = "Fold";
        playerCallValues[roundIteratorSeat] = 0;
        playerTotalCallValues[roundIteratorSeat] += 0;

        isPlaying[roundIteratorSeat] = false;
        isActiveInRound[roundIteratorSeat] = false;
        activeCountInRound--;

        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();
    }

    private void handleUserRaiseRequest(String username, long value) {

        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        foldCost = 0;
        roundStarterSeat = roundIteratorSeat;
        hasAnyoneCalled = true;
        hasAnyoneRaised = true;

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        tempUser.setTotalCallCount(tempUser.getTotalCallCount() + 1);
        tempUser.setRaiseCount(tempUser.getRaiseCount() + 1);

        long match = value;

        tempUser.setBoardCoin(tempUser.getBoardCoin() - match);
        playerCalls[roundIteratorSeat] = "Raise";
        playerCallValues[roundIteratorSeat] += match;
        playerTotalCallValues[roundIteratorSeat] += match;
        roundCoins += match;
        roundCall = playerCallValues[roundIteratorSeat];

        if (tempUser.getBoardCoin() == 0) {
            isActiveInRound[roundIteratorSeat] = false;
            allInCountInRound++;
            roundStarterSeat = -1;
        }

        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();
    }

    private void handleUserCheckRequest(String username) {

        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        tempUser.setTotalCallCount(tempUser.getTotalCallCount() + 1);
        tempUser.setCheckCount(tempUser.getCheckCount() + 1);

        foldCost = 0;
        playerCalls[roundIteratorSeat] = "Check";
        if (roundStarterSeat == -1) roundStarterSeat = roundIteratorSeat;

        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();

    }

    private void handleUserAllInRequest(String username) {

        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        foldCost = 0;
        hasAnyoneCalled = true;
        hasAnyoneRaised = true;

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        tempUser.setTotalCallCount(tempUser.getTotalCallCount() + 1);
        tempUser.setAllInCount(tempUser.getAllInCount() + 1);

        long match = tempUser.getBoardCoin();

        playerCalls[roundIteratorSeat] = "AllIn";
        playerCallValues[roundIteratorSeat] += match;
        playerTotalCallValues[roundIteratorSeat] += match;
        roundCoins += match;

        if (playerCallValues[roundIteratorSeat] > roundCall) {
            roundCall = playerCallValues[roundIteratorSeat];
            roundStarterSeat = -1;
        }

        tempUser.setBoardCoin(0);
        allInCountInRound++;
        isActiveInRound[roundIteratorSeat] = false;

        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();
    }


    private void turnMessage(String username, int seatPosition, String call, long value) {


        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "TurnInfo");

        send.put("gameData", tempJson);
        send.put("dataType", "TurnInfo");

        JSONObject temp = new JSONObject();

        temp.put("username", username);
        temp.put("seatPosition", seatPosition);
        temp.put("call", call);
        temp.put("value", value);

        send.put("data", temp);

        sendMessageToAll(send.toString());
    }

    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //                  FUNCTIONS TO DECLARE WINNER
    //
    //                  AND START A NEW GAME
    //
    //      SENT STRING EXAMPLE:
    //
    //      GameThread Result : 2 20000 a 2.(12,4,0).(12,1,0).(3,3,1).(3,4,0).(10,1,0) b 2.(12,4,0).(12,1,0).(3,3,1).(3,4,0).(10,1,0) true 3
    //
    //                      2           ->          WINNER COUNT
    //                      20000       ->          WINNER COIN FOR EACH USER
    //                      a           ->          USERNAME
    //                      POWER STRING ->         WINNER a POWER STRING
    //                      b           ->          USERNAME
    //                      POWER STRING ->         WINNER b POWER STRING
    //                      true        ->          SHOW WINNER CARDS TO ALL
    //                      3           ->          WINNER FOUND AT LEVEL 3
    //
    //
    //
    //
    //
    //
    //
    //==================================================================================================================

    private void endRound() {

        showAllCardsAtEnd = false;

        if (activeCountInRound == 0) {

            roundStarterSeat = -1;
            clearWinnerData();
            startNewRound();
            return;
        }
        else if (activeCountInRound > 1 && cardShowed < 5) {

            turnCount = 0;
            cycleCount++;
            roundCall = 0;
            roundIteratorSeat = -1;

            for (int i = 0; i < maxPlayerCount; i++) {

                if (inGamePlayers[i] == null) continue;
                if (isActiveInRoom[i] == false) continue;
                if (isPlaying[i] == false) continue;
                if (isActiveInRound[i] == false) continue;

                playerCallValues[i] = 0;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    oneCycleEndMsg();

                    showMoreCards();
                    playRound();
                }
            }).start();

            return;
        }
        else if (activeCountInRound > 1) showAllCardsAtEnd = true;

        roundStarterSeat = -1;

        //SAYS JE KADER KE CHECK KORA LAGBE
        for (int i = 0; i < maxPlayerCount; i++) {
            checker[i] = 1;

            if (inGamePlayers[i] == null || isActiveInRoom[i] == false || isPlaying[i] == false) checker[i] = 0;
        }

        //STORES WINNER DATA INITIALIZED VALUES
        gameRunning = false;
        winnerWhenChecked();
        loadWinnerData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                winnerResultSend();
                startNewRound();
            }
        }).start();
    }

    private void test(){

        activeCountInRound = Server.pokerServer.getLeastPlayerCount();

        showAllCardsAtEnd = true;

        playerTotalCallValues[0] = 1200000;
        playerTotalCallValues[1] = 1500000;
        playerTotalCallValues[2] = 2000000;
        playerTotalCallValues[3] = 2500000;

        checker[0] = 1;
        checker[1] = 1;
        checker[2] = 1;
        checker[3] = 1;

        boardCards.set(0, new Card(1, 5, 0));
        boardCards.set(1, new Card(0, 1, 0));
        boardCards.set(2, new Card(3, 7, 0));
        boardCards.set(3, new Card(2, 10, 0));
        boardCards.set(4, new Card(2, 0, 0));

        inGamePlayers[0].getUser().getPlayerCards().set(0, new Card(0, 5, 1));
        inGamePlayers[0].getUser().getPlayerCards().set(1, new Card(2, 5, 1));

        inGamePlayers[1].getUser().getPlayerCards().set(0, new Card(1, 10, 1));
        inGamePlayers[1].getUser().getPlayerCards().set(1, new Card(1, 12, 1));

        inGamePlayers[2].getUser().getPlayerCards().set(0, new Card(3, 10, 1));
        inGamePlayers[2].getUser().getPlayerCards().set(1, new Card(0, 12, 1));

        inGamePlayers[3].getUser().getPlayerCards().set(0, new Card(2, 12, 1));
        inGamePlayers[3].getUser().getPlayerCards().set(1, new Card(3, 0, 1));



        /*
        User tempUser;
        String tempS;
        ArrayList powers = new ArrayList<String[]>();
        ArrayList powersSaver = new ArrayList<String>();
        ArrayList sevenCards = new ArrayList<Card>();


        for (int i = 0; i < 5; i++) sevenCards.add(boardCards.get(i));

        sevenCards.add(boardCards.get(0));
        sevenCards.add(boardCards.get(0));


        //GOT POWERS OF ALL ACTIVE PLAYERS

        for (int i = 0; i < maxPlayerCount; i++) {

            if (checker[i] == 0) {

                powers.add(new String[]{"nai"});
                powersSaver.add("Nai");

                continue;
            }

            tempUser = inGamePlayers[i].getUser();

            sevenCards.set(5, tempUser.getPlayerCards().get(0));
            sevenCards.set(6, tempUser.getPlayerCards().get(1));

            tempS = Card.getPower(sevenCards);

            powers.add(tempS.split("\\."));
            powersSaver.add(tempS);
        }
        */
        /*
        //DETECTING WINNER DATA


        int maxVal = -1;
        int maxLoc = -1;
        int maxCount = activeCountInRound;
        int maxFinderCheckLevel = -1;
        int totalPossibleLevel = 6;

        int curVal = -1;
        int curLoc = -1;

        for (int i = 0; i < totalPossibleLevel; i++) {

            maxVal = -1;
            maxLoc = -1;

            for (int j = 0; j < maxPlayerCount; j++) {

                if (checker[j] == 0) continue;

                String[] temp = (String[]) powers.get(j);

                curVal = -1;
                curLoc = -1;


                //================================================================================
                //
                //  RETRIEVE DATA FROM FORMAT         ( CARD VALUE , SUIT VALUE , LOCATION VALUE )
                //
                //  OR RETRIEVE POWER VALUE FORMAT        POWER VALUE.CARD1.CARD2............
                //
                //=================================================================================

                if (temp[i].charAt(0) == '(') {

                    String[] tt = temp[i].substring(1, temp[i].length() - 1).split(",");

                    curVal = Integer.valueOf(tt[0]);
                    curLoc = Integer.valueOf(tt[2]);
                } else curVal = Integer.valueOf(temp[i]);


                //GETTING MAX DATA

                if (curVal > maxVal) {

                    maxVal = curVal;
                    maxLoc = curLoc;

                } else if (curVal == maxVal) {

                    if (curLoc == maxLoc) continue;

                    else if (curLoc > maxLoc) {

                        maxLoc = curLoc;

                    }
                } else continue;

            }

            //TEMP MAX COUNT MEANS WINNER ACCORDING TO i-TH LEVEL

            int tempMaxCount = 0;


            for (int j = 0; j < maxPlayerCount; j++) {

                if (checker[j] == 0) continue;

                String[] temp = (String[]) powers.get(j);


                //LOADING DATA

                curVal = -1;
                curLoc = -1;

                if (temp[i].charAt(0) == '(') {

                    String[] tt = temp[i].substring(1, temp[i].length() - 1).split(",");

                    curVal = Integer.valueOf(tt[0]);
                    curLoc = Integer.valueOf(tt[2]);

                } else curVal = Integer.valueOf(temp[i]);


                //THIS PLAYER LOST, DONT CHECK ANYMORE
                //ELSE INCREASE

                if (curVal < maxVal || (curVal == maxVal && curLoc < maxLoc)) checker[j] = 0;
                else tempMaxCount++;
            }


            //CHANGE LEVEL VALUE

            if (maxCount > tempMaxCount) {
                maxFinderCheckLevel = i;
                maxCount = tempMaxCount;
            }
        }


        //LOADING WINNER DATA

        roundWinnerCount = 0;
        roundWinnersSeat.clear();
        roundWinnersAmount.clear();
        roundPlayersCoinBack.clear();


        for (int i = 0; i < maxPlayerCount; i++) {

            if (checker[i] == 1) {

                roundWinnerCount++;
                roundWinnersSeat.add(i);
                roundResultPower += powersSaver.get(i) + " ";
            }
        }
        resultFoundAtLevel = maxFinderCheckLevel;*/



        /*//MAKING RESULTANT STRING TO SEND

        long winAmount = (long) ((double) roundCoins / roundWinnerCount);
        String[] results = roundResultPower.split(" ");


        JSONObject winners = new JSONObject();
        winners.put("winAmount", winAmount);

        JSONArray array = new JSONArray();
        JSONObject temp;

        for (int i = 0; i < roundWinnersSeat.size(); i++) {

            temp = new JSONObject();
            int k = (int) roundWinnersSeat.get(i);
            User tempUser = inGamePlayers[k].getUser();

            temp.put("username", tempUser.getUsername());
            temp.put("resultString", results[i]);
            temp.put("seatPosition", tempUser.getSeatPosition());

            array.put(temp);
        }

        winners.put("winnerData", array);
        winners.put("showCards", showAllCardsAtEnd);

        if (showAllCardsAtEnd) {
            sendAllCards();
            winners.put("showWinLevel", resultFoundAtLevel);
        }
        */




        /*
        long winAmount;
        if (roundWinnerCount != 0) winAmount = (long) (((double) roundCoins) / roundWinnerCount);
        else winAmount = 0;

        int kk = 0;
        int winners[] = new int[maxPlayerCount];
        String[] results = roundResultPower.split(" ");

        for (int i = 0; i < maxPlayerCount; i++) winners[i] = 0;

        for (int i = 0; i < roundWinnersSeat.size(); i++) {
            int k = (int) roundWinnersSeat.get(i);
            winners[k] = 1;
        }


        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            User temp = inGamePlayers[i].getUser();
            temp.setRoundsPlayed(temp.getRoundsPlayed() + 1);

            if (winners[i] == 1) {
                temp.setRoundsWon(temp.getRoundsWon() + 1);
                temp.setWinStreak(temp.getWinStreak() + 1);
                temp.setCoinWon(temp.getCoinWon() + winAmount);
                temp.setBiggestWin(max(temp.getBiggestWin(), winAmount));
                if (showAllCardsAtEnd) temp.setBestHand(Card.compareHand(temp.getBestHand(), results[kk]));
                temp.setBoardCoin(temp.getBoardCoin() + winAmount);

                kk++;
            } else {
                temp.setWinStreak(0);
                temp.setCoinLost(temp.getCoinLost() + playerTotalCallValues[i]);
            }
        }*/
    }

    private void winnerWhenChecked() {

        clearWinnerData();

        int winLevel = 1;
        long[] potValues = new long[maxPlayerCount];
        ArrayList sevenCards = new ArrayList<Card>();
        ArrayList powers = new ArrayList<String[]>();


        for(int i=0; i<maxPlayerCount; i++) potValues[i] = playerTotalCallValues[i];

        for(int i=0; i<5; i++) sevenCards.add(boardCards.get(i));
        sevenCards.add(boardCards.get(0));
        sevenCards.add(boardCards.get(0));


        for(int i=0; i<maxPlayerCount; i++){

            if(checker[i] == 0) {
                powers.add(new String[]{"nai"});
                continue;
            }
            if(inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

            User tempUser = inGamePlayers[i].getUser();

            sevenCards.set(5, tempUser.getPlayerCards().get(0));
            sevenCards.set(6, tempUser.getPlayerCards().get(1));

            roundResultPower[i] = Card.getPower(sevenCards);
            powers.add(roundResultPower[i].split("\\."));
        }

        while(true){

            int ase = 0;
            for(int i=0; i<maxPlayerCount; i++){
                if(checker[i] == 1) ase++;
            }

            if( ase < 1 ) break;

            long minPot = Long.MAX_VALUE;                   //done
            long winAmount = 0;                             //done
            long eachWinnerAmount = 0;                      //done

            ArrayList winners = new ArrayList<Integer>();
            int winnerFoundLevel = -1;

            int[] tempChecker = new int[maxPlayerCount];
            for(int i=0; i<maxPlayerCount; i++) tempChecker[i] = checker[i];

            int maxVal = -1;
            int maxLoc = -1;
            int curVal = -1;
            int curLoc = -1;
            int maxFinderCheckLevel = -1;
            int maxCount = activeCountInRound;
            int totalPossibleLevel = 6;


            for(int i=0; i<totalPossibleLevel; i++){

                maxVal = -1;
                maxLoc = -1;

                for(int j=0; j<maxPlayerCount; j++){

                    if(tempChecker[j] == 0) continue;

                    String[] temp = (String[]) powers.get(j);

                    curVal = -1;
                    curLoc = -1;


                    //================================================================================
                    //
                    //  RETRIEVE DATA FROM FORMAT         ( CARD VALUE , SUIT VALUE , LOCATION VALUE )
                    //
                    //  OR RETRIEVE POWER VALUE FORMAT        POWER VALUE.CARD1.CARD2............
                    //
                    //=================================================================================

                    if (temp[i].charAt(0) == '(') {

                        String[] tt = temp[i].substring(1, temp[i].length() - 1).split(",");

                        curVal = Integer.valueOf(tt[0]);
                        curLoc = Integer.valueOf(tt[2]);

                    } else curVal = Integer.valueOf(temp[i]);


                    //GETTING MAX DATA

                    if (curVal > maxVal) {

                        maxVal = curVal;
                        maxLoc = curLoc;

                    } else if (curVal == maxVal) {

                        if (curLoc == maxLoc) continue;

                        else if (curLoc > maxLoc) {

                            maxLoc = curLoc;

                        }
                    } else continue;


                }

                int tempMaxCount = 0;


                for (int j = 0; j < maxPlayerCount; j++) {

                    if (tempChecker[j] == 0) continue;

                    String[] temp = (String[]) powers.get(j);


                    //LOADING DATA

                    curVal = -1;
                    curLoc = -1;

                    if (temp[i].charAt(0) == '(') {

                        String[] tt = temp[i].substring(1, temp[i].length() - 1).split(",");

                        curVal = Integer.valueOf(tt[0]);
                        curLoc = Integer.valueOf(tt[2]);

                    } else curVal = Integer.valueOf(temp[i]);


                    //THIS PLAYER LOST, DONT CHECK ANYMORE
                    //ELSE INCREASE

                    if (curVal < maxVal || (curVal == maxVal && curLoc < maxLoc)) tempChecker[j] = 0;
                    else tempMaxCount++;
                }

                //CHANGE LEVEL VALUE
                if (maxCount > tempMaxCount) {
                    maxFinderCheckLevel = i;
                    maxCount = tempMaxCount;
                }

                if(maxCount == 1) break;
            }

            winnerFoundLevel = maxFinderCheckLevel;
            for(int i=0; i<maxPlayerCount; i++){
                if(tempChecker[i] == 1) {
                    winners.add(i);
                    minPot = min(potValues[i], minPot);
                }
            }

            for(int i=0; i<maxPlayerCount; i++){

                if(inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

                long k = min(potValues[i] , minPot);
                winAmount += k;
                potValues[i] -= k;
            }

            eachWinnerAmount = (long) (((double) winAmount) / winners.size());

            for(int i=0; i<winners.size(); i++){
                int k = (int) winners.get(i);
                roundWinnersSeat[k] = 1;
                roundWinnersAmount[k] += eachWinnerAmount;
                resultFoundAtLevel[k].add(winnerFoundLevel);
                winnerLevel[k].add(winLevel);
                winnerLevelAmount[k].add(eachWinnerAmount);
            }


            for(int i=0; i<maxPlayerCount; i++){
                if(potValues[i] == 0) checker[i] = 0;
            }
            winLevel++;
        }

        for(int i=0; i<maxPlayerCount; i++) {

            if(inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

            roundPlayersCoinBack[i] += potValues[i];
        }
    }

    private void clearWinnerData(){

        maxWinLevelCount = 0;
        roundWinnerCount = 0;

        roundWinnersSeat = new int[maxPlayerCount];
        roundResultPower = new String[maxPlayerCount];
        resultFoundAtLevel = new ArrayList[maxPlayerCount];
        roundWinnersAmount = new long[maxPlayerCount];
        roundPlayersCoinBack = new long[maxPlayerCount];

        winnerLevel = new ArrayList[maxPlayerCount];
        for(int i=0; i<maxPlayerCount; i++) winnerLevel[i] = new ArrayList<Integer>();

        winnerLevelAmount = new ArrayList[maxPlayerCount];
        for(int i=0; i<maxPlayerCount; i++) winnerLevelAmount[i] = new ArrayList<Long>();

        resultFoundAtLevel = new ArrayList[maxPlayerCount];
        for(int i=0; i<maxPlayerCount; i++) resultFoundAtLevel[i] = new ArrayList<Integer>();


        for(int i=0; i<maxPlayerCount; i++){
            roundWinnersSeat[i] = 0;
            roundResultPower[i] = "";
            roundWinnersAmount[i] = 0;
            roundPlayersCoinBack[i] = 0;
        }
    }



    private void oneCycleEndMsg() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "CycleEnd");

        send.put("gameData", tempJson);

        send.put("dataType", "CycleEnd");
        send.put("data", "One cycle has ended, starting new cycle");

        sendMessageToAll(send.toString());

        waitGame(2);
    }

    private void sendAllCards() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "ShowAllCards");

        send.put("gameData", tempJson);
        send.put("dataType", "ShowAllCards");

        JSONObject allCards = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject temp;
        JSONArray array2;

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;
            if (isActiveInRoom[i] == false) continue;
            if (isPlaying[i] == false) continue;

            User tempUser = inGamePlayers[i].getUser();

            temp = new JSONObject();
            temp.put("seatPosition", tempUser.getSeatPosition());
            temp.put("username", tempUser.getUsername());

            array2 = new JSONArray();
            array2.put(((Card) tempUser.getPlayerCards().get(0)).toString3());
            array2.put(((Card) tempUser.getPlayerCards().get(1)).toString3());

            temp.put("cards", array2);

            array.put(temp);
        }
        allCards.put("allPlayerCards", array);


        JSONArray array1 = new JSONArray();
        for (int i = 0; i < boardCards.size(); i++) array1.put(((Card) boardCards.get(i)).toString3());

        allCards.put("allBoardCards", array1);
        send.put("data", allCards);

        sendMessageToAll(send.toString());
    }


    private void winnerResultSend() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "Result");

        send.put("gameData", tempJson);


        JSONObject data = new JSONObject();
        JSONArray temp = new JSONArray();
        JSONObject tempJson1;

        for(int i=0; i<maxPlayerCount; i++){

            if(inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

            tempJson1 = new JSONObject();

            tempJson1.put("seat", i);
            tempJson1.put("hasWon", roundWinnersSeat[i] != 0);
            tempJson1.put("winAmount", roundWinnersAmount[i]);
            tempJson1.put("coinBack", roundPlayersCoinBack[i]);
            tempJson1.put("power", roundResultPower[i]);

            JSONArray jsonArray = new JSONArray();

            for(int j=0; j<winnerLevel[i].size(); j++){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("level", winnerLevel[i].get(j));
                jsonObject.put("amount", winnerLevelAmount[i].get(j));
                jsonObject.put("resultFoundAtLevel", resultFoundAtLevel[i].get(j));

                maxWinLevelCount = Math.max(maxWinLevelCount, (int) winnerLevel[i].get(j));

                jsonArray.put(jsonObject);
            }
            tempJson1.put("winLevel", jsonArray);

            temp.put(tempJson1);
        }


        data.put("eachPlayerData", temp);
        data.put("showCards", showAllCardsAtEnd);

        if (showAllCardsAtEnd) sendAllCards();

        send.put("dataType", "Result");
        send.put("data", data);

        sendMessageToAll(send.toString());

        double v = 3.5 * (double) maxWinLevelCount;
        waitGame( v );
    }


    private void loadWinnerData() {

        for(int i=0; i<maxPlayerCount; i++){

            if(inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

            User temp = inGamePlayers[i].getUser();
            temp.setRoundsPlayed(temp.getRoundsPlayed() + 1);
            temp.setBoardCoin(temp.getBoardCoin() + roundPlayersCoinBack[i]);

            if (roundWinnersSeat[i] == 1) {
                temp.setRoundsWon(temp.getRoundsWon() + 1);
                temp.setWinStreak(temp.getWinStreak() + 1);
                temp.setCoinWon(temp.getCoinWon() + roundWinnersAmount[i]);
                temp.setBiggestWin(max(temp.getBiggestWin(), roundWinnersAmount[i]));
                if (showAllCardsAtEnd) temp.setBestHand( Card.compareHand(temp.getBestHand(), roundResultPower[i] ));
                temp.setBoardCoin(temp.getBoardCoin() + roundWinnersAmount[i]);
            } else {
                temp.setWinStreak(0);
                temp.setCoinLost(temp.getCoinLost() + playerTotalCallValues[i]);
            }
        }
    }

    //==================================================================================================================
    //
    //==================================================================================================================







    //==================================================================================================================
    //
    //          ON JOIN BY CODE REQUESTS
    //
    //==================================================================================================================

    public void askBoardCoin(ServerToClient s){

        s.setGameThread(this);
        sendAskBoardCoin(s);
    }

    private void sendAskBoardCoin(ServerToClient s){

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("gameId", gameId);
        tempJson.put("gameCode", gameCode);
        tempJson.put("boardType", boardType);
        tempJson.put("ownerId", getOwnerId());
        tempJson.put("minCallValue", minCallValue);
        tempJson.put("minEntryValue", minEntryValue);
        tempJson.put("gameRequest", "AskBoardCoin");

        send.put("gameData", tempJson);
        s.sendMessage(send.toString());
    }

    //==================================================================================================================
    //
    //==================================================================================================================














    //==================================================================================================================
    //
    //              WAIT FOR PLAYERS
    //              OR
    //              WAIT FOR PLAYERS TO BUY COINS
    //
    //==================================================================================================================

    private void waitForPlayersToBuyMsg() {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "WaitForPlayersToBuy");

        send.put("gameData", tempJson);

        sendMessageToAll(send.toString());
    }

    private void requestAddBoardCoin(ServerToClient s, JSONObject jsonObject) {

        int loc = s.getUser().getSeatPosition();

        JSONObject gameData = jsonObject.getJSONObject("gameData");

        User temp = s.getUser();
        long currentCoin = temp.getCurrentCoin();

        long amount = gameData.getLong("amount");

        if (amount > currentCoin) sendAddBoardCoinResponse(false, -1, loc);
        else {

            temp.setCurrentCoin(temp.getCurrentCoin() - amount);
            temp.setBoardCoin(temp.getBoardCoin() + amount);

            sendAddBoardCoinResponse(true, amount, loc);
            sendPlayersDataToAll();
        }

    }

    private void sendAddBoardCoinResponse(boolean success, long value, int loc) {

        JSONObject send = initiateJson();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "AddBoardCoinResponse");
        tempJson.put("amount", value);
        tempJson.put("success", success);

        send.put("gameData", tempJson);
        send.put("dataType", "AddBoardCoin");

        sendMessage(loc, send.toString());
    }





    //==================================================================================================================
    //
    //                  GETTERS, SETTERS, toString FUNCTION
    //
    //==================================================================================================================


    @Override
    public int compareTo(Object o) {

        GameThread g = (GameThread) o;

        if(playerCount > g.playerCount) return 1;
        else if(playerCount < g.playerCount) return -1;
        else return 0;
    }

    private void waitGame(double sec){

        Long secMilli = (long) (sec * 1000);

        LocalTime start = LocalTime.now();

        while(true){

            LocalTime cur = LocalTime.now();
            long diff = SECONDS.between(start, cur) * 1000;

            if(diff >= secMilli) break;
        }
    }

    private long max(long a, long b) {
        if (a > b) return a;
        else return b;
    }

    private long min(long a, long b) {
        if (a > b) return b;
        else return a;
    }

    @Override
    public String toString() {
        return "GameThread{" +
                "gameCode=" + gameCode +
                ", gameId=" + gameId +
                ", boardType='" + boardType + '\'' +
                ", minCallValue=" + minCallValue +
                ", minEntryValue=" + minEntryValue +
                ", hasBot=" + hasBot +
                ", playerCount=" + playerCount +
                ", maxPlayerCount=" + maxPlayerCount +
                ", isPrivate=" + isPrivate +
                ", owner=" + owner +
                ", inGamePlayers=" + Arrays.toString(inGamePlayers) +
                ", playerCallValues=" + Arrays.toString(playerCallValues) +
                ", playerTotalCallValues=" + Arrays.toString(playerTotalCallValues) +
                ", playerCalls=" + Arrays.toString(playerCalls) +
                ", isActiveInRoom=" + Arrays.toString(isActiveInRoom) +
                ", isPlaying=" + Arrays.toString(isPlaying) +
                ", isActiveInRound=" + Arrays.toString(isActiveInRound) +
                ", jsonIncoming=" + jsonIncoming +
                ", checker=" + Arrays.toString(checker) +
                ", waitingToClose=" + waitingToClose +
                ", gameRunning=" + gameRunning +
                ", closeTimer=" + closeTimer +
                ", boardCards=" + boardCards +
                ", roundCount=" + roundCount +
                ", cycleCount=" + cycleCount +
                ", turnCount=" + turnCount +
                ", roundCoins=" + roundCoins +
                ", roundCall=" + roundCall +
                ", roundIteratorName='" + roundIteratorName + '\'' +
                ", roundIteratorSeat=" + roundIteratorSeat +
                ", roundStarterSeat=" + roundStarterSeat +
                ", cardShowed=" + cardShowed +
                ", hasAnyoneCalled=" + hasAnyoneCalled +
                ", activeCountInRound=" + activeCountInRound +
                ", allInCountInRound=" + allInCountInRound +
                ", smallBlindSeat=" + smallBlindSeat +
                ", bigBlindSeat=" + bigBlindSeat +
                ", foldCost=" + foldCost +
                ", showAllCardsAtEnd=" + showAllCardsAtEnd +
                ", previousCycleStarterSeat=" + previousCycleStarterSeat +
                ", roundWinnerCount=" + roundWinnerCount +
                ", roundWinnersSeat=" + roundWinnersSeat +
                ", roundResultPower='" + roundResultPower + '\'' +
                ", resultFoundAtLevel=" + resultFoundAtLevel +
                '}' + "\n";
    }

    public String printGameThread(){

        String ret = "";

        ret += "Id: " + gameId + "\n";
        ret += "Code: " + gameCode + "\n";
        ret += "Board Type: " + boardType + "\n";
        ret += "Min Entry Value: " + ( (double) minEntryValue / 100000 ) + "lac\n";
        ret += "Min Call Value: " + ( (double) minCallValue / 100000 ) + "lac\n";
        ret += "Player Count: " + playerCount + "\n";
        ret += "Has bot: " + hasBot + "\n";
        ret += "Players: " + "\n";


        for(int i=0; i<maxPlayerCount; i++){
            if(inGamePlayers[i] == null || inGamePlayers[i].getUser() == null) continue;

            User user = inGamePlayers[i].getUser();

            ret += "Seat: " + i + " ";
            ret += "Username: " + user.getUsername() + " ";
            ret += "Id: " + user.getId() + " ";
            ret += "isBot: " + user.isBot() + " ";

            if(user.getLoginMethod().equals("facebook")) ret += "Login id: " + user.getFb_id() + " ";
            else if(user.getLoginMethod().equals("google")) ret += "Login id: " + user.getGmail_id() + " ";

            ret += "Board coin: " + ( (double) user.getBoardCoin() / 100000 ) + "lac ";
            ret += "\n";
        }
        ret += "\n";
        return ret;

    }

    public int getGameCode() {
        return gameCode;
    }

    public int getGameId() {
        return gameId;
    }

    public String getBoardType() {
        return boardType;
    }

    public long getMinCoinBoard() {
        return minCallValue;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public int getOwnerId(){

        int k;
        if(owner == null) k = -1;
        else k = owner.getUser().getId();

        return k;
    }

    public long getMinCallValue() {
        return minCallValue;
    }

    public long getMinEntryValue() {
        return minEntryValue;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public boolean isHasAnyoneRaised() {
        return hasAnyoneRaised;
    }

    public long getRoundCall() {
        return roundCall;
    }

    public ArrayList getBoardCards() {
        return boardCards;
    }

    public ArrayList getPlayerCards(){

        ArrayList ret = new ArrayList<Card[]>();

        for(int i=0; i<maxPlayerCount; i++){

            if(inGamePlayers[i] == null) ret.add(null);
            else{
                Card[] temp = new Card[2];
                temp[0] = (Card) inGamePlayers[i].getUser().getPlayerCards().get(0);
                temp[1] = (Card) inGamePlayers[i].getUser().getPlayerCards().get(1);

                ret.add(temp);
            }
        }
        return ret;
    }

    public int getBotLoc() {
        return botLoc;
    }

    public User getPlayer(int i){
        if(inGamePlayers[i] == null) return null;
        else return inGamePlayers[i].getUser();
    }

    public boolean isHasBot() {
        return hasBot;
    }

    //==================================================================================================================
    //
    //==================================================================================================================

}
