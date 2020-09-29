package edu.umd.cs.eclipse.courseProjectManager;

import java.util.*;

public class Event
{
    private Date date;
    private String message;
    private Throwable exception;
    private boolean isError;
    
    public Event(final String message) {
        this(message, null);
    }
    
    public Event(final String message, final Throwable exception) {
        this.date = new Date();
        this.message = message;
        this.exception = exception;
    }
    
    public void setIsError(final boolean isError) {
        this.isError = isError;
    }
    
    public boolean isError() {
        return this.isError;
    }
    
    public Date getDate() {
        return this.date;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public Throwable getException() {
        return this.exception;
    }
}
