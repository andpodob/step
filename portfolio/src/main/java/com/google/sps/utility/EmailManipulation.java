package com.google.sps.utility;

public class EmailManipulation {
    public static String getDomain(String email){
        int index = email.indexOf('@');
        try {
            return email.substring(index+1, email.length());
        }catch(IndexOutOfBoundsException e){
            return "";
        }
    }
}