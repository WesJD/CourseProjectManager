package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import java.util.zip.*;
import org.apache.commons.httpclient.methods.multipart.*;
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.apache.commons.httpclient.*;
import org.eclipse.ui.*;
import java.net.*;
import java.io.*;
import org.apache.commons.httpclient.methods.*;

public class TurninProjectAction implements IObjectActionDelegate
{
    private ISelection selection;
    
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    }
    
    private Set<IResource> getProjectResources(final IProject project) throws CoreException {
        final Set<IResource> projectResources = new HashSet<IResource>();
        project.accept((IResourceVisitor)new IResourceVisitor() {
            public boolean visit(final IResource resource) {
                projectResources.add(resource);
                return true;
            }
        });
        return projectResources;
    }
    
    private List<IEditorPart> getDirtyEditorsForProject(final IWorkbench workbench, final IProject project) throws CoreException {
        final Set<IResource> projectResources = this.getProjectResources(project);
        final List<IEditorPart> dirtyEditors = new LinkedList<IEditorPart>();
        final IWorkbenchWindow[] wwinList = workbench.getWorkbenchWindows();
        for (int i = 0; i < wwinList.length; ++i) {
            final IWorkbenchWindow wwin = wwinList[i];
            final IWorkbenchPage[] pageList = wwin.getPages();
            for (int j = 0; j < pageList.length; ++j) {
                final IWorkbenchPage page = pageList[j];
                final IEditorReference[] editorReferenceList = page.getEditorReferences();
                for (int k = 0; k < editorReferenceList.length; ++k) {
                    final IEditorReference editorRef = editorReferenceList[k];
                    final IEditorPart editor = editorRef.getEditor(true);
                    if (editor != null && editor.isDirty()) {
                        final IEditorInput input = editor.getEditorInput();
                        final IResource resource = (IResource)input.getAdapter((Class)IResource.class);
                        if (resource != null) {
                            Debug.print("Got a resource from a dirty editor: " + resource.getName());
                            if (projectResources.contains(resource)) {
                                dirtyEditors.add(editor);
                            }
                        }
                    }
                }
            }
        }
        return dirtyEditors;
    }
    
    private boolean saveDirtyEditors(final IProject project, final IWorkbench workbench) throws CoreException {
        final List<IEditorPart> dirtyEditors = this.getDirtyEditorsForProject(workbench, project);
        final boolean noDirt = dirtyEditors.isEmpty() || (workbench.saveAllEditors(true) && this.getDirtyEditorsForProject(workbench, project).isEmpty());
        if (noDirt) {
            return true;
        }
        AutoCVSPlugin.getPlugin().getEventLog().logMessage("Submission of project " + project.getName() + " cancelled");
        return false;
    }
    
    static Properties getUserProperties(final IResource submitUserResource) throws IOException {
        final Properties userProperties = new Properties();
        if (submitUserResource != null) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(submitUserResource.getRawLocation().toString());
                userProperties.load(fileInputStream);
            }
            catch (FileNotFoundException ex) {
                return userProperties;
            }
            finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return userProperties;
    }
    
    private static Properties getSubmitUserProperties(final IProject project) throws IOException {
        final IResource submitUserResource = project.findMember(".submitUser");
        return getUserProperties(submitUserResource);
    }
    
    public void run(final IAction action) {
        final String timeOfSubmission = "t" + System.currentTimeMillis();
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            Dialogs.errorDialog(null, "Warning: project submission failed", "Could not submit project", "Internal error: Can't get workbench", 4);
            return;
        }
        final IWorkbenchWindow wwin = workbench.getActiveWorkbenchWindow();
        if (wwin == null) {
            Dialogs.errorDialog(null, "Error submitting project", "Could not submit project", "Internal error: Can't get workbench window", 4);
            return;
        }
        final Shell parent = wwin.getShell();
        if (!(this.selection instanceof IStructuredSelection)) {
            Dialogs.errorDialog(parent, "Warning: Selection is Invalid", "Invalid turnin action: You have selected an object that is not a Project. Please select a Project and try again.", "Object selected is not a Project", 2);
            return;
        }
        final IStructuredSelection structured = (IStructuredSelection)this.selection;
        final Object obj = structured.getFirstElement();
        Debug.print("Selection object is a " + obj.getClass().getName() + " @" + System.identityHashCode(obj));
        IProject project;
        if (obj instanceof IProject) {
            project = (IProject)obj;
        }
        else {
            if (!(obj instanceof IProjectNature)) {
                Dialogs.errorDialog(null, "Warning: Selection is Invalid", "Invalid turnin action: You have selected an object that is not a Project. Please select a Project and try again.", "Object selected is not a Project", 2);
                return;
            }
            project = ((IProjectNature)obj).getProject();
        }
        Debug.print("Got the IProject for the turnin action @" + System.identityHashCode(project));
        try {
            if (!this.saveDirtyEditors(project, workbench)) {
                Dialogs.errorDialog(parent, "Submit not performed", "Projects cannot be submitted unless all open files are saved", "Unsaved files prevent submission", 2);
                return;
            }
        }
        catch (CoreException e) {
            Dialogs.errorDialog(parent, "Submit not performed", "Could not turn on cvs management for all project files", (Throwable)e);
            return;
        }
        final IResource submitProjectFile = project.findMember(".submit");
        if (submitProjectFile == null) {
            Dialogs.errorDialog(parent, "Warning: Project submission not enabled", "Submission is not enabled", "There is no .submit file for the project", 4);
            return;
        }
        Properties allSubmissionProps = null;
        try {
            allSubmissionProps = this.getAllProperties(timeOfSubmission, parent, project, submitProjectFile);
        }
        catch (IOException e2) {
            final String message = "IOException finding .submit and .submitUser files; ";
            AutoCVSPlugin.getPlugin().getEventLog().logError(message, e2);
            Dialogs.errorDialog(parent, "Submission failed", message, e2.getMessage(), 4);
            Debug.print("IOException: " + e2);
            return;
        }
        catch (CoreException e3) {
            final String message = "IOException finding .submit and .submitUser files; ";
            AutoCVSPlugin.getPlugin().getEventLog().logError(message, (Throwable)e3);
            Dialogs.errorDialog(parent, "Submission failed", message, e3.getMessage(), 4);
            Debug.print("CoreException: " + e3);
            return;
        }
        try {
            final Collection<IFile> cvsFiles = findFilesForSubmission(project);
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
            final ZipOutputStream zipfile = new ZipOutputStream(bytes);
            zipfile.setComment("zipfile for submission created by CourseProjectManager version " + AutoCVSPlugin.getPlugin().getVersion());
            try {
                final byte[] buf = new byte[4096];
                for (final IFile file : cvsFiles) {
                    if (!file.exists()) {
                        Debug.print("Resource " + file.getName() + " being ignored because it doesn't exist");
                    }
                    else {
                        final ZipEntry entry = new ZipEntry(file.getProjectRelativePath().toString());
                        entry.setTime(file.getModificationStamp());
                        zipfile.putNextEntry(entry);
                        final InputStream in = file.getContents();
                        try {
                            while (true) {
                                final int n = in.read(buf);
                                if (n < 0) {
                                    break;
                                }
                                zipfile.write(buf, 0, n);
                            }
                        }
                        finally {
                            in.close();
                        }
                        in.close();
                        zipfile.closeEntry();
                    }
                }
            }
            catch (IOException e4) {
                Dialogs.errorDialog(parent, "Warning: Project submission failed", "Unable to zip files for submission\n", e4);
                return;
            }
            finally {
                if (zipfile != null) {
                    zipfile.close();
                }
            }
            if (zipfile != null) {
                zipfile.close();
            }
            final MultipartPostMethod filePost = new MultipartPostMethod(allSubmissionProps.getProperty("submitURL"));
            addAllPropertiesButSubmitURL(allSubmissionProps, filePost);
            final byte[] allInput = bytes.toByteArray();
            filePost.addPart((Part)new FilePart("submittedFiles", (PartSource)new ByteArrayPartSource("submit.zip", allInput)));
            final HttpClient client = new HttpClient();
            client.setConnectionTimeout(5000);
            final int status = client.executeMethod((HttpMethod)filePost);
            if (status == 200) {
                Dialogs.okDialog(parent, "Project submission successful", "Project " + allSubmissionProps.getProperty("projectNumber") + " was submitted successfully\n" + filePost.getResponseBodyAsString());
            }
            else {
                Dialogs.errorDialog(parent, "Warning: Project submission failed", "Project submission failed", String.valueOf(filePost.getStatusText()) + "\n ", 8);
                AutoCVSPlugin.getPlugin().getEventLog().logMessage(filePost.getResponseBodyAsString());
            }
        }
        catch (CoreException e3) {
            Dialogs.errorDialog(parent, "Warning: Project submission failed", "Project submissions via https failed\n", (Throwable)e3);
        }
        catch (HttpConnection.ConnectionTimeoutException ex) {
            Dialogs.errorDialog(parent, "Warning: Project submission failed", "Project submissions failed", "Connection timeout while trying to connect to submit server\n ", 4);
        }
        catch (IOException e2) {
            Dialogs.errorDialog(parent, "Warning: Project submission failed", "Project submissions failed\n ", e2);
        }
    }
    
    static void addAllPropertiesButSubmitURL(final Properties allSubmissionProps, final MultipartPostMethod filePost) {
        for (final Map.Entry<?, ?> e : allSubmissionProps.entrySet()) {
            final String key = (String)e.getKey();
            final String value = (String)e.getValue();
            if (!key.equals("submitURL")) {
                filePost.addParameter(key, value);
            }
        }
    }
    
    static Collection<IFile> findFilesForSubmission(final IProject project) {
        final IResource submitignoreFile = project.findMember(".submitIgnore");
        SubmitIgnoreFilter ignoreFilter;
        if (submitignoreFile != null) {
            final String filename = submitignoreFile.getRawLocation().toString();
            try {
                ignoreFilter = SubmitIgnoreFilter.createSubmitIgnoreFilterFromFile(filename);
            }
            catch (IOException ex) {
                Debug.print("Unable to create new ignore SubmitFilter: " + filename);
                ignoreFilter = new SubmitIgnoreFilter();
            }
        }
        else {
            ignoreFilter = new SubmitIgnoreFilter();
        }
        final IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IPath path = project.getLocation();
        final Collection<IFile> result = new LinkedHashSet<IFile>();
        recursiveFindFiles(result, ignoreFilter, path, myWorkspaceRoot, project);
        final IResource submitIncludeFile = project.findMember(".submitInclude");
        if (submitIncludeFile instanceof IFile) {
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(((IFile)submitIncludeFile).getContents()));
                while (true) {
                    final String filePath = reader.readLine();
                    if (filePath == null) {
                        break;
                    }
                    final IResource fileResource = project.findMember(filePath);
                    if (!(fileResource instanceof IFile)) {
                        continue;
                    }
                    result.add((IFile)fileResource);
                }
            }
            catch (IOException e) {
                Debug.print("Error handling .submitInclude file", e);
            }
            catch (CoreException e2) {
                Debug.print("Error handling .submitInclude file", (Throwable)e2);
            }
        }
        return result;
    }
    
    private static void recursiveFindFiles(final Collection<IFile> filesToSubmit, final SubmitIgnoreFilter ignoreFilter, final IPath path, final IWorkspaceRoot myWorkspaceRoot, final IProject project) {
        final IContainer container = myWorkspaceRoot.getContainerForLocation(path);
        try {
            final IResource[] iResources = container.members();
            IResource[] array;
            for (int length = (array = iResources).length, i = 0; i < length; ++i) {
                final IResource iR = array[i];
                if (!ignoreFilter.matches(iR.getLocation().toString())) {
                    switch (iR.getType()) {
                        case 2: {
                            final IPath tempPath = iR.getLocation();
                            recursiveFindFiles(filesToSubmit, ignoreFilter, tempPath, myWorkspaceRoot, project);
                            break;
                        }
                        case 1: {
                            filesToSubmit.add((IFile)iR);
                            break;
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    private Properties getAllProperties(final String timeOfSubmission, final Shell parent, final IProject project, final IResource submitProjectFile) throws IOException, FileNotFoundException, HttpException, CoreException {
        final Properties allSubmissionProps = new Properties();
        final FileInputStream fileInputStream = new FileInputStream(submitProjectFile.getRawLocation().toString());
        allSubmissionProps.load(fileInputStream);
        fileInputStream.close();
        allSubmissionProps.setProperty("cvstagTimestamp", timeOfSubmission);
        allSubmissionProps.setProperty("submitClientTool", "EclipsePlugin");
        allSubmissionProps.setProperty("submitClientVersion", AutoCVSPlugin.getPlugin().getVersion());
        allSubmissionProps.setProperty("hasFailedCVSOperation", Boolean.toString(AutoCVSPlugin.getPlugin().hasFailedOperation()));
        Properties userProperties = getSubmitUserProperties(project);
        final String authentication = allSubmissionProps.getProperty("authentication.type");
        Debug.print("properties: " + userProperties);
        if (this.invalidSubmitUser(userProperties)) {
            InputStream submitUser = null;
            if (!authentication.equals("ldap")) {
                submitUser = getSubmitUserForOpenId(parent, allSubmissionProps);
            }
            else {
                final PasswordDialog passwordDialog = new PasswordDialog(parent);
                final int passwordStatus = passwordDialog.open();
                if (passwordStatus != 0) {
                    Debug.print("PasswordDialog failed");
                }
                final String username = passwordDialog.getUsername();
                final String password = passwordDialog.getPassword();
                submitUser = getSubmitUserFileFromServer(username, password, allSubmissionProps);
            }
            Debug.print("I have input stream from the server");
            final IFile submitUserFile = project.getFile(".submitUser");
            final IResource submitUserResource = project.findMember(".submitUser");
            Debug.print("\nsubmitUserResource = " + submitUserResource + "\n");
            if (submitUserResource == null) {
                submitUserFile.create(submitUser, true, (IProgressMonitor)null);
            }
            else {
                submitUserFile.setContents(submitUser, true, false, (IProgressMonitor)null);
            }
            try {
                Thread.sleep(2000L);
            }
            catch (InterruptedException ex) {}
            Debug.print("created .submituser file");
            userProperties = getSubmitUserProperties(project);
        }
        if (this.invalidSubmitUser(userProperties)) {
            throw new IOException("Cannot find classAccount in user properties even after negotiating with the SubmitServer for a one-time password for this project");
        }
        addPropertiesNotAlreadyDefined(allSubmissionProps, userProperties);
        if (this.invalidSubmitUser(allSubmissionProps)) {
            throw new IOException("Cannot find classAccount in all properties even after negotiating with the SubmitServer for a one-time password for this project");
        }
        return allSubmissionProps;
    }
    
    private boolean invalidSubmitUser(final Properties userProperties) {
        return userProperties.getProperty("cvsAccount") == null && userProperties.getProperty("classAccount") == null;
    }
    
    public static boolean openURL(final String u) {
        try {
            final URL url = new URL(u);
            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
            return true;
        }
        catch (PartInitException ex) {
            return false;
        }
        catch (MalformedURLException ex2) {
            return false;
        }
    }
    
    public static InputStream getSubmitUserForOpenId(final Shell parent, final Properties properties) throws IOException {
        final String courseKey = properties.getProperty("courseKey");
        final String projectNumber = properties.getProperty("projectNumber");
        final String baseURL = properties.getProperty("baseURL");
        final String encodedProjectNumber = URLEncoder.encode(projectNumber, "UTF-8");
        final String u = String.valueOf(baseURL) + "/view/submitStatus.jsp?courseKey=" + courseKey + "&projectNumber=" + encodedProjectNumber;
        final SubmitUserDialog submitUserDialog = new SubmitUserDialog(parent);
        System.out.println(u);
        openURL(u);
        final int status = submitUserDialog.open();
        if (status != 0) {
            Debug.print("SubmitUserDialog failed");
        }
        final String classAccount = submitUserDialog.getClassAccount();
        final String oneTimePassword = submitUserDialog.getOneTimePassString();
        final String results = String.format("classAccount=%s%noneTimePassword=%s%n", classAccount, oneTimePassword);
        return new ByteArrayInputStream(results.getBytes());
    }
    
    public void selectionChanged(final IAction action, final ISelection selection) {
        this.selection = selection;
    }
    
    public static void addPropertiesNotAlreadyDefined(final Properties dst, final Properties src) {
        for (final Map.Entry<?, ?> entry : src.entrySet()) {
            if (!dst.containsKey(entry.getKey())) {
                dst.setProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }
    }
    
    public static String readLine(final BufferedReader reader) throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                return null;
            }
            final int startComment = line.indexOf(35);
            if (startComment != -1) {
                line = line.substring(0, startComment);
            }
            line = line.replaceAll("\\s+", "");
        } while (line.equals(""));
        return line;
    }
    
    public static void main(final String[] args) throws Exception {
        final String HOME = System.getenv("HOME");
        final SubmitIgnoreFilter submitIgnoreFilter = SubmitIgnoreFilter.createSubmitIgnoreFilterFromFile(String.valueOf(HOME) + "/submitignore");
        final Iterator<String> ii = submitIgnoreFilter.iterator();
        while (ii.hasNext()) {
            System.out.println(ii.next());
        }
    }
    
    private static InputStream getSubmitUserFileFromServer(final String loginName, final String password, final Properties allProperties) throws IOException, HttpException {
        String url = allProperties.getProperty("baseURL");
        url = String.valueOf(url) + "/eclipse/NegotiateOneTimePassword";
        final PostMethod post = new PostMethod(url);
        post.addParameter("loginName", loginName);
        post.addParameter("password", password);
        addParameter(post, "courseKey", allProperties);
        addParameter(post, "projectNumber", allProperties);
        post.addParameter("submitClientVersion", AutoCVSPlugin.getPlugin().getVersion());
        final HttpClient client = new HttpClient();
        client.setConnectionTimeout(5000);
        final int status = client.executeMethod((HttpMethod)post);
        if (status != 200) {
            throw new HttpException("Unable to negotiate one-time password with the server: " + post.getResponseBodyAsString());
        }
        return post.getResponseBodyAsStream();
    }
    
    static void addParameter(final PostMethod post, final String name, final Properties properties) {
        final String property = properties.getProperty(name);
        if (property != null) {
            post.addParameter(name, property);
        }
    }
}
