package com.example.PokerServer.Objects;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.model.PendingRefund;
import com.example.PokerServer.model.PendingTransaction;
import com.example.PokerServer.model.Refund;
import com.example.PokerServer.model.Transaction;
import org.json.JSONObject;

import java.util.ArrayList;

public class TransactionMethods {

    private static int minCoinWithdraw;
    private static double coinPricePerCrore;
    private static long[] coinAmountOnBuy;
    private static double[] coinPriceOnBuy;
    private static String[] bkashLinks;


    //returns coin price
    public static double getCurrencyAmount(long coinAmount, String req){

        double price = 0.0;

        if(req.equals("buy")) {

            for(int i=0; i<coinAmountOnBuy.length; i++){
                if(coinAmount == coinAmountOnBuy[i]){
                    price = coinPriceOnBuy[i];
                    break;
                }
            }
        }
        else if(req.equals("withdraw")){

            price = coinPricePerCrore * ( coinAmount / 10000000 );
        }
        return price;
    }





    //NOTIFY E COIN SET KORA LAGBE

    public static void notifyUser(PendingTransaction ptr, String action){

        JSONObject jsonObject = ptr.getJson();
        jsonObject.put("reason", "");
        Server.pokerServer.makeNotification(action, "pendingTransaction", jsonObject);
    }

    public static void notifyUser(PendingTransaction ptr, String action, String reason){

        JSONObject jsonObject = ptr.getJson();
        jsonObject.put("reason", reason);
        Server.pokerServer.makeNotification(action, "pendingTransaction", jsonObject);
    }

    public static void notifyUser(Transaction tr, String action){

        JSONObject jsonObject = tr.getJson();
        jsonObject.put("reason", "");
        Server.pokerServer.makeNotification(action, "transaction", jsonObject);
    }

    public static void notifyUser(PendingRefund pr, String action){

        Server.pokerServer.makeNotification(action, "pendingRefund", pr.getJson());
    }

    public static void notifyUser(Refund r, String action){

        Server.pokerServer.makeNotification(action, "refund", r.getJson());
    }







    public static void multipleRequest(ArrayList<String[]> cmds, int ids[]){

        for(int i=0; i<cmds.size(); i++){

            String[] cmd = cmds.get(i);

            int id = ids[i];
            String command = cmd[0];
            String type = cmd[1];

            if(command.equals("approve")){
                if(type.equals("buy")) Server.pokerServer.getDb().addTransaction(id);
                else if(type.equals("withdraw")){

                    String transactionId = cmd[2];
                    String sender = cmd[3];

                    Server.pokerServer.getDb().addTransaction(id, transactionId, sender);
                }
            }
            else if(command.equals("reject")){

                String reason = cmd[2];
                Server.pokerServer.getDb().removePendingTransaction(id, true, reason);
            }
            else if(command.equals("refund")){

                String reason = cmd[2];
                double amount = Double.valueOf(cmd[3]);

                Server.pokerServer.getDb().addPendingRefund(id, amount, reason);
            }
            else if(command.equals("approveRefund")){

                String transactionId = cmd[2];
                String sender = cmd[3];
                Server.pokerServer.getDb().addRefund(id, transactionId, sender);
            }
        }
    }














    public static long[] getCoinAmountOnBuy() {
        return coinAmountOnBuy;
    }

    public static void setCoinAmountOnBuy(long[] coinAmountOnBuy) {
        TransactionMethods.coinAmountOnBuy = coinAmountOnBuy;
    }

    public static double getCoinPricePerCrore() {
        return coinPricePerCrore;
    }

    public static void setCoinPricePerCrore(double coinPricePerCrore) {
        TransactionMethods.coinPricePerCrore = coinPricePerCrore;
    }

    public static double[] getCoinPriceOnBuy() {
        return coinPriceOnBuy;
    }

    public static void setCoinPriceOnBuy(double[] coinPriceOnBuy) {
        TransactionMethods.coinPriceOnBuy = coinPriceOnBuy;
    }

    public static int getMinCoinWithdraw() {
        return minCoinWithdraw;
    }

    public static void setMinCoinWithdraw(int minCoinWithdraw) {
        TransactionMethods.minCoinWithdraw = minCoinWithdraw;
    }

    public static String[] getBkashLinks() {
        return bkashLinks;
    }

    public static void setBkashLinks(String[] bkashLinks) {
        TransactionMethods.bkashLinks = bkashLinks;
    }

    /*

        AUTO TRANSACTION FUNCTIONS



        public void addTransaction(int accountId, String trType, String trMethod, String trId, long coinAmount, double price){

            Transaction tr = new Transaction();

            tr.setAccount(findById(accountId));
            tr.setType(trType);
            tr.setMethod(trMethod);
            tr.setTransactionId(trId);
            tr.setCoinAmount(coinAmount);
            tr.setPrice(price);

            transactionRepository.saveAndFlush(tr);
        }


        public static boolean validateBuyCoinRequest(long coinAmount, String method, String transactionId){

            double money = getCurrencyAmount(coinAmount, "buy");

            return true;
        }

        //returns transaction id

        public static String validateWithdrawCoinRequest(long coinAmount, String method, String receiverAccount){

            String ret = "";

            double price = getCurrencyAmount(coinAmount, "withdraw");

            ret = "lol";            // return "" if not success

            return ret;
        }

    */



}
