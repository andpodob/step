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

import java.sql.Timestamp;
import java.util.Date;

public final class Comment {
    
    private final String author; //later class Author can be introduced - to discuss with hosts
    private final Timestamp timestamp;
    private final String comment;



    public Comment(String author, String comment, Date publishDate){
        this.author = author;
        this.comment = comment;
        this.timestamp = new Timestamp(publishDate.getTime());
    }

    public Comment(String author, String comment){
        this.author = author;
        this.comment = comment;
        //obtaining timestamp
        Date date = new Date();
        this.timestamp = new Timestamp(date.getTime());
    }

    public String getAuthor(){
        return this.author;
    }

    public String getComment(){
        return this.comment;
    }

    public Timestamp getTimestamp(){
        return this.timestamp;
    }
}