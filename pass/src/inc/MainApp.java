package inc;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

import models.*;
import inc.*;

public class MainApp {

    private static final long serialVersionUID = 1L;

    public static DataManager datas = null; // containing all the data
    public static Crypto cipher; // for encryption/decryption
    public static String pathToClassFolder;

    public static int winHeight = 400; // dimensions of the main frame
    public static int winWidth = 800;

    public static MyTable table; // the jtable
    public static String[] columnNames; // the headers for the jtable

    public static void main(String[] args) {

        // get the path to the current .class folder
        pathToClassFolder = (MainApp.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath());

         
         datas.addNode("Google", "Smith", "Snowboarding", "dlskafj", "");
         datas.addNode("John", "Doe", "Rowing", "pass", "");
         datas.addNode("paypal", "winthoutid@hotmail.fr", "", "pass", "");

        // creates a cipher object for cryption/decryption and loads the
        // serialized datas
        try {

            cipher = new Crypto("PBKDF2WithHmacSHA1", "AES/CBC/PKCS5Padding",
                    "AES", 65536, 128, "my_pass", "my_salt");

            datas = (DataManager) cipher.deserializeObject(pathToClassFolder
                    + "\\datas.ser",
                    Functionalities.readIv(pathToClassFolder + "\\iv.ser"));

        } catch (Exception e) {
            System.out.println("could not retrieve iv and datas");
            e.printStackTrace();
            System.exit(0);
        }

        // initializes the main Frame
        JFrame window = new JFrame("accounts and passwords");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(winWidth, winHeight);
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();

        int winY = (screensize.height - winHeight) / 2;
        int winX = (screensize.width - winWidth) / 2;

        window.setLocation(winX, winY);
        window.setVisible(true);

        // adds a listener to serialize data on quit
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                if (datas != null) {
                    try {
                        datas.refactor();
                        byte[] iv = cipher.serializeObject(pathToClassFolder
                                + "\\datas.ser", datas);
                        Functionalities.saveIv(iv, pathToClassFolder
                                + "\\iv.ser");
                    } catch (Exception e) {
                        System.out
                                .println("error in serialization. Possible data loss");
                        e.printStackTrace();
                    }
                }

                System.exit(0);
            }
        });

        // creates the main container
        JPanel container = new JPanel();
        container.setOpaque(false);

        // creates the array of column names for the JTable
        String[] attrnames = datas.getAttributeNames();
        columnNames = new String[attrnames.length + 1];
        columnNames[0] = "id";
        System.arraycopy(attrnames, 0, columnNames, 1, attrnames.length);

        // creates the jtable
        try {
            table = new MyTable(columnNames, datas.dataAsObjectArray());
        } catch (Exception e) {
            System.out.println("problem while filling the table with data");
            e.printStackTrace();
        }

        // adds listener to the jtable
        table.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {

                try {
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    MyTableModel model = (MyTableModel) e.getSource();
                    String columnName = model.getColumnName(col);
                    String newValue = (String) model.getValueAt(row, col);
                    int id = (int) model.getValueAt(row, 0);
                    System.out.println("size " + model.getColumnCount());
                    if(datas.nodeExists(id)){
                        datas.updateNode(id,
                                columnName, newValue);
                    }else{
                        System.out.println("id does not exist");
                        datas.addNode(
                                (String) model.getValueAt(row, 1), 
                                (String) model.getValueAt(row, 2), 
                                (String) model.getValueAt(row, 3), 
                                (String) model.getValueAt(row, 4),
                                (String) model.getValueAt(row, 5)
                        );
                    }

                    System.out.println(datas.getNodeAt(id).toString());
                    
                } catch (Exception e1) {
                    System.out.println("problem while updating field");
                    e1.printStackTrace();
                }
                // table.update(colname, data2);
            }

        });

        // sets the size of the JTable and hide "id" column
        table.setPreferredScrollableViewportSize(new Dimension((winWidth - 40),
                winHeight));
        table.setAutoCreateRowSorter(true);
        // table.setFillsViewportHeight(true);
        table.removeColumn(table.getColumn("id"));
        table.setRowHeight(20);

        // add scrollpane and JTable to the window
        JScrollPane scrollPane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        container.add(scrollPane);
        window.getContentPane().add(container);
        container.updateUI();

    }

    /*****************************************************************************
     * public static DataManager deserializeDatas() {
     * 
     * DataManager datas = null; ObjectInputStream ois = null;
     * 
     * try {
     * 
     * FileInputStream fichier = new FileInputStream((MainApp.class
     * .getProtectionDomain().getCodeSource().getLocation() .getPath() +
     * "\\datas.ser")); ois = new ObjectInputStream(fichier); datas =
     * (DataManager) ois.readObject(); ois.close();
     * 
     * } catch (java.io.IOException e) { e.printStackTrace(); } catch
     * (ClassNotFoundException e) { e.printStackTrace(); } finally { }
     * 
     * return datas; }// end deserialize
     * 
     * public static void serializeDatas() {
     * 
     * try {
     * 
     * FileOutputStream fichier = new FileOutputStream((MainApp.class
     * .getProtectionDomain().getCodeSource().getLocation() .getPath() +
     * "\\datas.ser"));
     * 
     * ObjectOutputStream oos = new ObjectOutputStream(fichier);
     * oos.writeObject(datas); oos.flush(); oos.close();
     * 
     * } catch (java.io.IOException e) { e.printStackTrace(); }
     * 
     * }// end serializedatas
     ***************************************************************/

}// end class
