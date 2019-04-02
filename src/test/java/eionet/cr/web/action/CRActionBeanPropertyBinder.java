package eionet.cr.web.action;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.validation.ValidationErrors;

/**
 * Extension of {@link DefaultActionBeanPropertyBinder} in order to directly inject the proper file bean.
 *
 * @author Jaanus
 */
public class CRActionBeanPropertyBinder extends DefaultActionBeanPropertyBinder {

    /** Name for the request attribute via which we inject rich-type (e.g. file bean) request parameters for the action bean. */
    public static final String RICH_TYPE_REQUEST_PARAMS_ATTR_NAME = "RICH_TYPE_REQUEST_PARAMS";

    /**
     * Default constructor.
     */
    public CRActionBeanPropertyBinder() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder#bind(net.sourceforge.stripes.action.ActionBean,
     * net.sourceforge.stripes.action.ActionBeanContext, boolean)
     */
    @Override
    public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {

        ValidationErrors validationErrors = super.bind(bean, context, validate);

        if (bean != null && context != null) {
            HttpServletRequest request = context.getRequest();
            if (request != null) {
                Object o = request.getAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME);
                if (o instanceof HashMap<?, ?>) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> richTypeRequestParams = (HashMap<String, Object>) o;
                    for (Entry<String, Object> entry : richTypeRequestParams.entrySet()) {

                        String paramName = entry.getKey();
                        Object paramValue = entry.getValue();
                        BeanUtil.setPropertyValue(paramName, bean, paramValue);
                    }
                }
            }
        }

        return validationErrors;
    }
}