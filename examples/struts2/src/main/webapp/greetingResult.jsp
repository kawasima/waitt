<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Greeting Result</title>
</head>
<body>
    <h1><s:property value="greeting" /></h1>
    <p>Served by WAITT with Struts2 <s:property value="@org.apache.struts2.StrutsConstants@STRUTS_DEVMODE" /></p>
    <p><a href="<s:url action='greeting' />">Try again</a></p>
    <p><a href="<s:url action='' />">Back to Home</a></p>
</body>
</html>
