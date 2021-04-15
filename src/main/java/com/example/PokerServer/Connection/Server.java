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
    public ArrayList pendingQueue;              //      SERVER TO CLIENT OBJECTS OF QUEUE
    public ArrayList queueTimeCount;            //      TIME COUNTER FOR EACH OBJECT IN QUEUE
    public ArrayList gameThreads;               //      GAME THREADS
    private String host;                  //      SERVER IP
    private int port;                     //      SERVER PORT
    private int queueWaitLimit;                 //      WAIT LIMIT IN QUEUE

    private int maxPlayerCount;                 //      MAX PLAYER COUNT IN A GAME
    private int leastPlayerCount;               //      LEAST NUMBER OF PLAYER FOR A GAME

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


    public Server(int c, int waitLimit) {

        setGui();

        casualConnections = new ArrayList<ServerToClient>();
        loggedInUsers = new ArrayList<ServerToClient>();
        pendingQueue = new ArrayList<ServerToClient>();
        queueTimeCount = new ArrayList<Integer>();
        gameThreads = new ArrayList<GameThread>();


        leastPlayerCount = c;
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

    private void makeGameThread(ArrayList<ServerToClient> clients) {

        int id, code;

        int temp1[] = new int[gameThreads.size()];
        for (int i = 0; i < gameThreads.size(); i++) temp1[i] = ((GameThread) gameThreads.get(i)).getGameId();

        id = Randomizer.randomUnique(temp1, gameThreads.size() + 1);

        int temp2[] = new int[gameThreads.size()];
        for (int i = 0; i < gameThreads.size(); i++) temp2[i] = ((GameThread) gameThreads.get(i)).getGameCode();

        code = Randomizer.randomUnique(temp2, 10000);


        //Making and starting game thread

        GameThread g = new GameThread(clients, id, code, "Normal", 10000, false);
        addGameThread(g);

        //setting gameThreads in serverToClient side

        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).setGameThread(g);
            clients.get(i).getUser().setSeatPosition(i);
        }
        new Thread(g).start();


        String bleh = "Game thread created. id: " + id + " code: " + code + "\n";
        bleh += "        users: ";
        for (int i = 0; i < clients.size(); i++)
            bleh += ((ServerToClient) clients.get(i)).getUser().getUsername() + " ";
        addTextInGui(bleh);
    }

    private void addInGameThread(ServerToClient s) {

    }

    //MAKE A NEW GAME OR ADD USER IN A EXISTING GAME LOGICS

    private void tryMakeGameThread() {

        if (pendingQueue.size() >= leastPlayerCount) {

            ArrayList<ServerToClient> temp = new ArrayList<>();
            ServerToClient tempObj;

            for (int i = 0; i < leastPlayerCount; i++) {

                tempObj = (ServerToClient) pendingQueue.get(0);
                temp.add(tempObj);

                removeFromPendingQueue(tempObj);
            }
            makeGameThread(temp);
        }
    }

    private void queueIterator() {

        tryMakeGameThread();

        for (int i = 0; i < pendingQueue.size(); i++) {

            ServerToClient s = (ServerToClient) pendingQueue.get(i);
            int t = (int) queueTimeCount.get(i);
            t++;

            queueTimeCount.set(i, t);

            if (t == queueWaitLimit) {
                s.requestAbort();
                removeFromPendingQueue(s);
            } else s.requestJoin(false);
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

        gameThreads.add(g);
    }

    public void addInPendingQueue(ServerToClient s) {

        pendingQueue.add(s);
        queueTimeCount.add(0);
    }

    public void addInLoggedUser(ServerToClient s) {

        loggedInUsers.add(s);
    }

    public void addInCasualConnection(ServerToClient s) {

        casualConnections.add(s);
    }

    public void removeGameThread(GameThread g) {

        gameThreads.remove(g);
        g = null;
    }

    public void removeFromGameThread(ServerToClient s) {

        GameThread gg = s.getGameThread();

        if (gg == null) return;
        gg.exitGameResponse(s);
    }

    public void removeFromPendingQueue(ServerToClient s) {

        int loc = -1;

        for (int i = 0; i < pendingQueue.size(); i++) {

            if (pendingQueue.get(i) == s) {

                loc = i;
                break;
            }
        }

        if (loc == -1) return;

        pendingQueue.remove(loc);
        queueTimeCount.remove(loc);
    }

    public void removeFromLoggedUsers(ServerToClient s) {

        removeFromGameThread(s);
        removeFromPendingQueue(s);

        loggedInUsers.remove(s);
    }

    public void removeFromCasualConnections(ServerToClient s) {

        removeFromLoggedUsers(s);
        casualConnections.remove(s);
    }

    public ServerToClient getServerToClient(WebSocketSession session) {

        String k = session.getHandshakeHeaders().get("sec-websocket-key").get(0);

        for (int i = 0; i < casualConnections.size(); i++) {

            ServerToClient c = (ServerToClient) casualConnections.get(i);

            if (k.equals(c.getWebSocketKey()) == true) return c;
        }
        return null;
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
        String ret = "Queue size: " + pendingQueue.size() + "\n";
        for (int i = 0; i < pendingQueue.size(); i++)
            ret += "        " + ((ServerToClient) pendingQueue.get(i)).getUser().toString();

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
        String ret = "Currently running game threads: " + gameThreads.size() + "\n";
        for (int i = 0; i < gameThreads.size(); i++) ret += "        " + gameThreads.get(i).toString();

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
