package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collections;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class CommentsList {
    private ArrayList<Comment> comments;
    
    public CommentsList(){
        comments = new ArrayList<Comment>();
    }

    public CommentsList(int maxComments){
        this();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(maxComments));
        for (Entity entity : results) {
            comments.add(entityToComment(entity));
        }
    }

    public void add(Comment comment){
        comments.add(comment);
    }

    //to test
    public Comment entityToComment(Entity entity){
        String comment = (String)entity.getProperty("comment");
        String userName = (String)entity.getProperty("user-name");
        Long timestamp = (Long)entity.getProperty("timestamp");
        return new Comment(userName, comment, timestamp);
    }
    
    //to test
    public String asJsonArray(){ 
        Gson gson = new Gson();
        Type typeOfComments = new TypeToken<ArrayList<Comment>>(){}.getType();
        Collections.sort(comments, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        String json = gson.toJson(comments, typeOfComments);
        return json;
    }
}