package com.example.PokerServer.model;

import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pending_refund")
public class PendingRefund implements Serializable {

    @Id
    @GeneratedValue(generator = "pr_generator")
    @SequenceGenerator(
            name = "pr_generator",
            sequenceName = "pr_sequence",
            initialValue = 1
    )
    @Column(name = "pr_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "pr_type")
    private String type;

    @Column(name = "pr_method")
    private String method;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "coin_amount")
    private long coinAmount;

    @Column(name = "refund_amount")
    private double refundAmount;

    @Column(name = "request_time")
    private Date requestTime;

    @Column(name = "refund_request_time")
    private Date refundRequestTime;

    @Column(name = "sender_number")
    private String sender;

    @Column(name = "receiver_number")
    private String receiver;

    @Column(name = "reason")
    private String reason;

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

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
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

    public Date getRefundRequestTime() {
        return refundRequestTime;
    }

    public void setRefundRequestTime(Date refundRequestTime) {
        this.refundRequestTime = refundRequestTime;
    }

    public String printPendingRefund(){

        String ret = "";
        ret += "Id: " + id + "\n";
        ret += "Account id: " + account.getId() + "\n";
        ret += "Type: " + type + "\n";
        ret += "Method: " + method + "\n";
        ret += "Transaction id: " + transactionId + "\n";
        ret += "Coin amount: " + coinAmount + "\n";
        ret += "Refund amount: " + refundAmount + "\n";
        ret += "Sender: " + sender + "\n";
        ret += "Receiver: " + receiver + "\n";
        ret += "Request time: " + requestTime + "\n";
        ret += "Refund Request time: " + refundRequestTime + "\n";
        ret += "Reason: " + reason + "\n";


        return ret;
    }

    public JSONObject getJson(){

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", id);
        jsonObject.put("account_id", account.getId());
        jsonObject.put("type", type);
        jsonObject.put("method", method);
        jsonObject.put("transactionId", transactionId);
        jsonObject.put("coinAmount", coinAmount);
        jsonObject.put("refundAmount", refundAmount);
        jsonObject.put("receiver", receiver);
        jsonObject.put("sender", sender);
        jsonObject.put("requestTime", requestTime);
        jsonObject.put("refundRequestTime", refundRequestTime);
        jsonObject.put("reason", reason);

        return jsonObject;
    }
}
