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

import java.util.Date;

public final class Comment {
    
    private final String author; //later class Author can be introduced - to discuss with hosts
    private final Date date;
    private final String comment;



    public Comment(String author, String comment, Date publishDate){
        this.author = author;
        this.comment = comment;
        this.date = publishDate;
    }

    public Comment(String author, String comment){
        this.author = author;
        this.comment = comment;
        this.date = new Date();
    }

    public String getAuthor(){
        return this.author;
    }

    public String getComment(){
        return this.comment;
    }

    public Date getDate(){
        return this.date;
    }
}