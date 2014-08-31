package passinterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: lucy
 * Date: 3/14/13
 * Version: 0.1
 */
@Target( ElementType.FIELD)
@Retention( RetentionPolicy.RUNTIME)
public @interface PathToRefactor {
}//end interface
