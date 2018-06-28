package com.tanuj;

import com.mongodb.*;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Tanuj on 5/19/18.
 */
public class SubServer extends WebSocketServer {
    static final String channel = "slides";
  private static final String PASSWORD;
  static LinkedList<WebSocket> conns;
    static String jedisAdd;

    static Map<Integer, String> images;

    static Integer currentTime = 1;
    static boolean synched = false;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MongoClient mongo;
  private Thread t = null;

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

    public SubServer(InetSocketAddress add, String jedisAdd)
        throws UnknownHostException {
        super(add);
        this.jedisAdd = "redis-10133.c53.west-us.azure.cloud.redislabs.com";
        conns = new LinkedList<WebSocket>();
        images = new HashMap<Integer, String>();
        startSync(0);
        Thread sub = new Thread(new SubThread());
        sub.start();
        System.out.println("Not Blocking");
    }

  private void startSync(final int first) throws UnknownHostException {
    mongo = new MongoClient(new MongoClientURI(
        "mongodb://admin:admin123@ds018308.mlab.com:18308/statestore"));
    synched = false;
    t = new Thread(new Runnable() {
      public void run() {
        DB db = mongo.getDB("statestore");
        DBCollection coll = db.getCollection("slides");
        /*BasicDBObject obj = new BasicDBObject();
        obj.put("time", -1);
        DBObject doc = coll.findOne(obj);
        int latest = Integer.parseInt((String) doc.get("image"));*/
        BasicDBObject regexQuery = new BasicDBObject();
        regexQuery.put("time", new BasicDBObject("$gte",
            first));
        DBCursor cursor = coll.find(regexQuery);
        currentTime = cursor.size();
        while(cursor.hasNext()){
          DBObject o = cursor.next();
//          BasicDBObject dbObj = (BasicDBObject) o.get("_id");
          int time = ((Number) o.get("time")).intValue();
          System.out.println("Got " + String.valueOf(time));
          String image = (String) o.get("image");
          images.put(time, image);
        }
        synched = true;
      }
    });
    t.start();
  }


  class SubThread implements Runnable{

        public void run() {
            Jedis jedis = new Jedis(jedisAdd, 10133);
            System.out.println("Starting subscriber for channel " + channel);
            //final ImageDisplay display = new ImageDisplay(640, 480);
            jedis.auth(PASSWORD);
            jedis.connect();
            jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                super.onMessage(channel, message);
                System.out.println("Got message");
              try {
                Message m = MAPPER.readValue(message, Message.class);
                images.put(m.time, m.image);
                System.out.println(m.image);
                System.out.println(m.time);
                System.out.println(m.command);
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
                //catchup on what was missed

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
        if(t != null){
          synchronized (t) {
            if(t != null) {

              try {
                t.join();
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              t = null;
            }
          }
        }
        conns.add(webSocket);
        sendImage(webSocket, currentTime, 1);
        //synchronize(webSocket, -1, currentTime+1);
    }

    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        //webSocket.send("Connection closed");
        //broadcast("some guy closed");
        System.out.println("closed");
        conns.remove(webSocket);
    }

    public void onMessage(WebSocket webSocket, String s) {
      try {
        Message m = MAPPER.readValue(s, Message.class);
        if(m.command == 1){
          int request = m.time;
          if(request == -1){
            request = 1;
          }
          sendImage(webSocket, request, 1);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      //synchronize(webSocket, lastSeen, upTill);
    }

    private void sendImage(WebSocket webSocket, int request, int command) {
      Message m = new Message();
      m.command = command;
      m.time = request;
      if(!images.containsKey(request)){
        System.out.println("Asked for bad photo");
        System.out.println(request);
        return;
      }
      m.image = images.get(request);
      try {
        webSocket.send(MAPPER.writeValueAsString(m));
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("Should've sent " + String.valueOf(request));
    }

    /*void synchronize(WebSocket webSocket, int last, int till){
      for(int i = last+1;i < till;i++){
        webSocket.send(images.get(i));
      }
    }*/

    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("what the");
        e.printStackTrace();
    }

    public void onStart() {
        System.out.println("Started");
    }

    public static void main(String args[]) throws UnknownHostException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        final WebSocketServer server = new SubServer(new InetSocketAddress(host, port), args[2]);
        new Thread(new Runnable() {
          public void run() {
            server.run();
          }
        }).start();
        WebSocketServer spillover = new MemorySpilloverStore(new InetSocketAddress(host, 8022));
        spillover.run();
    }
}
