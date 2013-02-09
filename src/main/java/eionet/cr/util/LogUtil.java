/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.util;

import org.apache.log4j.Logger;

/**
 * Utility class for various logging functionality.
 *
 * @author jaanus
 */
public class LogUtil {

    /**
     * Hide constructor of utility class.
     */
    private LogUtil() {
        // Just hiding the constructor.
    }

    /**
     * Trace the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param loggers the loggers
     */
    public static void trace(Object message, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].trace(message);
            }
        }
    }

    /**
     * Trace the given message and throwable in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param throwable the throwable
     * @param loggers the loggers
     */
    public static void trace(Object message, Throwable throwable, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].trace(message, throwable);
            }
        }
    }

    /**
     * Debug the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param loggers the loggers
     */
    public static void debug(Object message, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].debug(message);
            }
        }
    }

    /**
     * Debug the given message and throwable in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param throwable the throwable
     * @param loggers the loggers
     */
    public static void debug(Object message, Throwable throwable, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].debug(message, throwable);
            }
        }
    }

    /**
     * Info the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param loggers the loggers
     */
    public static void info(Object message, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].info(message);
            }
        }
    }

    /**
     * Info the given message and throwable in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param throwable the throwable
     * @param loggers the loggers
     */
    public static void info(Object message, Throwable throwable, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].info(message, throwable);
            }
        }
    }

    /**
     * Warn the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param loggers the loggers
     */
    public static void warn(Object message, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].warn(message);
            }
        }
    }

    /**
     * Warn the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param throwable the throwable
     * @param loggers the loggers
     */
    public static void warn(Object message, Throwable throwable, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].warn(message, throwable);
            }
        }
    }

    /**
     * Error the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param loggers the loggers
     */
    public static void error(Object message, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].error(message);
            }
        }
    }

    /**
     * Error the given message and throwable in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param throwable the throwable
     * @param loggers the loggers
     */
    public static void error(Object message, Throwable throwable, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].error(message, throwable);
            }
        }
    }

    /**
     * Fatal the given message in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param loggers the loggers
     */
    public static void fatal(Object message, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].fatal(message);
            }
        }
    }

    /**
     * Fatal the given message and throwable in all the given loggers. Latter may be null!
     *
     * @param message the message
     * @param throwable the throwable
     * @param loggers the loggers
     */
    public static void fatal(Object message, Throwable throwable, Logger... loggers) {

        if (loggers == null || loggers.length == 0) {
            return;
        }

        for (int i = 0; i < loggers.length; i++) {
            if (loggers[i] != null) {
                loggers[i].fatal(message, throwable);
            }
        }
    }
}
