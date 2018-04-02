<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="../../header.jspf" %>
<workflow-ui:menu activeTab="workflows" />

<div id="page">
    <div class="inner clear">
        <div class="box">
            <h1><spring:message code="workflow.definitions.title" /></h1>
            <table id="definitionsTable" class="data">
                <thead>
                <tr>
                    <th><spring:message code="workflow.definitions.name" /></th>
                    <c:forEach items="${statuses}" var="status">
                        <th><spring:message code="workflowinstance.status.facade.${status}" /></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${workflows}" var="workflow">
                    <tr>
                        <td>
                            <c:url value="${urlPrefix}/console/workflow/instances" var="workflowUrl">
                                <c:param name="workflowName" value="${workflow.key}" />
                            </c:url>
                            <a href="${workflowUrl}"><c:out value="${workflow.key}" /></a>
                        </td>
                        <c:forEach items="${statuses}" var="status">
                            <td class="${workflow.value[status] > 0 ? ('highlight-' += status) : ''}">
                                <c:url value="${urlPrefix}/console/workflow/instances" var="workflowStatusUrl">
                                    <c:param name="workflowName" value="${workflow.key}" />
                                    <c:param name="status" value="${status}" />
                                </c:url>
                                <a href="${workflowStatusUrl}">${not empty workflow.value[status] ? workflow.value[status] : 0}</a>
                            </td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <script type="text/javascript">
                $(document).ready(function () {
                    $('#definitionsTable').dataTable({
                        pageLength: -1,
                        sDom: 'rt<"dataTables-pager"ip>'
                    });
                });
            </script>
        </div>
    </div>
</div>

<%@ include file="../../footer.jspf" %>