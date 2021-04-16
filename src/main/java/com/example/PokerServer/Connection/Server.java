package com.example.PokerServer.Connection;

import com.example.PokerServer.GameThread.GameThread;
import com.example.PokerServer.Objects.Randomizer;
import org.springframework.web.socket.WebSocketSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server extends JFrame {


    //=====================================================
    //
    //          SERVER SIDE SERVER OBJECT
    //
    //=====================================================

    public static Server pokerServer = null;    //      SERVER INSTANCE
    public ArrayList casualConnections;         //      SERVER TO CLIENT OBJECTS
    public ArrayList loggedInUsers;             //      SERVER TO CLIENT OBJECTS OF LOGGED IN USERS
    private String host;                  //      SERVER IP
    private int port;                     //      SERVER PORT
    private int queueWaitLimit;                 //      WAIT LIMIT IN QUEUE

    private int maxPlayerCount;                 //      MAX PLAYER COUNT IN A GAME
    private int leastPlayerCount;               //      LEAST NUMBER OF PLAYER FOR A GAME


    public ArrayList[] pendingQueue;
    public ArrayList[] queueTimeCount;            //      TIME COUNTER FOR EACH OBJECT IN QUEUE
    public ArrayList[] gameThreads;             //      GAME THREADS
    public String boardType[] = {"board1", "board2", "board3", "board4", "board5"};
    public int minCoin[] = {10000, 10000, 10000, 10000, 10000};
    private int boardTypeCount;

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


    public Server(int leastPlayerCount, int maxPlayerCount, int waitLimit) {

        setGui();

        boardTypeCount = 5;

        casualConnections = new ArrayList<ServerToClient>();
        loggedInUsers = new ArrayList<ServerToClient>();

        gameThreads = new ArrayList[boardTypeCount];
        pendingQueue = new ArrayList[boardTypeCount];
        queueTimeCount = new ArrayList[boardTypeCount];
        for (int i = 0; i < boardTypeCount; i++) {
            gameThreads[i] = new ArrayList<GameThread>();
            pendingQueue[i] = new ArrayList<ServerToClient>();
            queueTimeCount[i] = new ArrayList<Integer>();
        }


        this.leastPlayerCount = leastPlayerCount;
        this.maxPlayerCount = maxPlayerCount;
        queueWaitLimit = waitLimit;

        try {

            port = 8080;
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


    //================================================================================
    //
    //              MAKING GAMETHREADS
    //
    //================================================================================

    private int roomIdGenerator() {
        int id;

        int sz = 0;
        ArrayList temp = new ArrayList<Integer>();

        for (int i = 0; i < boardTypeCount; i++) {
            sz += gameThreads[i].size();
            for (int j = 0; j < gameThreads[i].size(); j++) {
                temp.add(((GameThread) gameThreads[i].get(j)).getGameId());
            }
        }
        id = Randomizer.randomUnique(temp, sz + 1);

        return id;
    }

    private int roomCodeGenerator() {
        int code;

        ArrayList temp = new ArrayList<Integer>();

        for (int i = 0; i < boardTypeCount; i++) {

            for (int j = 0; j < gameThreads[i].size(); j++) {
                temp.add(((GameThread) gameThreads[i].get(j)).getGameCode());
            }
        }
        code = Randomizer.randomUnique(temp, 10000);


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

    private void makeGameThread(ArrayList<ServerToClient> clients, int boardKey) {

        int id = roomIdGenerator();
        int code = roomCodeGenerator();

        //Making and starting game thread

        GameThread g = new GameThread(clients, id, code, boardType[boardKey], minCoin[boardKey], maxPlayerCount, false);
        addGameThread(g);

        //setting gameThreads in serverToClient side
        new Thread(g).start();

        String bleh = "Game thread created. id: " + id + " code: " + code + "\n";
        bleh += "        users: ";
        for (int i = 0; i < clients.size(); i++) bleh += clients.get(i).getUser().getUsername() + " ";

        addTextInGui(bleh);
    }

    //MAKE A NEW GAME OR ADD USER IN A EXISTING GAME LOGICS

    private void tryMakeGameThread() {

        for (int i = 0; i < boardTypeCount; ) {

            if (pendingQueue[i].size() < 2) {
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
                if (g.getPlayerCount() == 0) continue;

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

    public ServerToClient getServerToClient(WebSocketSession session) {

        String k = session.getHandshakeHeaders().get("sec-websocket-key").get(0);

        for (int i = 0; i < casualConnections.size(); i++) {

            ServerToClient c = (ServerToClient) casualConnections.get(i);

            if (k.equals(c.getWebSocketKey()) == true) return c;
        }
        return null;
    }

    public void addGameThread(GameThread g) {

        int typeLoc = getBoardKey(g.getBoardType());

        gameThreads[typeLoc].add(g);
    }

    public void addInPendingQueue(ServerToClient s) {

        int typeLoc = getBoardKey(s.getUser().getBoardType());

        pendingQueue[typeLoc].add(s);
        queueTimeCount[typeLoc].add(0);
    }

    public void addInLoggedUser(ServerToClient s) {

        loggedInUsers.add(s);
    }

    public void addInCasualConnection(ServerToClient s) {

        casualConnections.add(s);
    }

    public void removeGameThread(GameThread g) {

        int typeLoc = getBoardKey(g.getBoardType());

        gameThreads[typeLoc].remove(g);
        g = null;
    }

    public void removeFromGameThread(ServerToClient s) {

        GameThread gg = s.getGameThread();

        if (gg == null) return;
        gg.exitGameResponse(s);
    }

    public boolean removeFromPendingQueue(ServerToClient s) {

        String boardName = "";
        if (s.getUser() != null) boardName = s.getUser().getBoardType();

        int typeLoc = getBoardKey(boardName);
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

        removeFromGameThread(s);
        s.requestAbort();
        loggedInUsers.remove(s);
    }

    public void removeFromCasualConnections(ServerToClient s) {

        removeFromLoggedUsers(s);
        casualConnections.remove(s);
    }

    //================================================================================
    //
    //================================================================================


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
        for (int i = 0; i < loggedInUsers.size(); i++)
            ret += "        " + ((ServerToClient) loggedInUsers.get(i)).getUser().toString();

        addTextInGui(ret);
    }

    private void printGameThread() {
        String ret = "Currently running game threads: ";

        for (int i = 0; i < boardTypeCount; i++) {

            ret += "Type -> " + boardType[i] + "\n";
            for (int j = 0; j < gameThreads[i].size(); j++)
                ret += "        number " + i + " " + gameThreads[i].get(j).toString();

        }
        addTextInGui(ret);

    }

    private void printAll() {
        printConnections();
        printLoggedInUsers();
        printQueue();
        printGameThread();
    }

    //
    //==================================================================


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


    //================================================================================================================================================
    //
    //================================================================================================================================================


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

    //
    //================================================================================================================================================


}
