package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * A very simple calculator action.
 * @author Tim Fennell
 */
@UrlBinding("/calculator.action")
public class CalculatorActionBean implements ActionBean {
    private ActionBeanContext context;
    @Validate(required=true) private double numberOne;
    @Validate(required=true) private double numberTwo;
    private double result;

    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context; }

    public double getNumberOne() { return numberOne; }
    public void setNumberOne(double numberOne) { this.numberOne = numberOne; }

    public double getNumberTwo() { return numberTwo; }
    public void setNumberTwo(double numberTwo) { this.numberTwo = numberTwo; }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

    @DefaultHandler
    public Resolution addition() {
        result = getNumberOne() + getNumberTwo();
        return new ForwardResolution("/pages/calculator.jsp");
    }
    
    public Resolution division() {
	result = numberOne / numberTwo;
	return new ForwardResolution("/pages/calculator.jsp"); 
    }
    
    @ValidationMethod(on="division")
    public void avoidDivideByZero(ValidationErrors errors) {
	if (this.numberTwo == 0) {
		errors.add("numberTwo", new SimpleError("Dividing by zero is not allowed."));
	}
    }
}