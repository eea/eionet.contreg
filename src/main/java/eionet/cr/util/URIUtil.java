package eionet.cr.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class URIUtil {
	
	/** */
	private static HashSet schemes;

	/**
	 * Returns true if the following conditions are all met:
	 * - the given string is not null nor empty
	 * - the given string passes <code>new java.net.URI(str)</code> without the <code>java.net.URISyntaxException</code> being thrown
	 * - the given string is an <strong>absolute</strong> URI, meaning it has a scheme part specified
	 * - URI scheme in the given string matches one of those specified by [RFC4395] (see http://www.iana.org/assignments/uri-schemes.html)
	 * 
	 * Otherwise returns false.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isURI(String str){
		
		if (StringUtils.isEmptyOrNull(str))
			return false;
		
		try {
			URI uri = new URI(str);
			if (!uri.isAbsolute()) // we consider legal only those URIs that have a scheme part specified
				return false;
			
			if (schemes==null)
				initSchemes();
			
			return schemes.contains(uri.getScheme()); 
		}
		catch (URISyntaxException e) {
			return false;
		}
	}

	/**
	 * 
	 */
	private static synchronized void initSchemes() {
		
		schemes = new HashSet();
		
		// schemes as specified by [RFC4395] (see http://www.iana.org/assignments/uri-schemes.html)
		schemes.add("aaa");
		schemes.add("aaas");
		schemes.add("acap");
		schemes.add("cap");
		schemes.add("cid");
		schemes.add("crid");
		schemes.add("data");
		schemes.add("dav");
		schemes.add("dict");
		schemes.add("dns");
		schemes.add("fax");
		schemes.add("file");
		schemes.add("ftp");
		schemes.add("go");
		schemes.add("gopher");
		schemes.add("h323");
		schemes.add("http");
		schemes.add("https");
		schemes.add("icap");
		schemes.add("im");
		schemes.add("imap");
		schemes.add("info");
		schemes.add("ipp");
		schemes.add("iris");
		schemes.add("iris.beep");
		schemes.add("iris.xpc");
		schemes.add("iris.xpcs");
		schemes.add("iris.lwz");
		schemes.add("ldap");
		schemes.add("mailto");
		schemes.add("mid");
		schemes.add("modem");
		schemes.add("msrp");
		schemes.add("msrps");
		schemes.add("mtqp");
		schemes.add("mupdate");
		schemes.add("news");
		schemes.add("nfs");
		schemes.add("nntp");
		schemes.add("opaquelocktoken");
		schemes.add("pop");
		schemes.add("pres");
		schemes.add("rtsp");
		schemes.add("service");
		schemes.add("shttp");
		schemes.add("sip");
		schemes.add("sips");
		schemes.add("snmp");
		schemes.add("soap.beep");
		schemes.add("soap.beeps");
		schemes.add("tag");
		schemes.add("tel");
		schemes.add("telnet");
		schemes.add("tftp");
		schemes.add("thismessage");
		schemes.add("tip");
		schemes.add("tv");
		schemes.add("urn");
		schemes.add("vemmi");
		schemes.add("xmlrpc.beep");
		schemes.add("xmlrpc.beeps");
		schemes.add("xmpp");
		schemes.add("z39.50r");
		schemes.add("z39.50s");
		schemes.add("afs");
		schemes.add("dtn");
		schemes.add("iax");
		schemes.add("mailserver");
		schemes.add("pack");
		schemes.add("tn3270");
		schemes.add("prospero");
		schemes.add("wais");
	}
}
