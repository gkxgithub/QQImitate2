package com.atgkx.qqserver.service;

import com.atgkx.qqbean.Message;
import com.atgkx.qqbean.MessageType;
import com.atgkx.qqbean.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存有socket的线程，用于对象和某个客户端保持通信
 */
public class ServerConnectClientThread extends Thread{
    private String userId;//连接到服务器的用户id
    private Socket socket;

    public ServerConnectClientThread(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public void run() {
        //放到外面了，表示只输出一次就可以
        System.out.println("服务端与客户端 " + userId + " 保持通信，正在读取数据......");
        while(true){
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();
                //根据message类型，做相应的业务处理
                if(message.getMesType().equals(MessageType.MESSAGE_GET_ONLINE_FRIEND)) {
                    System.out.println(message.getSender() + "申请查看在线用户列表");
                    String onlineUserList = ManageClientThreads.getOnlineUser();

                    //构建一个Message对象，返回给客户端
                    Message message2 = new Message();
                    message2.setMesType(MessageType.MESSAGE_RET_ONLINE_FRIEND);
                    message2.setContent(onlineUserList);
                    message2.setGetter(message.getSender());

                    //返回给客户端（它里面本来就有socket，因为它本就是一个线程，设计之初已包含一个socket）
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message2);

                } else if (message.getMesType().equals(MessageType.MESSAGE_COMM_MES)) {
                    //根据message获取getterId，然后获得相应的线程
                    ServerConnectClientThread serverConnectClientThread = ManageClientThreads.getServerConnectClientThread(message.getGetter());
                    //得到相应socket的对象输出流，将message对象转发给指定的客户端
                    ObjectOutputStream oos = new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());
                    oos.writeObject(message);
                } else if (message.getMesType().equals(MessageType.MESSAGE_TOALL_MES)) {
//                    System.out.println(message.getSender() + "群发消息");
                    //遍历管理线程的集合，得到每一个ServerConnectClientThread对象，得到socket，得到socket对应的输出流
                    HashMap<String, ServerConnectClientThread> hm = ManageClientThreads.getHm();

                    Iterator<String> iterator = hm.keySet().iterator();
                    while (iterator.hasNext()) {
                        String onLineUserId = iterator.next().toString();

                        if(!onLineUserId.equals(message.getSender())) {//将发消息的人排除
                            //转发message
                            ObjectOutputStream oos = new ObjectOutputStream(hm.get(onLineUserId).getSocket().getOutputStream());
                            oos.writeObject(message);
                        }
                    }
                } else if (message.getMesType().equals(MessageType.MESSAGE_CLIENT_EXIT)) {
                    //客户端有很多socket，但是如果现在从这里拿到的socket就是此线程里的socket
                    System.out.println(message.getSender() + "退出");
                    //将此客户端对应线程，从集合移除
                    ManageClientThreads.removeServerConnectClientThread(message.getSender());
                    //关闭连接
                    socket.close();
                    //退出线程
                    break;
                } else if (message.getMesType().equals(MessageType.MESSAGE_FILE_MES)) {
                    //根据getter id 获取到对应的线程，将message对象转发
                    ServerConnectClientThread serverConnectClientThread = ManageClientThreads.getServerConnectClientThread(message.getGetter());
                    ObjectOutputStream oos = new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());
                    //转发
                    oos.writeObject(message);
                } else if(message.getMesType().equals(MessageType.MESSAGE_SET_PERSONSIGN)) {
                    String content = message.getContent();
                    User user = QQServer.getUser(message.getSender());
                    System.out.println(userId + "新设置了个性签名:" + content);
                    //需要先确保personSignList不为null才能往里面添加数据
                    user.personSignList.add(content);
                } else if (message.getMesType().equals(MessageType.MESSAGE_GET_PERSONSIGN)) {
                    User user = QQServer.getUser(message.getGetter());
                    message.setPersonSignList(user.getPersonSignList());
                    message.setMesType(MessageType.MESSAGE_RET_PERSONSIGN);
                    //根据getter id 获取到对应的线程，将message对象转发
                    ServerConnectClientThread serverConnectClientThread = ManageClientThreads.getServerConnectClientThread(message.getSender());
                    ObjectOutputStream oos = new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());

                    oos.writeObject(message);
                } else if (message.getMesType().equals(MessageType.MESSAGE_MSG)) {
                    //处理离线信息
                    System.out.println(message.getSender() + "给" + message.getGetter() + "发来一条离线信息：" + message.getContent());
                    ConcurrentHashMap<String, String> offlineMsg = QQServer.getOfflineMsg();
                    String s = offlineMsg.get(message.getGetter());
                    if(s == null){
                        offlineMsg.put(message.getGetter(), message.getSender() + "给你发来一条留言：" + message.getContent());
                    } else {
                        s = s + " " + message.getSender() + "给你发来一条留言：" + message.getContent();
                        offlineMsg.put(message.getGetter(), s);
                    }
                }else {
                    System.out.println("其它类型的message，暂时不处理");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
