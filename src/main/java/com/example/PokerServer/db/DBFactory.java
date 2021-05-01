package com.example.PokerServer.db;

public class DBFactory {

    public static DB getDB(){

        return BeanUtil.getBean(DB.class);
    }
}
