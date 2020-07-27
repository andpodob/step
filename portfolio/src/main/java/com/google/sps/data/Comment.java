// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;


public final class Comment {
    
    private final String author;
    private final Long timestamp;
    private final String comment;

    public Comment(String author, String comment, long timestamp){
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public Comment(String author, String comment){
        this(author, comment, System.currentTimeMillis());
    }

    public String getAuthor(){
        return this.author;
    }

    public String getComment(){
        return this.comment;
    }

    public Long getTimestamp(){
        return this.timestamp;
    }

    public Entity asDatastoreEntity(){
        Entity entity = new Entity("Comment");
        entity.setProperty("user-name", this.author);
        entity.setProperty("timestamp", this.timestamp);
        entity.setProperty("comment", this.comment);

        return entity;
    }
}
