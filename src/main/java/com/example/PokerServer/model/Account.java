package com.example.PokerServer.model;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "account")
public class Account implements Serializable {

    @Id
    @GeneratedValue(generator = "user_generator")
    @SequenceGenerator(
            name = "user_generator",
            sequenceName = "user_sequence",
            initialValue = 1
    )
    @Column(name = "account_id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "biggest_win")
    private long biggestWin;

    @Column(name = "best_hand")
    private String bestHand;

    @Column(name = "exp")
    private long exp;

    @Column(name = "fbid")
    private String fb_id;

    @Column(name = "google_id")
    private  String google_id;

    @Column(name = "login_method")
    private  String login_method;

    @Column(name = "current_coins")
    private  long current_coins;

    @Column(name = "coins_won")
    private long coins_won;

    @Column(name = "coins_lost")
    private  long coins_lost;

    @Column(name = "rounds_played")
    private  int rounds_played;

    @Column(name = "rounds_won")
    private  int rounds_won;

    @Column(name = "call_count")
    private  int call_count;

    @Column(name = "fold_count")
    private  int fold_count;

    @Column(name = "raise_count")
    private  int raise_count;

    @Column(name = "check_count")
    private  int check_count;

    @Column(name =  "allin_count")
    private  Integer allInCount;

    @Column(name =  "total_call_count")
    private  Integer totalCallCount;

    @Column(name = "cointVideo_count")
    private  Integer coinVideoCount;            //      COUNT OF COIN VIDEO AVAILABILITY

    @Column(name = "lastCoinVideo_Available_time")
    private Date lastCoinVideoAvailableTime;    //  LAST COIN VIDEO AVAILABLE IN TIME

    @Column(name = "lastloggedIntime")
    private Date lastLoggedInTime;      //      LAST LOGGED IN TIME

    @Column(name = "lastFree_coin_time")
    private Date lastFreeCoinTime;

    @Column(name = "winStreak")
    private Integer winStreak;              //      CURRENT WIN STREAK


    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<PendingTransaction> pendingTransactions;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<PendingRefund> pendingRefunds;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Refund> refunds;

    public List<PendingTransaction> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(List<PendingTransaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Integer getTotalCallCount() {
        return totalCallCount;
    }

    public void setTotalCallCount(Integer totalCallCount) {
        this.totalCallCount = totalCallCount;
    }

    public Integer getAllInCount() {
        return allInCount;
    }

    public void setAllInCount(Integer allInCount) {
        this.allInCount = allInCount;
    }

    public Integer getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(Integer winStreak) {
        this.winStreak = winStreak;
    }

    public Integer getCoinVideoCount() {
        return coinVideoCount;
    }

    public void setCoinVideoCount(Integer coinVideoCount) {
        this.coinVideoCount = coinVideoCount;
    }

    public Date getLastCoinVideoAvailableTime() {
        return lastCoinVideoAvailableTime;
    }

    public void setLastCoinVideoAvailableTime(Date lastCoinVideoAvailableTime) {
        this.lastCoinVideoAvailableTime = lastCoinVideoAvailableTime;
    }

    public Date getLastLoggedInTime() {
        return lastLoggedInTime;
    }

    public void setLastLoggedInTime(Date lastLoggedInTime) {
        this.lastLoggedInTime = lastLoggedInTime;
    }

    public Date getLastFreeCoinTime() {
        return lastFreeCoinTime;
    }

    public void setLastFreeCoinTime(Date lastFreeCoinTime) {
        this.lastFreeCoinTime = lastFreeCoinTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public String getFb_id() {
        return fb_id;
    }

    public void setFb_id(String fb_id) {
        this.fb_id = fb_id;
    }

    public String getGoogle_id() {
        return google_id;
    }

    public void setGoogle_id(String google_id) {
        this.google_id = google_id;
    }

    public String getLogin_method() {
        return login_method;
    }

    public void setLogin_method(String login_method) {
        this.login_method = login_method;
    }

    public long getCurrent_coins() {
        return current_coins;
    }

    public void setCurrent_coins(long current_coins) {
        this.current_coins = current_coins;
    }

    public long getCoins_won() {
        return coins_won;
    }

    public void setCoins_won(long coins_won) {
        this.coins_won = coins_won;
    }

    public int getRounds_played() {
        return rounds_played;
    }

    public void setRounds_played(int rounds_played) {
        this.rounds_played = rounds_played;
    }

    public int getRounds_won() {
        return rounds_won;
    }

    public void setRounds_won(int rounds_won) {
        this.rounds_won = rounds_won;
    }

    public int getCall_count() {
        return call_count;
    }

    public void setCall_count(int call_count) {
        this.call_count = call_count;
    }

    public int getFold_count() {
        return fold_count;
    }

    public void setFold_count(int fold_count) {
        this.fold_count = fold_count;
    }

    public int getRaise_count() {
        return raise_count;
    }

    public void setRaise_count(int raise_count) {
        this.raise_count = raise_count;
    }

    public int getCheck_count() {
        return check_count;
    }

    public void setCheck_count(int check_count) {
        this.check_count = check_count;
    }

    public long getCoins_lost() {
        return coins_lost;
    }

    public void setCoins_lost(long coins_lost) {
        this.coins_lost = coins_lost;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getBiggestWin() {
        return biggestWin;
    }

    public void setBiggestWin(long biggestWin) {
        this.biggestWin = biggestWin;
    }

    public String getBestHand() {
        return bestHand;
    }

    public void setBestHand(String bestHand) {
        this.bestHand = bestHand;
    }

    public List<PendingRefund> getPendingRefunds() {
        return pendingRefunds;
    }

    public List<Refund> getRefunds() {
        return refunds;
    }

    public String printAccount(){

        String ret = "";

        ret += "Id: " + id + "\n";
        ret += "Username: " + username + "\n";
        ret += "Login method: " + login_method + "\n";
        if(login_method.equals("facebook")) ret += "Facebook: " + fb_id + "\n";
        if(login_method.equals("google")) ret += "Google: " + google_id + "\n";
        ret += "Current coin: " + current_coins + "\n";
        ret += "Coin won: " + coins_won + "\n";
        ret += "Coin lost: " + coins_lost + "\n";
        ret += "Win streak: " + winStreak + "\n";
        ret += "Rounds played: " + rounds_played + "\n";
        ret += "Rounds won: " + rounds_won + "\n";
        ret += "Biggest win: " + biggestWin + "\n";
        ret += "Best hand: " + bestHand + "\n";
        ret += "Exp: " + exp + "\n";
        ret += "Call count: " + call_count + "\n";
        ret += "Raise count: " + raise_count + "\n";
        ret += "AllIn count: " + allInCount + "\n";
        ret += "Fold count: " + fold_count + "\n";
        ret += "Check count: " + check_count + "\n";
        ret += "Total call count: " + totalCallCount + "\n";
        ret += "Coin video count: " + coinVideoCount + "\n";
        ret += "Coin video available time: " + lastCoinVideoAvailableTime + "\n";
        ret += "Last free coin time: " + lastFreeCoinTime + "\n";
        ret += "Last login time: " + lastLoggedInTime + "\n";
        ret += "\n";

        ret += "Pending transaction count: " + pendingTransactions.size() + "\n\n";
        for(PendingTransaction x : pendingTransactions){
            ret += x.printPendingTransaction() + "\n\n";
        }
        ret += "\n";

        ret += "Transaction count: " + transactions.size() + "\n\n";
        for(Transaction x : transactions){
            ret += x.printTransaction() + "\n\n";
        }
        ret += "\n";

        ret += "Pending refund count: " + pendingRefunds.size() + "\n\n";
        for(PendingRefund x : pendingRefunds){
            ret += x.printPendingRefund() + "\n\n";
        }
        ret += "\n";

        ret += "Refund count: " + refunds.size() + "\n\n";
        for(Refund x : refunds){
            ret += x.printRefund() + "\n\n";
        }
        ret += "\n";

        return ret;
    }
}
