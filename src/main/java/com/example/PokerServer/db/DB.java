package com.example.PokerServer.db;


import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Objects.TransactionMethods;
import com.example.PokerServer.Objects.User;
import com.example.PokerServer.model.*;
import com.example.PokerServer.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;


public class DB {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PendingTransactionRepository pendingTransactionRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private PendingRefundRepository pendingRefundRepository;


    public static <U> Function<Object, U> filterAndCast(Class<? extends U> clazz) {
        return t -> clazz.isInstance(t) ? clazz.cast(t) : null;
    }





    //=======================================================================
    //
    //              ACCOUNT TABLE STUFF
    //
    //=======================================================================


    public User getUserData(String account_id, String account_type, String username) throws ClassCastException {

        User user = null;
        try {

            int id = 0;

            if(account_type.equals("facebook")) id = findByFbId(account_id);
            else if(account_type.equals("google")) id = findByGoogleId(account_id);

            if (id != 0){

                Account account = findById(id);

                try {
                    user = new User(account.getId(), account.getUsername(), account.getFb_id(), account.getGoogle_id(), account.getLogin_method(), "",
                                    account.getExp(), account.getCurrent_coins(), account.getCoins_won(), account.getCoins_lost(), account.getRounds_won(), account.getRounds_played(),
                                    account.getWinStreak(), account.getTotalCallCount(), account.getCall_count(), account.getRaise_count(), account.getFold_count(), account.getAllInCount(),
                                    account.getCheck_count(), account.getBiggestWin(), account.getBestHand(), account.getCoinVideoCount(), account.getLastCoinVideoAvailableTime(),
                                    account.getLastLoggedInTime(), account.getLastFreeCoinTime(), Calendar.getInstance().getTime());
                }
                catch (Exception  e){
                    e.printStackTrace();
                    System.out.println("some fields are null");
                    return null;
                }

            }
            else{
                String fbId = "";
                String googleId = "";

                if(account_type.equals("facebook")) fbId = account_id;
                else if(account_type.equals("google")) googleId = account_id;


                user = new User(-1, username, fbId, googleId, account_type, "",
                        0, Server.pokerServer.initialCoin, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, "",
                        Server.pokerServer.dailyCoinVideoCount, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(),
                        User.firstLastFreeCoinTime(), Calendar.getInstance().getTime());
                saveUser(user, true);

                int thisId = -1;
                if(account_type.equals("facebook")) thisId = findByFbId(fbId);
                else if(account_type.equals("google")) thisId = findByGoogleId(googleId);

                user.setId(thisId);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return user;

    }

    public void saveUser(User user, boolean isNew) throws  RuntimeException{

        if(isNew){

            Account acc = new Account();

            acc.setUsername(user.getUsername());
            acc.setFb_id(user.getFb_id());
            acc.setGoogle_id(user.getGmail_id());
            acc.setLogin_method(user.getLoginMethod());
            acc.setExp(user.getExp());
            acc.setCurrent_coins(user.getCurrentCoin());
            acc.setCoins_won(user.getCoinWon());
            acc.setCoins_lost(user.getCoinLost());
            acc.setRounds_won(user.getRoundsWon());
            acc.setRounds_played(user.getRoundsPlayed());
            acc.setWinStreak(user.getWinStreak());
            acc.setTotalCallCount(user.getTotalCallCount());
            acc.setCall_count(user.getCallCount());
            acc.setRaise_count(user.getRaiseCount());
            acc.setFold_count(user.getFoldCount());
            acc.setAllInCount(user.getAllInCount());
            acc.setCheck_count(user.getCheckCount());
            acc.setBiggestWin(user.getBiggestWin());
            acc.setBestHand(user.getBestHand());
            acc.setCoinVideoCount(user.getCoinVideoCount());
            acc.setLastCoinVideoAvailableTime(user.getLastCoinVideoAvailableTime());
            acc.setLastLoggedInTime(user.getLastLoggedInTime());
            acc.setLastFreeCoinTime(user.getLastFreeCoinTime());

            accountRepository.saveAndFlush(acc);
        }
        else{

            Account acc = findById(user.getId());

            acc.setExp(user.getExp());
            acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
            acc.setCoins_won(user.getCoinWon());
            acc.setCoins_lost(user.getCoinLost());
            acc.setRounds_won(user.getRoundsWon());
            acc.setRounds_played(user.getRoundsPlayed());
            acc.setWinStreak(user.getWinStreak());
            acc.setTotalCallCount(user.getTotalCallCount());
            acc.setCall_count(user.getCallCount());
            acc.setRaise_count(user.getRaiseCount());
            acc.setFold_count(user.getFoldCount());
            acc.setAllInCount(user.getAllInCount());
            acc.setCheck_count(user.getCheckCount());
            acc.setBiggestWin(user.getBiggestWin());
            acc.setBestHand(user.getBestHand());
            acc.setCoinVideoCount(user.getCoinVideoCount());
            acc.setLastCoinVideoAvailableTime(user.getLastCoinVideoAvailableTime());
            acc.setLastLoggedInTime(user.getCurrentLoginTime());
            acc.setLastFreeCoinTime(user.getLastFreeCoinTime());

            accountRepository.saveAndFlush(acc);
        }
    }

    public void updateUser(User user, String columns){

        Account acc = findById(user.getId());

            if(columns.equals("gameEnd")){
                acc.setExp(user.getExp());
                acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
                acc.setCoins_won(user.getCoinWon());
                acc.setCoins_lost(user.getCoinLost());
                acc.setRounds_won(user.getRoundsWon());
                acc.setRounds_played(user.getRoundsPlayed());
                acc.setWinStreak(user.getWinStreak());
                acc.setTotalCallCount(user.getTotalCallCount());
                acc.setCall_count(user.getCallCount());
                acc.setRaise_count(user.getRaiseCount());
                acc.setFold_count(user.getFoldCount());
                acc.setAllInCount(user.getAllInCount());
                acc.setCheck_count(user.getCheckCount());
                acc.setBiggestWin(user.getBiggestWin());
                acc.setBestHand(user.getBestHand());
            }
            else if(columns.equals("addCoinVideo")){
                acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
                acc.setCoinVideoCount(user.getCoinVideoCount());
                acc.setLastCoinVideoAvailableTime(user.getLastCoinVideoAvailableTime());
            }
            else if(columns.equals("addLoginCoin")){
                acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
                acc.setLastFreeCoinTime(user.getLastFreeCoinTime());
            }
            else if(columns.equals("removeWithdrawCoin")){
                acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
            }
            else if(columns.equals("addCoin")){
                acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
            }
        accountRepository.saveAndFlush(acc);
    }


    public int findByGoogleId(String googleId){

        int ret = 0;
        List<Account> accountList = accountRepository.findAll();

        for(int i=0; i<accountList.size(); i++) {

            Account acc = accountList.get(i);
            if(acc.getGoogle_id() == null) continue;

            if(acc.getGoogle_id().equals(googleId)){
                ret = acc.getId();
                break;
            }
        }
        return ret;
    }

    public int findByFbId(String fb_id){

        int ret = 0;
        List<Account> accountList = accountRepository.findAll();

        for(int i=0; i<accountList.size(); i++) {

            Account acc = accountList.get(i);
            if(acc.getFb_id() == null) continue;

            if(acc.getFb_id().equals(fb_id)){
                ret = acc.getId();
                break;
            }
        }
        return ret;
    }

    public Account findById(int id){

        Account ret = accountRepository.findById(id).get();
        return ret;
    }

    //==========================================================================
    //
    //==========================================================================



    //=======================================================================
    //
    //              TRANSACTION TABLE STUFF
    //
    //=======================================================================

    private PendingTransaction findPendingTransactionById(int ptrId){

        PendingTransaction ptr = pendingTransactionRepository.findById(ptrId).get();
        return ptr;
    }

    private PendingRefund findPendingRefundById(int rrId){

        PendingRefund rr = pendingRefundRepository.findById(rrId).get();
        return rr;
    }

    public int getPendingTransactionCount(int id){

        int ret = 0;

        Account acc = accountRepository.findById(id).get();
        if(acc != null) ret = acc.getPendingTransactions().size();

        return ret;
    }




    public void addPendingTransaction(int accountId, String trType, String trMethod, String trId, long coinAmount, double price, Date requestTime, String sender, String receiver){

        PendingTransaction ptr = new PendingTransaction();

        ptr.setAccount(findById(accountId));
        ptr.setType(trType);
        ptr.setMethod(trMethod);
        ptr.setTransactionId(trId);
        ptr.setCoinAmount(coinAmount);
        ptr.setPrice(price);
        ptr.setRequestTime(requestTime);
        ptr.setSender(sender);
        ptr.setReceiver(receiver);

        pendingTransactionRepository.saveAndFlush(ptr);
        TransactionMethods.notifyUser(ptr, "added");
    }

    public void removePendingTransaction(int id, boolean notify, String reason){

        PendingTransaction ptr = findPendingTransactionById(id);
        pendingTransactionRepository.delete(ptr);
        if(notify) TransactionMethods.notifyUser(ptr, "rejected", reason);
    }



    public void addPendingRefund(int pendingTransactionId, double amount, String reason){

        PendingRefund pr = new PendingRefund();
        PendingTransaction ptr = findPendingTransactionById(pendingTransactionId);
        removePendingTransaction(pendingTransactionId, false, "");

        pr.setAccount( findById( ptr.getAccount().getId() ) );
        pr.setType(ptr.getType());
        pr.setMethod(ptr.getMethod());
        pr.setTransactionId(ptr.getTransactionId());
        pr.setCoinAmount(ptr.getCoinAmount());
        pr.setRefundAmount(amount);
        pr.setRequestTime(ptr.getRequestTime());
        pr.setSender(ptr.getSender());
        pr.setReceiver(ptr.getReceiver());
        pr.setReason(reason);
        pr.setRefundRequestTime(Calendar.getInstance().getTime());

        pendingRefundRepository.saveAndFlush(pr);
        TransactionMethods.notifyUser(pr, "added");
    }

    public void removePendingRefund(int id){

        PendingRefund pr = findPendingRefundById(id);
        pendingRefundRepository.delete(pr);
    }



    public void addRefund(int refundRequestId, String transactionId, String sender){

        Refund r = new Refund();
        PendingRefund pr = findPendingRefundById(refundRequestId);
        removePendingRefund(refundRequestId);

        r.setAccount( findById( pr.getAccount().getId() ) );
        r.setType(pr.getType());
        r.setMethod(pr.getMethod());
        r.setTransactionId(pr.getTransactionId());
        r.setRefundTransactionId(transactionId);
        r.setCoinAmount(pr.getCoinAmount());
        r.setRefundAmount(pr.getRefundAmount());
        r.setReceiver(pr.getSender());
        r.setSender(sender);
        r.setRequestTime(pr.getRequestTime());
        r.setRefundRequestTime(pr.getRefundRequestTime());
        r.setRefundTime(Calendar.getInstance().getTime());
        r.setReason(pr.getReason());

        refundRepository.saveAndFlush(r);
        TransactionMethods.notifyUser(r, "approved");
    }



    public void addTransaction(int pendingTransactionId){

        Transaction tr = new Transaction();
        PendingTransaction ptr = findPendingTransactionById(pendingTransactionId);
        removePendingTransaction(pendingTransactionId, false, "");

        tr.setAccount( findById( ptr.getAccount().getId() ) );
        tr.setType(ptr.getType());
        tr.setMethod(ptr.getMethod());
        tr.setTransactionId(ptr.getTransactionId());
        tr.setCoinAmount(ptr.getCoinAmount());
        tr.setPrice(ptr.getPrice());
        tr.setSender(ptr.getSender());
        tr.setReceiver(ptr.getReceiver());
        tr.setRequestTime(ptr.getRequestTime());
        tr.setApprovalTime(Calendar.getInstance().getTime());

        transactionRepository.saveAndFlush(tr);
        TransactionMethods.notifyUser(tr, "approved");
    }

    public void addTransaction(int pendingTransactionId, String transactionId, String sender){

        Transaction tr = new Transaction();
        PendingTransaction ptr = findPendingTransactionById(pendingTransactionId);
        removePendingTransaction(pendingTransactionId, false, "");

        tr.setAccount( findById( ptr.getAccount().getId() ) );
        tr.setType(ptr.getType());
        tr.setMethod(ptr.getMethod());
        tr.setTransactionId(transactionId);
        tr.setCoinAmount(ptr.getCoinAmount());
        tr.setPrice(ptr.getPrice());
        tr.setSender(sender);
        tr.setReceiver(ptr.getReceiver());
        tr.setRequestTime(ptr.getRequestTime());
        tr.setApprovalTime(Calendar.getInstance().getTime());

        transactionRepository.saveAndFlush(tr);
        TransactionMethods.notifyUser(tr, "approved");
    }


    public JSONObject getAllTransactions(int accountId){

        JSONObject ret = new JSONObject();
        JSONArray array = new JSONArray();

        Account acc = accountRepository.findById(accountId).get();

        for(Transaction x : acc.getTransactions()) array.put(x.getJson());
        ret.put("transactions", array);

        array = new JSONArray();
        for(PendingTransaction x : acc.getPendingTransactions()) array.put(x.getJson());
        ret.put("pendingTransactions", array);

        array = new JSONArray();
        for(PendingRefund x : acc.getPendingRefunds()) array.put(x.getJson());
        ret.put("pendingRefunds", array);

        array = new JSONArray();
        for(Refund x : acc.getRefunds()) array.put(x.getJson());
        ret.put("refunds", array);

        return ret;
    }

    //=======================================================================
    //
    //=======================================================================


    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public PendingTransactionRepository getPendingTransactionRepository() {
        return pendingTransactionRepository;
    }

    public RefundRepository getRefundRepository() {
        return refundRepository;
    }

    public PendingRefundRepository getPendingRefundRepository() {
        return pendingRefundRepository;
    }



    public static String printDatabase(DB database){

        String ret = "";
        ret += printAccounts(database);
        ret += "\n\n\n\n";
        ret += printPendingTransactions(database);
        ret += "\n\n\n\n";
        ret += printPendingRefunds(database);
        ret += "\n\n\n\n";
        ret += printTransactions(database);
        ret += "\n\n\n\n";
        ret += printRefunds(database);
        ret += "\n\n\n\n";

        return ret;
    }

    public static String printAccounts(DB database){

        List<Account> accounts = database.getAccountRepository().findAll();

        String ret = "Account count: " + accounts.size() + "\n";
        for(Account x : accounts) ret += x.printAccount() + "\n\n\n\n";

        return ret;
    }

    public static String printPendingTransactions(DB database){

        List<PendingTransaction> pendingTransactions = database.getPendingTransactionRepository().findAll();

        String ret = "Pending transaction count: " + pendingTransactions.size() + "\n";
        for(PendingTransaction x : pendingTransactions) ret += x.printPendingTransaction() + "\n";

        return ret;
    }

    public static String printTransactions(DB database){

        List<Transaction> transactions = database.getTransactionRepository().findAll();

        String ret = "Transaction count: " + transactions.size() + "\n";
        for(Transaction x : transactions) ret += x.printTransaction() + "\n";

        return ret;
    }

    public static String printPendingRefunds(DB database){

        List<PendingRefund> pendingRefunds = database.getPendingRefundRepository().findAll();

        String ret = "Pending refund count: " + pendingRefunds.size() + "\n";
        for(PendingRefund x : pendingRefunds) ret += x.printPendingRefund() + "\n";

        return ret;
    }

    public static String printRefunds(DB database){

        List<Refund> refunds = database.getRefundRepository().findAll();

        String ret = "Refunds count: " + refunds.size() + "\n";
        for(Refund x : refunds) ret += x.printRefund() + "\n";

        return ret;
    }













    //TESTING PURPOSE
    public void testDatabase(){

        System.out.println("Testing database");

        int[] ids = new int[5];
        ArrayList cmds = new ArrayList<String[]>();

        if(pendingRefundRepository.findAll().size() > 0){
            ids[0] = pendingRefundRepository.findAll().get(0).getId();
            cmds.add(new String[]{ "approveRefund", "hahahaha", "hahahaha", "ami" });
        }
        else {
            ids[0] = -1;
            cmds.add(new String[]{"nai", "haha"});
        }

        if(pendingTransactionRepository.findAll().size() > 0){
            ids[1] = pendingTransactionRepository.findAll().get(0).getId();
            cmds.add(new String[]{ "reject", "hahahaha", "amar iccha", "ami" });
        }
        else {
            ids[1] = -1;
            cmds.add(new String[]{"nai", "haha"});
        }

        if(pendingTransactionRepository.findAll().size() > 1){
            ids[2] = pendingTransactionRepository.findAll().get(1).getId();
            cmds.add(new String[]{ "refund", "hahahaha", "amar iccha", "10.0" });
        }
        else {
            ids[2] = -1;
            cmds.add(new String[]{"nai", "haha"});
        }

        if(pendingTransactionRepository.findAll().size() > 2){
            ids[3] = pendingTransactionRepository.findAll().get(2).getId();
            cmds.add(new String[]{ "approve", "buy", "hahahaha", "ami" });
        }
        else {
            ids[3] = -1;
            cmds.add(new String[]{"nai", "haha"});
        }

        if(pendingTransactionRepository.findAll().size() > 3){
            ids[4] = pendingTransactionRepository.findAll().get(3).getId();
            cmds.add(new String[]{ "approve", "withdraw", "hahahaha", "ami" });
        }
        else {
            ids[4] = -1;
            cmds.add(new String[]{"nai", "haha"});
        }

        for(int i=0; i<5; i++){
            String baal = "id: " + ids[i] + " cmd: ";
            for(String x : (String[]) cmds.get(i)) baal += x + " ";
            System.out.println(baal);
        }

        TransactionMethods.multipleRequest(cmds, ids);
    }


}
