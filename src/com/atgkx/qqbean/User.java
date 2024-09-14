package com.atgkx.qqbean;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean ifSign;//是否为注册
    private String userId;//用户id
    private String passwd;//密码
    public ArrayList<String> personSignList;//个性签名列表

    public ArrayList<String> getPersonSignList(){
        return personSignList;
    }

    public void setPersonSignList(ArrayList<String> personSignList){
        this.personSignList = personSignList;
    }

    public User(String userId, String passwd){
        this.userId = userId;
        this.passwd = passwd;
        this.personSignList = new ArrayList<>();
}

    public User(boolean ifSign, String userId, String passwd){
        this.ifSign = ifSign;
        this.userId = userId;
        this.passwd = passwd;
        this.personSignList = new ArrayList<>();
    }

    public User(boolean ifSign, String userId, String passwd, ArrayList<String> personSignList){
        this.ifSign = ifSign;
        this.userId = userId;
        this.passwd = passwd;
        this.personSignList = personSignList;
    }

    public User(){
        this.personSignList = new ArrayList<>();
    }

    public String getUserId(){
        return userId;
    }

    public String getPasswd(){
        return passwd;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public void setPasswd(String passwd){
        this.passwd = passwd;
    }

    public boolean isIfSign(){
        return ifSign;
    }

    public void setIfSign(boolean ifSign){
        this.ifSign = ifSign;
    }
}