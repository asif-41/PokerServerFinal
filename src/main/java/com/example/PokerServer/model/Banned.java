package com.example.PokerServer.model;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "banned_accounts")
public class Banned implements Serializable {

    @Id
    @GeneratedValue(generator = "bn_generator")
    @SequenceGenerator(
            name = "bn_generator",
            sequenceName = "bn_sequence",
            initialValue = 1
    )
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "account_key")
    private String account_key;

    @Column(name = "login_method")
    private  String login_method;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAccount_key() {
        return account_key;
    }

    public String getLogin_method() {
        return login_method;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAccount_key(String account_key) {
        this.account_key = account_key;
    }

    public void setLogin_method(String login_method) {
        this.login_method = login_method;
    }

    public String printBannedAccount(){

        String ret = "";

        ret += "Id: " + id + "\n";
        ret += "Username: " + username + "\n";
        ret += "Login method: " + login_method + "\n";
        ret += "Account key: " + account_key + "\n";

        return ret;
    }
}
