<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Greeting Form</title>
</head>
<body>
    <h1>Greeting Form</h1>
    <s:form action="greeting">
        <s:textfield name="name" label="Your Name" />
        <s:submit value="Greet" />
    </s:form>
    <p><a href="<s:url action='' />">Back to Home</a></p>
</body>
</html>
