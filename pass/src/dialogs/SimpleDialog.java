package dialogs;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

/**
 * Creates a modal dialog asking for password and salt.
 * When the user clicks "ok", the main frame can access the fields with getPass and getPass.
 * If the user pressed "cancel" or "quit", the boolean status is set to false.
 * 
 * @author lucy
 * @date  17.11.2012
 * @version 0.1
 */

public class SimpleDialog extends JDialog {

        private String pass, salt;
        private boolean status = true; //set to false if cancel or close pressed
        

        public static void main(String[]args){
    	
        // initializes the main Frame
        JFrame window = new JFrame("accounts and passwords");
        window.setPreferredSize(new Dimension(200, 300));
        SimpleDialog d = new SimpleDialog(window);
        d.setVisible(true);
        System.out.println(d.getPass() + d.getSalt() );
//        System.exit(0);
    }
        
    /**
     * Creates new form SimpleDialog
     */
	public SimpleDialog(JFrame parent) {
		super(parent, "informations", true);
		this.setPreferredSize(new Dimension(200, 300));
		this.setLocationRelativeTo(parent);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//adds listener to window's close button
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//sets status to false and dispose
				System.out.println("sdkjf");
				((SimpleDialog) e.getSource()).setStatus(false);				
			}
		});
		initComponents();
	}
    


    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        passL = new javax.swing.JLabel();
        saltL = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        passTF = new javax.swing.JPasswordField();
        saltTF = new javax.swing.JPasswordField();

        setMinimumSize(new java.awt.Dimension(320, 150));
        setModal(true);
        setResizable(false);

        passL.setText("Password:");

        saltL.setText("Salt:");

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(passL))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(saltL)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(passTF)
                            .addComponent(saltTF, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(138, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passL)
                    .addComponent(passTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saltL)
                    .addComponent(saltTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addContainerGap())))
        );
    }// </editor-fold>

    /**
     * listener for the cancel button. Sets status to false and 
     * hide the dialog window.
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.status = false;
        this.dispose();
    }

    /**
     * listener for the ok button. Sets the pass and salt to the new values and 
     * hides the dialog window.
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        	
		this.pass = new String(this.passTF.getPassword());
		this.salt = new String(this.saltTF.getPassword());
		
		if (this.pass.length() < 5) {
			this.passTF.requestFocus();
			return;
		}

		if (this.salt.length() < 5) {
			this.saltTF.requestFocus();
			return;
		}
		
		//reset the form for later use and dispose
		this.reset();
		this.dispose();
	}    
    
    /**
     * resets the textareas to empty values
     */
    public void reset(){
    	this.passTF.setText("");
    	this.saltTF.setText("");
    }

    
    /**********************************************
      getters and setters
     **********************************************/
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
                
    // Variables declaration for GUI
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel passL;
    private javax.swing.JPasswordField passTF;
    private javax.swing.JLabel saltL;
    private javax.swing.JPasswordField saltTF;
    // End of variables declaration
}
