package com.example.PokerServer.db;


import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Connection.ServerToClient;
import com.example.PokerServer.Objects.TransactionMethods;
import com.example.PokerServer.Objects.User;
import com.example.PokerServer.model.*;
import com.example.PokerServer.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.*;
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

    @Autowired
    private BannedRepository bannedRepository;


    public static <U> Function<Object, U> filterAndCast(Class<? extends U> clazz) {
        return t -> clazz.isInstance(t) ? clazz.cast(t) : null;
    }





    //=======================================================================
    //
    //              ACCOUNT TABLE STUFF
    //
    //=======================================================================

    private String getImageLink(String databaseLink, String incomingLink){

        String ret = "";

        if(databaseLink.equals("empty")) ret = incomingLink;
        else ret = databaseLink;

        return ret;
    }

    private String setImageLink(String link){

        String ret = "";

        String[] data = link.split("=");
        if(data[0].equals("http://" + Server.pokerServer.getHost() + ":" + Server.pokerServer.getPort() + "/image?id")) ret = link;
        else ret = "empty";

        return ret;
    }




    public User getUserData(String account_id, String account_type, String username, String imageLink) throws ClassCastException {

        User user = null;
        try {

            int id = 0;

            if(account_type.equals("facebook")) id = findByFbId(account_id);
            else if(account_type.equals("google")) id = findByGoogleId(account_id);

            if (id != 0){

                Account account = findById(id);

                try {
                    user = new User(account.getId(), account.getUsername(), account.getFb_id(), account.getGoogle_id(), account.getLogin_method(), getImageLink(account.getImageLink(), imageLink),
                                    account.getExp(), account.getCurrent_coins(), account.getCoins_won(), account.getCoins_lost(), account.getRounds_won(), account.getRounds_played(),
                                    account.getWinStreak(), account.getTotalCallCount(), account.getCall_count(), account.getRaise_count(), account.getFold_count(), account.getAllInCount(),
                                    account.getCheck_count(), account.getBiggestWin(), account.getBestHand(), account.getCoinVideoCount(), account.getLastCoinVideoAvailableTime(),
                                    account.getLastLoggedInTime(), account.getLastFreeCoinTime(), Calendar.getInstance().getTime());
                    updateUser(user, "toggleLogin");
                }
                catch (Exception  e){
                    System.out.println("some fields are null");

                    if(Server.pokerServer.printError){
                        e.printStackTrace(System.out);
                        System.out.println();
                    }
                    return null;
                }

            }
            else{
                String fbId = "";
                String googleId = "";

                if(account_type.equals("facebook")) fbId = account_id;
                else if(account_type.equals("google")) googleId = account_id;

                user = new User(-1, username, fbId, googleId, account_type, imageLink,
                        0, Server.pokerServer.initialCoin, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, "",
                        Server.pokerServer.dailyCoinVideoCount, Calendar.getInstance().getTime(), Calendar.getInstance().getTime(),
                        User.firstLastFreeCoinTime(), Calendar.getInstance().getTime());
                saveUser(user, true);

                int thisId = -1;
                if(account_type.equals("facebook")) thisId = findByFbId(fbId);
                else if(account_type.equals("google")) thisId = findByGoogleId(googleId);

                user.setId(thisId);
                updateUser(user, "toggleLogin");
            }
        }
        catch (Exception e){

            if(Server.pokerServer.printError){
                e.printStackTrace(System.out);
                System.out.println();
            }
        }
        return user;

    }

    public void saveUser(User user, boolean isNew) throws  RuntimeException{

        if(isNew){

            Account acc = new Account();

            acc.setUsername(user.getUsername());
            acc.setFb_id(user.getFb_id());
            acc.setImageLink(setImageLink(user.getImageLink()));
            acc.setGoogle_id(user.getGmail_id());
            acc.setLogin_method(user.getLoginMethod());
            acc.setLoggedIn(false);
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
            if(acc == null) return ;

            acc.setImageLink(setImageLink(user.getImageLink()));
            acc.setLoggedIn(false);
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

        if(acc == null) return;

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
            else if(columns.equals("toggleLogin")){

                boolean cur = acc.isLoggedIn();
                acc.setLoggedIn(! cur);
            }
            else if(columns.equals("image")){

                acc.setImageLink(setImageLink(user.getImageLink()));
            }
        accountRepository.saveAndFlush(acc);
    }

    public void forceLogout(){

        List<Account> accountList = accountRepository.findAll();

        for(int i=0; i<accountList.size(); i++) {

            Account acc = accountList.get(i);
            if(acc.isLoggedIn()) {
                acc.setLoggedIn(false);
                accountRepository.saveAndFlush(acc);
            }
        }
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

        Account ret = null;
        if( ! accountRepository.findById(id).isEmpty() ) ret = accountRepository.findById(id).get();
        return ret;
    }

    public Banned findBannedById(int id){

        Banned ret = null;
        if( ! bannedRepository.findById(id).isEmpty() ) ret = bannedRepository.findById(id).get();
        return ret;
    }

    public int findBannedId(String type, String key){

        int ret = -1;
        List<Banned> bannedList = bannedRepository.findAll();

        for(int i=0; i<bannedList.size(); i++){

            Banned x = bannedList.get(i);

            if(x.getLogin_method().equals(type) && x.getAccount_key().equals(key)) {
                ret = i;
                break;
            }
        }

        return ret;
    }


    public void removeAccount(int id){

        Account acc = findById(id);

        if(acc == null) return ;
        else if(acc.isLoggedIn()) {
            ServerToClient s = Server.pokerServer.getLoggedUser(id);
            if(s.getUser() != null) s.sendResponseLogout();
        }

        for(PendingTransaction x : acc.getPendingTransactions()) pendingTransactionRepository.delete(x);
        for(Transaction x : acc.getTransactions()) transactionRepository.delete(x);
        for(PendingRefund x : acc.getPendingRefunds()) pendingRefundRepository.delete(x);
        for(Refund x : acc.getRefunds()) refundRepository.delete(x);

        accountRepository.delete(acc);
    }

    public void editAccount(Hashtable map){

        int id = Integer.valueOf((String) map.get("id"));
        String username = (String) map.get("username");
        long exp = Long.valueOf((String) map.get("exp"));
        int coinVideoCount = Integer.valueOf((String) map.get("coinVideoCount"));
        long currentCoin = Long.valueOf((String) map.get("currentCoin"));

        Account acc = findById(id);
        if(acc == null || acc.isLoggedIn()) return ;

        acc.setCurrent_coins(currentCoin);
        acc.setCoinVideoCount(coinVideoCount);
        acc.setExp(exp);
        acc.setUsername(username);

        accountRepository.saveAndFlush(acc);
    }

    public void banAccount(int id){

        Account acc = findById(id);

        if(acc == null) return ;
        else if(acc.isLoggedIn()) {
            ServerToClient s = Server.pokerServer.getLoggedUser(id);
            if(s.getUser() != null) s.sendResponseLogout();
        }

        for(PendingTransaction x : acc.getPendingTransactions()) pendingTransactionRepository.delete(x);
        for(Transaction x : acc.getTransactions()) transactionRepository.delete(x);
        for(PendingRefund x : acc.getPendingRefunds()) pendingRefundRepository.delete(x);
        for(Refund x : acc.getRefunds()) refundRepository.delete(x);

        Banned banned = new Banned();

        banned.setLogin_method(acc.getLogin_method());
        banned.setUsername(acc.getUsername());

        if(acc.getLogin_method().equals("facebook")) banned.setAccount_key(acc.getFb_id());
        else if(acc.getLogin_method().equals("google")) banned.setAccount_key(acc.getGoogle_id());

        bannedRepository.saveAndFlush(banned);
        accountRepository.delete(acc);
    }

    public void removeBannedAccount(int id){

        System.out.println("Permitting " + id);

        Banned banned = findBannedById(id);
        if(banned == null) return ;

        bannedRepository.delete(banned);
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

        PendingTransaction ptr = null;
        if( ! pendingTransactionRepository.findById(ptrId).isEmpty()) ptr = pendingTransactionRepository.findById(ptrId).get();
        return ptr;
    }

    private PendingRefund findPendingRefundById(int rrId){

        PendingRefund rr = null;
        if( ! pendingRefundRepository.findById(rrId).isEmpty() ) rr = pendingRefundRepository.findById(rrId).get();
        return rr;
    }

    public int getPendingTransactionCount(int id){

        int ret = 0;
        Account acc = findById(id);
        if( acc != null ) ret = acc.getPendingTransactions().size();

        return ret;
    }




    public void addPendingTransaction(int accountId, String trType, String trMethod, String trId, long coinAmount, double price, Date requestTime, String sender, String receiver){

        PendingTransaction ptr = new PendingTransaction();
        Account acc = findById(accountId);

        if(acc == null) return;

        ptr.setAccount(acc);
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
        if(ptr == null) return;

        Account acc = ptr.getAccount();

        if(acc != null) acc.getPendingTransactions().remove(ptr);
        pendingTransactionRepository.delete(ptr);

        if(notify) TransactionMethods.notifyUser(ptr, "rejected", reason);
    }



    public void addPendingRefund(int pendingTransactionId, double amount, String reason){

        PendingRefund pr = new PendingRefund();
        PendingTransaction ptr = findPendingTransactionById(pendingTransactionId);
        removePendingTransaction(pendingTransactionId, false, "");

        Account acc = findById(ptr.getAccount().getId());
        if(acc == null) return;

        pr.setAccount( acc );
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
        if(pr == null) return ;

        Account acc = pr.getAccount();

        if(acc != null) acc.getPendingRefunds().remove(pr);
        pendingRefundRepository.delete(pr);
    }



    public void addRefund(int refundRequestId, String transactionId, String sender){

        Refund r = new Refund();
        PendingRefund pr = findPendingRefundById(refundRequestId);
        removePendingRefund(refundRequestId);

        Account acc = findById( pr.getAccount().getId() );
        if(acc == null) return ;

        r.setAccount( acc );
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
        r.setPrev_receiver(pr.getReceiver());

        refundRepository.saveAndFlush(r);
        TransactionMethods.notifyUser(r, "approved");
    }



    public void addTransaction(int pendingTransactionId){

        Transaction tr = new Transaction();
        PendingTransaction ptr = findPendingTransactionById(pendingTransactionId);
        removePendingTransaction(pendingTransactionId, false, "");

        Account acc = findById( ptr.getAccount().getId() );
        if(acc == null) return ;

        tr.setAccount( acc );
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

    //  only for buying
    public void addTransaction(int accId, String type, long coinAmount, double price, String method, String sender, String receiver, String trxId, Date requestTime){

        Transaction tr = new Transaction();

        Account acc = findById(accId);
        if(acc == null) return ;

        tr.setAccount( acc );
        tr.setType(type);
        tr.setMethod(method);
        tr.setTransactionId(trxId);
        tr.setCoinAmount(coinAmount);
        tr.setPrice(price);
        tr.setSender(sender);
        tr.setReceiver(receiver);
        tr.setRequestTime(requestTime);
        tr.setApprovalTime(requestTime);

        transactionRepository.saveAndFlush(tr);
        TransactionMethods.notifyUser(tr, "approved");
    }


    public void addTransaction(int pendingTransactionId, String transactionId, String sender){

        Transaction tr = new Transaction();
        PendingTransaction ptr = findPendingTransactionById(pendingTransactionId);
        removePendingTransaction(pendingTransactionId, false, "");

        Account acc = findById( ptr.getAccount().getId() );
        if(acc == null) return ;

        tr.setAccount( acc );
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

        Account acc = findById(accountId);
        if(acc == null) return ret;

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




    public void editPendingTransactions(Hashtable map){

        int count = Integer.valueOf((String) map.get("count"));
        int[] ids = new int[count];
        ArrayList cmds = new ArrayList<String[]>();

        for(int i=0; i<count; i++){

            int id = Integer.valueOf((String) map.get("id" + i));
            String action = (String) map.get("action" + id);

            ids[i] = id;

            if(action.equals("approve")){

                String type = (String) map.get("type" + id);
                if(type.equals("buy")){
                    cmds.add(new String[]{action, type});
                }
                else if(type.equals("withdraw")){

                    String transactionId = (String) map.get("transactionId" + id);
                    String sender = (String) map.get("senderNumber" + id) ;

                    cmds.add(new String[]{action, type, transactionId, sender});
                }
            }
            else if(action.equals("reject")){
                String reason = (String) map.get("reason" + id);
                cmds.add(new String[]{action, "", reason});
            }
            else if(action.equals("refund")){

                String reason = (String) map.get("reason" + id);
                double amount = 0.0;
                try{
                    amount = Double.valueOf((String) map.get("refundAmount" + id));
                }catch (Exception e){
                    cmds.add(new String[]{});

                    if(Server.pokerServer.printError){
                        e.printStackTrace(System.out);
                        System.out.println();
                    }
                    continue;
                }
                cmds.add(new String[]{action, "", reason, String.valueOf(amount)});

            }
        }
        TransactionMethods.multipleRequest(cmds, ids);
    }

    public void editPendingRefunds(Hashtable map){

        int count = Integer.valueOf((String) map.get("count"));
        int[] ids = new int[count];
        ArrayList cmds = new ArrayList<String[]>();

        for(int i=0; i<count; i++){

            int id = Integer.valueOf((String) map.get("id" + i));
            String transactionId = (String) map.get("transactionId" + id);
            String sender = (String) map.get("senderNumber" + id);

            ids[i] = id;
            cmds.add(new String[]{"approveRefund", "", transactionId, sender});
        }
        TransactionMethods.multipleRequest(cmds, ids);
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

    public BannedRepository getBannedRepository() {
        return bannedRepository;
    }

    public List<Account> getAccounts(int page){

        ArrayList ret = new ArrayList();
        List accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        int st = (page-1)*30;
        int en = Math.min( st + 30, accounts.size() );

        for(int i=st; i<en; i++) ret.add(accounts.get(i));
        return ret;
    }

    public List<Transaction> getTransactions(int page){

        ArrayList ret = new ArrayList();
        List transactions = transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        int st = (page-1)*30;
        int en = Math.min( st + 30, transactions.size() );

        for(int i=st; i<en; i++) ret.add(transactions.get(i));
        return ret;
    }

    public List<Transaction> getRefunds(int page){

        ArrayList ret = new ArrayList();
        List refunds = refundRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        int st = (page-1)*30;
        int en = Math.min( st + 30, refunds.size() );

        for(int i=st; i<en; i++) ret.add(refunds.get(i));
        return ret;
    }

    public List<Transaction> getPendingRefunds(int page){

        ArrayList ret = new ArrayList();
        List pendingRefunds = pendingRefundRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        int st = (page-1)*30;
        int en = Math.min( st + 30, pendingRefunds.size() );

        for(int i=st; i<en; i++) ret.add(pendingRefunds.get(i));
        return ret;
    }

    public List<PendingTransaction> getPendingTransactions(int page){

        ArrayList ret = new ArrayList();
        List pendingTransactions = pendingTransactionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        int st = (page-1)*30;
        int en = Math.min( st + 30, pendingTransactions.size() );

        for(int i=st; i<en; i++) ret.add(pendingTransactions.get(i));
        return ret;
    }

    public List<Banned> getBannedAccounts(int page){

        ArrayList ret = new ArrayList();
        List bannedAccounts = bannedRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        int st = (page-1)*30;
        int en = Math.min( st + 30, bannedAccounts.size() );

        for(int i=st; i<en; i++) ret.add(bannedAccounts.get(i));
        return ret;
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
        ret += printBannedAccounts(database);
        ret += "\n\n\n\n";

        return ret;
    }

    public static String printAccounts(DB database){

        List<Account> accounts = database.getAccountRepository().findAll();

        String ret = "Account count: " + accounts.size() + "\n\n";
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

        String ret = "Transaction count: " + transactions.size() + "\n\n";
        for(Transaction x : transactions) ret += x.printTransaction() + "\n";

        return ret;
    }

    public static String printPendingRefunds(DB database){

        List<PendingRefund> pendingRefunds = database.getPendingRefundRepository().findAll();

        String ret = "Pending refund count: " + pendingRefunds.size() + "\n\n";
        for(PendingRefund x : pendingRefunds) ret += x.printPendingRefund() + "\n";

        return ret;
    }

    public static String printRefunds(DB database){

        List<Refund> refunds = database.getRefundRepository().findAll();

        String ret = "Refunds count: " + refunds.size() + "\n\n";
        for(Refund x : refunds) ret += x.printRefund() + "\n";

        return ret;
    }

    public static String printBannedAccounts(DB database){

        List<Banned> banned = database.getBannedRepository().findAll();

        String ret = "Banned count: " + banned.size() + "\n\n";
        for(Banned x : banned) ret += x.printBannedAccount() + "\n";

        return ret;
    }




    /*
    //TESTING PURPOSE
    public void testDatabase(){


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                db.testDatabase();
            }
        }, 15000);

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
    */

}
