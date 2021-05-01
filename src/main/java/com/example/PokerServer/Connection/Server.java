package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.Randomizer;
import com.example.PokerServer.Objects.User;
import com.example.PokerServer.db.DB;
import com.example.PokerServer.db.DBFactory;
import org.json.JSONArray;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Server {


    //=====================================================
    //
    //          SERVER SIDE SERVER OBJECT
    //
    //=====================================================

    public static Server pokerServer = null;    //      SERVER INSTANCE

    public int dailyCoinVideoCount;              //      DAILY COIN VIDEO COUNT
    public long eachVideoCoin;

    private int maxPlayerCount;                 //      MAX PLAYER COUNT IN A GAME
    private int leastPlayerCount;               //      LEAST NUMBER OF PLAYER FOR A GAME
    public long freeLoginCoin;

    private String host;                        //      SERVER IP
    private int port;                           //      SERVER PORT
    private int queueWaitLimit;                 //      WAIT LIMIT IN QUEUE
    private int queueCheckTimeInterval;         //      QUEUE CHECK INTERVAL
    private int gameCodeLowerLimit;
    private int gameCodeUpperLimit;             //      ROOM CODE UPPER LIMIT
    private ArrayList[] waitingRoom;
    private ArrayList[] pendingQueue;
    private ArrayList[] queueTimeCount;          //      TIME COUNTER FOR EACH OBJECT IN QUEUE
    private ArrayList[] gameThreads;             //      GAME THREADS
    private ArrayList casualConnections;         //      SERVER TO CLIENT OBJECTS
    private ArrayList loggedInUsers;             //      SERVER TO CLIENT OBJECTS OF LOGGED IN USERS
    private int boardTypeCount;                  //      BOARD TYPE COUNT
    private String boardType[];                  //      BOARD NAME FOR EACH TYPE {"board1", "board2", "board3", "board4", "board5"};
    private long minEntryValue[];                 //      MIN ENTRY VALUE FOR EACH TYPE {100000, 100000, 100000, 100000, 100000};
    private long minCallValue[];                  //      MIN CALL VALUE FOR EACH TYPE {10000, 10000, 10000, 10000, 10000};
    private ArrayList guests;                   //      GUEST INTEGERS
    private int maxGuestLimit;                  //      MAX GUEST LIMIT
    public long initialCoin;                    //      INITIAL COIN

    public DB db;
    public int waitingRoomWaitAtStart;
    public int delayInStartingGame;

    //====================================================


    //========================================================================================
    //
    //          CONSTRUCTOR
    //
    //          HAS A TIMER TO CHECK QUEUE EACH 5 SECONDS
    //
    //========================================================================================


    public Server(int boardTypeCount, String[] boardType, long[] minEntryValue, long[] minCallValue, int gameCodeLowerLimit, int gameCodeUpperLimit, int leastPlayerCount, int maxPlayerCount,
                  int queueCheckTimeInterval,
                  int queueWaitLimit, int port, int maxGuestLimit, long initialCoin, int dailyCoinVideoCount, long eachVideoCoin, long freeLoginCoin,
                  int waitingRoomWaitAtStart, int delayInStartingGame ) {

        this.waitingRoomWaitAtStart = waitingRoomWaitAtStart;
        this.delayInStartingGame = delayInStartingGame;

        this.boardTypeCount = boardTypeCount;
        this.boardType = boardType;
        this.minCallValue = minCallValue;
        this.minEntryValue = minEntryValue;


        this.gameCodeLowerLimit = gameCodeLowerLimit;
        this.gameCodeUpperLimit = gameCodeUpperLimit;
        this.leastPlayerCount = leastPlayerCount;
        this.maxPlayerCount = maxPlayerCount;
        this.queueWaitLimit = queueWaitLimit;
        this.queueCheckTimeInterval = queueCheckTimeInterval * 1000;

        this.freeLoginCoin = freeLoginCoin;
        this.eachVideoCoin = eachVideoCoin;
        this.dailyCoinVideoCount = dailyCoinVideoCount;
        this.initialCoin = initialCoin;
        this.maxGuestLimit = maxGuestLimit;
        guests = new ArrayList<Integer>();

        casualConnections = new ArrayList<ServerToClient>();
        loggedInUsers = new ArrayList<ServerToClient>();

        waitingRoom = new ArrayList[boardTypeCount];
        gameThreads = new ArrayList[boardTypeCount];
        pendingQueue = new ArrayList[boardTypeCount];
        queueTimeCount = new ArrayList[boardTypeCount];
        for (int i = 0; i < boardTypeCount; i++) {
            gameThreads[i] = new ArrayList<GameThread>();
            pendingQueue[i] = new ArrayList<ServerToClient>();
            queueTimeCount[i] = new ArrayList<Integer>();
            waitingRoom[i] = new ArrayList<WaitingRoom>();
        }
        db = DBFactory.getDB();

        try {
            this.port = port;
            host = InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            System.out.println("Exception in fetching ip -> " + e);
        }

        //TIMER TO CHECK QUEUE
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queueIterator();
            }
        }, 0, 1000);
    }

    //========================================================================================
    //
    //========================================================================================






    //========================================================================================
    //
    //              LOAD USER FROM DATABASE
    //
    //========================================================================================

    private User makeGuestUser(String imageLink) {

        if (guests.size() == maxGuestLimit) return null;

        int id = Randomizer.randomUnique(guests, maxGuestLimit) + 10000;
        guests.add(id);

        User user;
        user = new User(-1, "Guest_" + id, "", "", "guest", imageLink,
                0, initialCoin, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, "",
                dailyCoinVideoCount, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(),
                Calendar.getInstance().getTime(), Calendar.getInstance().getTime());

        return user;
    }

    private User loadUserFromDatabase(String account_id, String account_type, String username, String imageLink) {

        User user = db.getUserData(account_id, account_type, username);
        user.setImageLink(imageLink);

        return user;
    }

    public void loadUserToDatabase(User user) {

        if(user == null || user.getLoginMethod().equals("guest")) return ;
        db.saveUser(user, false);
    }

    public void updateDatabase(User user, String cmd){

        db.updateUser(user, cmd);
    }

    public User makeUser(String account_id, String account_type, String username, String imageLink) {

        User user = null;

        if (account_type.equals("guest")) user = makeGuestUser(imageLink);
        else if(account_type.equals("facebook")) user = loadUserFromDatabase(account_id, account_type, username, imageLink);
        else if(account_type.equals("google")) user = loadUserFromDatabase(account_id, account_type, username, imageLink);

        return user;
    }


    public void addTransaction(User user, String type, String method, String transactionId, long coinAmount, double amount){

        db.addTransaction(user.getId(), type, method, transactionId, coinAmount, amount);
    }

    public JSONArray getTransactionsOfUser(User user){

        JSONArray array = new JSONArray();

        if(user.getLoginMethod().equals("guest") || user.getId() < 0) return array;

        array = db.getTransactions(user.getId());
        return array;
    }

    //========================================================================================
    //
    //========================================================================================













    //================================================================================
    //
    //              BASIC STUFF
    //
    //================================================================================

    private int gameIdGenerator() {
        int id;

        int sz = 0;
        ArrayList temp = new ArrayList<Integer>();

        for (int i = 0; i < boardTypeCount; i++) {
            sz += gameThreads[i].size();
            for (int j = 0; j < gameThreads[i].size(); j++) {
                temp.add(((GameThread) gameThreads[i].get(j)).getGameId());
            }
        }

        for (int i = 0; i < boardTypeCount; i++) {
            sz += waitingRoom[i].size();
            for (int j = 0; j < waitingRoom[i].size(); j++) {
                temp.add(((WaitingRoom) waitingRoom[i].get(j)).getGameId());
            }
        }

        id = Randomizer.randomUnique(temp, sz + 1);

        return id;
    }

    private int gameCodeGenerator() {
        int code;

        ArrayList temp = new ArrayList<Integer>();

        for (int i = 0; i < boardTypeCount; i++) {

            for (int j = 0; j < gameThreads[i].size(); j++) {
                temp.add(((GameThread) gameThreads[i].get(j)).getGameCode());
            }
        }

        for (int i = 0; i < boardTypeCount; i++) {

            for (int j = 0; j < waitingRoom[i].size(); j++) {
                temp.add(((WaitingRoom) waitingRoom[i].get(j)).getGameCode());
            }
        }
        code = Randomizer.randomUnique(temp, gameCodeUpperLimit - gameCodeLowerLimit) + gameCodeLowerLimit;

        return code;
    }

    private int getBoardKey(String name) {

        int ret = -1;

        for (int i = 0; i < boardTypeCount; i++) {
            if (name.equals(boardType[i])) {
                ret = i;
                break;
            }
        }
        return ret;
    }


    public GameThread findGameThreadByCode(int code) {

        for (int i = 0; i < boardTypeCount; i++) {

            for (int j = 0; j < gameThreads[i].size(); j++) {

                GameThread g = (GameThread) gameThreads[i].get(j);
                if (g.getGameCode() == code) return g;
            }
        }
        return null;
    }

    public WaitingRoom findWaitingRoomByCode(int code) {

        for (int i = 0; i < boardTypeCount; i++) {

            for (int j = 0; j < waitingRoom[i].size(); j++) {

                WaitingRoom w = (WaitingRoom) waitingRoom[i].get(j);
                if (w.getGameCode() == code) return w;
            }
        }
        return null;
    }


    public ServerToClient getServerToClient(WebSocketSession session) {

        String k = session.getHandshakeHeaders().get("sec-websocket-key").get(0);

        for (int i = 0; i < casualConnections.size(); i++) {

            ServerToClient c = (ServerToClient) casualConnections.get(i);
            if (k.equals(c.getWebSocketKey()) == true) return c;
        }
        return null;
    }

    public ServerToClient getConnectionThroughUsername(String username) {

        for (int i = 0; i < loggedInUsers.size(); i++) {

            ServerToClient s = (ServerToClient) loggedInUsers.get(i);
            String tempUser = s.getUser().getUsername();

            if (tempUser.equals(username)) return s;
        }
        return null;
    }

    //================================================================================
    //
    //================================================================================




    //================================================================================
    //
    //              WAITING ROOM OPERATIONS
    //
    //================================================================================

    public void createWaitingRoom(ServerToClient owner, String boardName) {

        int id = gameIdGenerator();
        int code = gameCodeGenerator();
        int boardKey = getBoardKey(boardName);

        WaitingRoom w = new WaitingRoom(owner, id, code, maxPlayerCount, boardType[boardKey], minEntryValue[boardKey], minCallValue[boardKey]);
        addWaitingRoom(w);

        owner.sendCreateWaitingRoomResponse(id, code, boardName);
        new Thread(w).start();
    }

    private void removeFromWaitingRoom(ServerToClient s) {

        WaitingRoom w = s.getWaitingRoom();
        if (w == null) return;

        int loc = s.getUser().getSeatPosition();
        w.removeFromWaitingRoom(loc);
    }

    //================================================================================
    //
    //================================================================================





    //================================================================================
    //
    //              GAMETHREAD OPERATIONS
    //
    //================================================================================

    private void makeGameThread(ArrayList<ServerToClient> clients, int boardKey) {

        int id = gameIdGenerator();
        int code = gameCodeGenerator();

        //Making and starting game thread

        GameThread g = new GameThread(clients, false, null, id, code, maxPlayerCount, boardType[boardKey], minEntryValue[boardKey], minCallValue[boardKey]);
        addGameThread(g);

        //setting gameThreads in serverToClient side
        new Thread(g).start();
    }

    public void makeGameThread(WaitingRoom w) {

        int id = w.getGameId();
        int code = w.getGameCode();
        int boardKey = getBoardKey(w.getBoardType());

        ArrayList clients = new ArrayList<ServerToClient>();

        for (int i = 0; i < w.getMaxPlayerCount(); i++) {

            ServerToClient s = w.getAtSeat(i);
            if (s != null) clients.add(s);
        }
        ServerToClient owner = w.getOwner();

        //Making and starting game thread

        GameThread g = new GameThread(clients, true, owner, id, code, w.getMaxPlayerCount(), boardType[boardKey], minEntryValue[boardKey], minCallValue[boardKey]);
        addGameThread(g);

        //setting gameThreads in serverToClient side
        new Thread(g).start();

        removeWaitingRoom(w);
    }

    public void removeFromGameThread(ServerToClient s) {

        GameThread gg = s.getGameThread();

        if (gg == null) return;
        gg.exitGameResponse(s);
    }

    //================================================================================
    //
    //================================================================================




    //================================================================================
    //
    //          QUEUE ITERATOR OPERATIONS
    //
    //================================================================================

    private void tryMakeGameThread() {

        for (int i = 0; i < boardTypeCount; ) {

            if (pendingQueue[i].size() < leastPlayerCount) {
                i++;
                continue;
            }

            int cnt = Math.min(maxPlayerCount, pendingQueue[i].size());
            ArrayList temp = new ArrayList<ServerToClient>();

            for (int j = 0; j < cnt; j++) {
                temp.add(pendingQueue[i].get(0));
                pendingQueue[i].remove(0);
                queueTimeCount[i].remove(0);
            }

            makeGameThread(temp, i);
            temp.clear();
        }
    }

    private void tryAddingToGameThread() {

        for (int i = 0; i < boardTypeCount; i++) {

            if (pendingQueue[i].size() == 0) continue;

            for (int j = 0; j < gameThreads[i].size(); j++) {

                if (pendingQueue[i].size() == 0) break;

                GameThread g = (GameThread) gameThreads[i].get(j);

                if (g.isPrivate() == true) continue;
                else if (g.getPlayerCount() == 0 || g.getPlayerCount() == g.getMaxPlayerCount()) continue;

                for (int k = g.getPlayerCount(); k < g.getMaxPlayerCount(); k++) {

                    if (pendingQueue[i].size() == 0) break;

                    g.addInGameThread((ServerToClient) pendingQueue[i].get(0));
                    pendingQueue[i].remove(0);
                    queueTimeCount[i].remove(0);
                }
            }
        }
    }

    private void queueIterator() {

        tryAddingToGameThread();
        tryMakeGameThread();

        for (int i = 0; i < boardTypeCount; i++) {
            for (int j = 0; j < pendingQueue[i].size(); j++) {

                ServerToClient s = (ServerToClient) pendingQueue[i].get(j);
                int t = (int) queueTimeCount[i].get(j);
                t++;

                queueTimeCount[i].set(j, t);

                if (t == queueWaitLimit) {
                    s.requestAbort();
                } else s.requestJoin(false, null);
            }
        }
    }

    //================================================================================
    //
    //================================================================================





    //================================================================================
    //
    //              QUEUE OPERATIONS
    //
    //================================================================================

    public void addGameThread(GameThread g) {

        if (g == null) return;

        int typeLoc = getBoardKey(g.getBoardType());
        if (typeLoc != -1) gameThreads[typeLoc].add(g);
    }

    private void addWaitingRoom(WaitingRoom w) {

        if (w == null) return;

        int typeLoc = getBoardKey(w.getBoardType());
        if (typeLoc != -1) waitingRoom[typeLoc].add(w);
    }

    public void addInPendingQueue(ServerToClient s) {

        if (s == null) return;
        if (s.getUser() == null) return;

        int typeLoc = getBoardKey(s.getUser().getBoardType());

        if (typeLoc == -1) return;

        pendingQueue[typeLoc].add(s);
        queueTimeCount[typeLoc].add(0);
    }

    public void addInLoggedUser(ServerToClient s) {

        if (s.getUser() == null) return;

        loggedInUsers.add(s);
    }

    public void addInCasualConnection(ServerToClient s) {

        if (s == null) return;

        casualConnections.add(s);
    }


    public void removeGameThread(GameThread g) {

        if (g == null) return;

        int typeLoc = getBoardKey(g.getBoardType());

        if (typeLoc == -1) return;

        gameThreads[typeLoc].remove(g);
    }

    public void removeWaitingRoom(WaitingRoom w) {

        if (w == null) return;

        int typeLoc = getBoardKey(w.getBoardType());
        if (typeLoc == -1) return;

        waitingRoom[typeLoc].remove(w);
    }

    public boolean removeFromPendingQueue(ServerToClient s) {

        if (s == null || s.getUser() == null) return false;

        int typeLoc = getBoardKey(s.getUser().getBoardType());
        if (typeLoc == -1) return false;

        int loc = -1;
        for (int i = 0; i < pendingQueue[typeLoc].size(); i++) {

            if (pendingQueue[typeLoc].get(i) == s) {

                loc = i;
                break;
            }
        }
        if (loc == -1) return false;

        pendingQueue[typeLoc].remove(loc);
        queueTimeCount[typeLoc].remove(loc);
        return true;
    }

    public void removeFromLoggedUsers(ServerToClient s) {

        if (s == null) return;
        if (s.getUser() == null) return;

        removeFromWaitingRoom(s);
        removeFromGameThread(s);
        s.requestAbort();

        if (s.getUser().getLoginMethod().equals("guest")) {
            int nameCode = Integer.valueOf(s.getUser().getUsername().split("_")[1]);
            guests.remove((Integer) nameCode);
        }

        loadUserToDatabase(s.getUser());
        loggedInUsers.remove(s);
    }

    public void removeFromCasualConnections(ServerToClient s) {

        if (s == null) return;

        removeFromLoggedUsers(s);
        casualConnections.remove(s);
    }

    //================================================================================
    //
    //================================================================================










    //================================================================================================================================================
    //
    //          GETTERS AND SETTERS
    //
    //================================================================================================================================================

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ArrayList getLoggedInUsers() {
        return loggedInUsers;
    }

    //================================================================================================================================================
    //
    //================================================================================================================================================
}
