package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.jface.dialogs.*;

public class SubmitUserDialog extends Dialog
{
    private String userData;
    private String classAccount;
    private String oneTimePassword;
    private Text userDataText;
    private Text errorMessageText;
    private Button okButton;
    private String message;
    private String title;
    
    public SubmitUserDialog(final Shell parent) {
        super(parent);
        this.message = "Enter submit user data from submit server web page";
        this.title = "User data from Submit server";
    }
    
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite)super.createDialogArea(parent);
        if (this.message != null) {
            final Label label = new Label(composite, 64);
            label.setText(this.message);
            final GridData data = new GridData(1796);
            data.widthHint = this.convertHorizontalDLUsToPixels(300);
            label.setLayoutData((Object)data);
            label.setFont(parent.getFont());
        }
        final Label usernameLabel = new Label(composite, 64);
        usernameLabel.setText("Info:");
        final GridData data = new GridData(544);
        data.widthHint = this.convertHorizontalDLUsToPixels(300);
        usernameLabel.setLayoutData((Object)data);
        usernameLabel.setFont(parent.getFont());
        (this.userDataText = new Text(composite, 2052)).setLayoutData((Object)new GridData(768));
        this.userDataText.addModifyListener((ModifyListener)new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                SubmitUserDialog.this.validateInput();
            }
        });
        (this.errorMessageText = new Text(composite, 8)).setLayoutData((Object)new GridData(768));
        this.errorMessageText.setBackground(this.errorMessageText.getDisplay().getSystemColor(22));
        applyDialogFont((Control)composite);
        return (Control)composite;
    }
    
    protected void validateInput() {
        String info = this.userDataText.getText();
        if (info.length() > 2) {
            final int checksum = Integer.parseInt(info.substring(info.length() - 1), 16);
            info = info.substring(0, info.length() - 1);
            final int hash = info.hashCode() & 0xF;
            if (checksum == hash) {
                final String[] fields = info.split(";");
                if (fields.length == 2) {
                    this.classAccount = fields[0];
                    this.oneTimePassword = fields[1];
                    this.setErrorMessage(null);
                    return;
                }
            }
        }
        final String errorMessage = "The information should be your account name and a string of hexidecimal digits, separated by a semicolon";
        this.setErrorMessage(errorMessage);
    }
    
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        if (this.title != null) {
            shell.setText(this.title);
        }
    }
    
    protected void setErrorMessage(final String errorMessage) {
        this.errorMessageText.setText((errorMessage == null) ? "" : errorMessage);
        this.okButton.setEnabled(errorMessage == null);
        this.errorMessageText.getParent().update();
    }
    
    protected void createButtonsForButtonBar(final Composite parent) {
        this.okButton = this.createButton(parent, 0, IDialogConstants.OK_LABEL, true);
        this.createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
        this.userDataText.setFocus();
        if (this.userData != null) {
            this.userDataText.setText(this.userData);
            this.userDataText.selectAll();
        }
    }
    
    protected void buttonPressed(final int buttonId) {
        if (buttonId == 0) {
            this.userData = this.userDataText.getText();
        }
        super.buttonPressed(buttonId);
    }
    
    public String getClassAccount() {
        return this.classAccount;
    }
    
    public String getOneTimePassString() {
        return this.oneTimePassword;
    }
}
