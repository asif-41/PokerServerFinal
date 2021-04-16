package com.example.PokerServer.GameThread;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.Objects.Card;
import com.example.PokerServer.Objects.Randomizer;
import com.example.PokerServer.Objects.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GameThread implements Runnable {


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

    private int gameCode;                               //  GAME CODE TO JOIN BY CODE
    private int gameId;                                 //  GAME ID, PRIMARY KEY OF DATABASE
    private String boardType;                           //  TYPE OF BOARD
    private int minCoinBoard;                           //  MINIMUM CALL VALUE FOR THIS TYPE OF BOARD

    private int playerCount;                            //  PLAYER COUNT IN GAME ROOM, SIZE OF inGamePlayers
    private boolean isPrivate;                          //  INDICATOR IF THIS ROOM IS PRIVATE

    private ArrayList boardCards;                       //  BOARD CARDS, INITIALIZED AT THE START OF A ROUND
    //  NOT SENT ALL AT FIRST, SENT TO PLAYERS EVENTUALLY

    private int roundCount;                             //  ROUND COUNT IN CURRENT GAME ROOM
    private int cycleCount;                             //  CYCLE COUNT
    private int turnCount;                              //  TURN COUNTS IN CURRENT ROUND
    private int roundCoins;                             //  ROUND COINS GIVEN IN THE BOARD
    private int roundCall;                              //  MINIMUM CALL VALUE OF CURRENT ROUND
    private String roundIteratorName;                   //  NAME OF WHICH PLAYER IS TAKING TURN
    private int roundIteratorSeat;                      //  CURRENT PLAYER ER SEAT
    private int roundStarterSeat;                    //  SEAT OF ROUND STARTER
    //  CURRENT ROUND

    private int cardShowed;                             //  THE NUMBER OF CARDS SENT TO PLAYERS
    //  0, THEN 3, THEN 4, THEN 5

    private boolean hasAnyoneCalled;                    //  HAS ANYONE CALLED IN CURRENT ROUND
    private int activeCountInRound;                     //  ACTIVE PLAYERS IN CURRENT ROUND
    // PLAYER COUNT - ( LOW PLAYERS + FOLD PLAYERS)

    private int allInCountInRound;                      //  NUMBER OF PLAYERS THAT GAVE ALL IN IN CURRENT ROUND

    private int smallBlindSeat;                         //  LOCATION OF PLAYER WHO WILL HAVE SMALL BLIND
    private int bigBlindSeat;                           //  LOCATION OF PLAYER WHO WILL HAVE BIG BLIND

    private int foldCost;                               //  FOLD COST FOR BLINDINGS
    private boolean smallBlindMsgSent;                  //  HAVE WE SENT THE SMALL BLIND MESSAGE?
    private boolean bigBlindMsgSent;                    //  HAVE WE SENT THE BIG BLIND MESSAGE?
    private boolean showAllCardsAtEnd;                  //  DO WE NEED TO SHOW ALL, EVERYONES CARDS AT THE END OF THE ROUND
    //  IF GAME ENDS BEFORE cardShowed = 5, THEN WE DONT HAVE TO

    private int previousCycleStarterSeat;               //  Last round e kaar theke start kora hoisilo

    private int roundWinnerCount;                       //  WINNER COUNT
    private ArrayList roundWinnersSeat;                     //  INTEGER ARRAYLIST, LOCATES WINNER LOCATIONS IN inGamePlayers
    private String roundResultPower;                    //  POWER STRING FOR CARDS OF WINNERS
    private int resultFoundAtLevel;                     //  RESULT FOUND AT THIS LEVEL WHILE CHECKING POWER STRING
    //  INDICATES IF WE NEED A KICKER OR NOT

    private JSONObject jsonIncoming;                    //  INCOMING MSGS


    private ServerToClient[] inGamePlayers;             //  IN GAME PLAYERS ACCORDING TO SEAT SEQUENCE
    private int[] playerCallValues;                     //  CALL VALUES OF ALL PLAYERS
    private String[] playerCalls;                       //  CALLS OF PLAYERS

    // inGamePlayers[i] != null                         //  IS PLAYER IN ROOM
    private boolean[] isActiveInRoom;                   //  IS PLAYER ACTIVE IN ROOM
    private boolean[] isPlaying;                        //  IS PLAYER PLAYING IN CURRENT ROUND
    private boolean[] isActiveInRound;                  //  IS PLAYER ACTIVE IN ROUND

    private int checker[];                              //  checker[i] = 0 IF i-th PLAYER FOLDED OR DID NOT HAVE
    //  ENOUGH COIN TO PLAY IN THIS ROUND


    //  "Fold"      "Call"   "Raise"   "Check"   "AllIn"
    //  "Low"   ->      IN GAME ROOM
    //                  BUT DOES NOT HAVE ENOUGH COIN
    //  "New"   ->      MATRO DHUKSE, NEXT ROUND THEKE KHELBE
    private boolean waitingToClose;                     //  IF WAITING TO CLOSE GAMEROOM
    private boolean gameRunning;                        //  IF GAME RUNNING
    private int maxPlayerCount;                         //  MAX PLAYER IN THIS ROOM

    private Timer closeTimer;

    //=======================================================================================================================
    //
    //=======================================================================================================================


    //==================================================================================================================
    //
    //                  INITIALIZING GAME THREAD
    //
    //==================================================================================================================

    public GameThread(ArrayList<ServerToClient> s, int id, int code, String boardname, int minCoin, int maxPlayerCount, boolean privacy) {

        this.maxPlayerCount = maxPlayerCount;
        closeTimer = null;

        isPrivate = privacy;
        gameCode = code;
        gameId = id;
        minCoinBoard = minCoin;
        boardType = boardname;

        initializeGameThread(s);

        jsonIncoming = null;
        waitingToClose = false;
        gameRunning = false;
    }

    private void initializeGameThread(ArrayList<ServerToClient> s) {

        checker = new int[maxPlayerCount];
        inGamePlayers = new ServerToClient[maxPlayerCount];
        playerCalls = new String[maxPlayerCount];
        playerCallValues = new int[maxPlayerCount];
        isActiveInRoom = new boolean[maxPlayerCount];
        isPlaying = new boolean[maxPlayerCount];
        isActiveInRound = new boolean[maxPlayerCount];

        for (int i = 0; i < maxPlayerCount; i++) {
            checker[i] = -1;
            inGamePlayers[i] = null;
            playerCalls[i] = "Empty";
            playerCallValues[i] = -1;

            isActiveInRound[i] = false;
            isPlaying[i] = false;
            isActiveInRoom[i] = false;
        }

        boardCards = new ArrayList<Card>();
        for (int i = 0; i < s.size(); i++) {
            inGamePlayers[i] = s.get(i);
            inGamePlayers[i].setGameThread(this);
            inGamePlayers[i].getUser().setSeatPosition(i);
        }

        playerCount = s.size();
    }

    @Override
    public void run() {

        //WELCOME MESSAGE
        welcomeRoomMsg();

        roundCount = 0;
        previousCycleStarterSeat = -1;
        startNewRound();
    }

    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //      SENDS MESSAGE TO loc-TH CLIENT SAVED IN inGamePlayers ARRAYLIST
    //
    //==================================================================================================================

    private void sendMessage(int loc, String s) {

        ServerToClient temp = inGamePlayers[loc];

        if (temp == null) return;
        temp.sendMessage(s);
    }


    //==================================================================================================================
    //
    //      SENDS s MSG TO ALL USERS
    //
    //      SENDS exceptionMsg TO loc-TH CLIENT FROM inGamePlayer ARRAYLIST
    //
    //==================================================================================================================

    private void sendMessageToAllExceptOne(String s, int loc, String exceptionMsg) {

        sendMessage(loc, exceptionMsg);

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


    //==================================================================================================================
    //
    //      ENABLES GAME BUTTONS WHEN IT IS PLAYER'S TURN
    //
    //      SENDS A STRING, ABOUT THE BUTTONS AVAILABLE FOR THE CURRENT USER
    //
    //      FORMAT:    5 Fold smallBlind 5000 Call Raise Check AllIn
    //
    //                  5           ->   BUTTON COUNT
    //                  smallBlind  ->   BLIND TYPE
    //                  5000        ->   BLIND VALUE
    //
    //==================================================================================================================


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


    //==================================================================================================================
    //
    //      SEND PLAYER CARDS TO CLIENT, AT THE START OF EACH ROUND
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //      REVEAL BOARD CARDS ONE BY ONE TO ALL CLIENTS
    //
    //==================================================================================================================

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
    //      COMMUNICATION WITH IN GAME USERS
    //
    //      STRING PARAMETER FORMATS:    HAS INFORMATION ABOUT USER COMMANDS
    //
    //              GameThread a command :Call 10000   ->     USER a CALLED 10000
    //
    //              GameThread b command :Raise 20000  ->     USER b RAISED 20000
    //
    //              GameThread b command :Fold         ->     USER b FOLDED
    //
    //              GameThread b command :AllIn 95000  ->     USER b WENT ALL IN
    //
    //              GameThread a command :Check        ->     USER a WANTED TO CHECK
    //
    //==================================================================================================================

    public void incomingMessage(String tempp, ServerToClient serverToClient) {

        JSONObject gameData, callData;

        try {
            jsonIncoming = new JSONObject(tempp);
            gameData = jsonIncoming.getJSONObject("gameData");

        } catch (Exception e) {
            System.out.println("Error in getting json in gameThread\n" + e);
            jsonIncoming = null;
            return;
        }

        //CHECKING IF USERNAME MATCHES CURRENT USERNAME
        String username = jsonIncoming.getString("username");

        if (gameData.getString("requestType").equals("GameCall")) {

            callData = gameData.getJSONObject("callData");

            if (callData.getString("call").equals("Call")) {

                int value = callData.getInt("cost");
                handleUserCallRequest(value, username);
            } else if (callData.getString("call").equals("Fold")) {

                handleUserFoldRequest(username);
            } else if (callData.getString("call").equals("Raise")) {

                int value = callData.getInt("cost");
                handleUserRaiseRequest(value, username);
            } else if (callData.getString("call").equals("Check")) {

                handleUserCheckRequest(username);
            } else if (callData.getString("call").equals("AllIn")) {

                handleUserAllInRequest(username);
            }
        } else if (gameData.getString("requestType").equals("StartNewRound")) {

            startNewRound();
        } else if (gameData.getString("requestType").equals("ExitGame")) {

            exitGameResponse(serverToClient);
        }
    }


    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //              HANDLE USER REQUESTS
    //
    //
    //==================================================================================================================

    private void handleUserCallRequest(int value, String username) {


        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        foldCost = 0;

        hasAnyoneCalled = true;

        playerCalls[roundIteratorSeat] = "Call";
        playerCallValues[roundIteratorSeat] = value;
        roundCoins += value;

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();
        tempUser.setCurrentCoin(tempUser.getCurrentCoin() - value);

        if (tempUser.getCurrentCoin() == 0) {
            playerCalls[roundIteratorSeat] = "AllIn";
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

        playerCalls[roundIteratorSeat] = "Fold";
        User tempUser = inGamePlayers[roundIteratorSeat].getUser();

        tempUser.setCurrentCoin(tempUser.getCurrentCoin() - foldCost);
        roundCoins += foldCost;

        isPlaying[roundIteratorSeat] = false;
        isActiveInRound[roundIteratorSeat] = false;
        activeCountInRound--;

        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();
    }

    private void handleUserRaiseRequest(int value, String username) {

        if (!username.equals(roundIteratorName)) {
            System.out.println("Error username did not match in both end");
            return;
        }

        foldCost = 0;
        roundStarterSeat = roundIteratorSeat;

        hasAnyoneCalled = true;
        playerCalls[roundIteratorSeat] = "Raise";
        playerCallValues[roundIteratorSeat] = value;
        roundCoins += value;
        roundCall = value;

        User tempUser = inGamePlayers[roundIteratorSeat].getUser();
        tempUser.setCurrentCoin(tempUser.getCurrentCoin() - value);

        if (tempUser.getCurrentCoin() == 0) {
            playerCalls[roundIteratorSeat] = "AllIn";
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
        User tempUser = inGamePlayers[roundIteratorSeat].getUser();
        int value = tempUser.getCurrentCoin();

        hasAnyoneCalled = true;
        playerCalls[roundIteratorSeat] = "AllIn";
        playerCallValues[roundIteratorSeat] = value;
        roundCoins += value;
        allInCountInRound++;

        isActiveInRound[roundIteratorSeat] = false;

        if (value > roundCall) roundStarterSeat = -1;
        roundCall = max(roundCall, value);

        tempUser.setCurrentCoin(0);

        turnMessage(roundIteratorName, roundIteratorSeat, playerCalls[roundIteratorSeat], playerCallValues[roundIteratorSeat]);
        playRound();
    }


    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //                  FUNCTIONS TO ENSURE GAMEPLAY
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

            if (roundStarterSeat == -1) {

                previousCycleStarterSeat = findNextAvailableSeatFromThisPosition(previousCycleStarterSeat);
                roundIteratorSeat = previousCycleStarterSeat;
                bigBlindSeat = roundIteratorSeat;
                smallBlindSeat = findNextAvailableSeatFromThisPosition(roundIteratorSeat);
                bigBlindMsgSent = false;
                smallBlindMsgSent = false;

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
            String username = inGamePlayers[roundIteratorSeat].getUser().getUsername();
            roundIteratorName = username;
        }
    }

    private void playRound() {

        //EKJON BAADE SHOBAI FOLD NAILE LOW

        if (activeCountInRound == 1 || activeCountInRound == 0) {
            endRound();
            return;
        }
        //sendBoardInfo();
        updateRoomData();
        sendBoardInfo();


        //SHOBAI ALL IN DISE
        //ALL IN DIYE EKJON BAKI THAKLE OITA NEXT TURN ER FUNCTION E HANDLE HOBE

        if (allInCountInRound == activeCountInRound) {
            endRound();
            return;
        }

        System.out.println("Prev iterator " + roundIteratorSeat);
        System.out.println("Prev starter " + roundStarterSeat);

        setupForNextTurn();

        System.out.println("Next iterator " + roundIteratorSeat);
        System.out.println("Next starter " + roundStarterSeat);

        //ROUND E EKBAR TOTAL GHURA HOISE

        if (roundIteratorSeat == roundStarterSeat) {
            endRound();
            return;
        }
        //enableGameButtons2();
        sendNextTurnInfo();
        enableGameButtons();
    }

    private int max(int a, int b) {
        if (a > b) return a;
        else return b;
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
            noWinnerResult();
            startNewRound();
            return;
        } else if (activeCountInRound > 1 && cardShowed < 5) {

            turnCount = 0;
            cycleCount++;
            roundCall = minCoinBoard;
            roundIteratorSeat = -1;

            for (int i = 0; i < maxPlayerCount; i++) {

                if (inGamePlayers[i] == null) continue;
                if (isActiveInRoom[i] == false) continue;
                if (isPlaying[i] == false) continue;
                if (isActiveInRound[i] == false) continue;

                playerCalls[i] = "";
                playerCallValues[i] = 0;
            }
            oneCycleEndMsg();

            showMoreCards();
            playRound();

            return;
        } else if (activeCountInRound > 1) showAllCardsAtEnd = true;


        roundStarterSeat = -1;

        //SAYS JE KADER KE CHECK KORA LAGBE

        for (int i = 0; i < maxPlayerCount; i++) {
            checker[i] = 1;

            if (inGamePlayers[i] == null || isActiveInRoom[i] == false || isPlaying[i] == false) checker[i] = 0;
        }


        //INITIALIZING NECESSARY VALUES

        roundWinnerCount = 0;
        roundResultPower = "";
        resultFoundAtLevel = -1;
        roundWinnersSeat = new ArrayList<Integer>();


        //STORES WINNER DATA INITIALIZED VALUES

        winnerWhenChecked();
        winnerResultSend();


        deployToDatabase();
        startNewRound();
    }

    private void winnerWhenOneActivePlayer() {


        //LOADING WINNER DATA

        int loc = -1;
        roundWinnerCount = 0;
        roundWinnersSeat.clear();


        for (int i = 0; i < maxPlayerCount; i++) {

            if (checker[i] == 1) {
                loc = i;
                break;
            }
        }

        User tempUser = inGamePlayers[loc].getUser();

        ArrayList sevenCards = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) sevenCards.add(boardCards.get(i));

        sevenCards.add(tempUser.getPlayerCards().get(0));
        sevenCards.add(tempUser.getPlayerCards().get(1));

        roundWinnerCount = 1;
        roundWinnersSeat.add(loc);
        roundResultPower = Card.getPower(sevenCards) + " ";
        resultFoundAtLevel = -1;
    }

    private void winnerWhenChecked() {

        if (activeCountInRound == 1) {
            winnerWhenOneActivePlayer();
            return;
        }

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


        for (int i = 0; i < maxPlayerCount; i++) {

            if (checker[i] == 1) {

                roundWinnerCount++;
                roundWinnersSeat.add(i);
                roundResultPower += powersSaver.get(i) + " ";
            }
        }
        resultFoundAtLevel = maxFinderCheckLevel;
    }


    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //                  FUNCTIONS TO START OR END A ROUND
    //
    //                  DATABASE FUNCTIONS
    //
    //      startNewRound()         ->      INITIALIZES A ROUND
    //                                      INITIALIZES DATA FROM DATABASE
    //
    //      deployToDatabase()      ->      AFTER END OF A ROUND, CALL THIS FUNCTION
    //                                      TO WRITE IN DATABASE
    //
    //      makeClientsWait()       ->      SENDS WAIT REQUEST TO CLIENTS
    //
    //      waitRequestFromClient() ->      WAIT REQUEST FROM CLIENT
    //
    //
    //==================================================================================================================

    private void startNewRound() {

        activeCountInRound = 0;
        roundCall = minCoinBoard;

        //HERE PLAYER DATA MUST COME FROM DATABASE

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            playerCalls[i] = "";
            playerCallValues[i] = 0;

            User tempUser = inGamePlayers[i].getUser();

            if (tempUser.getCurrentCoin() < minCoinBoard) {

                playerCalls[i] = "Low";

                isActiveInRoom[i] = false;
                isPlaying[i] = false;
                isActiveInRound[i] = false;
            } else {
                isActiveInRoom[i] = true;
                isPlaying[i] = true;
                isActiveInRound[i] = true;

                activeCountInRound++;
            }
        }

        if (activeCountInRound == 0 || activeCountInRound == 1) {

            gameRunning = false;

            if (waitingToClose == true) return;

            waitForPlayersToBuyMsg();
            waitingToClose = true;

            closeTimer = new Timer();
            closeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    closeRoom();
                }
            }, 30000);

            return;
        }
        if (closeTimer != null) closeTimer.cancel();

        updateRoomData();

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

        roundStartMsg();

        loadCards();
        checkCoinInBothSide();

        sendPlayerCardsToClient();
        sendBoardCardsToClient(new int[]{});

        playRound();
    }

    private void deployToDatabase() {

    }

    private void makeClientsWait() {

    }

    private void waitRequestFromClient() {

    }

    //==================================================================================================================
    //
    //==================================================================================================================


    //==================================================================================================================
    //
    //          CLOSE FUNCTIONS
    //
    //==================================================================================================================

    private void closeRoom() {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            ServerToClient s = inGamePlayers[i];
            s.leaveGameRoom();
            kickFromRoomRequest(i);
        }
        Server.pokerServer.removeGameThread(this);
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

    public void exitGameResponse(ServerToClient s) {

        int seatPosition = s.getUser().getSeatPosition();
        int starterSeat = roundStarterSeat;
        User tempUser = inGamePlayers[seatPosition].getUser();


        if (playerCalls[seatPosition].equals("Low") || playerCalls[seatPosition].equals("Fold")) {
        } else activeCountInRound--;

        if (playerCalls[seatPosition].equals("AllIn")) allInCountInRound--;
        if (seatPosition == previousCycleStarterSeat) {
        }

        playerCount--;
        inGamePlayers[seatPosition] = null;
        playerCalls[seatPosition] = "Empty";
        playerCallValues[seatPosition] = -1;

        isActiveInRoom[seatPosition] = false;
        isPlaying[seatPosition] = false;
        isActiveInRound[seatPosition] = false;

        s.leaveGameRoom();
        if (gameRunning == false) {
            return;
        }

        if (seatPosition == smallBlindSeat && smallBlindMsgSent == false) {
            tempUser.setCurrentCoin(tempUser.getCurrentCoin() - foldCost);
            roundCoins += minCoinBoard / 2;
            smallBlindMsgSent = true;
        } else if (seatPosition == bigBlindSeat && bigBlindMsgSent == false) {
            tempUser.setCurrentCoin(tempUser.getCurrentCoin() - foldCost);
            roundCoins += minCoinBoard;
            bigBlindMsgSent = true;
        }


        if (seatPosition == roundIteratorSeat) {

            //PLAY ROUND AFTERING NULLING CURRENT LOCATION
            tempUser.setCurrentCoin(tempUser.getCurrentCoin() - foldCost);
            roundCoins += foldCost;

            playRound();
            return;
        } else if (seatPosition == roundStarterSeat) {

            //SET ROUND STARTER = NEXT ACTIVE PLAYER
            //NEXT ACTIVE KEU NA THAKLE STARTER = -1

            roundStarterSeat = getNextActivePlayer(starterSeat, roundIteratorSeat);
        }

        if (activeCountInRound == 1 || activeCountInRound == 0) endRound();
    }

    //==================================================================================================================
    //
    //==================================================================================================================




    //==================================================================================================================
    //
    //
    //
    //==================================================================================================================

    public void addInGameThread(ServerToClient s) {

        System.out.println("Ekjon re add korbe");

        int loc = -1;

        for (int i = 0; i < maxPlayerCount; i++) {
            if (inGamePlayers[i] == null) {
                loc = i;
                break;
            }
        }
        s.setGameThread(this);

        s.getUser().setSeatPosition(loc);
        inGamePlayers[loc] = s;

        playerCalls[loc] = "New";
        playerCallValues[loc] = 0;
        isActiveInRoom[loc] = false;
        isPlaying[loc] = false;
        isActiveInRound[loc] = false;

        playerCount++;
        welcomeRoomMsg(loc);

        if (gameRunning == false) startNewRound();
    }













    //==================================================================================================================
    //
    //              COMMUNICATION WITH
    //              JSON CODE
    //
    //==================================================================================================================

    private JSONObject initiateResponse() {

        JSONObject temp = new JSONObject();

        temp.put("sender", "Server");
        temp.put("ip", Server.pokerServer.getHost());
        temp.put("port", Server.pokerServer.getPort());
        temp.put("requestType", "GameRoom");

        return temp;
    }

    private void updateRoomData() {

        //shobar data database theke nibe

        //updating player position
        
        /*
        for (int i = 0; i < playerCount; i++) {
            User tempUser = ((ServerToClient) inGamePlayers.get(i)).getUser();
            tempUser.setSeatPosition(i);
            seat[i] = i;
        }
        */
        
        sendRoomData();
    }


    private void sendRoomData() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "LoadRoomData");

        send.put("gameData", tempJson);

        JSONArray array = new JSONArray();

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            JSONObject temp = new JSONObject();
            User tempUser = inGamePlayers[i].getUser();

            temp.put("username", tempUser.getUsername());
            temp.put("totalCoin", tempUser.getCurrentCoin() - tempUser.getCurrentCoin());
            temp.put("boardPlayerCoin", tempUser.getCurrentCoin());
            temp.put("seatPosition", tempUser.getSeatPosition());
            temp.put("playerCall", playerCalls[i]);
            temp.put("playerCallValue", playerCallValues[i]);

            array.put(temp);
        }
        send.put("data", array);

        sendMessageToAll(send.toString());
    }

    private void roundStartMsg() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "RoundStartMessage");

        send.put("gameData", tempJson);

        send.put("data", "Round " + roundCount + " start");
        sendMessageToAll(send.toString());
    }

    private void welcomeRoomMsg(int i) {

        if (inGamePlayers[i] == null) return;

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("roomType", boardType);
        tempJson.put("entryCoinAmount", minCoinBoard);
        tempJson.put("gameRequest", "WelcomeMessage");
        tempJson.put("seatPosition", inGamePlayers[i].getUser().getSeatPosition());

        send.put("gameData", tempJson);
        sendMessage(i, send.toString());
    }

    private void welcomeRoomMsg() {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            JSONObject send = initiateResponse();

            JSONObject tempJson = new JSONObject();

            tempJson.put("id", gameId);
            tempJson.put("code", gameCode);
            tempJson.put("roomType", boardType);
            tempJson.put("entryCoinAmount", minCoinBoard);
            tempJson.put("gameRequest", "WelcomeMessage");
            tempJson.put("seatPosition", inGamePlayers[i].getUser().getSeatPosition());

            send.put("gameData", tempJson);
            sendMessage(i, send.toString());

        }

    }

    private void checkCoinInBothSide() {


        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            JSONObject send = initiateResponse();

            JSONObject tempJson = new JSONObject();

            tempJson.put("id", gameId);
            tempJson.put("code", gameCode);
            tempJson.put("gameRequest", "CheckCoinBothEnd");

            send.put("gameData", tempJson);

            send.put("data", inGamePlayers[i].getUser().getCurrentCoin());

            sendMessage(i, send.toString());
        }
    }

    private void sendPlayerCardsToClient() {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;
            if (isActiveInRoom[i] == false) continue;

            JSONObject send = initiateResponse();

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

        JSONObject send = initiateResponse();

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

    private void waitForPlayersToBuyMsg() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "WaitForPlayersToBuy");

        send.put("gameData", tempJson);

        sendMessageToAll(send.toString());
    }

    private void sendBoardInfo() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "BoardData");

        send.put("gameData", tempJson);


        JSONObject temp = new JSONObject();

        temp.put("roundCount", roundCount);
        temp.put("boardCoin", roundCoins);
        temp.put("turnCount", turnCount);
        temp.put("cycleCount", cycleCount);
        temp.put("minimumCallValue", roundCall);

        send.put("data", temp);

        sendMessageToAll(send.toString());
    }

    private void disableButtons(int j) {

        for (int i = 0; i < maxPlayerCount; i++) {

            if (inGamePlayers[i] == null) continue;

            if (i == j) continue;

            JSONObject send = initiateResponse();

            JSONObject tempJson = new JSONObject();

            tempJson.put("id", gameId);
            tempJson.put("code", gameCode);
            tempJson.put("gameRequest", "DisableGameButtons");

            send.put("gameData", tempJson);

            sendMessage(i, send.toString());
        }
    }

    private void enableGameButtons() {

        disableButtons(roundIteratorSeat);

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "EnableGameButtons");

        send.put("gameData", tempJson);


        JSONArray array = new JSONArray();
        JSONObject temp;
        User tempUser = inGamePlayers[roundIteratorSeat].getUser();


        //      FOLD BUTTON
        String blindType = "";

        if (roundIteratorSeat == smallBlindSeat && smallBlindMsgSent == false) {
            foldCost = minCoinBoard / 2;
            blindType = "SmallBlind";
            smallBlindMsgSent = true;
        } else if (roundIteratorSeat == bigBlindSeat && bigBlindMsgSent == false) {
            foldCost = minCoinBoard;
            blindType = "BigBlind";
            bigBlindMsgSent = true;
        } else {
            blindType = "NoBlind";
            foldCost = 0;
        }

        temp = new JSONObject();
        temp.put("name", "Fold");
        temp.put("cost", foldCost);
        temp.put("blindType", blindType);

        array.put(temp);


        //All In button

        temp = new JSONObject();
        temp.put("name", "AllIn");
        temp.put("cost", tempUser.getCurrentCoin());

        array.put(temp);


        //Call or raise button


        if (roundCall <= tempUser.getCurrentCoin()) {

            temp = new JSONObject();
            temp.put("name", "Call");
            temp.put("cost", roundCall);

            array.put(temp);


            temp = new JSONObject();
            temp.put("name", "Raise");
            temp.put("cost", roundCall);

            array.put(temp);
        }

        if (hasAnyoneCalled == false) {

            temp = new JSONObject();
            temp.put("name", "Check");
            temp.put("cost", 0);

            array.put(temp);
        }

        send.put("data", array);
        sendMessage(roundIteratorSeat, send.toString());
    }

    private void turnMessage(String username, int seatPosition, String call, int value) {


        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "TurnInfo");

        send.put("gameData", tempJson);


        JSONObject temp = new JSONObject();

        temp.put("username", username);
        temp.put("seatPosition", seatPosition);
        temp.put("call", call);
        temp.put("value", value);

        send.put("data", temp);

        sendMessageToAll(send.toString());
    }

    private void oneCycleEndMsg() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "CycleEnd");

        send.put("gameData", tempJson);

        send.put("data", "One cycle has ended, starting new cycle");

        sendMessageToAll(send.toString());
    }

    private void sendAllCards() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "AllCards");

        send.put("gameData", tempJson);


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

    private void noWinnerResult() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "Result");

        send.put("gameData", tempJson);

        send.put("data", new JSONArray());

        sendMessageToAll(send.toString());

    }

    private void winnerResultSend() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "Result");

        send.put("gameData", tempJson);

        //MAKING RESULTANT STRING TO SEND

        int winAmount = (int) ((double) roundCoins / roundWinnerCount);
        String[] results = roundResultPower.split(" ");


        JSONObject winners = new JSONObject();
        winners.put("winAmount", winAmount);

        JSONArray array = new JSONArray();
        JSONObject temp;

        for (int i = 0; i < roundWinnersSeat.size(); i++) {

            temp = new JSONObject();
            int k = (int) roundWinnersSeat.get(i);
            User tempUser = inGamePlayers[k].getUser();

            tempUser.setCurrentCoin(tempUser.getCurrentCoin() + winAmount);
            tempUser.setCoinWon(tempUser.getCoinWon() + winAmount);

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

        send.put("data", winners);

        sendMessageToAll(send.toString());
    }

    private void sendNextTurnInfo() {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "NextTurnInfo");

        send.put("gameData", tempJson);

        JSONObject temp = new JSONObject();

        temp.put("username", roundIteratorName);
        temp.put("seatPosition", roundIteratorSeat);

        send.put("data", temp);

        sendMessageToAll(send.toString());
    }

    private void kickFromRoomRequest(int player) {

        JSONObject send = initiateResponse();

        JSONObject tempJson = new JSONObject();

        tempJson.put("id", gameId);
        tempJson.put("code", gameCode);
        tempJson.put("gameRequest", "LeaveGame");

        send.put("gameData", tempJson);

        sendMessage(player, send.toString());
    }

    //==================================================================================================================
    //
    //                  GETTERS, SETTERS, toString FUNCTION
    //
    //==================================================================================================================

    @Override
    public String toString() {
        String ret = "playerCount=" + playerCount +
                ", isPrivate=" + isPrivate +
                ", gameCode=" + gameCode +
                ", gameId=" + gameId +
                ", roundCount=" + roundCount +
                ", boardName=" + boardType + "\n";

        for (int i = 0; i < maxPlayerCount; i++) {
            if (inGamePlayers[i] == null) continue;
            ret += inGamePlayers[i].getUser().getUsername() + " ";
        }

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

    public int getMinCoinBoard() {
        return minCoinBoard;
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

//==================================================================================================================
    //
    //==================================================================================================================

}
