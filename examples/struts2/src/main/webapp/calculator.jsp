<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Calculator</title>
</head>
<body>
    <h1>Calculator</h1>
    <s:actionerror />
    <s:form action="calculator">
        <s:textfield name="operand1" label="Operand 1" />
        <s:select name="operator" label="Operator"
                  list="#{'+':'Add (+)', '-':'Subtract (-)', '*':'Multiply (*)', '/':'Divide (/)'}"/>
        <s:textfield name="operand2" label="Operand 2" />
        <s:submit value="Calculate" />
    </s:form>
    <s:if test="result != null">
        <h2>Result: <s:property value="operand1" /> <s:property value="operator" /> <s:property value="operand2" /> = <s:property value="result" /></h2>
    </s:if>
    <p><a href="<s:url action='' />">Back to Home</a></p>
</body>
</html>
