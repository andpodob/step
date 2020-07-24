package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class CommentsList {
    private int chunkSize;
    private DatastoreService datastore;

    public CommentsList(){
        this(5);
    }

    public CommentsList(int chunkSize){
        datastore = DatastoreServiceFactory.getDatastoreService();
        this.chunkSize = chunkSize;
    }

    public String newestChunk(){
        return nextChunk(Long.MAX_VALUE);
    }

    public String nextChunk(long oldestSent){
        Filter olderThanFilter = new FilterPredicate("timestamp", FilterOperator.LESS_THAN, oldestSent);
        Query query = new Query("Comment").setFilter(olderThanFilter).addSort("timestamp", SortDirection.DESCENDING);
        Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(this.chunkSize));
        ArrayList<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results) {
            comments.add(entityToComment(entity));
        }
        
        Collections.sort(comments, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        return toJsonArray(comments);
    }

    public String prevChunk(long newestSent){
        Filter newerThanFilter = new FilterPredicate("timestamp", FilterOperator.GREATER_THAN, newestSent);
        Query query = new Query("Comment").setFilter(newerThanFilter).addSort("timestamp", SortDirection.ASCENDING);
        Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(this.chunkSize));
        ArrayList<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results) {
            comments.add(entityToComment(entity));
        }

        return toJsonArray(comments);
    }

    public void add(Comment comment){
        datastore.put(comment.asDatastoreEntity());
    }

    public Comment entityToComment(Entity entity){
        String comment = (String)entity.getProperty("comment");
        String userName = (String)entity.getProperty("user-name");
        Long timestamp = (Long)entity.getProperty("timestamp");
        return new Comment(userName, comment, timestamp);
    }
    
    private String toJsonArray(ArrayList<Comment> comments){
        Gson gson = new Gson();
        Type typeOfComments = new TypeToken<ArrayList<Comment>>(){}.getType();
        
        String json = gson.toJson(comments, typeOfComments);
        return json;
    }
}
