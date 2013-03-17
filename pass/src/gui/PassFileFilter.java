package gui;

import java.io.File;
import java.io.FilenameFilter;

/**
 * User: lucy
 * Date: 3/17/13
 * Version: 0.1
 */
public class PassFileFilter implements FilenameFilter {
        private String extension;
        private String pattern;



        public PassFileFilter( String extension ) {
            this.extension = extension;
            this.pattern = ".*" + extension + "$";
            System.out.println("pattern: " + pattern);
        }

        public String getDescription() {
            return "Easypass session file (*." + extension + ")";
        }


        public boolean accept( File dir, String name ) {
            return name.toLowerCase().matches( pattern );
        }

}//end class
