package com.atgkx.qqserver.service;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 用于管理和客户端通信的线程
 */
public class ManageClientThreads {
    private static HashMap<String, ServerConnectClientThread> hm = new HashMap<>();

    public static HashMap<String, ServerConnectClientThread> getHm() {
        return hm;
    }

    //添加线程对象到hm集合
    public static void addServerConnectClientThread(String userId, ServerConnectClientThread serverConnectClientThread){
        hm.put(userId, serverConnectClientThread);
    }

    //根据userId返回对应的线程
    public static ServerConnectClientThread getServerConnectClientThread(String userId){
        return hm.get(userId);
    }

    //这里编写方法，可以返回在线用户列表
    public static String getOnlineUser() {
        //集合遍历，遍历hashmap的key
        Iterator<String> iterator = hm.keySet().iterator();
        String onlineUsers = "";
        while(iterator.hasNext()){
            onlineUsers += iterator.next().toString() + " ";
        }
        return onlineUsers;
    }

    //从集合中，移除某个线程对象
    public static void removeServerConnectClientThread(String userId){
        hm.remove(userId);
    }
}
