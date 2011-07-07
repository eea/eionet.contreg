/**
 * CR's XML-RPC services. To invoke them, the client must execute the
 * methods on "ContRegService" service. NB! Please note that these
 * services are implemented with Apache's XML-RPC version 3. The
 * client configuration must set the enabledForExtensions flag,
 * which allows to exchange not only "trivial" XML-RPC data types,
 * but also any class that implements java.io.Serializable.
 *
 * @see <a href="http://ws.apache.org/xmlrpc/types.html">XML-RPC Data types</a>
 * @see <a href="http://ws.apache.org/xmlrpc/client.html">The XmlRpcClient</a>
 */

package eionet.cr.api.xmlrpc;

