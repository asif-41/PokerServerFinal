package com.example.PokerServer.Objects;

import org.json.JSONObject;

public class Notification {

    String type;
    String objectType;
    JSONObject data;
    int userId;
    long coinAdded;

    public Notification() {

        type = "";
        objectType = "";
        data = null;
        userId = -1;
        coinAdded = 0;
    }

    public Notification(String type, String objectType, JSONObject data) {

        this.type = type;
        this.objectType = objectType;
        this.data = data;
        userId = data.getInt("account_id");
        coinAdded = 0;

        if(objectType.equals("pendingTransaction") && type.equals("rejected") && data.getString("type").equals("withdraw")) coinAdded = data.getLong("coinAmount");
        else if(objectType.equals("transaction") && type.equals("approved") && data.getString("type").equals("buy")) coinAdded = data.getLong("coinAmount");
    }

    public JSONObject getJson(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", type);
        jsonObject.put("objectType", objectType);
        jsonObject.put("data", data);
        jsonObject.put("coinAdded", coinAdded);

        return jsonObject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getCoinAdded() {
        return coinAdded;
    }

    public void setCoinAdded(long coinAdded) {
        this.coinAdded = coinAdded;
    }
}
