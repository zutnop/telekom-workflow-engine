<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/jsp/init.jsp" %>
<c:url value="/css/main.css" var="mainCssUrl" />
<c:url value="/images/favicon.ico" var="faviconUrl" />
<c:url value="/js/jquery-1.11.1.min.js" var="jQueryUrl" />
<c:url value="/js/jquery.dataTables.js" var="dataTablesUrl" />

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title><spring:message code="header.title" /></title>
    <meta name="description" content="Workflow engine console">
    <meta name="viewport" content="width=device-width">

    <link rel="shortcut icon" href="${faviconUrl}">
    <link rel="stylesheet" type="text/css" href="${mainCssUrl}" />
    <script type="text/javascript" src="${jQueryUrl}"></script>
    <script type="text/javascript" src="${dataTablesUrl}"></script>
</head>
<body>
<div id="wrap">
    <div id="header">
        <div class="inner clear">
            <p id="logo"><a href="/console"><spring:message code="header.logo" /></a></p>
            <c:set var="environmentName" value="${configuration.getProperty('workflowengine.environment')}"/>
            <p id="portal"><spring:message code="header.name" /><c:if test="${not empty environmentName}">&nbsp;${environmentName}</c:if></p>
            <c:if test="${not empty sessionScope['SPRING_SECURITY_CONTEXT']}">
                <div id="logged-in">
                    <div id="user">
                        <ul>
                            <li><strong>${sessionScope['SPRING_SECURITY_CONTEXT'].authentication.principal}</strong></li>
                            <li><a href="<c:url value='/processlogout' />"><spring:message code="header.logout" /></a></li>
                        </ul>
                    </div>
                </div>
            </c:if>
        </div>
    </div>