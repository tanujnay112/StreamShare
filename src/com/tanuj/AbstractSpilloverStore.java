package com.tanuj;

import org.codehaus.jackson.map.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Tanuj on 6/12/18.
 */
public abstract class AbstractSpilloverStore extends WebSocketServer {

  private ObjectMapper MAPPER = new ObjectMapper();

  public AbstractSpilloverStore(InetSocketAddress address){
    super(address);
  }

  public static class Message{
    public int getTime() {
      return time;
    }

    public void setTime(int time) {
      this.time = time;
    }

    public String getImage() {
      return image;
    }

    public void setImage(String image) {
      this.image = image;
    }

    int time;

    String image;

    public int getCommand() {
      return command;
    }

    public void setCommand(int command) {
      this.command = command;
    }

    int command;
  }

  public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
    System.out.println("Got connection");
  }

  public void onClose(WebSocket webSocket, int i, String s, boolean b) {
    System.out.println("Closed connection");
  }

  public void onMessage(WebSocket webSocket, String s) {
    try {
      SubServer.Message m = MAPPER.readValue(s, SubServer.Message.class);
      if(m.command == 1){
        storeData(webSocket.toString(), m.time, m.image);
      }else{
        String responseImage = getData(webSocket.toString(), m.time);
        Message response = new Message();
        response.command = 0;
        response.time = m.time;
        response.image = responseImage;
        webSocket.send(MAPPER.writeValueAsString(response));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void onError(WebSocket webSocket, Exception e) {
    e.printStackTrace();
  }

  public void onStart() {

  }

  public abstract void storeData(String user, int slideno, String data);

  public abstract String getData(String user, int slideno);
}
