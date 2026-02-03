<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<h1>Exercice 3 : Passage de paramètre</h1>
<%
    String nameValue = request.getParameter("name");
%>
<form action="hello-servlet" method="get">
    <label for="name">Votre nom :</label>
    <input type="text" id="name" name="name" value="<%= nameValue == null ? "" : nameValue %>" />
    <button type="submit">Envoyer</button>
</form>
<%
    String message = (String) request.getAttribute("message");
    if (message != null && !message.trim().isEmpty()) {
%>
<h2><%= message %></h2>
<%
    }
%>
</body>
</html>
