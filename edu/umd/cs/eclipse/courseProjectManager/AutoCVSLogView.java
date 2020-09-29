package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.ui.part.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

public class AutoCVSLogView extends ViewPart
{
    private Text text;
    
    public void createPartControl(final Composite parent) {
        (this.text = new Text(parent, 2816)).setEditable(false);
        this.text.append("Course Project Manager messages\n");
        final List<Event> oldEvents = AutoCVSPlugin.getPlugin().getEventLog().getAllEvents();
        for (final Event element : oldEvents) {
            this.appendEvent(element);
        }
        AutoCVSPlugin.getPlugin().getEventLog().purge();
        final EventLog.Listener listener = new EventLog.Listener() {
            public void logEvent(final Event event) {
                Display.getDefault().asyncExec((Runnable)new Runnable() {
                    public void run() {
                        AutoCVSLogView.this.appendEvent(event);
                        AutoCVSPlugin.getPlugin().getEventLog().purge();
                    }
                });
            }
        };
        AutoCVSPlugin.getPlugin().getEventLog().addListener(listener);
    }
    
    private void appendEvent(final Event event) {
        this.text.append("[" + event.getDate().toString() + "] ");
        if (event.isError()) {
            this.text.append("Error: ");
        }
        this.text.append(String.valueOf(event.getMessage()) + "\n");
        final Throwable e = event.getException();
        if (e != null) {
            this.text.append(e.toString());
            final StackTraceElement[] traceList = e.getStackTrace();
            for (int i = 0; i < traceList.length; ++i) {
                this.text.append("\t" + traceList[i].toString() + "\n");
            }
        }
    }
    
    public void setFocus() {
    }
}
