package eionet.cr.util;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;

import eionet.cr.config.GeneralConfig;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EMailSender {

	/**
	 * 
	 * @param subject
	 * @param body
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public static void sendToSysAdmin(String subject, String body) throws AddressException, MessagingException{
		
		send(getSysAdmins(), subject, body, false);
	}
	
	/**
	 * 
	 * @param to
	 * @param subject
	 * @param body
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public static void send(String[] to, String subject, String body, boolean ccSysAdmin) throws AddressException, MessagingException{

		Session session = Session.getDefaultInstance(GeneralConfig.getProperties(), null);
		MimeMessage message = new MimeMessage(session);
		for (int i=0; i<to.length; i++){
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
		}
		
		if (ccSysAdmin){
			String[] sysAdmins = getSysAdmins();
			if (sysAdmins!=null){
				for (int i=0; i<sysAdmins.length; i++){
					String sysAdmin = sysAdmins[i].trim();
					if (sysAdmin.length()>0){
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
	 * 
	 * @return
	 */
	private static String[] getSysAdmins(){
		
		String s = GeneralConfig.getProperty(GeneralConfig.MAIL_SYSADMINS);
		if (s!=null){
			s = s.trim();
			if (s.length()>0){
				return s.split(",");
			}
		}
		
		return null;
	}
}
