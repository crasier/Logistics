package com.sdeport.logistics.driver.bean;

public class User {
    private static User user;

    private String account;
    private String password;
    private String nick;
    private String token;
    private Role role;

    private Order collectedOrder;//正在进行

    private static class LazyHolder {
        public static final User INSTANCE = new User();
    }

    public static User getUser() {
        return LazyHolder.INSTANCE;
    }

    private User() {

    }

    public static void setUser(User user) {
        User.user = user;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Order getCollectedOrder() {
        return collectedOrder;
    }

    public void setCollectedOrder(Order collectedOrder) {
        this.collectedOrder = collectedOrder;
    }
}
