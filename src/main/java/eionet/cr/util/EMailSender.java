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

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import eionet.cr.config.GeneralConfig;

/**
 * Sends emails to system administrators.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class EMailSender {

    /**
     * to prevent public instancing.
     */
    private EMailSender() {

    }

    /**
     * Sends the email to system administrators in To: field.
     *
     * @param subject Subject of email
     * @param body Message
     * @throws MessagingException if sending fails
     */
    public static void sendToSysAdmin(String subject, String body) throws MessagingException {
        send(getSysAdmins(), subject, body, false);
    }

    /**
     * Sends the email - if there is a mail host in the configuration file.
     *
     * @param to Email recipients
     * @param subject Subject of email
     * @param body Message
     * @param ccSysAdmin whether to CC system administrators
     * @throws MessagingException if sending fails
     */
    public static void send(String[] to, String subject, String body, boolean ccSysAdmin)
    throws MessagingException {

        // if no mail.host specified in the properties, go no further
        String mailHost = GeneralConfig.getProperty("mail.host");
        if (mailHost == null || mailHost.trim().length() == 0) {
            return;
        }

        Authenticator authenticator = null;
        if (GeneralConfig.getProperty("mail.smtp.auth") != null && GeneralConfig.getProperty("mail.smtp.auth").equals("true")) {
            String user = GeneralConfig.getProperty("mail.user");
            String password = GeneralConfig.getProperty("mail.password");
            authenticator = new EMailAuthenticator(user, password);
        }
        Session session = Session.getDefaultInstance(GeneralConfig.getProperties(), authenticator);
        MimeMessage message = new MimeMessage(session);
        for (int i = 0; to != null && i < to.length; i++) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
        }

        if (ccSysAdmin) {
            String[] sysAdmins = getSysAdmins();
            if (sysAdmins != null) {
                for (int i = 0; i < sysAdmins.length; i++) {
                    String sysAdmin = sysAdmins[i].trim();
                    if (sysAdmin.length() > 0) {
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(sysAdmin));
                    }
                }
            }
        }

        message.setSubject("[CR] " + subject);
        message.setText(body);
        Transport.send(message);
    }

    /**
     * Returns syadmins email addresses.
     *
     * @return String[] list of sysadmin email addresses
     */
    private static String[] getSysAdmins() {

        String s = GeneralConfig.getProperty(GeneralConfig.MAIL_SYSADMINS);
        if (s != null) {
            s = s.trim();
            if (s.length() > 0) {
                return s.split(",");
            }
        }

        return new String[0];
    }
}
