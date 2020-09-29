package edu.umd.cs.eclipse.courseProjectManager;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

public class GetFilesToSubmit
{
    public static ArrayList<IResource> getAllCFilesInProject(final IProject project) {
        final ArrayList<IResource> allCFiles = new ArrayList<IResource>();
        final IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IPath path = project.getLocation();
        recursiveFindCFiles(allCFiles, path, myWorkspaceRoot);
        return allCFiles;
    }
    
    private static void recursiveFindCFiles(final ArrayList<IResource> filesToSubmit, final IPath path, final IWorkspaceRoot myWorkspaceRoot) {
        final IContainer container = myWorkspaceRoot.getContainerForLocation(path);
        try {
            final IResource[] iResources = container.members();
            IResource[] array;
            for (int length = (array = iResources).length, i = 0; i < length; ++i) {
                final IResource iR = array[i];
                if ("c".equalsIgnoreCase(iR.getFileExtension())) {
                    filesToSubmit.add(iR);
                }
                if (iR.getType() == 2) {
                    final IPath tempPath = iR.getLocation();
                    recursiveFindCFiles(filesToSubmit, tempPath, myWorkspaceRoot);
                }
            }
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    public static IProject getCurrentProject() {
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            final IStructuredSelection selection = (IStructuredSelection)window.getSelectionService().getSelection();
            final Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IAdaptable) {
                final IProject project = (IProject)((IAdaptable)firstElement).getAdapter((Class)IProject.class);
                return project;
            }
        }
        return null;
    }
}
