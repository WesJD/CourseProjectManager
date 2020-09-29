package edu.umd.cs.eclipse.courseProjectManager;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.jface.dialogs.*;

public class PasswordDialog extends Dialog
{
    private String username;
    private String password;
    private Text usernameText;
    private Text passwordText;
    private Text errorMessageText;
    private Button okButton;
    private String message;
    private String title;
    
    public PasswordDialog(final Shell parent) {
        super(parent);
        this.message = "Enter LDAP DirectoryID username and password";
        this.title = "Enter LDAP DirectoryID username and password";
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
        usernameLabel.setText("DirectoryID:");
        final GridData data = new GridData(544);
        data.widthHint = this.convertHorizontalDLUsToPixels(300);
        usernameLabel.setLayoutData((Object)data);
        usernameLabel.setFont(parent.getFont());
        (this.usernameText = new Text(composite, 2052)).setLayoutData((Object)new GridData(768));
        final Label passwordLabel = new Label(composite, 64);
        passwordLabel.setText("Password:");
        final GridData passwordGrid = new GridData(1824);
        passwordGrid.widthHint = this.convertHorizontalDLUsToPixels(300);
        passwordLabel.setLayoutData((Object)passwordGrid);
        passwordLabel.setFont(parent.getFont());
        (this.passwordText = new Text(composite, 2052)).setLayoutData((Object)new GridData(768));
        this.passwordText.setEchoChar('*');
        this.usernameText.addModifyListener((ModifyListener)new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                PasswordDialog.this.validateInput();
            }
        });
        (this.errorMessageText = new Text(composite, 8)).setLayoutData((Object)new GridData(768));
        this.errorMessageText.setBackground(this.errorMessageText.getDisplay().getSystemColor(22));
        applyDialogFont((Control)composite);
        return (Control)composite;
    }
    
    protected void validateInput() {
        final String errorMessage = null;
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
        if (this.password != null) {
            this.passwordText.setText(this.password);
            this.passwordText.selectAll();
        }
        this.usernameText.setFocus();
        if (this.username != null) {
            this.usernameText.setText(this.username);
            this.usernameText.selectAll();
        }
    }
    
    protected void buttonPressed(final int buttonId) {
        if (buttonId == 0) {
            this.username = this.usernameText.getText();
            this.password = this.passwordText.getText();
        }
        super.buttonPressed(buttonId);
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public String getUsername() {
        return this.username;
    }
}
