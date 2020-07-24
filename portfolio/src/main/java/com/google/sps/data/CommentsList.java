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
    private long oldestSent = Long.MAX_VALUE;
    private long newestSent = 0;
    private String currentlySent;


    public CommentsList(){
        this(5);
    }

    public CommentsList(int chunkSize){
        datastore = DatastoreServiceFactory.getDatastoreService();
        this.chunkSize = chunkSize;
    }

    public String newestChunk(){
        this.oldestSent = Long.MAX_VALUE;
        this.newestSent = 0;
        return nextChunk();
    }

    public String nextChunk(){
        Filter olderThanFilter = new FilterPredicate("timestamp", FilterOperator.LESS_THAN, oldestSent);
        Query query = new Query("Comment").setFilter(olderThanFilter).addSort("timestamp", SortDirection.DESCENDING);
        Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(this.chunkSize));
        ArrayList<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results) {
            comments.add(entityToComment(entity));
        }
        
        Collections.sort(comments, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        
        if(comments.size() == this.chunkSize){ //solves the case when for example chunk size is 5 and there are only 3 comments left
            newestSent = comments.get(comments.size()-1).getTimestamp();
            oldestSent = comments.get(0).getTimestamp();
        }else if(comments.size() == 0){ //solves the case when number of comments is multible of chunk size
            return this.currentlySent;
        }

        
        this.currentlySent = toJsonArray(comments);
        return this.currentlySent;
    }

    public String prevChunk(){
        Filter newerThanFilter = new FilterPredicate("timestamp", FilterOperator.GREATER_THAN, newestSent);
        Query query = new Query("Comment").setFilter(newerThanFilter).addSort("timestamp", SortDirection.ASCENDING);
        Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(this.chunkSize));
        ArrayList<Comment> comments = new ArrayList<Comment>();
        for (Entity entity : results) {
            comments.add(entityToComment(entity));
        }
        if(comments.size() < chunkSize){ //if if was not possible to download full chunk than it means we are currently on the top
            return newestChunk();
        }else{
            Collections.sort(comments, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
            newestSent = comments.get(comments.size()-1).getTimestamp();
            oldestSent = comments.get(0).getTimestamp();
            this.currentlySent = toJsonArray(comments);
            return this.currentlySent;
        }
    }

    public String currentChunk(){
        return this.currentlySent;
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
