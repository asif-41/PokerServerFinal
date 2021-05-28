package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.*;
import com.example.PokerServer.db.DB;
import com.example.PokerServer.db.DBFactory;
import org.json.JSONObject;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.util.*;

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
    private long maxEntryValue[];
    private long mcr[];
    private ArrayList guests;                   //      GUEST INTEGERS
    private int maxGuestLimit;                  //      MAX GUEST LIMIT
    public long initialCoin;                    //      INITIAL COIN

    public DB db;
    public int waitingRoomWaitAtStart;
    public int delayInStartingGame;

    public int maxPendingReq;
    public ArrayList<TransactionNumber> transactionNumbers;
    private ArrayList unsentNotifications;

    private long delayLoginOnForce;
    private boolean allowLogin;
    private Timer loginDelay;

    //====================================================


    //========================================================================================
    //
    //          CONSTRUCTOR
    //
    //          HAS A TIMER TO CHECK QUEUE EACH 5 SECONDS
    //
    //========================================================================================


    public Server(int boardTypeCount, String[] boardType, long[] minEntryValue, long[] maxEntryValue, long[] minCallValue, long[] mcr,
                  int gameCodeLowerLimit, int gameCodeUpperLimit, int leastPlayerCount, int maxPlayerCount,
                  int queueCheckTimeInterval,
                  int queueWaitLimit, int port, int maxGuestLimit, long initialCoin, int dailyCoinVideoCount, long eachVideoCoin, long freeLoginCoin,
                  int waitingRoomWaitAtStart, int delayInStartingGame, int maxPendingReq,
                  double coinPricePerCrore, long[] coinAmountOnBuy, double[] coinPriceOnBuy, ArrayList<TransactionNumber> transactionNumbers, long delayLoginOnForce) {

        TransactionMethods.setCoinPricePerCrore(coinPricePerCrore);
        TransactionMethods.setCoinAmountOnBuy(coinAmountOnBuy);
        TransactionMethods.setCoinPriceOnBuy(coinPriceOnBuy);

        this.waitingRoomWaitAtStart = waitingRoomWaitAtStart;
        this.delayInStartingGame = delayInStartingGame;

        this.boardTypeCount = boardTypeCount;
        this.boardType = boardType;
        this.minCallValue = minCallValue;
        this.minEntryValue = minEntryValue;
        this.maxEntryValue = maxEntryValue;
        this.mcr = mcr;

        this.transactionNumbers = transactionNumbers;
        this.maxPendingReq = maxPendingReq;

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
        unsentNotifications = new ArrayList<Notification>();

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

        allowLogin = true;
        loginDelay = null;
        this.delayLoginOnForce = delayLoginOnForce;
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
                User.firstLastFreeCoinTime(), Calendar.getInstance().getTime());

        return user;
    }

    private User loadUserFromDatabase(String account_id, String account_type, String username, String imageLink) {

        if(account_type.equals("guest")) return null;

        User user = db.getUserData(account_id, account_type, username);
        user.setImageLink(imageLink);

        return user;
    }

    public void loadUserToDatabase(User user) {

        if(user == null || user.getLoginMethod().equals("guest")) return ;
        db.saveUser(user, false);
    }

    public void updateDatabase(User user, String cmd){

        if(user.getLoginMethod().equals("guest")) return ;

        db.updateUser(user, cmd);
    }

    public User makeUser(String account_id, String account_type, String username, String imageLink) {

        User user = null;

        if( ! allowLogin) return null;

        if( checkIfAlreadyLoggedIn(account_id, account_type, username) ) return null;

        if (account_type.equals("guest")) user = makeGuestUser(imageLink);
        else if(account_type.equals("facebook")) user = loadUserFromDatabase(account_id, account_type, username, imageLink);
        else if(account_type.equals("google")) user = loadUserFromDatabase(account_id, account_type, username, imageLink);

        return user;
    }

    public JSONObject getAllTransactionsOfUser(User user){

        JSONObject jsonObject = new JSONObject();

        if(user.getLoginMethod().equals("guest") || user.getId() < 0) return jsonObject;

        jsonObject = db.getAllTransactions(user.getId());
        return jsonObject;
    }

    private boolean checkIfAlreadyLoggedIn(String account_id, String account_type, String username){

        if(account_type.equals("guest")) return false;

        for(int i=0; i < loggedInUsers.size(); i++){

            ServerToClient s = (ServerToClient) loggedInUsers.get(i);
            if(s == null || s.getUser() == null) continue;

            User user = s.getUser();

            String userAccountType = user.getLoginMethod();
            String userAccountId = "";
            if(userAccountType.equals("facebook")) userAccountId = user.getFb_id();
            else if(userAccountType.equals("google")) userAccountId = user.getGmail_id();

            if(userAccountType.equals(account_type) && userAccountId.equals(account_id)) {
                return true;
            }
        }
        return false;
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
            if (k.equals(c.getWebSocketKey())) return c;
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

        GameThread g = new GameThread(clients, false, null, id, code, maxPlayerCount, boardType[boardKey], minEntryValue[boardKey], minCallValue[boardKey]);
        addGameThread(g);

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

        GameThread g = new GameThread(clients, true, owner, id, code, w.getMaxPlayerCount(), boardType[boardKey], minEntryValue[boardKey], minCallValue[boardKey]);
        addGameThread(g);

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

                if (g.isPrivate()) continue;
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
        if (typeLoc != -1) {
            gameThreads[typeLoc].add(g);
            sortGameThreads(typeLoc);
        }
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

        if (s != null && s.getUser() != null){
            removeFromWaitingRoom(s);
            removeFromGameThread(s);
            s.requestAbort();

            if (s.getUser().getLoginMethod().equals("guest")) {
                int nameCode = Integer.valueOf(s.getUser().getUsername().split("_")[1]);
                guests.remove((Integer) nameCode);
            }
            loadUserToDatabase(s.getUser());
        }
        loggedInUsers.remove(s);
    }

    public void removeFromCasualConnections(ServerToClient s) {

        removeFromLoggedUsers(s);
        casualConnections.remove(s);
    }

    //================================================================================
    //
    //================================================================================










    //================================================================================
    //
    //      WEBPAGE CONTROLS
    //
    //================================================================================

    public ArrayList<Notification> userUnsentNotifications(User user){

        ArrayList ret = new ArrayList<Notification>();


        if ( user != null & !(user.getId() < 0) ){

            for(int i=0; i<unsentNotifications.size(); i++){

                Notification x = (Notification) unsentNotifications.get(i);
                if(x.getUserId() != user.getId()) continue;

                ret.add(x);
                unsentNotifications.remove(x);
                i--;
            }
        }
        return ret;
    }

    private ServerToClient findLoggedUserWithId(int id){

        ServerToClient s = null;

        for(Object x : loggedInUsers){

            ServerToClient xx = (ServerToClient) x;

            if(xx == null) continue;
            else if(xx.getUser() == null) continue;
            else if(xx.getUser().getId() < 0) continue;

            if(xx.getUser().getId() == id){
                s = xx;
                break;
            }
        }

        return s;
    }

    public void forceLogoutAll(){

        while(loggedInUsers.size() != 0){

            ServerToClient x = (ServerToClient) loggedInUsers.get(0);
            x.forceLogout("Updates are coming from server! please relogin");
        }

        if(loginDelay != null) loginDelay.cancel();

        loginDelay = new Timer();
        allowLogin = false;

        loginDelay.schedule(new TimerTask() {
            @Override
            public void run() {
                allowUserLogin();
            }
        }, delayLoginOnForce);
    }

    private void allowUserLogin(){

        allowLogin = true;
        if(loginDelay != null) loginDelay.cancel();
        loginDelay = null;

    }

    public void makeNotification(String type, String objectType, JSONObject data){

        Notification notification = new Notification(type, objectType, data);
        ServerToClient s = findLoggedUserWithId(notification.getUserId());

        if(s == null || s.getGameThread() != null || s.getWaitingRoom() != null || s.getUser().getBoardCoin() != 0) unsentNotifications.add(notification);
        else {
            ArrayList temp = new ArrayList<Notification>();
            temp.add(notification);

            s.sendNotifications(temp);
        }
    }

    public void addTransaction(String type, String number){

        if(type.equals("") || number.equals("")) return ;

        TransactionNumber tr = new TransactionNumber(transactionNumbers.size(), type, number);
        transactionNumbers.add(tr);
    }

    //================================================================================
    //
    //================================================================================


    public void editBasicData(Hashtable<String, String> map){
        try{
            int ot = Integer.parseInt(map.get("otherTable"));
            if(ot != -1){

                try{
                    int mpr = Integer.parseInt(map.get("maxPendingReq"));
                    if(mpr > 0) maxPendingReq = mpr;
                }catch (Exception e){
                }

                try{
                    int dcvc = Integer.parseInt(map.get("dailyCoinVideoCount"));
                    if(dcvc > 0) dailyCoinVideoCount = dcvc;
                }catch (Exception e){
                }

                try{
                    long flc = Long.parseLong(map.get("FreeLoginCoin"));
                    if(flc >= 0) freeLoginCoin = flc;
                }catch (Exception e){
                }

                try{
                    long evc = Long.parseLong(map.get("eachVideoCoin"));
                    if(evc >= 0) eachVideoCoin = evc;
                }catch (Exception e){
                }

                try{
                    long ic = Long.parseLong(map.get("initialCoin"));
                    if(ic >= 0) initialCoin = ic;
                }catch (Exception e){
                }

                try{
                    long dlof = Long.parseLong(map.get("delayLoginOnForce"));
                    if(dlof >= 0) delayLoginOnForce = dlof;
                }catch (Exception e){
                }
            }
        }catch (Exception e){
            System.out.println("Error in converting data of hashmap in basic data edit(other table)");
            System.out.println("Error -> " + e);
        }

        try{
            int wc = Integer.parseInt(map.get("withdrawCount"));
            if(wc != 0) {
                try{
                    double d = Double.parseDouble(map.get("coinPriceOnWithdraw"));
                    TransactionMethods.setCoinPricePerCrore(d);
                }catch (Exception e){
                }
            }
        }catch (Exception e){
            System.out.println("Error in converting data of hashmap in basic data edit(withdraw)");
            System.out.println("Error -> " + e);
        }

        try{
            int bpc = Integer.parseInt(map.get("buyPackageCount"));
            for(int i=0; i<bpc; i++){
                try{

                    int loc = Integer.parseInt(map.get("buyPackageId" + i));
                    long amount = Long.parseLong(map.get("coinAmountOnBuy" + loc));
                    double price = Double.parseDouble(map.get("coinPriceOnBuy" + loc));

                    if(amount < 0) continue;;
                    if(price < 0) continue;;

                    TransactionMethods.getCoinPriceOnBuy()[loc] = price;
                    TransactionMethods.getCoinAmountOnBuy()[loc] = amount;
                }catch (Exception e){
                }

            }

        }catch (Exception e){
            System.out.println("Error in converting data of hashmap in basic data edit(buy package)");
            System.out.println("Error -> " + e);
        }


        try{
            int ttc = Integer.parseInt(map.get("transactionTable"));
            for(int i=0; i<ttc; i++){
                try{

                    int id = Integer.parseInt(map.get("transactionId" + i));
                    String type = map.get("transactionType" + id);
                    String number = map.get("transactionNumber" + id);

                    if(type.equals("") || number.equals("")) continue;

                    for(TransactionNumber x : transactionNumbers){
                        if(x.getId() != id) continue;
                        x.setType(type);
                        x.setNumber(number);
                        break;
                    }
                }catch (Exception e){
                }

            }

        }catch (Exception e){
            System.out.println("Error in converting data of hashmap in basic data edit(Transaction number)");
            System.out.println("Error -> " + e);
        }



        try{
            int bdec = Integer.parseInt(map.get("boardDataEditCount"));
            for(int i=0; i<bdec; i++){
                try{

                    int loc = Integer.parseInt(map.get("boardLoc" + i));
                    long minCallValue = Long.parseLong(map.get("minCallValue" + loc));
                    long minEntryValue = Long.parseLong(map.get("minEntryValue" + loc));
                    long maxEntryValue = Long.parseLong(map.get("maxEntryValue" + loc));
                    long mcr = Long.parseLong(map.get("mcr" + loc));

                    if(minCallValue < 0) continue;
                    if(minEntryValue < 0) continue;
                    if(maxEntryValue < minEntryValue) continue;
                    if(mcr < 0) continue;

                    this.minCallValue[loc] = minCallValue;
                    this.minEntryValue[loc] = minEntryValue;
                    this.maxEntryValue[loc] = maxEntryValue;
                    this.mcr[loc] = mcr;

                }catch (Exception e){
                }

            }

        }catch (Exception e){
            System.out.println("Error in converting data of hashmap in basic data edit(Board data)");
            System.out.println("Error -> " + e);
        }


        try{
            int fl = Integer.parseInt(map.get("forceLogout"));
            if(fl == 1) forceLogoutAll();
        }catch (Exception e){
            System.out.println("Error in converting data of hashmap in basic data edit(force logout)");
            System.out.println("Error -> " + e);
        }


    }







    //================================================================================================================================================
    //
    //          GETTERS AND SETTERS
    //
    //================================================================================================================================================

    public void sortGameThreads(String boardType){

        Collections.sort(gameThreads[getBoardKey(boardType)]);
    }

    public void sortGameThreads(int boardKey){
        Collections.sort(gameThreads[boardKey]);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ArrayList getGuests() {
        return guests;
    }

    public void setGuests(ArrayList guests) {
        this.guests = guests;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ArrayList[] getWaitingRoom() {
        return waitingRoom;
    }

    public void setWaitingRoom(ArrayList[] waitingRoom) {
        this.waitingRoom = waitingRoom;
    }

    public ArrayList[] getPendingQueue() {
        return pendingQueue;
    }

    public void setPendingQueue(ArrayList[] pendingQueue) {
        this.pendingQueue = pendingQueue;
    }

    public ArrayList[] getGameThreads() {
        return gameThreads;
    }

    public void setGameThreads(ArrayList[] gameThreads) {
        this.gameThreads = gameThreads;
    }

    public ArrayList getCasualConnections() {
        return casualConnections;
    }

    public void setCasualConnections(ArrayList casualConnections) {
        this.casualConnections = casualConnections;
    }

    public ArrayList getLoggedInUsers() {
        return loggedInUsers;
    }

    public void setLoggedInUsers(ArrayList loggedInUsers) {
        this.loggedInUsers = loggedInUsers;
    }

    public String[] getBoardType() {
        return boardType;
    }

    public void setBoardType(String[] boardType) {
        this.boardType = boardType;
    }

    public long[] getMinEntryValue() {
        return minEntryValue;
    }

    public void setMinEntryValue(long[] minEntryValue) {
        this.minEntryValue = minEntryValue;
    }

    public long[] getMinCallValue() {
        return minCallValue;
    }

    public void setMinCallValue(long[] minCallValue) {
        this.minCallValue = minCallValue;
    }

    public int getBoardTypeCount() {
        return boardTypeCount;
    }

    public long[] getMaxEntryValue() {
        return maxEntryValue;
    }

    public void setMaxEntryValue(long[] maxEntryValue) {
        this.maxEntryValue = maxEntryValue;
    }

    public long[] getMcr() {
        return mcr;
    }

    public void setMcr(long[] mcr) {
        this.mcr = mcr;
    }

    public int getQueueWaitLimit() {
        return queueWaitLimit;
    }

    public void setQueueWaitLimit(int queueWaitLimit) {
        this.queueWaitLimit = queueWaitLimit;
    }

    public ArrayList[] getQueueTimeCount() {
        return queueTimeCount;
    }

    public void setQueueTimeCount(ArrayList[] queueTimeCount) {
        this.queueTimeCount = queueTimeCount;
    }

    public int getMaxGuestLimit() {
        return maxGuestLimit;
    }

    public void setMaxGuestLimit(int maxGuestLimit) {
        this.maxGuestLimit = maxGuestLimit;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount) {
        this.maxPlayerCount = maxPlayerCount;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public int getMaxPendingReq() {
        return maxPendingReq;
    }

    public void setMaxPendingReq(int maxPendingReq) {
        this.maxPendingReq = maxPendingReq;
    }

    public ArrayList<TransactionNumber> getTransactionNumbers() {
        return transactionNumbers;
    }

    public void setTransactionNumbers(ArrayList<TransactionNumber> transactionNumbers) {
        this.transactionNumbers = transactionNumbers;
    }

    public int getDailyCoinVideoCount() {
        return dailyCoinVideoCount;
    }

    public void setDailyCoinVideoCount(int dailyCoinVideoCount) {
        this.dailyCoinVideoCount = dailyCoinVideoCount;
    }

    public long getEachVideoCoin() {
        return eachVideoCoin;
    }

    public void setEachVideoCoin(long eachVideoCoin) {
        this.eachVideoCoin = eachVideoCoin;
    }

    public long getFreeLoginCoin() {
        return freeLoginCoin;
    }

    public void setFreeLoginCoin(long freeLoginCoin) {
        this.freeLoginCoin = freeLoginCoin;
    }

    public long getInitialCoin() {
        return initialCoin;
    }

    public long getDelayLoginOnForce() {
        return delayLoginOnForce;
    }

    public void setDelayLoginOnForce(long delayLoginOnForce) {
        this.delayLoginOnForce = delayLoginOnForce;
    }

    public void setInitialCoin(long initialCoin) {
        this.initialCoin = initialCoin;
    }

    //================================================================================================================================================
    //
    //================================================================================================================================================
}
