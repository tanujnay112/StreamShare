package com.tanuj;


import org.codehaus.jackson.map.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Tanuj on 5/19/18.
 */
public class SubServer extends WebSocketServer {
    static final String channel = "slides";
    static LinkedList<WebSocket> conns;
    static String jedisAdd;

    static Map<Integer, String> images;

    static Integer currentTime = -1;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static class Message{
      int time;
      String image;
    }

    public SubServer(InetSocketAddress add, String jedisAdd){
        super(add);
        this.jedisAdd = jedisAdd;
        conns = new LinkedList<WebSocket>();
        images = new HashMap<Integer, String>();
        //Subscriber s = new Subscriber(jedisAdd);
        Thread sub = new Thread(new SubThread());
        sub.start();
        System.out.println("Not Blocking");
    }

    class SubThread implements Runnable{

        public void run() {
            Jedis jedis = new Jedis(jedisAdd);
            System.out.println("Starting subscriber for channel " + channel);
            //final ImageDisplay display = new ImageDisplay(640, 480);
            jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                super.onMessage(channel, message);
                System.out.println("Got message");
              try {
                Message m = MAPPER.readValue(message, Message.class);
                images.put(m.time, m.image);
                synchronized (currentTime) {
                  if(currentTime < m.time)
                    currentTime = m.time;
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
              broadcastBytes(message);
            }

            private void broadcastBytes(String imageBytes) {
                System.out.println("Broadcasting");
                //System.out.println(imageBytes.length());
                broadcast(imageBytes);
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                System.out.println("Subscribed");
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
            }

            @Override
            public void onPMessage(String pattern, String channel, String message) {
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
            }

            @Override
            public void onPSubscribe(String pattern, int subscribedChannels) {
            }

            }, channel);
        }
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        //webSocket.send("Connection opened");
        //broadcast("some guy connected");
        System.out.println("got connection");
        conns.add(webSocket);
        synchronize(webSocket, -1, currentTime+1);
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        //webSocket.send("Connection closed");
        //broadcast("some guy closed");
        System.out.println("closed");
        conns.remove(webSocket);
    }

    public void onMessage(WebSocket webSocket, String s) {
        String[] stuff = s.split(";");
        int lastSeen = Integer.parseInt(stuff[0]);
        int upTill = Integer.parseInt(stuff[1]);
        if(lastSeen < 0){
         lastSeen = -1;
        }
        synchronize(webSocket, lastSeen, upTill);
    }

    void synchronize(WebSocket webSocket, int last, int till){
      for(int i = last+1;i < till;i++){
        webSocket.send(images.get(i));
      }
    }

    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("what the");
        e.printStackTrace();
    }

    public void onStart() {
        System.out.println("Started");
    }

    public static void main(String args[]){
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        WebSocketServer server = new SubServer(new InetSocketAddress(host, port), args[2]);
        server.run();
    }
}
