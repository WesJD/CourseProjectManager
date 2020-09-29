package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.ui.dialogs.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

public class AutoCVSPropertyPage extends PropertyPage
{
    private Button autoCVSButton;
    private boolean origAutoCVS;
    
    protected Control createContents(final Composite parent) {
        this.noDefaultAndApplyButton();
        boolean hasAutoCVSNature = false;
        try {
            hasAutoCVSNature = this.getProject().hasNature("edu.umd.cs.eclipse.courseProjectManager.autoCVSNature");
            Debug.print("createContents: hasAutoCVSNature ==> " + hasAutoCVSNature);
        }
        catch (CoreException e) {
            AutoCVSPlugin.getPlugin().getEventLog().logError("Exception getting project nature", (Throwable)e);
        }
        final Control control = this.addControl(parent, hasAutoCVSNature);
        this.origAutoCVS = hasAutoCVSNature;
        return control;
    }
    
    public boolean performOk() {
        Debug.print("performOk() called");
        try {
            final AutoCVSPlugin plugin = AutoCVSPlugin.getPlugin();
            final boolean autoCVSEnabled = this.autoCVSButton.getSelection();
            Debug.print("\tAutoCVS ==> " + autoCVSEnabled);
            if (autoCVSEnabled != this.origAutoCVS) {
                if (autoCVSEnabled) {
                    plugin.addAutoCVSNature(this.getProject());
                }
                else {
                    plugin.removeAutoCVSNature(this.getProject());
                }
            }
        }
        catch (CoreException e) {
            Debug.print("Core exception in performOK()", (Throwable)e);
            AutoCVSPlugin.getPlugin().getEventLog().logError("Exception getting project nature", (Throwable)e);
        }
        return true;
    }
    
    private IProject getProject() {
        return (IProject)this.getElement();
    }
    
    private Control addControl(final Composite parent, final boolean hasAutoCVSNature) {
        final Composite composite = new Composite(parent, 0);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout((Layout)gridLayout);
        final GridData data = new GridData();
        data.verticalAlignment = 4;
        data.horizontalAlignment = 4;
        composite.setLayoutData((Object)data);
        final Font font = parent.getFont();
        final Label label = new Label(composite, 0);
        final boolean hasSubmitFile = AutoCVSPlugin.hasSubmitFile(this.getProject());
        label.setText("CourseProjectManager allows submission of projects");
        final Label blank = new Label(composite, 0);
        blank.setText("");
        if (hasSubmitFile) {
            (this.autoCVSButton = new Button(composite, 32)).setText("Enable Course Project Submission");
            this.autoCVSButton.setFont(font);
            this.autoCVSButton.setSelection(hasAutoCVSNature);
        }
        else {
            final Label msg = new Label(composite, 0);
            msg.setText("A project needs to have a .submit file to enable course project submission.");
            final Label msg2 = new Label(composite, 0);
            msg2.setText("Download a .submit file for the project from the submit server.");
        }
        return (Control)composite;
    }
}
