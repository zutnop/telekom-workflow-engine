<%@ page isErrorPage="true" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="header.jspf" %>

<h1>An exception occured:</h1>

<pre>
Message:
<%=exception.getMessage()%>

StackTrace:
<spring:eval expression="T(org.apache.commons.lang3.exception.ExceptionUtils).getStackTrace(exception)"/>
</pre>
<br/><br/>

<%@ include file="footer.jspf" %>