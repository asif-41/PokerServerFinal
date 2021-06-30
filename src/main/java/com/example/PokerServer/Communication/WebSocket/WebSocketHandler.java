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
        System.out.println("Received binary message");
        super.handleBinaryMessage(session, message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        System.out.println("Received pong message");
        super.handlePongMessage(session, message);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveMessage(session, message.getPayload());
            }
        }).start();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        System.out.println("Error in web socket -> " + exception.toString());
        closeServerToClient(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        System.out.print("Closing status in web socket -> " + printCloseStatus(status));
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
                if(done) {
                    s.incomingMsg(s.getIncoming());
                    s.setIncoming("");
                }

            }catch (Exception e){
                System.out.println("Error in converting incoming message to json: " + e + " msg -> " + message);
                s.setIncoming("");
            }
        }
    }

    private void closeServerToClient(WebSocketSession session) {

        ServerToClient s = Server.pokerServer.getServerToClient(session);

        if (s == null) {
            Server.pokerServer.removeFromCasualConnections(null);
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

    private String printCloseStatus(CloseStatus status){

        int c = status.getCode();

        String ret = "\n";
        ret += "Status code: " + c + " reason: ";

        if(c == 1000) ret += "normal closure" + "\n";
        else if(c == 1001) ret += "an endpoint is going away" + "\n";
        else if(c == 1002) ret += "an endpoint is terminating the connection due to a protocol error" + "\n";
        else if(c == 1003) ret += "an endpoint is terminating the connection because it has received a type of data it cannot accept" + "\n";
        else if(c == 1004) ret += "unknown error" + "\n";
        else if(c == 1005) ret += "unknown error" + "\n";
        else if(c == 1006) ret += "unknown error" + "\n";
        else if(c == 1007) ret += "an endpoint is terminating the connection because it has received data within a message that was not consistent with the type of the message" + "\n";
        else if(c == 1008) ret += "an endpoint is terminating the connection because it has received a message that violates its policy" + "\n";
        else if(c == 1009) ret += "an endpoint is terminating the connection because it has received a message that is too big for it to process" + "\n";
        else if(c == 1010) ret += "an endpoint (client) is terminating the connection because it has expected the server to negotiate one or more extension, but the server didn't return them in the response message of the WebSocket handshake" + "\n";
        else if(c == 1011) ret += "a server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request" + "\n";
        else if(c == 1012) ret += "the service is restarted" + "\n";
        else if(c == 1013) ret += "the service is experiencing overload" + "\n";
        else if(c == 1014) ret += "unknown error" + "\n";
        else if(c == 1015) ret += "unknown error" + "\n";


        return ret;
    }

}
