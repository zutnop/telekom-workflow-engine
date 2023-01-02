<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="../../header.jspf" %>
<workflow-ui:menu activeTab="workflows" />

<div id="page">
    <div class="inner clear">

		<c:choose>
			<c:when test="${not empty unknownAction}">
				<div class="message error">
					<spring:message code="${errorMessage}" arguments="${unknownAction}" />
				</div>
			</c:when>
			<c:when test="${not empty errorMessage}">
				<div class="message error">
					<spring:message code="${errorMessage}" />
				</div>
			</c:when>
			<c:when test="${not empty successMessage}">
				<div class="message success">
					<spring:message code="${successMessage}" />
				</div>
			</c:when>
		</c:choose>

        <div class="box">
	        <div class="dataTables_wrapper">
	            <h1><spring:message code="workflow.instance.details.title"/></h1>

	            <table class="data">
	            	<thead>
	                    <tr>
	                    	<th><spring:message code="workflowinstance.refnum"/></th>
	                    	<th><spring:message code="workflowinstance.nameandversion"/></th>
	                    	<th><spring:message code="workflowinstance.keephistory"/></th>
							<th><spring:message code="workflowinstance.archivePeriodLength"/></th>
	                    	<th><spring:message code="workflowinstance.label1"/></th>
	                    	<th><spring:message code="workflowinstance.label2"/></th>
	                    	<th><spring:message code="workflowinstance.cluster"/></th>
	                    	<th><spring:message code="workflowinstance.node"/></th>
	                    	<th><spring:message code="workflowinstance.status"/></th>
	                    	<th><spring:message code="workflowinstance.locked"/></th>
	                    	<th><spring:message code="workflowinstance.created"/></th>
	                    	<th><spring:message code="workflowinstance.lastupdated"/></th>
	                    </tr>
	            	</thead>
	                <tbody>
	                    <tr>
	                        <td><c:out value="${workflowInstance.refNum}" /></td>
	                        <td><c:out value="${workflowInstance.workflowName}" />:<c:choose><c:when test="${not empty workflowInstance.workflowVersion}"><c:out value="${workflowInstance.workflowVersion}" /></c:when><c:otherwise><spring:message code="workflowinstance.workflowversion.latest"/></c:otherwise></c:choose></td>
	                        <td class="${workflowInstance.keepHistory=='true'?'':'highlight-keephistory'}"><c:out value="${workflowInstance.keepHistory}" /></td>
							<td><c:out value="${workflowInstance.archivePeriodLength}" /></td>
	                        <td><c:out value="${workflowInstance.label1}" /></td>
	                        <td><c:out value="${workflowInstance.label2}" /></td>
	                        <td><c:out value="${workflowInstance.clusterName}" /></td>
	                        <td><c:out value="${workflowInstance.nodeName}" /></td>
	                        <td class="${'highlight-' += workflowInstance.facadeStatus}">
	                           <spring:message code="workflowinstance.status.facadedetailed.${workflowInstance.facadeStatus}" arguments="${workflowInstance.status}"/>
	                        </td>
							<td><spring:message code="workflowinstance.locked.${workflowInstance.locked}"/></td>
	                        <td><c:out value="${workflowInstance.dateCreatedText}" /></td>
	                        <td><c:out value="${workflowInstance.dateUpdatedText}" /></td>
	                    </tr>
	                </tbody>
	            </table>
	        </div>
		</div>

        <div class="box">
        	<div class="dataTables_wrapper">
	            <h1><spring:message code="workflow.instance.environment.title"/></h1>
	            <c:choose>
					<c:when test="${not empty workflowInstance.attributeList}">
						<table class="data">
			            	<thead>
			                    <tr>
			                    	<th><spring:message code="workflow.instance.environment.attribute"/></th>
			                    	<th><spring:message code="workflow.instance.environment.value"/></th>
			                    </tr>
			            	</thead>
			                <tbody>
			                	<c:forEach var="attribute" items="${workflowInstance.attributeList}">
				                    <tr>
				                        <td><c:out value="${attribute.left}" /></td>
				                        <td><c:out value="${attribute.right}" /></td>
				                    </tr>
				                </c:forEach>
			               </tbody>
			            </table>
					</c:when>
					<c:otherwise>
						<p><spring:message code="workflow.instance.environment.empty"/></p>
					</c:otherwise>
				</c:choose>
			</div>
        </div>

        <div class="box">
        	<div class="dataTables_wrapper">
	            <h1><spring:message code="workflow.instance.workitem.title"/></h1>
	 			<c:choose>
					<c:when test="${not empty workItems}">
			            <table class="data">
			            	<thead>
			                    <tr>
			                    	<th><spring:message code="workflow.instance.workitem.refnum"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.tokenid"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.status"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.signal"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.timer"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.task"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.humantask"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.arguments"/></th>
			                    	<th><spring:message code="workflow.instance.workitem.result"/></th>
			                    	<workflow-ui:adminAccess>
			                    		<th><spring:message code="workflow.instance.workitem.action"/></th>
			                    	</workflow-ui:adminAccess>
			                    </tr>
			            	</thead>
			                <tbody>
			                	<c:forEach var="wi" items="${workItems}">
				                    <tr class="${(wi.status == 'COMPLETED' or wi.status == 'CANCELLED') ? 'text-inactive' : ''}">
				                        <td><c:out value="${wi.refNum}" /></td>
				                        <td><c:out value="${wi.tokenId}" /></td>
				                        <td><c:out value="${wi.status}" /></td>
				                        <td><c:out value="${wi.signal}" /></td>
				                        <td><c:out value="${wi.dueDateText}" /></td>
				                        <td><c:if test="${not empty wi.bean}"><c:out value="${wi.bean}"/>/<c:out value="${wi.method}"/></c:if></td>
				                        <td><c:if test="${not empty wi.role || not empty wi.userName}"><c:out value="${wi.role}"/>/<c:out value="${wi.userName}"/></c:if></td>
				                        <td><c:if test="${not empty wi.arguments}"><c:out value="${wi.arguments}" /></c:if></td>
				                        <td><c:out value="${wi.result}" /></td>
				                        <workflow-ui:adminAccess>
				                            <td>
				                            	<c:choose>
				                            		<c:when test="${wi.status == 'NEW' && (wi.type != 'TIMER' || wi.dueDateInFuture)}">
				                            			<c:url value="${urlPrefix}/console/workflow/instances/${workflowInstance.refNum}/item/${wi.refNum}" var="url" />
				                            			<a href="${url}"><spring:message code="workflow.instance.workitem.action.${wi.type}"/></a>
				                            		</c:when>
				                            		<c:otherwise>&nbsp;</c:otherwise>
				                            	</c:choose>
				                            </td>
				                        </workflow-ui:adminAccess>
				                    </tr>
				                </c:forEach>
			               </tbody>
			            </table>
					</c:when>
					<c:otherwise>
						<p><spring:message code="workflow.instance.workitem.empty"/></p>
					</c:otherwise>
				</c:choose>
			</div>
        </div>

        <div class="box">
        	<div class="dataTables_wrapper">
	            <h1><spring:message code="workflow.instance.token.title"/></h1>
	            <c:choose>
					<c:when test="${not empty workflowInstance.tokenList}">
			            <table class="data">
			            	<thead>
			                    <tr>
			                    	<th><spring:message code="workflow.instance.token.id"/></th>
			                    	<th><spring:message code="workflow.instance.token.parentid"/></th>
			                    	<th><spring:message code="workflow.instance.token.nodeid"/></th>
			                    	<th><spring:message code="workflow.instance.token.active"/></th>
			                    </tr>
			            	</thead>
			                <tbody>
			                	<c:forEach var="token" items="${workflowInstance.tokenList}">
				                    <tr class="${token.active ? '' : 'text-inactive'}">
				                        <td><c:out value="${token.id}" /></td>
				                        <td><c:out value="${token.parentId}" /></td>
				                        <td><c:out value="${token.nodeId}" /></td>
				                        <td><spring:message code="workflow.instance.token.active.${token.active}"/></td>
				                    </tr>
				                </c:forEach>
			               </tbody>
			            </table>
					</c:when>
					<c:otherwise>
						<p><spring:message code="workflow.instance.token.empty"/></p>
					</c:otherwise>
				</c:choose>
			</div>
        </div>

        <div class="box">
            <h1><spring:message code="workflow.instance.history.title"/></h1>
            <c:choose>
                <c:when test="${not empty workflowInstance.executionSteps}">
                    <c:forEach items="${workflowInstance.executionSteps}" var="step" >
                        <p><c:out value="${step}"/></p>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <p><spring:message code="workflow.instance.history.empty"/></p>
                </c:otherwise>
            </c:choose>
        </div>

		<c:if test="${not empty executionError}">
	        <div class="box">
	            <h1><spring:message code="workflow.instance.error.title"/></h1>
	            <c:choose>
				    <c:when test="${workflowInstance.status eq 'STARTING_ERROR'}">
				        <p><spring:message code="workflow.instance.error.start"/><c:out value="${executionError.errorText}"/></p>
				    </c:when>
				    <c:when test="${workflowInstance.status eq 'EXECUTING_ERROR'}">
				        <p><spring:message code="workflow.instance.error.complete" arguments="${executionError.woitRefNum}"/><c:out value="${executionError.errorText}"/></p>
				    </c:when>
				    <c:when test="${workflowInstance.status eq 'ABORTING_ERROR'}">
				        <p><spring:message code="workflow.instance.error.abort"/><c:out value="${executionError.errorText}"/></p>
				    </c:when>
				</c:choose>
				<pre><c:out value="${executionError.errorDetails}"></c:out></pre>
        	</div>
		</c:if>
    </div>

	<c:url value="${urlPrefix}/console/workflow/instances/${workflowInstance.refNum}" var="url" />
	<form method="post" action="${url}" id="form">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input type="hidden" name="action" id="action"/>
	</form>
	<script type="text/javascript">
		function submit(action, confirmMsg) {
			if (!confirmMsg || confirm(confirmMsg)) {
				$("#action").val(action);
				$("#form").submit();
			}
		}
	</script>
	<workflow-ui:adminAccess>
		<p class="actions">
		    <c:if test="${workflowInstance.status eq 'NEW' || workflowInstance.status eq 'STARTING' || workflowInstance.status eq 'STARTING_ERROR' || workflowInstance.status eq 'EXECUTING' || workflowInstance.status eq 'EXECUTING_ERROR' || workflowInstance.status eq 'SUSPENDED'}">
			    <a href="javascript:submit('abort', '<spring:message code="workflow.instance.action.abort.confirm" />')" class="button"><spring:message code="workflow.instance.action.abort"/></a>
		    </c:if>
		    <c:if test="${workflowInstance.status eq 'SUSPENDED' }">
		  	    <a href="javascript:submit('resume')" class="button"><spring:message code="workflow.instance.action.resume"/></a>
		    </c:if>
		    <c:if test="${workflowInstance.status eq 'EXECUTING' }">
		   	    <a href="javascript:submit('suspend')" class="button"><spring:message code="workflow.instance.action.suspend"/></a>
		    </c:if>
		    <c:if test="${workflowInstance.status eq 'STARTING_ERROR' || workflowInstance.status eq 'EXECUTING_ERROR' || workflowInstance.status eq 'ABORTING_ERROR'}">
		  	    <a href="javascript:submit('retry')" class="button"><spring:message code="workflow.instance.action.retry"/></a>
		    </c:if>
	    </p>
    </workflow-ui:adminAccess>
</div>

<%@ include file="../../footer.jspf" %>