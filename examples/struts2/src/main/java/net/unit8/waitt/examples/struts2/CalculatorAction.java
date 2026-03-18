package net.unit8.waitt.examples.struts2;

import com.opensymphony.xwork2.validator.annotations.IntRangeFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import org.apache.struts2.ActionSupport;

public class CalculatorAction extends ActionSupport {
    private int operand1;
    private int operand2;
    private String operator = "+";
    private Integer result;

    public String input() {
        return INPUT;
    }

    @Override
    public String execute() {
        switch (operator) {
            case "+": result = operand1 + operand2; break;
            case "-": result = operand1 - operand2; break;
            case "*": result = operand1 * operand2; break;
            case "/":
                if (operand2 == 0) {
                    addActionError("Cannot divide by zero.");
                    return INPUT;
                }
                result = operand1 / operand2;
                break;
            default:
                addActionError("Unknown operator: " + operator);
                return INPUT;
        }
        return SUCCESS;
    }

    public int getOperand1() { return operand1; }
    public void setOperand1(int operand1) { this.operand1 = operand1; }

    public int getOperand2() { return operand2; }
    public void setOperand2(int operand2) { this.operand2 = operand2; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Integer getResult() { return result; }
}
