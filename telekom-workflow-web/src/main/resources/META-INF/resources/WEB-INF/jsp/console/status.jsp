<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="../header.jspf" %>
<workflow-ui:menu activeTab="status" />

<div id="page">
    <div class="inner clear">
        <c:forEach items="${mbeans}" var="mbean">
            <div class="box">
                <div class="dataTables_wrapper">
                    <h2>[<spring:message code="status.mbean" />]&nbsp;<c:out value="${mbean.key}" /></h2>
                    <table class="data dataTable" style="margin-bottom: 2px;">
                        <thead>
                        <tr>
                            <th><spring:message code="status.name" /></th>
                            <th><spring:message code="status.description" /></th>
                            <th><spring:message code="status.value" /></th>
                        </tr>
                        </thead>
                        <c:forEach items="${mbean.value}" var="attribute">
                            <tr>
                                <td><c:out value="${attribute.name}" /></td>
                                <td><c:out value="${attribute.description}" /></td>
                                <td><c:out value="${attribute.value}" /></td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </div>
            <br/>
        </c:forEach>
    </div>
</div>

<%@ include file="../footer.jspf" %>