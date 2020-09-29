package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.ui.plugin.*;
import org.osgi.framework.*;
import java.util.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.debug.core.*;

public class AutoCVSPlugin extends AbstractUIPlugin implements IStartup
{
    static final String ID = "edu.umd.cs.eclipse.courseProjectManager";
    static final String DISABLED = "disabled";
    static final String ENABLED = "enabled";
    public static final String AUTO_CVS_NATURE = "edu.umd.cs.eclipse.courseProjectManager.autoCVSNature";
    private boolean failedOperation;
    static final String SUBMITIGNORE = ".submitIgnore";
    static final String SUBMITINCLUDE = ".submitInclude";
    static final String SUBMITUSER = ".submitUser";
    static final String SUBMITPROJECT = ".submit";
    private EventLog eventLog;
    private ResourceBundle messageBundle;
    private static AutoCVSPlugin plugin;
    
    static boolean hasSubmitFile(final IProject project) {
        final IResource submitProjectFile = project.findMember(".submit");
        return submitProjectFile != null;
    }
    
    public EventLog getEventLog() {
        return this.eventLog;
    }
    
    public static String getMessage(final String key) {
        return getPlugin().messageBundle.getString(key);
    }
    
    public String getId() {
        return this.getBundle().getSymbolicName();
    }
    
    public String getVersion() {
        return this.getBundle().getHeaders().get("Bundle-Version");
    }
    
    public boolean hasFailedOperation() {
        return this.failedOperation;
    }
    
    public AutoCVSPlugin() {
        AutoCVSPlugin.plugin = this;
    }
    
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        Debug.print("Starting up at " + new Date());
        this.eventLog = new EventLog();
        this.messageBundle = ResourceBundle.getBundle("edu.umd.cs.eclipse.courseProjectManager.Messages");
        final DebugPlugin debugPlugin = DebugPlugin.getDefault();
        debugPlugin.getLaunchManager().addLaunchListener((ILaunchListener)new LaunchLogger(null));
        try {
            this.autoUpdateWorkspaceProjects();
        }
        catch (CoreException e) {
            this.eventLog.logError("Exception updating workspace projects", (Throwable)e);
        }
    }
    
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        AutoCVSPlugin.plugin = null;
    }
    
    public static AutoCVSPlugin getPlugin() {
        return AutoCVSPlugin.plugin;
    }
    
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
            final IWorkbenchWindow wwin = workbench.getActiveWorkbenchWindow();
            if (wwin == null) {
                Debug.print("Could not get handle of active workbench window");
            }
            return wwin;
        }
        return null;
    }
    
    public static ImageDescriptor getImageDescriptor(final String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("AutoCVSPlugin", path);
    }
    
    public void addAutoCVSNature(final IProject project) throws CoreException {
        this.addProjectNature(project, "edu.umd.cs.eclipse.courseProjectManager.autoCVSNature");
    }
    
    void removeAutoCVSNature(final IProject project) throws CoreException {
        this.removeProjectNature(project, "edu.umd.cs.eclipse.courseProjectManager.autoCVSNature");
    }
    
    private void removeProjectNature(final IProject project, final String natureId) throws CoreException {
        if (!hasProjectNature(project, natureId)) {
            return;
        }
        final IProjectDescription projectDescription = project.getDescription();
        final String[] ids = projectDescription.getNatureIds();
        final String[] updateIds = new String[ids.length - 1];
        int count = 0;
        for (int i = 0; i < ids.length; ++i) {
            if (!ids[i].equals(natureId)) {
                updateIds[count++] = ids[i];
            }
        }
        projectDescription.setNatureIds(updateIds);
        project.setDescription(projectDescription, (IProgressMonitor)null);
    }
    
    private void addProjectNature(final IProject project, final String natureId) throws CoreException {
        if (hasProjectNature(project, natureId)) {
            return;
        }
        final IProjectDescription projectDescription = project.getDescription();
        final String[] ids = projectDescription.getNatureIds();
        final String[] updateIds = new String[ids.length + 1];
        System.arraycopy(ids, 0, updateIds, 0, ids.length);
        updateIds[ids.length] = natureId;
        projectDescription.setNatureIds(updateIds);
        project.setDescription(projectDescription, (IProgressMonitor)null);
    }
    
    private static boolean hasProjectNature(final IProject project, final String natureId) {
        try {
            return project.hasNature(natureId);
        }
        catch (CoreException e) {
            AutoCVSPlugin.plugin.eventLog.logError("Exception getting project nature", (Throwable)e);
            return false;
        }
    }
    
    void attemptCVSUpdate(final IProject project, final int syncMode) throws CoreException {
    }
    
    public void setFailedOperation(final boolean failedOperation) {
        this.failedOperation = failedOperation;
    }
    
    private void autoUpdateWorkspaceProjects() throws CoreException {
        final IProject[] projectList = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        Debug.print("Workspace contains " + projectList.length + " projects");
    }
    
    public static boolean hasAutoCVSNature(final IProject project) {
        return hasProjectNature(project, "edu.umd.cs.eclipse.courseProjectManager.autoCVSNature");
    }
    
    public void earlyStartup() {
    }
    
    private static class LaunchLogger implements ILaunchListener
    {
        public void launchAdded(final ILaunch launch) {
            IProject project = null;
            final ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
            if (launchConfiguration == null) {
                return;
            }
            try {
                final String projectName = launchConfiguration.getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", (String)null);
                if (projectName == null) {
                    AutoCVSPlugin.getPlugin().getEventLog().logMessage("Unable to determine project that was just executed: " + launch.getLaunchMode());
                    Debug.print("Unable to determine project that was just executed: " + launch.getLaunchMode());
                    return;
                }
                project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                if (project == null) {
                    Debug.print("Unable to find project " + projectName);
                    return;
                }
                if (!AutoCVSPlugin.hasSubmitFile(project)) {
                    Debug.print("project doesn use cvs, can't log run event " + projectName);
                }
            }
            catch (CoreException e) {
                Debug.print("Unable to retrieve name of project", (Throwable)e);
            }
        }
        
        public void launchRemoved(final ILaunch launch) {
        }
        
        public void launchChanged(final ILaunch launch) {
        }
    }
}
