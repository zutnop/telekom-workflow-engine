<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="workflow-ui" uri="http://telekom.ee/tags/workflow" %>
<%@ attribute name="activeTab" required="false" %>

<div id="nav">
    <ul class="clear">
        <li id="status-nav">
            <a href="<c:url value='${urlPrefix}/console/status'/>"><spring:message code="menu.status" /></a>
        </li>
        <li id="workflows-nav">
            <a href="<c:url value='${urlPrefix}/console/workflow/definitions'/>"><spring:message code="menu.definitions" /></a>
        </li>
        <workflow-ui:adminAccess>
	        <li id="create-nav">
	            <a href="<c:url value='${urlPrefix}/console/workflow/create'/>"><spring:message code="menu.create" /></a>
	        </li>
        </workflow-ui:adminAccess>
    </ul>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        $('#${activeTab}-nav').addClass('active');
    });
</script>