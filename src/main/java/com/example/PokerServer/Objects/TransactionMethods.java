package com.example.PokerServer.Objects;

public class TransactionMethods {

    //returns coin price

    public static double getCurrencyAmount(long coinAmount){

        return 10.0;
    }

    public static boolean validateBuyCoinRequest(long coinAmount, String method, String transactionId){

        double money = getCurrencyAmount(coinAmount);

        return true;
    }



    //returns transaction id

    public static String validateWithdrawCoinRequest(long coinAmount, String method, String receiverAccount){

        String ret = "";

        double price = getCurrencyAmount(coinAmount);

        ret = "lol";            // return "" if not success

        return ret;
    }

}
