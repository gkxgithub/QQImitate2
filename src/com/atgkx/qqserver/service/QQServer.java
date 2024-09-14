package com.atgkx.qqserver.service;

import com.atgkx.qqbean.Message;
import com.atgkx.qqbean.MessageType;
import com.atgkx.qqbean.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器，在监听9999，等待客户端连接
 */
public class QQServer {

    private ServerSocket ss = null;

    //创建一个集合，保存三个默认存在的用户
    private static ConcurrentHashMap<String, User> validUsers = new ConcurrentHashMap<>();

    //创建一个集合，存放多个离线用户接收的信息
    private static ConcurrentHashMap<String, String> offlineMsg = new ConcurrentHashMap<>();

    static {//静态代码块
        validUsers.put("100", new User("100", "123456"));
        validUsers.put("200", new User("200", "123456"));
        validUsers.put("300", new User("300", "123456"));
    }

    //验证用户是否合法
    public boolean checkUser(String userId, String passwd){
        User user = validUsers.get(userId);
        if(user == null){
            return false;
        }
        if(!user.getPasswd().equals(passwd)){
            return false;
        }
        return true;
    }

    /**
     * 用户注册方法
     * @param userId
     * @param passwd
     * @return
     */
    public boolean signUser(String userId, String passwd){
        //判断用户是否已经存在，防止用户密码被覆盖
        User user = validUsers.get(userId);
        if (user == null) {
            validUsers.put(userId, new User(userId, passwd));
            return true;
        } else {
            return false;
        }
    }

    //通过id拿到user对象
    public static User getUser(String userId){
        return validUsers.get(userId);
    }

    public static ConcurrentHashMap<String, String> getOfflineMsg(){
        return offlineMsg;
    }

    public static void setOfflineMsg(ConcurrentHashMap<String, String> offlineMsg){
        QQServer.offlineMsg = offlineMsg;
    }

    //空参构造器
    public QQServer(){
        //如果真实开发过程端口可以写在配置文件里
        try {
            System.out.println("服务端在9999端口保持监听...");
            //启动推送新闻的线程
            new Thread(new SendNewsToAllService()).start();



            ss = new ServerSocket(9999);
            while (true) {//当和某个客户端连接了，依然会继续监听，因此这里使用到了while
                Socket socket = ss.accept();
                //得到socket关联的对象输入流
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //得到socket关联的对象输出流
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //拿到客户端发送的对象数据
                User u = (User) ois.readObject();

                //创建一个message对象，用来返回给客户端
                Message message = new Message();
                //验证
                //1.是否为注册
                if(u.isIfSign()){
                    if(signUser(u.getUserId(), u.getPasswd())) {
                        message.setMesType(MessageType.MESSAGE_SIGN_SUCCEED);
                    } else {
                        message.setMesType(MessageType.MESSAGE_SIGN_FAIL);
                    }
                    oos.writeObject(message);
                    socket.close();
                }
                //2.是否为登录
                else {
                    if(checkUser(u.getUserId(), u.getPasswd())){//登录成功
                        message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);

                        //登录成功后，给客户端发送离线消息
                        if(offlineMsg.get(u.getUserId()) != null) {
                            message.setContent(offlineMsg.get(u.getUserId()));
                            message.setMesType(MessageType.MESSAGE_MSG);
                            //将已经发送出去的离线消息移除
                            offlineMsg.remove(u.getUserId());
                        }

                        //将message对象回复客户端
                        oos.writeObject(message);

                        //创建一个线程，和客户端保持通信，该线程需要持有socket对象
                        ServerConnectClientThread serverConnectClientThread =
                                new ServerConnectClientThread(socket, u.getUserId());
                        //启动该线程
                        serverConnectClientThread.start();
                        //把线程对象，放到一个集合中管理
                        ManageClientThreads.addServerConnectClientThread(u.getUserId(), serverConnectClientThread);
                    } else {//登录失败
                        System.out.println("用户 id=" + u.getUserId() + "pwd= " + u.getPasswd());
                        message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);
                        oos.writeObject(message);
                        //关闭socket
                        socket.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //如果服务器退出了while，就说明服务器就不再监听了，因此关闭ServerSocket
                ss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
