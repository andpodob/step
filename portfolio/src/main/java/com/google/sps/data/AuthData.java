// This class is object representation of data response to clinet
// side on /auth endpoint

package com.google.sps.data;


public class AuthData {
    
    private final boolean isUserLoggedIn;
    private final String nickname;
    private final String loginUrl;
    private final String logoutUrl;
    private final String userId;

    public AuthData(boolean isUserLoggedIn, String nickname, String loginUrl, String logoutUrl, String userId){
        this.isUserLoggedIn = isUserLoggedIn;
        this.nickname = nickname;
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
        this.userId = userId;
    }

    public boolean getIsUserLoggedIn(){
        return this.isUserLoggedIn;
    }

    public String getNickname(){
        return this.nickname;
    }

    public String getLoginUrl(){
        return this.loginUrl;
    }

    public String getLogoutUrl(){
        return this.logoutUrl;
    }

    public String getUserId(){
        return this.userId;
    }
}
