package com.google.sps.data;

import java.util.ArrayList;
import java.util.Collections;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.Translate;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class CommentsList {
    private final DatastoreService datastore;

    private ArrayList<Comment> comments;

    public CommentsList() {
        datastore = DatastoreServiceFactory.getDatastoreService();
    }

    public CommentsList newestChunk(final int chunkSize) {
        return nextChunk(Long.MAX_VALUE, chunkSize);
    }

    public CommentsList nextChunk(final long oldestSent, final int chunkSize) {
        final Filter olderThanFilter = new FilterPredicate("timestamp", FilterOperator.LESS_THAN, oldestSent);
        final Query query = new Query("Comment").setFilter(olderThanFilter).addSort("timestamp",
                SortDirection.DESCENDING);
        final Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunkSize));
        comments = new ArrayList<Comment>();
        for (final Entity entity : results) {
            comments.add(entityToComment(entity));
        }

        Collections.sort(comments, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        return this;
    }

    public CommentsList prevChunk(final long newestSent, final int chunkSize) {
        final Filter newerThanFilter = new FilterPredicate("timestamp", FilterOperator.GREATER_THAN, newestSent);
        final Query query = new Query("Comment").setFilter(newerThanFilter).addSort("timestamp",
                SortDirection.ASCENDING);
        final Iterable<Entity> results = datastore.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunkSize));
        comments = new ArrayList<Comment>();
        for (final Entity entity : results) {
            comments.add(entityToComment(entity));
        }

        return this;
    }

    public void add(final Comment comment) {
        datastore.put(comment.asDatastoreEntity());
    }

    public Comment entityToComment(final Entity entity) {
        final String comment = (String) entity.getProperty("comment");
        final String userName = (String) entity.getProperty("user-name");
        final Long timestamp = (Long) entity.getProperty("timestamp");
        return new Comment(userName, comment, timestamp);
    }

    public CommentsList translateCommetns(String targetLanguage) {
        Translate translate = TranslateOptions.newBuilder().setProjectId("apodob-step-2020").setQuotaProjectId("apodob-step-2020").build().getService();      
        Translation translation = translate.translate("dzien dobry", Translate.TranslateOption.targetLanguage(targetLanguage));
        System.out.println(translation.getTranslatedText());
        return this;
    }
    
    public String asJson(String targetLanguage) {
        ArrayList<Comment> translatedComments = new ArrayList<Comment>();
        final Gson gson = new Gson();
        final Type typeOfComments = new TypeToken<ArrayList<Comment>>(){}.getType();
        if(targetLanguage.equals("org")){
            return gson.toJson(comments, typeOfComments);
        } else{
            Translate translate = TranslateOptions.newBuilder().setProjectId("apodob-step-2020").setQuotaProjectId("apodob-step-2020").build().getService();      
            for(Comment comment : comments){
                translatedComments.add(new Comment(comment.getAuthor(), 
                                translate.translate(comment.getComment(),Translate.TranslateOption.targetLanguage(targetLanguage)).getTranslatedText(),
                                comment.getTimestamp()));
            }
            return gson.toJson(translatedComments, typeOfComments);
        }   
    }
}
