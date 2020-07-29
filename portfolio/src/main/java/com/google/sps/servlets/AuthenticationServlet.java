package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.AuthData;

@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;");
        UserService userService = UserServiceFactory.getUserService();
        boolean isUserLoggedIn = true;
        String userId = null;
        String nickname = null;
        String logoutUrl = null;
        String loginUrl = null;
        if(userService.isUserLoggedIn()){
            isUserLoggedIn = true;
            userId = userService.getCurrentUser().getUserId();
            nickname = getUserNickname(userId);
            logoutUrl = userService.createLogoutURL("/");
        } else {
            String urlToRedirectToAfterUserLogsIn = "/";
            isUserLoggedIn = false;
            loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
        }
        AuthData authData = new AuthData(isUserLoggedIn, nickname, loginUrl, logoutUrl, userId);
        String authDataJson = toJson(authData);
        response.getWriter().println(authDataJson);
    }

    private String getUserNickname(String id) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query =
            new Query("UserInfo")
                .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
        PreparedQuery results = datastore.prepare(query);
        Entity entity = results.asSingleEntity();
        if (entity == null) {
          return null;
        }
        String nickname = (String) entity.getProperty("nickname");
        return nickname;
    }

    private String toJson(AuthData authData){
        Gson gson = new Gson();
        Type typeOfAuthData = new TypeToken<AuthData>(){}.getType();
        return gson.toJson(authData, typeOfAuthData);
    }
}
