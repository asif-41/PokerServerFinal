package com.example.PokerServer.model;


import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "refund")
public class Refund implements Serializable {

    @Id
    @GeneratedValue(generator = "r_generator")
    @SequenceGenerator(
            name = "r_generator",
            sequenceName = "r_sequence",
            initialValue = 1
    )
    @Column(name = "r_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "tr_type")
    private String type;

    @Column(name = "tr_method")
    private String method;

    @Column(name = "prev_transaction_id")
    private String transactionId;

    @Column(name = "refund_transaction_id")
    private String refundTransactionId;

    @Column(name = "coin_amount")
    private long coinAmount;

    @Column(name = "refund_amount")
    private double refundAmount;

    @Column(name = "receiver_number")
    private String receiver;

    @Column(name = "sender_number")
    private String sender;

    @Column(name = "request_time")
    private Date requestTime;

    @Column(name = "refund_time")
    private Date refundTime;

    @Column(name = "refund_request_time")
    private Date refundRequestTime;

    @Column(name = "reason")
    private String reason;

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

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Date getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Date refundTime) {
        this.refundTime = refundTime;
    }

    public Date getRefundRequestTime() {
        return refundRequestTime;
    }

    public void setRefundRequestTime(Date refundRequestTime) {
        this.refundRequestTime = refundRequestTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String printRefund(){

        String ret = "";
        ret += "Id: " + id + "\n";
        ret += "Account id: " + account.getId() + "\n";
        ret += "Type: " + type + "\n";
        ret += "Method: " + method + "\n";
        ret += "Previous transaction id: " + transactionId + "\n";
        ret += "Refund transaction id: " + refundTransactionId + "\n";
        ret += "Coin amount: " + coinAmount + "\n";
        ret += "refundAmount: " + refundAmount + "\n";
        ret += "Sender number: " + sender + "\n";
        ret += "Receiver number: " + receiver + "\n";
        ret += "Request time: " + requestTime + "\n";
        ret += "Refund Request time: " + refundRequestTime + "\n";
        ret += "Refund time: " + refundTime + "\n";
        ret += "Reason: " + reason + "\n";

        return ret;
    }

    public JSONObject getJson(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", id);
        jsonObject.put("account_id", account.getId());
        jsonObject.put("type", type);
        jsonObject.put("method", method);
        jsonObject.put("prevTransactionId", transactionId);
        jsonObject.put("refundTransactionId", refundTransactionId);
        jsonObject.put("coinAmount", coinAmount);
        jsonObject.put("refundAmount", refundAmount);
        jsonObject.put("receiver", receiver);
        jsonObject.put("sender", sender);
        jsonObject.put("requestTime", requestTime);
        jsonObject.put("refundRequestTime", refundRequestTime);
        jsonObject.put("refundTime", refundTime);
        jsonObject.put("reason", reason);

        return jsonObject;
    }
}
