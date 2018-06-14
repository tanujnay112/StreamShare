package com.tanuj;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.File;

/**
 * Created by Tanuj on 6/12/18.
 */
public class PdfGenerator extends WebSocketServer {
  public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

  }

  public void onClose(WebSocket webSocket, int i, String s, boolean b) {

  }

  public void onMessage(WebSocket webSocket, String s) {

  }

  public void onError(WebSocket webSocket, Exception e) {

  }

  public void onStart() {

  }

  private File generatePDF(String user){}

  private String getImage(int timestamp){
    //RPC into SubServer
  }

  private String getAnnotations(String user){
    //RPC into spilloverfacade
  }

}
