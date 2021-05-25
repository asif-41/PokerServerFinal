package com.example.PokerServer.controller;

import com.example.PokerServer.Connection.Server;
import com.example.PokerServer.Objects.TransactionMethods;
import com.example.PokerServer.Objects.TransactionNumber;
import com.example.PokerServer.PokerServerApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

@Controller
@RequestMapping("/database")
public class WebpagesController {

    private boolean authorizeAdmin(String username, String password){

        if(username == null || password == null) return false;

        if(username.equals(PokerServerApplication.getUsername()) && password.equals(PokerServerApplication.getPassword())) return true;
        else return false;
    }

    private boolean authorizeAdmin2(String username, String password){

        if(username == null || password == null) return false;

        if(username.equals(PokerServerApplication.getEditUsername()) && password.equals(PokerServerApplication.getEditPassword())) return true;
        else return false;

    }



    @RequestMapping("/forbidden")
    public String showForbidden(){

        return "Forbidden";
    }





    //ALL TABLE VIEWS
    @RequestMapping("/showAccounts")
    public String showAccounts(@RequestParam(required = false) String username, @RequestParam(required = false) String password, @RequestParam(required = false) String page, Model model){

        int Page;

        if(page == null) Page = 1;
        else {
            try{
                Page = Integer.parseInt(page);
            }catch (Exception e){
                return "Forbidden";
            }
        }

        if(Page < 1) Page = 1;

        String ret = "";

        if(authorizeAdmin(username, password)){

            model.addAttribute("accountList", Server.pokerServer.getDb().getAccounts(Page));
            model.addAttribute("pageNo", Page);
            ret += "Accounts";
        }
        else ret += "Forbidden";

        return ret;
    }

    @RequestMapping(value = "/editAccount", method = RequestMethod.POST)
    public RedirectView editAccount(@RequestBody String D){

        String data = D;

        try {
            data = java.net.URLDecoder.decode(D, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {

        }

        String[] Data = data.split("&");
        Hashtable map = new Hashtable<String, String>();

        for(String d : Data){
            String[] temp = d.split("=");

            String key, value;
            key = temp[0];
            if(temp.length < 2) value = "";
            else value = temp[1];

            map.put(key, value);
        }
        String username = (String) map.get("usernameAdmin");
        String password = (String) map.get("passwordAdmin");

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){

            Server.pokerServer.getDb().editAccount(map);
            redirectView.setUrl("showAccounts?username=" + PokerServerApplication.getUsername() + "&password=" + PokerServerApplication.getPassword() + "&page=" + map.get("page"));
        }
        else {
            redirectView.setUrl("forbidden");
        }

        return redirectView;
    }

    @RequestMapping(value = "/removeAccount", method = RequestMethod.POST)
    public RedirectView removeAccount(@RequestParam int id, @RequestParam(required = false) String username,
                                      @RequestParam(required = false) String password, @RequestParam(required = false) String Page){

        int page;

        if(Page == null) page = 1;
        else page = Integer.parseInt(Page);

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){
            Server.pokerServer.getDb().removeAccount(id);
            redirectView.setUrl("showAccounts?username=" + PokerServerApplication.getUsername() + "&password=" + PokerServerApplication.getPassword() + "&page=" + page);
        }
        else {
            redirectView.setUrl("forbidden");
        }

        return redirectView;
    }




    @RequestMapping("/showPendingTransactions")
    public String showPendingTransactions(@RequestParam(required = false) String username, @RequestParam(required = false) String password, @RequestParam(required = false) String page, Model model){

        int Page;

        if(page == null) Page = 1;
        else {
            try{
                Page = Integer.parseInt(page);
            }catch (Exception e){
                return "Forbidden";
            }
        }

        if(Page < 1) Page = 1;

        String ret = "";

        if(authorizeAdmin(username, password)){

            model.addAttribute("pendingTransactionsList", Server.pokerServer.getDb().getPendingTransactions(Page));
            model.addAttribute("pageNo", Page);
            ret += "PendingTransactions";
        }
        else ret += "Forbidden";

        return ret;
    }


