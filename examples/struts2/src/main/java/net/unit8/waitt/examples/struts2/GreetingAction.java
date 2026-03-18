package net.unit8.waitt.examples.struts2;

import org.apache.struts2.ActionSupport;

public class GreetingAction extends ActionSupport {
    private String name;
    private String greeting;

    @Override
    public String execute() {
        if (name != null && !name.isEmpty()) {
            greeting = "Welcome aboard, " + name + "! Struts2 is running on WAITT.";
            return SUCCESS;
        }
        return INPUT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGreeting() {
        return greeting;
    }
}
