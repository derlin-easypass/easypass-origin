package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * User: lucy
 * Date: 03/03/13
 * Version: 0.1
 */
public class PassMenuBar extends JMenuBar {

    private Insets inset = new Insets( 4, 9, 4, 9 );
    private PassFrame frame;
    private ListenerFactory listfactory;


    PassMenuBar( PassFrame frame ) {
        this.frame = frame;
        this.listfactory = new ListenerFactory( frame );
        this.add( getEditMenu() );
        this.add( getFileMenu() );
        this.add( getSessionMenu() );
    }//end constructor


    private JMenu getEditMenu() {
        // --------------------------- Build edit menu in the menu bar.
        JMenu editMenu;
        JMenuItem undoSubMenu, redoSubMenu, addRowSubMenu, deleteRowSubMenu;
        JMenuItem copySubMenu, cutSubMenu, pasteSubMenu;

        editMenu = new JMenu( "edit" );
        editMenu.setMargin( inset );


        // adds the add row subMenu
        addRowSubMenu = new JMenuItem( "add row" );
        addRowSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        addRowSubMenu.addActionListener( listfactory.createAddRowListener() );


        // adds the delete selected rows subMenu
        deleteRowSubMenu = new JMenuItem( "delete selected rows" );
        deleteRowSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );

        deleteRowSubMenu.addActionListener( listfactory.createDelRowListener() );


        //adds the copy submenu
        copySubMenu = new JMenuItem( "copy" );
        copySubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        copySubMenu.addActionListener( listfactory.createCopyListener() );


        //adds the cut submenu
        cutSubMenu = new JMenuItem( "cut" );
        copySubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        copySubMenu.addActionListener( listfactory.createCutListener() );


        //adds the paste submenu
        pasteSubMenu = new JMenuItem( "paste" );
        copySubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        copySubMenu.addActionListener( listfactory.createPasteListener() );


        // add undo submenu
        undoSubMenu = new JMenuItem( frame.undoManager.getUndoAction() );
        undoSubMenu.setMnemonic( KeyEvent.VK_Z );
        undoSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );

        // add redo submenu
        redoSubMenu = new JMenuItem( frame.undoManager.getRedoAction() );
        redoSubMenu.setMnemonic( KeyEvent.VK_Y );
        redoSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );


        //adds submenus to the edit menu
        editMenu.add( cutSubMenu );
        editMenu.add( copySubMenu );
        editMenu.add( pasteSubMenu );
        editMenu.addSeparator();
        editMenu.add( deleteRowSubMenu );
        editMenu.add( addRowSubMenu );
        editMenu.addSeparator();
        editMenu.add( undoSubMenu );
        editMenu.add( redoSubMenu );

        return editMenu;
    }//end getEditMenu


    private JMenu getFileMenu() {
        JMenu fileMenu;
        JMenuItem saveSubMenu, jsonSubMenu, printSubMenu;

        // --------------------------- Build the option menu.
        fileMenu = new JMenu( "file" );
        //TODO fileMenu.setMnemonic( KeyEvent.VK_A );

        // save option
        saveSubMenu = new JMenuItem( "save", KeyEvent.VK_T );
        saveSubMenu.setMargin( inset );
        saveSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        saveSubMenu.addActionListener( listfactory.createSaveListener() );

        // save as json menu
        jsonSubMenu = new JMenuItem( "export as Json", KeyEvent.VK_E );
        jsonSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_E,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
        jsonSubMenu.addActionListener( listfactory.createSaveAsJsonListener() );

        // print subMenu
        fileMenu.addSeparator();
        printSubMenu = new JMenuItem( "print" );
        printSubMenu.setMnemonic( KeyEvent.VK_P );
        printSubMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );

        printSubMenu.addActionListener( listfactory.createPrintListener() );

        fileMenu.add( jsonSubMenu );
        fileMenu.add( printSubMenu );
        fileMenu.add( saveSubMenu );
        return fileMenu;

    }//end getFileMenu


    private JMenu getSessionMenu() {
        // -------------------------build menu to manage session

        JMenu sessionMenu;
        JMenuItem newSessionSubMenu, deleteSessionSubMenu, editSessionSubmenu;

        sessionMenu = new JMenu( "session" );
        sessionMenu.setMargin( inset );

        // open a new session menu
        newSessionSubMenu = new JMenuItem( "open..." );
        newSessionSubMenu.addActionListener( listfactory.createNewSessionListener() );

        // refactor current session
        editSessionSubmenu = new JMenuItem( "edit..." );
        editSessionSubmenu.addActionListener( listfactory.createRefactorSessionListener() );

        // delete session
        // TODO
        deleteSessionSubMenu = new JMenuItem( "delete..." );
        deleteSessionSubMenu.addActionListener( listfactory.createDelSessionListener() );

        sessionMenu.add( newSessionSubMenu );
        sessionMenu.add( editSessionSubmenu );
        sessionMenu.add( deleteSessionSubMenu );

        return sessionMenu;
    }//end getSessionMenu
}//end class
