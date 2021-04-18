package com.example.PokerServer.Communication.WebSocket;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
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

        receiveMessage(session, "Error -> " + exception.toString());
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
        if (s != null) s.incomingMsg(message);
    }

    private void closeServerToClient(WebSocketSession session) {

        ServerToClient s = Server.pokerServer.getServerToClient(session);

        s.closeEverything();
        s = null;
    }

    private void createServerToClient(WebSocketSession session) {

        ServerToClient s = new ServerToClient(session);

        Server.pokerServer.addInCasualConnection(s);
        new Thread(s).start();
    }

    //==================================================================
    //
    //==================================================================
}
