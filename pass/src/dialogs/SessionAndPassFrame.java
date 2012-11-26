/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dialogs;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author lucy
 */
public class SessionAndPassFrame extends javax.swing.JDialog {

    private String[] sessionList;
    private String pass, salt, session;
    private boolean status = true; //set to false if window closed or cancel button pressed

    /**
     * Creates new form SessionAndPassFrame
     */
    public SessionAndPassFrame(javax.swing.JFrame parent, String[] sessions) throws ClassNotFoundException, IllegalAccessException, InstantiationException, UnsupportedLookAndFeelException {
        super(parent, "session and credentials", true);
        if(sessions == null){
            this.sessionList = new String[0];
        }
            
        this.sessionList = sessions;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        sessionL = new javax.swing.JLabel();
        if(this.sessionList == null){
            this.sessionCombo = new javax.swing.JComboBox<String>(new String[]{"choose..."});
        }else{
            sessionCombo = new javax.swing.JComboBox<String>(sessionList);
            this.sessionCombo.insertItemAt("choose...", 0);
            this.sessionCombo.setSelectedIndex(0);
        }
        passL = new javax.swing.JLabel();
        saltL = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        passTF = new javax.swing.JPasswordField();
        saltTF = new javax.swing.JPasswordField();
        newSessionButton = new javax.swing.JButton();

        setTitle("EasyPass - open session");
        setResizable(false);

        sessionL.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        sessionL.setLabelFor(sessionCombo);
        sessionL.setText("Choose your session :");
        sessionL.setFocusable(false);

        sessionCombo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        sessionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sessionComboActionPerformed(evt);
            }
        });

        passL.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        passL.setText("Password:");

        saltL.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        saltL.setText("Salt:");

        okButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        okButton.setText("Launch");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        newSessionButton.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        newSessionButton.setText("New...");
        newSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSessionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sessionL)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(sessionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(newSessionButton))
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(passL)
                                .addComponent(saltL))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(saltTF)
                                .addComponent(passTF)))))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sessionL)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(sessionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newSessionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passTF, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saltL)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(saltTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(11, 11, 11))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>

    private void sessionComboActionPerformed(java.awt.event.ActionEvent evt) {                                             
        if (!((String) this.sessionCombo.getSelectedItem()).equals("choose...")) {
            this.passTF.setEnabled(true);
            this.saltTF.setEnabled(true);
        } else {
            this.passTF.setEnabled(false);
            this.saltTF.setEnabled(false);
        }
    }                                            

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {                                         
        this.pass = new String(this.passTF.getPassword());
        this.salt = new String(this.saltTF.getPassword());
        this.session = (String) this.sessionCombo.getSelectedItem();
        
        if(this.pass.length() == 0 || this.salt.length() == 0){
            return;
        }

        //reset the form for later use and dispose
        this.reset();
        this.setVisible(false);
    }                                        

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        this.status = false;
        this.setVisible(false);
    }                                            

    private void newSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        String newSession = (String) JOptionPane.showInputDialog(
                this,
                "New session name : ",
                ""
                );
        //the (?i) makes everything on the right case-insensitive
        if(newSession != null && newSession.matches("^(?i)[a-z][a-z1-9\\._-]{2,}$")){
            
            //if this session name doesn't already exist, show error message and return
            for(int i = 0; i < this.sessionCombo.getItemCount(); i++){
                if(newSession.equals(this.sessionCombo.getItemAt( i ))){
                    JOptionPane.showMessageDialog(this, 
                            "This session name is already in use ",
                            "error",
                            JOptionPane.WARNING_MESSAGE
                            );
                    return;
                }//end if
            }//end for
            
            //add new session
            this.sessionCombo.addItem(newSession);
            this.sessionCombo.setSelectedIndex(this.sessionCombo.getItemCount() - 1);
            
        }else{
            JOptionPane.showMessageDialog(this, 
                    "A session name must have min. 3 characters and start with a letter \n "
                    + "Accepted : letters, digits. \nDelimiters : _.-",
                    "error",
                    JOptionPane.WARNING_MESSAGE
                    );
        }
    }                                                

    /**
     * resets the textareas to empty values
     */
    public void reset() {
        this.passTF.setText("");
        this.saltTF.setText("");
        this.sessionCombo.setSelectedIndex(0);
    }

    /**
     * ********************************************
     * getters and setters ********************************************
     */
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
    
    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SessionAndPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SessionAndPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SessionAndPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SessionAndPassFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    String[] sessions = {""};
                    new SessionAndPassFrame(null, null).setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();;
                }
            }
        });
    }
    // Variables declaration - do not modify
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton newSessionButton;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel passL;
    private javax.swing.JPasswordField passTF;
    private javax.swing.JLabel saltL;
    private javax.swing.JPasswordField saltTF;
    private javax.swing.JComboBox sessionCombo;
    private javax.swing.JLabel sessionL;
    // End of variables declaration
}
