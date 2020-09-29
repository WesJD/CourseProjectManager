package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.swt.widgets.*;
import java.lang.reflect.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.graphics.*;

public class Dialogs
{
    public static void errorDialog(final Shell parent, final String title, final String message, Throwable e) {
        AutoCVSPlugin.getPlugin().getEventLog().logError(String.valueOf(title) + ": " + message, e);
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException)e).getTargetException();
        }
        Debug.print("Error dialog:");
        Debug.print("  title: " + title);
        Debug.print("  message: " + message);
        Debug.print("  exception:", e);
        IStatus status;
        if (e instanceof CoreException) {
            final IStatus exceptionStatus = ((CoreException)e).getStatus();
            if (exceptionStatus.getSeverity() != 4) {
                status = exceptionStatus;
            }
            else {
                status = (IStatus)new IStatus() {
                    public IStatus[] getChildren() {
                        return exceptionStatus.getChildren();
                    }
                    
                    public int getCode() {
                        return exceptionStatus.getCode();
                    }
                    
                    public Throwable getException() {
                        return exceptionStatus.getException();
                    }
                    
                    public String getMessage() {
                        return exceptionStatus.getMessage();
                    }
                    
                    public String getPlugin() {
                        return exceptionStatus.getPlugin();
                    }
                    
                    public int getSeverity() {
                        return 2;
                    }
                    
                    public boolean isMultiStatus() {
                        return exceptionStatus.isMultiStatus();
                    }
                    
                    public boolean isOK() {
                        return exceptionStatus.isOK();
                    }
                    
                    public boolean matches(final int severityMask) {
                        return exceptionStatus.matches(severityMask);
                    }
                };
            }
        }
        else {
            status = (IStatus)new Status(4, AutoCVSPlugin.getPlugin().getDescriptor().getUniqueIdentifier(), 0, message, e);
        }
        AutoCVSPlugin.getPlugin().getLog().log(status);
        new ErrorDialog(parent, title, message, status, 15).open();
    }
    
    public static void errorDialog(final Shell parent, final String title, final String message, final String reason, final int severity) {
        AutoCVSPlugin.getPlugin().getEventLog().logError(String.valueOf(title) + ": " + message + ":" + reason);
        Debug.print("Error dialog:");
        Debug.print("  title: " + title);
        Debug.print("  message: " + message);
        Debug.print("  reason:" + reason);
        final IStatus status = (IStatus)new Status(severity, AutoCVSPlugin.getPlugin().getId(), 0, reason, (Throwable)null);
        AutoCVSPlugin.getPlugin().getLog().log(status);
        new ErrorDialog(parent, title, message, status, 15).open();
    }
    
    public static void okDialog(final Shell parent, final String title, final String message) {
        AutoCVSPlugin.getPlugin().getEventLog().logMessage(String.valueOf(title) + ": " + message);
        new MessageDialog(parent, title, (Image)null, message, 0, new String[] { "OK" }, 0).open();
    }
}
