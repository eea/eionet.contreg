package eionet.repgen;

/**
 * Logger for report generator.
 *
 * @author Kaido Laine
 */
public class Logger {

    //FIXME - use some real logger instead
    public static void log(String msg) {
            System.out.println(msg);
    }

    public static void error(String msg) {
        System.err.println(msg);
}

}
