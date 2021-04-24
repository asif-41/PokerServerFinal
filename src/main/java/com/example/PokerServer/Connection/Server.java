package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.GameThread.WaitingRoom;
import com.example.PokerServer.Objects.Randomizer;
import com.example.PokerServer.Objects.User;
import org.springframework.web.socket.WebSocketSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Server extends JFrame {


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
    private long initialCoin;                    //      INITIAL COIN

    public int waitingRoomWaitAtStart;
    public int delayInStartingGame;

    //====================================================


    //========================================
    //
    //      JFRAME GUI SHITS
    //
    //========================================

    private JTextArea textArea;
    private JButton printAll;
    private JButton printConnections;
    private JButton printLoggedInUsers;
    private JButton printQueue;
    private JButton printGameThread;
    private JButton clearButton;
    private JPanel buttons;

    //=========================================


    //========================================================================================
    //
    //          CONSTRUCTOR
    //
    //          HAS A TIMER TO CHECK QUEUE EACH 5 SECONDS
    //
    //========================================================================================


    public Server(int boardTypeCount, String[] boardType, long[] minEntryValue, long[] minCallValue, int gameCodeUpperLimit, int leastPlayerCount, int maxPlayerCount, int queueCheckTimeInterval,
                  int queueWaitLimit, int port, int maxGuestLimit, long initialCoin, int dailyCoinVideoCount, long eachVideoCoin, long freeLoginCoin,
                  int waitingRoomWaitAtStart, int delayInStartingGame ) {

        setGui();

        this.waitingRoomWaitAtStart = waitingRoomWaitAtStart;
        this.delayInStartingGame = delayInStartingGame;

        this.boardTypeCount = boardTypeCount;
        this.boardType = boardType;
        this.minCallValue = minCallValue;
        this.minEntryValue = minEntryValue;


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



        try {
            this.port = port;
            host = InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            Server.pokerServer.addTextInGui("Exception in fetching ip -> " + e);
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

    private User makeGuestUser() {

        if (guests.size() == maxGuestLimit) return null;

        int id = Randomizer.randomUnique(guests, maxGuestLimit) + 10000;

        guests.add(id);

        User user;
        user = new User(-1, "Guest_" + id, Calendar.getInstance().getTime(), -1, "", "", "guest",
                0, initialCoin, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, "",
                dailyCoinVideoCount, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), Calendar.getInstance().getTime());

        return user;
    }

    private User loadUserFromDatabase(String account_id, String account_type, String username) {

        //CREATE IF DOES NOT EXIST
        //RETURN EXISTING IF DOES EXIST

        return null;
    }

    private void loadUserToDatabase(User user) {

    }

    public User makeUser(String account_id, String account_type, String username) {

        User user;

        if (account_type.equals("guest")) user = makeGuestUser();
        else user = loadUserFromDatabase(account_id, account_type, username);

        return user;
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
        code = Randomizer.randomUnique(temp, gameCodeUpperLimit);

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

        String bleh = "Waiting room created. id: " + id + " code: " + code + "\n";
        bleh += "        Owner : " + owner.getUser().getUsername();

        addTextInGui(bleh);
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

        String bleh = "Game thread created. id: " + id + " code: " + code + "\n";
        bleh += "        users: ";
        for (int i = 0; i < clients.size(); i++) bleh += clients.get(i).getUser().getUsername() + " ";

        addTextInGui(bleh);
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

        String bleh = "Game thread created. id: " + id + " code: " + code + "\n";
        bleh += "        users: ";
        for (int i = 0; i < clients.size(); i++) bleh += ((ServerToClient) clients.get(i)).getUser().getUsername() + " ";
        addTextInGui(bleh);

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
        g = null;
    }

    public void removeWaitingRoom(WaitingRoom w) {

        if (w == null) return;

        int typeLoc = getBoardKey(w.getBoardType());
        if (typeLoc == -1) return;

        waitingRoom[typeLoc].remove(w);
        w = null;
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
        } else loadUserToDatabase(s.getUser());

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































    //=================================================================
    //
    //      QUEUE PRINTERS FOR DEBUGGING PURPOSE
    //
    //
    //=================================================================

    private void printQueue() {

        String ret = "Queue info: \n";

        for (int i = 0; i < boardTypeCount; i++) {

            ret += "Type " + i + "\n";
            for (int j = 0; j < pendingQueue[i].size(); j++) {
                ret += "        " + ((ServerToClient) pendingQueue[i].get(j)).getUser().toString();
            }
        }
        addTextInGui(ret);
    }

    private void printConnections() {
        String ret = "Currently active connections: " + casualConnections.size();
        addTextInGui(ret);
    }

    private void printLoggedInUsers() {

        String ret = "Logged in users: " + loggedInUsers.size() + "\n";

        for (int i = 0; i < loggedInUsers.size(); i++) {

            ret += "        " + ((ServerToClient) loggedInUsers.get(i)).getUser().toString();
        }

        addTextInGui(ret);
    }

    private void printGameThread() {
        String ret = "Currently running game threads: ";

        for (int i = 0; i < boardTypeCount; i++) {

            ret += "Type -> " + boardType[i] + "\n";
            for (int j = 0; j < gameThreads[i].size(); j++)
                ret += "        number " + j + " " + gameThreads[i].get(j).toString();

        }
        addTextInGui(ret);

    }

    private void printWaitingRoom() {

        String ret = "Currently running waiting rooms: ";

        for (int i = 0; i < boardTypeCount; i++) {

            ret += "Type -> " + boardType[i] + "\n";
            for (int j = 0; j < waitingRoom[i].size(); j++)
                ret += "        number " + j + " " + waitingRoom[i].get(j).toString();

        }
        addTextInGui(ret);

    }

    private void printAll() {
        printConnections();
        printLoggedInUsers();
        printQueue();
        printGameThread();
        printWaitingRoom();
    }

    //==================================================================
    //
    //==================================================================


    //================================================================================================================================================
    //
    //          SETTING UP GUI          DONE
    //
    //================================================================================================================================================

    private void setGui() {

        setTitle("Server");

        textArea = new JTextArea();
        addTextInGui("\n        Welcome to the server");
        textArea.setEditable(false);

        printAll = new JButton("Print All");
        printConnections = new JButton("Print connections");
        printLoggedInUsers = new JButton("Print Logged users");
        printQueue = new JButton("Print queued users");
        printGameThread = new JButton("Print game threads");
        clearButton = new JButton("Clear text");


        buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setPreferredSize(new Dimension(200, 200));
        buttons.setBorder(new EmptyBorder(10, 10, 10, 10));

        printAll.setBorder(new CompoundBorder(new LineBorder(buttons.getBackground(), 10), printAll.getBorder()));
        printQueue.setBorder(new CompoundBorder(new LineBorder(buttons.getBackground(), 10), printQueue.getBorder()));
        printLoggedInUsers.setBorder(new CompoundBorder(new LineBorder(buttons.getBackground(), 10), printLoggedInUsers.getBorder()));
        printConnections.setBorder(new CompoundBorder(new LineBorder(buttons.getBackground(), 10), printConnections.getBorder()));
        printGameThread.setBorder(new CompoundBorder(new LineBorder(buttons.getBackground(), 10), printGameThread.getBorder()));
        clearButton.setBorder(new CompoundBorder(new LineBorder(buttons.getBackground(), 10), clearButton.getBorder()));

        clearButton.addActionListener(e -> clearGui());
        printAll.addActionListener(e -> printAll());
        printQueue.addActionListener(e -> printQueue());
        printConnections.addActionListener(e -> printConnections());
        printLoggedInUsers.addActionListener(e -> printLoggedInUsers());
        printGameThread.addActionListener(e -> printGameThread());

        buttons.add(printAll);
        buttons.add(printConnections);
        buttons.add(printLoggedInUsers);
        buttons.add(printQueue);
        buttons.add(printGameThread);
        buttons.add(clearButton);


        add(new JScrollPane(textArea));
        add(buttons, BorderLayout.EAST);
        setSize(650, 500);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addTextInGui(String text) {
        String temp = textArea.getText();
        temp += "        " + text + "\n\n";
        textArea.setText(temp);
    }

    private void clearGui() {
        textArea.setText("");
    }

    //================================================================================================================================================
    //
    //================================================================================================================================================


}
