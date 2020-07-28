// This class is object representation of data response to clined
// side on /auth endpoint

package com.google.sps.data;


public class AuthData {
    
    private final boolean isUserLoggedIn;
    private final String nickname;
    private final String loginUrl;
    private final String logoutUrl;

    public AuthData(boolean isUserLoggedIn, String nickname, String loginUrl, String logoutUrl){
        this.isUserLoggedIn = isUserLoggedIn;
        this.nickname = nickname;
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
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
}
