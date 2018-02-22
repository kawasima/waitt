<html>
<head>
<title><tiles:getAsString name="title" /></title>
</head>
<body>
<table width="100%">
  <tr><td colspan="2"><tiles:insert page="header.jsp" /></td></tr>
  <tr>
    <td width="20%"><tiles:insert page="menu.jsp" /></td>
    <td><tiles:insert attribute="content" /></td>
  </tr>
  <tr><td colspan="2"><tiles:insert page="footer.jsp" /></td></tr>
</table>
</body>
</html>