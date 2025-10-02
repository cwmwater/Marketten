package com.example.Marketten.exception;

public class CustomJWTException  extends  RuntimeException{
    public CustomJWTException(String message) {
        super(message);
    }
}
