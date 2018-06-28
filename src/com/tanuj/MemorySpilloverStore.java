package com.tanuj;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by Tanuj on 6/24/18.
 */
public class MemorySpilloverStore extends AbstractSpilloverStore{
  private HashMap<String, HashMap<Integer, String>> store;

  public MemorySpilloverStore(InetSocketAddress address){
    super(address);
    store = new HashMap<String, HashMap<Integer, String>>();
  }

  public void storeData(String user, int slideno, String data) {
    if(!store.containsKey(user)){
      store.put(user, new HashMap<Integer, String>());
    }
    HashMap<Integer, String> map = store.get(user);
    map.put(slideno, data);
  }

  public String getData(String user, int slideno) {
    if(!store.containsKey(user)) {
      return null;
    }
    HashMap<Integer, String> map = store.get(user);
    if(!map.containsKey(slideno)){
      return null;
    }
    return map.get(slideno);
  }
}
