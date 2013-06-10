package eionet.cr.web.action;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.StreamingResolution;

/**
* Streaming resolution for sending
* back HTTP errors to external clients in plain/text format.
*
* @author kaido
*/
public class ErrorStreamingResolution extends StreamingResolution {
   /** HTTP error code. */
   private int errorCode;

   /** Error message sent to the stream .*/
   private String errorMessage;

   /**
    * Creates a new resolution.
    * @param errCode HTTP Error code to be returned
    * @param errMsg Error message
    */
   public ErrorStreamingResolution(int errCode, String errMsg) {
       super("text/plain");

       this.errorCode = errCode;
       this.errorMessage = errMsg;


   }

   @Override
   protected void applyHeaders(HttpServletResponse response) {
       response.setHeader("Accept-Ranges", "bytes");
       response.setHeader("Content-type", "text/plain");
   }

   @Override
   protected void stream(HttpServletResponse response) throws Exception {
       response.setContentType("text/plain");
       response.setStatus(errorCode);
       response.getWriter().write(errorMessage);
   }

}
