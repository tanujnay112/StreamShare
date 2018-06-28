package com.tanuj;

import com.mongodb.*;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Tanuj on 6/21/18.
 */
public class StateStoreSubscriber {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String PASSWORD;
  static MongoClient mongo;

  public static void main(String[] args) throws UnknownHostException {
    mongo = new MongoClient(new MongoClientURI(
        "mongodb://admin:admin123@ds018308.mlab.com:18308/statestore"));
    String connection = "redis-10133.c53.west-us.azure.cloud.redislabs.com";
    String channel = "slides";
    Jedis jedis = new Jedis(connection, 10133);
    jedis.auth(PASSWORD);
    jedis.connect();
    jedis.subscribe(new JedisPubSub() {
      @Override
      public void onMessage(String channel, String message) {
        super.onMessage(channel, message);
        System.out.println("got it");
        try {
          SubServer.Message
              m = MAPPER.readValue(message, SubServer.Message.class);
          writeToDB(m);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, channel);
  }

  private static void writeToDB(SubServer.Message m){
    DB db = mongo.getDB("statestore");
    DBCollection coll = db.getCollection("slides");
    BasicDBObject obj = new BasicDBObject();
    obj.put("time", m.time);
    obj.put("image", m.image);
    coll.insert(obj);
  }
}
