<html>

<head>
<title>Welcome to Tester</title>
</head>

<body>

<%

out.println("Welcome to the test web server.<br>");

out.println (
	"Request sent to = "
	+ request.getServerName()
	+ ":" + request.getServerPort()
	+ "<br>"
);

out.println (
	"Request received by = "
	+ request.getLocalName()
	+ " [" + request.getLocalAddr() + "]:"
	+ request.getLocalPort()
	+ "<br>"
);

out.println ("" + new java.util.Date());

%>

</body>

</html>
