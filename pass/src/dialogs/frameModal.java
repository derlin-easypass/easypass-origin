package dialogs;

import java.util.Set;

import javax.swing.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lucy
 */
public class frameModal extends JDialog {

    private String salt;
    private String pass;
    private String selectedSession;
    private String[] availableSessions;
    
    
    public static void main(String[]args){

        // initializes the main Frame
        JFrame window = new JFrame("accounts and passwords");
        //sets the listener to save data on quit
        String[] sess = {"choose...", "Lucy.Linder"};
        frameModal d = new frameModal(window, "prout", sess);
        System.out.println(d.getPass() + d.getSalt() + d.getSelectedSession());
        System.exit(0);
    }

    public String getSalt() {
        return salt;
    }

    public String getPass() {
        return pass;
    }

    public String getSelectedSession() {
        return selectedSession;
    }
    
    
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        upperPanel = new javax.swing.JPanel();
        welcomeText = new javax.swing.JTextArea();
        tabbedPane = new javax.swing.JTabbedPane();
        sessionTab = new javax.swing.JPanel();
        sessionText = new javax.swing.JTextArea();
        sessionCombo = new JComboBox(availableSessions);
        sessionCombo.insertItemAt("choose...", 0);
        newSessionButton = new javax.swing.JButton();
        passTF = new javax.swing.JTextField();
        PassLabel = new javax.swing.JLabel();
        saltLabel = new javax.swing.JLabel();
        saltTF = new javax.swing.JTextField();
        sessionOkButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        advancedTab = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setPreferredSize(new java.awt.Dimension(500, 350));

        upperPanel.setBackground(new java.awt.Color(255, 255, 255));

        welcomeText.setEditable(false);
        welcomeText.setColumns(20);
        welcomeText.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        welcomeText.setRows(5);
        welcomeText.setText("Welcome to the EasyPass. \n");
        welcomeText.setAutoscrolls(false);
        welcomeText.setBorder(null);
        welcomeText.setFocusable(false);
        welcomeText.setOpaque(false);
        welcomeText.setRequestFocusEnabled(false);
        welcomeText.setVerifyInputWhenFocusTarget(false);

        javax.swing.GroupLayout upperPanelLayout = new javax.swing.GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 464, Short.MAX_VALUE)
            .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(upperPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(welcomeText, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(upperPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(upperPanelLayout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addComponent(welcomeText, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        tabbedPane.setBackground(new java.awt.Color(255, 255, 255));

        sessionTab.setBackground(new java.awt.Color(255, 255, 255));

        sessionText.setEditable(false);
        sessionText.setColumns(20);
        sessionText.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        sessionText.setRows(5);
        sessionText.setText("Choose the session you want to load and provide your password and salt.\nTo create a new session, click the \"new...\" button.");
        sessionText.setAutoscrolls(false);
        sessionText.setBorder(null);
        sessionText.setOpaque(false);
        sessionText.setRequestFocusEnabled(false);
        sessionText.setVerifyInputWhenFocusTarget(false);

        sessionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sessionComboActionPerformed(evt);
            }
        });

        newSessionButton.setText("New...");
        newSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSessionButtonActionPerformed(evt);
            }
        });

        passTF.setEnabled(false);
        passTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passTFActionPerformed(evt);
            }
        });

        PassLabel.setLabelFor(passTF);
        PassLabel.setText("Password:");

        saltLabel.setLabelFor(saltTF);
        saltLabel.setText("Salt:");

        saltTF.setToolTipText("A salt is a passphrase used to cript datas more efficiently.");
        saltTF.setEnabled(false);
        saltTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saltTFActionPerformed(evt);
            }
        });

        sessionOkButton.setText("launch");
        sessionOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sessionOkButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sessionTabLayout = new javax.swing.GroupLayout(sessionTab);
        sessionTab.setLayout(sessionTabLayout);
        sessionTabLayout.setHorizontalGroup(
            sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sessionTabLayout.createSequentialGroup()
                .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sessionTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sessionText, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(sessionTabLayout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PassLabel)
                            .addComponent(saltLabel))
                        .addGap(18, 18, 18)
                        .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(passTF)
                            .addComponent(saltTF, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)))
                    .addGroup(sessionTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sessionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newSessionButton))
                    .addGroup(sessionTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sessionOkButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sessionTabLayout.setVerticalGroup(
            sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sessionTabLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(sessionText, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sessionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newSessionButton))
                .addGap(31, 31, 31)
                .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PassLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saltLabel)
                    .addComponent(saltTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addGroup(sessionTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sessionOkButton)
                    .addComponent(cancelButton))
                .addGap(23, 23, 23))
        );

        tabbedPane.addTab("session", sessionTab);

        advancedTab.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout advancedTabLayout = new javax.swing.GroupLayout(advancedTab);
        advancedTab.setLayout(advancedTabLayout);
        advancedTabLayout.setHorizontalGroup(
            advancedTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 459, Short.MAX_VALUE)
        );
        advancedTabLayout.setVerticalGroup(
            advancedTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 272, Short.MAX_VALUE)
        );

        tabbedPane.addTab("advanced", advancedTab);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upperPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(upperPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>
    
    
    /**
     * Creates new form frame
     */
    public frameModal(JFrame parent, String title, String[] availableSessions) {
        super(parent, title, true);
        this.availableSessions = availableSessions;
        initComponents();
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    

    private void saltTFActionPerformed(java.awt.event.ActionEvent evt) {                                       
    	// TODO add your handling code here:
    }                                      

    private void passTFActionPerformed(java.awt.event.ActionEvent evt) {                                       
        // TODO add your handling code here:
    }                                      

    private void sessionOkButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if(saltTF.getText().length() > 1 && passTF.getText().length() > 1){
        	this.salt = saltTF.getText();
        	this.pass = passTF.getText();
                this.selectedSession = (String)this.sessionCombo.getSelectedItem();
        }
        
        this.dispose();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }
    

    private void sessionComboActionPerformed(java.awt.event.ActionEvent evt) {
        if(!((String)this.sessionCombo.getSelectedItem()).equals("choose...")){
	    	this.passTF.setEnabled(true);
	        this.saltTF.setEnabled(true);
	        this.selectedSession = (String)this.sessionCombo.getSelectedItem();
        }else{
	    	this.passTF.setEnabled(false);
	        this.saltTF.setEnabled(false);
        }
    }
    
    private void newSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	String s = (String)JOptionPane.showInputDialog(
                this,
                "New session name : ",
                JOptionPane.PLAIN_MESSAGE
        );
    	
    	this.sessionCombo.addItem(s);

    }

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(frame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new frame().setVisible(true);
//            }
//        });
//    }
    
    // Variables declaration - do not modify
    private javax.swing.JLabel PassLabel;
    private javax.swing.JPanel advancedTab;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton newSessionButton;
    private javax.swing.JTextField passTF;
    private javax.swing.JLabel saltLabel;
    private javax.swing.JTextField saltTF;
    private javax.swing.JComboBox sessionCombo;
    private javax.swing.JButton sessionOkButton;
    private javax.swing.JPanel sessionTab;
    private javax.swing.JTextArea sessionText;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel upperPanel;
    private javax.swing.JTextArea welcomeText;
    // End of variables declaration
}
