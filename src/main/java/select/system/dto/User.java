package select.system.dto;

import java.io.Serializable;

/**
 * @author yeyuting
 * @create 2021/1/25
 */

public class User implements Serializable {

    private static final long serialVersionUID = 3529219554011221820L;

    int id ;
    String userName ;
    String password ;

    Double account ;


    String token ;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Double getAccount() {
        return account;
    }

    public void setAccount(Double account) {
        this.account = account;
    }
}
