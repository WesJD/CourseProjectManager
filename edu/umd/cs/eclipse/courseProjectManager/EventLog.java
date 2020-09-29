package edu.umd.cs.eclipse.courseProjectManager;

import java.util.*;

public class EventLog
{
    private List<Event> eventList;
    private List<Listener> listenerList;
    
    public EventLog() {
        this.eventList = new LinkedList<Event>();
        this.listenerList = new LinkedList<Listener>();
    }
    
    public synchronized void addListener(final Listener listener) {
        this.listenerList.add(listener);
    }
    
    public synchronized List<Event> getAllEvents() {
        final List<Event> result = new LinkedList<Event>();
        result.addAll(this.eventList);
        return result;
    }
    
    private void notifyListeners(final Event event) {
        for (final Listener element : this.listenerList) {
            element.logEvent(event);
        }
    }
    
    public synchronized void purge() {
        this.eventList.clear();
    }
    
    public void logEvent(final Event event) {
        this.eventList.add(event);
        this.notifyListeners(event);
        Debug.print(event.getMessage(), event.getException());
    }
    
    public synchronized void logError(final Event event) {
        event.setIsError(true);
        this.logEvent(event);
    }
    
    public void logMessage(final String message) {
        this.logEvent(new Event(message));
    }
    
    public void logError(final String message) {
        this.logError(new Event(message));
    }
    
    public void logError(final String message, final Throwable e) {
        this.logError(new Event(message, e));
    }
    
    public interface Listener
    {
        void logEvent(final Event p0);
    }
}
