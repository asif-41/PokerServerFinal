package com.example.PokerServer.Objects;

import java.util.Random;

public class Token {

    static Random random = new Random();
    static String letters = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./QWERTYUIOPASDFGHJKLZXCVBNM~!@#$%^&*()_+{}|:\"<>?";

    int id;
    int userId;
    String token;
    int time;

    public Token(int tokenId, int userId){

        id = tokenId;
        this.userId = userId;
        token = generateToken();
        time = 0;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public int getTime() {
        return time;
    }

    static String generateToken(){

        String ret = "";

        for(int i=0; i<20; i++)
            ret += letters.charAt(random.nextInt(letters.length()));

        return ret;
    }
}
