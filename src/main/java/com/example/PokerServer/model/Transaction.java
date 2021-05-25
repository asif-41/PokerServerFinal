package com.example.PokerServer.model;


import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "transaction")
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(generator = "tr_generator")
    @SequenceGenerator(
            name = "tr_generator",
            sequenceName = "tr_sequence",
            initialValue = 1
    )
    @Column(name = "tr_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "tr_type")
    private String type;

    @Column(name = "tr_method")
    private String method;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "coin_amount")
    private long coinAmount;

    @Column(name = "price")
    private double price;

    @Column(name = "receiver_number")
    private String receiver;

    @Column(name = "sender_number")
    private String sender;

    @Column(name = "request_time")
    private Date requestTime;

    @Column(name = "approval_time")
    private Date approvalTime;

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(Date approvalTime) {
        this.approvalTime = approvalTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(long coinAmount) {
        this.coinAmount = coinAmount;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String printTransaction(){

        String ret = "";
        ret += "Id: " + id + "\n";
        ret += "Account id: " + (account == null ? -2 : account.getId()) + "\n";
        ret += "Type: " + type + "\n";
        ret += "Method: " + method + "\n";
        ret += "Transaction id: " + transactionId + "\n";
        ret += "Coin amount: " + coinAmount + "\n";
        ret += "Price: " + price + "\n";
        ret += "Sender number: " + sender + "\n";
        ret += "Receiver number: " + receiver + "\n";
        ret += "Request time: " + requestTime + "\n";
        ret += "Approval time: " + approvalTime + "\n";

        return ret;
    }

    public JSONObject getJson(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", id);
        jsonObject.put("account_id", (account == null ? -2 : account.getId()));
        jsonObject.put("type", type);
        jsonObject.put("method", method);
        jsonObject.put("transactionId", transactionId);
        jsonObject.put("coinAmount", coinAmount);
        jsonObject.put("price", price);
        jsonObject.put("receiver", receiver);
        jsonObject.put("sender", sender);
        jsonObject.put("requestTime", requestTime);
        jsonObject.put("approvalTime", approvalTime);

        return jsonObject;
    }
}
