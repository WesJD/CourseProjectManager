package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.core.resources.*;

public class AutoCVSNature implements IProjectNature
{
    private IProject project;
    
    public AutoCVSNature() {
        Debug.print("AutoCVSNature created");
    }
    
    public void configure() {
        Debug.print("Setting AutoCVS nature for project");
    }
    
    public void deconfigure() {
        Debug.print("Unsetting AutoCVS nature for project");
    }
    
    public IProject getProject() {
        return this.project;
    }
    
    public void setProject(final IProject project) {
        this.project = project;
    }
}
