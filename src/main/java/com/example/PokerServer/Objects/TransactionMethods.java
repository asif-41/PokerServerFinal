package com.example.PokerServer.Objects;

public class TransactionMethods {

    private static long coinAmountOnBuy[] = { 30000000, 50000000, 100000000, 200000000, 500000000, 1000000000 };
    private static long coinPriceOnBuy[] = { 100, 150, 300, 600, 1480, 2950 };

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

            price = 26 * ( coinAmount / 10000000 );
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
