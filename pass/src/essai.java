import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class essai {

	public static void main(String[] args) throws HeadlessException,
			UnsupportedFlavorException, IOException {

		String clipboardContent;
		clipboardContent = (String) (Toolkit.getDefaultToolkit()
				.getSystemClipboard().getContents(null)
				.getTransferData(DataFlavor.stringFlavor));

		
		System.out.println("length " + clipboardContent.split("\t").length);
		Scanner stLines = new Scanner( clipboardContent );
        stLines.useDelimiter( "\n" );
        
        for( int i = 0; stLines.hasNext(); i++ ){
            // if it is not the first row, adds a delimiter to the old
            // values String
            // gets next row
            String line = stLines.next() + "\n";
            System.out.println("line length " + line.length());
            String[] tabs = line.split("\t");
            System.out.println("tabs length " + tabs.length);
            for(int j = 0; j < tabs.length; j++ ){
            	System.out.println(j + ": " + tabs[j]);
            }
        }
		
		 String str = "\t\t\t\t\t\t\t\t";
		//System.out.println(str.split("\t").length);
		System.out.println(clipboardContent);

		System.out.println();
//		System.out.println(i);

		// st = new Scanner( str);
		// st.useDelimiter( "\t" );
		// i=0;
		// while(st.hasNext()){ i++; System.out.print(st.next() + " | ");};
		// System.out.println(i);

	}
}
