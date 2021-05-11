package com.example.PokerServer.Communication.WebSocket;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import org.json.JSONObject;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class WebSocketHandler extends AbstractWebSocketHandler {


    //==================================================================
    //
    //             OVERRIDDENT FUNCTIONS FOR ABSTRACT CLASS
    //
    //==================================================================

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        createServerToClient(session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        receiveMessage(session, message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        System.out.println("Error -> " + exception.toString());
        closeServerToClient(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        closeServerToClient(session);
    }

    //==================================================================
    //
    //==================================================================


    //==================================================================
    //
    //             NECESSARY FUNCTIONS
    //
    //==================================================================

    private void receiveMessage(WebSocketSession session, String message) {

        ServerToClient s = Server.pokerServer.getServerToClient(session);
        if (s != null) {

            try{
                JSONObject jsonObject = new JSONObject(message);
                boolean done = jsonObject.getBoolean("done");
                String data = jsonObject.getString("data");

                s.setIncoming(s.getIncoming() + data);
                if(done){
                    s.incomingMsg(s.getIncoming());
                    s.setIncoming("");
                }
            }catch (Exception e){
                if(message == null) System.out.println("message null");
                System.out.println("Error in converting incoming message to json " + e);
                s.setIncoming("");
            }
        }
    }

    private void closeServerToClient(WebSocketSession session) {

        ServerToClient s = Server.pokerServer.getServerToClient(session);

        if (s == null) {
            Server.pokerServer.removeFromCasualConnections(s);
            return;
        }

        s.closeEverything();
    }

    private void createServerToClient(WebSocketSession session) {

        ServerToClient s = new ServerToClient(session);
        s.setIncoming("");

        Server.pokerServer.addInCasualConnection(s);
        new Thread(s).start();
    }

    //==================================================================
    //
    //==================================================================
}
