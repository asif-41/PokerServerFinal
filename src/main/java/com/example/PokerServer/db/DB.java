package com.example.PokerServer.db;


import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Objects.User;
import com.example.PokerServer.model.Account;
import com.example.PokerServer.model.Transaction;
import com.example.PokerServer.repository.AccountRepository;
import com.example.PokerServer.repository.TransactionRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;
import java.util.function.Function;


public class DB {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;


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
                        Calendar.getInstance().getTime(), Calendar.getInstance().getTime());
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
            else if(columns.equals("addBuyCoin")){
                acc.setCurrent_coins(user.getCurrentCoin() + user.getBoardCoin());
            }
            else if(columns.equals("removeWithdrawCoin")){
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

    public void addTransaction(int accountId, String trType, String trMethod, String trId, long coinAmount, double money){

        Transaction tr = new Transaction();

        tr.setAccount(findById(accountId));
        tr.setType(trType);
        tr.setMethod(trMethod);
        tr.setTransactionId(trId);
        tr.setCoinAmount(coinAmount);
        tr.setMoney(money);

        transactionRepository.saveAndFlush(tr);
    }

    public JSONArray getTransactions(int accountId){

        JSONArray array = new JSONArray();
        Account acc = accountRepository.findById(accountId).get();

        for(Transaction x : acc.getTransactions()){

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("type", x.getType());
            jsonObject.put("method", x.getMethod());
            jsonObject.put("transactionId", x.getTransactionId());
            jsonObject.put("coinAmount", x.getCoinAmount());
            jsonObject.put("money", x.getMoney());

            array.put(jsonObject);
        }
        return array;
    }

    //=======================================================================
    //
    //=======================================================================

}
