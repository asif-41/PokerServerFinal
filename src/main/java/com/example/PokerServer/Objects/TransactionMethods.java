package com.example.PokerServer.Objects;

public class TransactionMethods {

    private static long coinAmountOnBuy[] = {10000000};
    private static long coinPriceOnBuy[] = {30};

    private static long coinAmountOnWithdraw[] = {10000000};
    private static long coinPriceOnWithdraw[] = {26};

    //returns coin price

    public static double getCurrencyAmount(long coinAmount, String req){

        long[] coinAmounts = {};
        long[] coinPrices = {};

        if(req.equals("buy")) {
            coinAmounts = coinAmountOnBuy;
            coinPrices = coinPriceOnBuy;
        }
        else if(req.equals("withdraw")){
            coinAmounts = coinAmountOnWithdraw;
            coinPrices = coinPriceOnWithdraw;
        }

        double price = 0.0;

        for(int i=0; i<coinAmounts.length; i++){

            if(coinAmount == coinAmounts[i]){
                price = coinPrices[i];
                break;
            }
        }

        return price;
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

}
