package com.atgkx.qqbean;

public interface MessageType {
    //老师解读
    //1.接口中定义了一些常量
    //2.不同的常量的值，表示不同的消息类型
    String MESSAGE_LOGIN_SUCCEED = "1";//登录成功
    String MESSAGE_LOGIN_FAIL = "2";//登录失败
    String MESSAGE_COMM_MES = "3";//普通信息包
    String MESSAGE_GET_ONLINE_FRIEND = "4";//要求返回在线用户列表
    String MESSAGE_RET_ONLINE_FRIEND = "5";//返回在线用户列表
    String MESSAGE_CLIENT_EXIT = "6";//客户端请求退出
    String MESSAGE_TOALL_MES = "7";//发送给所有人
    String MESSAGE_FILE_MES = "8";//发送文件的

    String MESSAGE_SIGN_SUCCEED = "9";//注册成功
    String MESSAGE_SIGN_FAIL = "10";//注册失败
    String MESSAGE_SET_PERSONSIGN = "11";//设置个性签名
    String MESSAGE_GET_PERSONSIGN = "12";//要求返回个性签名
    String MESSAGE_RET_PERSONSIGN = "13";//返回个性签名
    String MESSAGE_MSG = "666";//留言

}
