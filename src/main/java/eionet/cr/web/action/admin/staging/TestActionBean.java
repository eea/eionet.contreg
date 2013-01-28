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

package eionet.cr.web.action.admin.staging;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SessionScope;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import eionet.cr.dao.DAOException;
import eionet.cr.web.action.AbstractActionBean;

/**
 * Type definition ...
 *
 * @author jaanus
 */
@SessionScope
@UrlBinding("/admin/test.action")
public class TestActionBean extends AbstractActionBean {

    private static final String STEP1_JSP = "/pages/admin/staging/step1.jsp";
    private static final String STEP2_JSP = "/pages/admin/staging/step2.jsp";
    private static final String STEP3_JSP = "/pages/admin/staging/step3.jsp";

    private String ctx;
    private String step1Value;
    private String step2Value;
    private String step3Value;

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution step1() {
        System.out.println("STEP1: " + toStr());
        return new ForwardResolution(STEP1_JSP);
    }

    public Resolution step2() {
        System.out.println("STEP2: " + toStr());
        return new ForwardResolution(STEP2_JSP);
    }

    public Resolution step3() {
        System.out.println("STEP3: " + toStr());
        return new ForwardResolution(STEP3_JSP);
    }

    @Before(stages = {LifecycleStage.BindingAndValidation})
    public void beforeBindingAndValidation() {

        String ctx = getContext().getRequest().getParameter("ctx");
        System.out.println("CTX = " + ctx);
        if (ctx != null && !ctx.equals(this.ctx)) {
            step1Value = null;
            step2Value = null;
            step3Value = null;
        }
    }

    /**
     *
     */
    @Before(stages = {LifecycleStage.CustomValidation})
    public void beforeCustomValidation(){
        System.out.println("beforeCustomValidation: " + toStr());
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"step1"})
    public void validateStep1() throws DAOException {
        System.out.println("validateStep1: " + toStr());
    }

    @ValidationMethod(on = {"step2"})
    public void validateStep2() throws DAOException {
        System.out.println("validateStep2: " + toStr());
    }

    @ValidationMethod(on = {"step3"})
    public void validateStep3() throws DAOException {
        System.out.println("validateStep3: " + toStr());
    }

    /**
     *
     * @return
     */
    public String getCtx() {
        return ctx;
    }

    /**
     * @param ctx the ctx to set
     */
    public void setCtx(String context) {
        this.ctx = context;
    }

    /**
     * @return the step1Value
     */
    public String getStep1Value() {
        return step1Value;
    }

    /**
     * @param step1Value the step1Value to set
     */
    public void setStep1Value(String step1Value) {
        this.step1Value = step1Value;
    }

    /**
     * @return the step2Value
     */
    public String getStep2Value() {
        return step2Value;
    }

    /**
     * @param step2Value the step2Value to set
     */
    public void setStep2Value(String step2Value) {
        this.step2Value = step2Value;
    }

    /**
     * @return the step3Value
     */
    public String getStep3Value() {
        return step3Value;
    }

    /**
     * @param step3Value the step3Value to set
     */
    public void setStep3Value(String step3Value) {
        this.step3Value = step3Value;
    }

    /**
     *
     * @return
     */
    private String toStr() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("ctx", ctx).append("step1Value", step1Value).append("step2Value", step2Value)
                .append("step3Value", step3Value).toString();
    }
}