    @RequestMapping(value = "/editPendingTransactions", method = RequestMethod.POST)
    public RedirectView editPendingTransactions(@RequestBody String D){

        String data = D;

        try {
            data = java.net.URLDecoder.decode(D, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {

        }

        int cnt = 0;

        String[] Data = data.split("&");
        Hashtable map = new Hashtable<String, String>();

        for(String d : Data){
            String[] temp = d.split("=");

            String key, value;
            key = temp[0];
            if(temp.length < 2) value = "";
            else value = temp[1];

            if(key.equals("id")){
                key += cnt;
                cnt++;
            }

            map.put(key, value);
        }
        map.put("count", String.valueOf(cnt));

        String username = (String) map.get("username");
        String password = (String) map.get("password");

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){

            Server.pokerServer.getDb().editPendingTransactions(map);
            redirectView.setUrl("showPendingTransactions?username=" + PokerServerApplication.getUsername() + "&password=" + PokerServerApplication.getPassword() + "&page=" + map.get("pageNo"));
        }
        else {
            redirectView.setUrl("forbidden");
        }

        return redirectView;
    }






    @RequestMapping("/showPendingRefunds")
    public String showPendingRefunds(@RequestParam(required = false) String username, @RequestParam(required = false) String password, @RequestParam(required = false) String page, Model model){

        int Page;

        if(page == null) Page = 1;
        else {
            try{
                Page = Integer.parseInt(page);
            }catch (Exception e){
                return "Forbidden";
            }
        }

        if(Page < 1) Page = 1;

        String ret = "";

        if(authorizeAdmin(username, password)){

            model.addAttribute("pendingRefundsList", Server.pokerServer.getDb().getPendingRefunds(Page));
            model.addAttribute("pageNo", Page);
            ret += "PendingRefunds";
        }
        else ret += "Forbidden";

        return ret;
    }

    @RequestMapping(value = "/editPendingRefunds", method = RequestMethod.POST)
    public RedirectView editPendingRefunds(@RequestBody String D){

        String data = D;

        try {
            data = java.net.URLDecoder.decode(D, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {

        }

        int cnt = 0;

        String[] Data = data.split("&");
        Hashtable map = new Hashtable<String, String>();

        for(String d : Data){
            String[] temp = d.split("=");

            String key, value;
            key = temp[0];
            if(temp.length < 2) value = "";
            else value = temp[1];

            if(key.equals("id")){
                key += cnt;
                cnt++;
            }

            map.put(key, value);
        }
        map.put("count", String.valueOf(cnt));
        String username = (String) map.get("username");
        String password = (String) map.get("password");

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){

            Server.pokerServer.getDb().editPendingRefunds(map);
            redirectView.setUrl("showPendingRefunds?username=" + PokerServerApplication.getUsername() + "&password=" + PokerServerApplication.getPassword() + "&page=" + map.get("pageNo"));
        }
        else {
            redirectView.setUrl("forbidden");
        }

        return redirectView;
    }







    @RequestMapping("/showTransactions")
    public String showTransactions(@RequestParam(required = false) String username, @RequestParam(required = false) String password, @RequestParam(required = false) String page, Model model){

        int Page;

        if(page == null) Page = 1;
        else {
            try{
                Page = Integer.parseInt(page);
            }catch (Exception e){
                return "Forbidden";
            }
        }

        if(Page < 1) Page = 1;

        String ret = "";

        if(authorizeAdmin(username, password)){

            model.addAttribute("transactionList", Server.pokerServer.getDb().getTransactions(Page));
            model.addAttribute("pageNo", Page);
            ret += "Transactions";
        }
        else ret += "Forbidden";

        return ret;
    }

    @RequestMapping("/showRefunds")
    public String showRefunds(@RequestParam(required = false) String username, @RequestParam(required = false) String password, @RequestParam(required = false) String page, Model model){

        int Page;

        if(page == null) Page = 1;
        else {
            try{
                Page = Integer.parseInt(page);
            }catch (Exception e){
                return "Forbidden";
            }
        }

        if(Page < 1) Page = 1;

        String ret = "";

        if(authorizeAdmin(username, password)){

            model.addAttribute("refundsList", Server.pokerServer.getDb().getRefunds(Page));
            model.addAttribute("pageNo", Page);
            ret += "Refunds";
        }
        else ret += "Forbidden";

        return ret;
    }






    private int digitCount(int x){

        int d = 0;
        int temp = x;
        do{
            temp /= 10;
            d += 1;
        }while(temp != 0);

        return d;
    }

    @RequestMapping(value = "/terminal")
    public String terminal(@RequestParam(required = false) String username, @RequestParam(required = false) String password, Model model){

        String ret;

        if(authorizeAdmin(username, password)){

            ArrayList data = new ArrayList<String>();

            try{
                File f = new File("./././././Files/terminal.out");
                Scanner sc = new Scanner(f);

                int cnt = 0;
                while(sc.hasNextLine()){
                    cnt++;
                    sc.nextLine();
                }

                int d = digitCount(cnt);

                sc = new Scanner(f);
                for(int i=1; i<=cnt; i++) {

                    String t = "";
                    int dd = digitCount(i);

                    t += "0".repeat(d - dd) + i;
                    t += ".   " + sc.nextLine();
                    data.add(t);
                }

            }catch (Exception e){
                data.add( "File not found......" + e);
            }

            model.addAttribute("data", data);
            model.addAttribute("count", data.size());

            ret = "Terminal";
        }
        else ret = "Forbidden";
        return ret;
    }




    @RequestMapping(value = "/dataEdit")
    public String dataEdit(@RequestParam(required = false) String username, @RequestParam(required = false) String password, Model model){

        String ret;

        if(authorizeAdmin(username, password)){

            int boardTypeCount = Server.pokerServer.getBoardTypeCount();
            long[] minCallValue = Server.pokerServer.getMinCallValue();
            long[] minEntryValue = Server.pokerServer.getMinEntryValue();
            long[] maxEntryValue = Server.pokerServer.getMaxEntryValue();
            long[] mcr = Server.pokerServer.getMcr();

            double pricePerCrore = TransactionMethods.getCoinPricePerCrore();
            double[] coinPriceOnBuy = TransactionMethods.getCoinPriceOnBuy();
            long[] coinAmountOnBuy = TransactionMethods.getCoinAmountOnBuy();

            ArrayList<TransactionNumber> transactionNumbers = Server.pokerServer.getTransactionNumbers();

            int maxPendingReq = Server.pokerServer.getMaxPendingReq();
            long initialCoin = Server.pokerServer.getInitialCoin();
            int dailyCoinVideoCount = Server.pokerServer.getDailyCoinVideoCount();
            long eachVideoCoin = Server.pokerServer.getEachVideoCoin();
            long FreeLoginCoin = Server.pokerServer.getFreeLoginCoin();

            model.addAttribute("boardTypeCount", boardTypeCount);
            model.addAttribute("minCallValue", minCallValue);
            model.addAttribute("minEntryValue", minEntryValue);
            model.addAttribute("maxEntryValue", maxEntryValue);
            model.addAttribute("mcr", mcr);
            model.addAttribute("pricePerCrore", pricePerCrore);
            model.addAttribute("coinPriceOnBuy", coinPriceOnBuy);
            model.addAttribute("coinAmountOnBuy", coinAmountOnBuy);
            model.addAttribute("transactionNumbers", transactionNumbers);
            model.addAttribute("maxPendingReq", maxPendingReq);
            model.addAttribute("initialCoin", initialCoin);
            model.addAttribute("dailyCoinVideoCount", dailyCoinVideoCount);
            model.addAttribute("eachVideoCoin", eachVideoCoin);
            model.addAttribute("FreeLoginCoin", FreeLoginCoin);

            ret = "DataEdit";
        }
        else ret = "Forbidden";
        return ret;
    }

    @RequestMapping(value = "/editDataLink", method = RequestMethod.POST)
    public RedirectView editDataResponse(@RequestBody String D){

        String data = D;

        try {
            data = java.net.URLDecoder.decode(D, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {

        }

        String[] Data = data.split("&");
        Hashtable map = new Hashtable<String, String>();

        int buyPackageCount = 0;
        int boardDataEditCount = 0;
        int transactionEditCount = 0;

        for(String d : Data){
            String[] temp = d.split("=");

            String key, value;
            key = temp[0];
            if(temp.length < 2) value = "";
            else value = temp[1];

            if(key.equals("buyPackageId")){
                key += buyPackageCount;
                buyPackageCount++;
            }
            else if(key.equals("transactionId")){
                key += transactionEditCount;
                transactionEditCount++;
            }
            else if(key.equals("boardLoc")){
                key += boardDataEditCount;
                boardDataEditCount++;
            }


            map.put(key, value);
        }
        String username = (String) map.get("username");
        String password = (String) map.get("password");

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){

            Server.pokerServer.editBasicData(map);
            redirectView.setUrl("dataEdit?username=" + PokerServerApplication.getUsername() + "&password=" + PokerServerApplication.getPassword());
        }
        else redirectView.setUrl("forbidden");

        return redirectView;
    }



    @RequestMapping(value = "/addTransaction")
    public String addTransaction(@RequestParam(required = false) String username, @RequestParam(required = false) String password, Model model){

        if(authorizeAdmin(username, password)){
            model.addAttribute("id", Server.pokerServer.getTransactionNumbers().size());
            return "AddTransaction";
        }
        else return "Forbidden";
    }

    @RequestMapping(value = "/addTransactionPost", method = RequestMethod.POST)
    public RedirectView addTransactionResponse(@RequestBody String D){

        String data = D;

        try {
            data = java.net.URLDecoder.decode(D, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {

        }

        String[] Data = data.split("&");
        Hashtable map = new Hashtable<String, String>();

        for(String d : Data){
            String[] temp = d.split("=");

            String key, value;
            key = temp[0];
            if(temp.length < 2) value = "";
            else value = temp[1];
            map.put(key, value);
        }

        String username = (String) map.get("username");
        String password = (String) map.get("password");

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){

            String type = (String) map.get("type");
            String number = (String) map.get("number");

            Server.pokerServer.addTransaction(type, number);
            redirectView.setUrl("dataEdit?username=" + PokerServerApplication.getUsername() + "&password=" + PokerServerApplication.getPassword());
        }
        else redirectView.setUrl("forbidden");

        return redirectView;
    }



    @RequestMapping(value = "/forceLogout", method = RequestMethod.POST)
    public RedirectView forceLogout(@RequestBody String D){

        String data = D;

        try {
            data = java.net.URLDecoder.decode(D, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {

        }

        String[] Data = data.split("&");
        Hashtable map = new Hashtable<String, String>();

        for(String d : Data){
            String[] temp = d.split("=");

            String key, value;
            key = temp[0];
            if(temp.length < 2) value = "";
            else value = temp[1];

            map.put(key, value);
        }
        String username = (String) map.get("username");
        String password = (String) map.get("password");

        RedirectView redirectView = new RedirectView();

        if(authorizeAdmin2(username, password)){

            Server.pokerServer.forceLogoutAll();

            redirectView.setUrl("showAccounts?username=" + PokerServerApplication.getUsername() +
                    "&password=" + PokerServerApplication.getPassword() + "&page=" +
                    Integer.parseInt((String) map.get("page")));
        }
        else redirectView.setUrl("forbidden");

        return redirectView;
    }






}
