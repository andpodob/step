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
import com.google.sps.data.Login;
import com.google.sps.utility.EmailManipulation;

@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;");
        UserService userService = UserServiceFactory.getUserService();
        String isUserLoggedIn = Login.LOGGED_OUT.name();
        String userId = null;
        String nickname = null;
        String logoutUrl = null;
        String loginUrl = null;
        String email = null;
        if(userService.isUserLoggedIn() && EmailManipulation.getDomain(userService.getCurrentUser().getEmail()).equals("google.com")){
            isUserLoggedIn = Login.SUCCESSFUL.name();
            userId = userService.getCurrentUser().getUserId();
            email = userService.getCurrentUser().getEmail();
            nickname = getUserNickname(userId);
            logoutUrl = userService.createLogoutURL("/");
            updateUserEmail(userId, email);
        } else {
            if(userService.isUserLoggedIn() && !EmailManipulation.getDomain(userService.getCurrentUser().getEmail()).equals("google.com")){
                System.out.println(userService.getCurrentUser().getAuthDomain());
                logoutUrl = userService.createLogoutURL("/");
                isUserLoggedIn = Login.BAD_DOMAIN.name();
            }else{
                loginUrl = userService.createLoginURL("/");
                isUserLoggedIn = Login.LOGGED_OUT.name();
            }
        }
        AuthData authData = new AuthData(isUserLoggedIn, nickname, loginUrl, logoutUrl, userId, email);
        String authDataJson = toJson(authData);
        response.getWriter().println(authDataJson);
    }

    private void updateUserEmail(String id, String email){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query =
            new Query("UserInfo")
                .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
        PreparedQuery results = datastore.prepare(query);
        Entity entity = results.asSingleEntity();
        if(entity != null){
            entity.setProperty("email", email);
        }
        else{
            entity = new Entity("UserInfo", id);
            entity.setProperty("id", id);
            entity.setProperty("email", email);
        }

        datastore.put(entity);
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
