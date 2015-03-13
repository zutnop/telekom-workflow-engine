<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="header.jsp" %>
<c:url value="/processlogin" var="loginUrl" />

<div class="grid-box">
    <h1><spring:message code="login.title" /></h1>
    <form action="${loginUrl}" method="post" name="loginForm" id="loginForm">
        <table class="form">
            <tbody>
            <c:choose>
                <c:when test="${param.error eq 'invalid'}">
                    <td colspan="2">
                        <div class="message error"><p><spring:message code="login.failed" /></p></div>
                    </td>
                </c:when>
                <c:when test="${param.error eq 'expired'}">
                    <td colspan="2">
                        <div class="message error"><p><spring:message code="login.expired" /></p></div>
                    </td>
                </c:when>
            </c:choose>
            <tr>
                <th><label for="username"><spring:message code="login.username" /></label></th>
                <td class="field"><input type="text" name="username" tabindex="1" id="username" class="text" /></td>
            </tr>
            <tr>
                <th><label for="password"><spring:message code="login.password" /></label></th>
                <td class="field"><input type="password" name="password" tabindex="2" id="password" class="text" /></td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <button onclick="javascript:loginForm.submit();"><spring:message code="login.btn" /></button>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>

<%@ include file="footer.jsp" %>