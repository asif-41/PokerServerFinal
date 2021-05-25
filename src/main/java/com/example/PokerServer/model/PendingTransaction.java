package com.example.PokerServer.model;

import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pending_transaction")
public class PendingTransaction implements Serializable {

    @Id
    @GeneratedValue(generator = "ptr_generator")
    @SequenceGenerator(
            name = "ptr_generator",
            sequenceName = "ptr_sequence",
            initialValue = 1
    )
    @Column(name = "ptr_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "ptr_type")
    private String type;

    @Column(name = "ptr_method")
    private String method;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "coin_amount")
    private long coinAmount;

    @Column(name = "price")
    private double price;

    @Column(name = "request_time")
    private Date requestTime;

    @Column(name = "sender_number")
    private String sender;

    @Column(name = "receiver_number")
    private String receiver;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
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

    public String printPendingTransaction(){

        String ret = "";
        ret += "Id: " + id + "\n";
        ret += "Account id: " + (account == null ? -2 : account.getId()) + "\n";
        ret += "Type: " + type + "\n";
        ret += "Method: " + method + "\n";
        ret += "Transaction id: " + transactionId + "\n";
        ret += "Coin amount: " + coinAmount + "\n";
        ret += "Price: " + price + "\n";
        ret += "Sender: " + sender + "\n";
        ret += "Receiver: " + receiver + "\n";
        ret += "Request time: " + requestTime + "\n";

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

        return jsonObject;
    }
}
