package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.Message;
import eionet.cr.web.context.CRActionBeanContext;

/**
 * Context for testing action beans.
 *
 * @author kaido
 */
public class CRTestActionBeanContext extends CRActionBeanContext {

    @Override
    public List<Message> getMessages(String key) {
        return new ArrayList<Message>();
    }

    @Override
    public String getRequestParameter(String parameterName) {
        return "";
    }

}
