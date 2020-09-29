package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.core.resources.*;

public class AutoSyncNature implements IProjectNature
{
    private IProject project;
    
    public void configure() {
    }
    
    public void deconfigure() {
    }
    
    public IProject getProject() {
        return this.project;
    }
    
    public void setProject(final IProject project) {
        this.project = project;
    }
}
