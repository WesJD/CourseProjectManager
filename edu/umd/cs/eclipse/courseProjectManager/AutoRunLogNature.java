package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class AutoRunLogNature implements IProjectNature
{
    private IProject project;
    
    public void configure() throws CoreException {
        Debug.print("Setting AutoRunLogNature");
    }
    
    public void deconfigure() throws CoreException {
        Debug.print("Un-setting AutoRunLogNature");
    }
    
    public IProject getProject() {
        return this.project;
    }
    
    public void setProject(final IProject project) {
        this.project = project;
    }
}
