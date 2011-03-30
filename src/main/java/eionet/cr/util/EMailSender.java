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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;

import eionet.cr.config.GeneralConfig;

/**
 * Sends emails to system administrators
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EMailSender {

    /**
     * Sends the email to system administrators in To: field.
     *
     * @param subject Subject of email
     * @param body Message
     * @throws AddressException
     * @throws MessagingException
     */
    public static void sendToSysAdmin(String subject, String body) throws AddressException, MessagingException {

        send(getSysAdmins(), subject, body, false);
    }

    /**
     * Sends the email - if there is a mail host in the configuration file.
     *
     * @param to Email recipients
     * @param subject Subject of email
     * @param body Message
     * @param ccSysAdmin whether to CC system administrators
     * @throws AddressException
     * @throws MessagingException
     */
    public static void send(String[] to, String subject, String body, boolean ccSysAdmin) throws AddressException, MessagingException {

        // if no mail.host specified in the properties, go no further
        String mailHost = GeneralConfig.getProperty("mail.host");
        if (mailHost == null || mailHost.trim().length() == 0) {
            return;
        }

        Session session = Session.getDefaultInstance(GeneralConfig.getProperties(), null);
        MimeMessage message = new MimeMessage(session);
        for (int i = 0; i < to.length; i++) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
        }

        if (ccSysAdmin) {
            String[] sysAdmins = getSysAdmins();
            if (sysAdmins != null) {
                for (int i = 0; i < sysAdmins.length; i++) {
                    String sysAdmin = sysAdmins[i].trim();
                    if (sysAdmin.length() > 0) {
                        message.addRecipient(Message.RecipientType.CC,
                            new InternetAddress(sysAdmin));
                    }
                }
            }
        }

        message.setSubject("[CR] " + subject);
        message.setText(body);
        Transport.send(message);
    }

    /**
     *
     * @return
     */
    private static String[] getSysAdmins() {

        String s = GeneralConfig.getProperty(GeneralConfig.MAIL_SYSADMINS);
        if (s != null) {
            s = s.trim();
            if (s.length() > 0) {
                return s.split(",");
            }
        }

        return null;
    }
}
