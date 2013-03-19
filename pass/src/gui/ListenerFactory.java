package gui;

import dialogs.RefactorSessionDialog;
import main.thread.PassLock;
import models.Exceptions;
import table.PassTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

import static gui.PassFrame.INFOS_DISPLAY_TIME;

/**
 * User: lucy
 * Date: 03/03/13
 * Version: 0.1
 */

public class ListenerFactory {

    private final PassFrame frame;


    public ListenerFactory( final PassFrame frame ) {
        this.frame = frame;
    }


    public ActionListener createCutListener() {

        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                frame.table.stopEditing();

                ( ( PassTableModel ) frame.table.getModel() ).cut( frame.table
                        .getSelectedRowsConvertedToModel(),
                        frame.table.getSelectedColumnsConvertedToModel(), true );
            }
        };
    }//end createCutListener


    public ActionListener createPasteListener() {

        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                System.out.println( "paste" );
                frame.table.stopEditing();

                try {
                    // gets the content of the clipboard
                    String clipboardContent = ( String ) ( Toolkit.getDefaultToolkit()
                            .getSystemClipboard().getContents( this ).getTransferData( DataFlavor
                                    .stringFlavor ) );

                    ( ( PassTableModel ) frame.table.getModel() ).paste( clipboardContent,
                            frame.table.convertRowIndexToModel( frame.table.getSelectedRow() ),
                            frame.table.convertColumnIndexToModel( frame.table.getSelectedColumn
                                    () ), true );

                } catch( Exception ex ) {
                    // TODO Auto-generated catch block
                    System.out.println( ex.getMessage() );
                }
            }
        };
    }//end createPasteListener


    public ActionListener createCopyListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                frame.table.stopEditing();

                ( ( PassTableModel ) frame.table.getModel() ).copy( frame.table
                        .getSelectedRowsConvertedToModel(),
                        frame.table.getSelectedColumnsConvertedToModel() );
            }
        };
    }//end createCopyListener


    public ActionListener createDelRowListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                frame.table.stopEditing();

                int[] selectedRows = frame.table.getSelectedRows();
                for( int i = 0; i < selectedRows.length; i++ ) {
                    // row index minus i since the frame.table size shrinks by 1
                    // everytime. Also converts the row indexes since the frame.table
                    // can be sorted/filtered
                    ( ( PassTableModel ) frame.table.getModel() ).deleteRow( frame.table
                            .convertRowIndexToModel( selectedRows[ i ] - i ) );
                }
            }
        };
    }//end createDelRowListener


    public ActionListener createAddRowListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                frame.table.stopEditing();
                // resets the filters --> shows all rows (global view)
                frame.clearFilterText();
                ( ( PassTableModel ) frame.table.getModel() ).addRow();
                // sets focus on the new row
                int lastRow = frame.table.getModel().getRowCount() - 1;
                // scrolls to the bottom of the frame.table
                frame.table.getSelectionModel().setSelectionInterval( lastRow, lastRow );
                frame.table.scrollRectToVisible( new Rectangle( frame.table.getCellRect( lastRow,
                        0, true ) ) );
            }
        };
    }//end createAddRowListener


    public ActionListener createSaveAsJsonListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                frame.table.stopEditing();

                if( frame.table.getSelectedRowCount() > 0 ) {
                    frame.table.clearSelection();
                }

                //shows a filechooser for the user to select the file in which to save the datas
                JFileChooser chooser = new JFileChooser();
                //adds a filter for txt doc
                chooser.addChoosableFileFilter( new FileFilter() {

                    @Override
                    public boolean accept( File file ) {
                        return file.isDirectory() || file.getName().endsWith( ".txt" );
                    }


                    @Override
                    public String getDescription() {
                        return "Plain text document (*.txt)";
                    }
                } );

                //creates a filter for .json docs and sets it to default filter
                FileFilter defaultFF =  new FileFilter() {

                    @Override
                    public boolean accept( File file ) {
                        return file.isDirectory() || file.getName().endsWith( ".json" );
                    }


                    @Override
                    public String getDescription() {
                        return "Json file (*.json)";
                    }
                } ;

                chooser.addChoosableFileFilter( defaultFF );
                chooser.setFileFilter( defaultFF );
                chooser.setCurrentDirectory( new File( System.getProperty( "user.home" )) );
                chooser.setDialogTitle( "select folder" );
                chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

                //shows filefilter. If the user cancels or does not choose a file, exists.
                if( chooser.showSaveDialog( frame ) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null ) {
                    return;
                }

                //performs export
                File file = chooser.getSelectedFile();
                try {
                    frame.session.writeAsJson( file );
                    JOptionPane.showMessageDialog( null, "data saved to " + file.getName(),
                            "export complete", JOptionPane.PLAIN_MESSAGE );
                } catch( IOException ee ) {
                    ee.printStackTrace();
                    JOptionPane.showMessageDialog( null, "an error occurred during export",
                            "export error", JOptionPane.ERROR_MESSAGE );
                }
            }
        };

        }//end createSaveAsJsonListener


    public ActionListener createPrintListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try {
                    frame.table.print( JTable.PrintMode.NORMAL );
                } catch( PrinterException pe ) {
                    System.err.println( "Error printing: " + pe.getMessage() );
                }
            }
        };
    }//end createPrintListener


    public ActionListener createSaveListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                // stops current editing
                frame.table.stopEditing();

                // if no modification to save, returns
                if( !frame.session.getModel().isModified() ) {
                    frame.showInfos( "everything up to date.", INFOS_DISPLAY_TIME );
                    return;
                }

                try {
                    // saves data
                    if( frame.session.save() ) {
                        System.out.println( "datas serialized" );
                        frame.showInfos( "data saved.", INFOS_DISPLAY_TIME );
                        frame.session.getModel().resetModified();

                    } else {
                        System.out.println( "data not saved" );
                        frame.showInfos( "an error occurred! Data not saved...",
                                INFOS_DISPLAY_TIME );
                    }
                } catch( Exception ee ) {
                    System.out.println( "error in serialization. Possible data loss" );
                    frame.showInfos( "an error occurred! Data not saved...", INFOS_DISPLAY_TIME );
                    ee.printStackTrace();
                }// end try
            }
        };
    }//end createSaveListener


    public ActionListener createNewSessionListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                //TODO
                synchronized( frame.lock ) {
                    frame.setVisible( false );
                    frame.lock.setMessage( PassLock.Message.DO_OPEN_SESSION );
                    frame.lock.notify();
                }
            }
        };
    }//end createDeleteSessionListener


    public ActionListener createRefactorSessionListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                RefactorSessionDialog dialog = new RefactorSessionDialog( null );
                // if the user closed the dialog or clicked cancel, simply
                // returns
                if( !dialog.getStatus() ) {
                    return;
                }

                try {
                    frame.session.refactor( dialog.getSessionName(), dialog.getPass() );
                    frame.setTitle( frame.getTitle() + ": " + frame.session.getName() );
                    frame.showInfos( "refactoring done.", INFOS_DISPLAY_TIME );

                } catch( Exceptions.RefactorException ex ) {
                    frame.showInfos( ex.getMessage(), INFOS_DISPLAY_TIME );
                }

            }
        };
    }//end createRefactorSessionListener


    public ActionListener createDelSessionListener() {
        return new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                if( JOptionPane.showConfirmDialog( null, "are you sure you want to permanently " +
                        "delete session \"" + frame.session.getName() + "\" ?", "delete session", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ) {

                    frame.session.delete();

                    frame.setVisible( false );

                    // TODO
                }
            }
        };
    }//end createDelSessionListener
}//end class
