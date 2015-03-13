<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="../../header.jsp" %>
<workflow-ui:menu activeTab="workflows" />

<div id="page">
    <div class="inner clear">
    	<c:if test="${not empty error}">
	   		<div class="message error">
				<spring:message code="${errorMessage}" arguments="${error}" />
			</div>
    	</c:if>

        <div class="box">
	        <div class="dataTables_wrapper">
	            <h1><spring:message code="workflow.item.title.${workItem.type}"/></h1>
				<form:form method="POST" commandName="form">
					<c:if test="${workItem.type != 'TIMER'}">
						<table class="form">
							<tbody>
								<tr>
									<th>
										<label class="form-header ${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('result') ? 'error' : ''}" for="i01">
											<spring:message code="workflow.item.resultlabel.${workItem.type}"/>:
										</label>
									</th>
									<td>
										<form:textarea path="result" id="i01"/>
										<form:errors path="result" element="p" cssClass="message error inline"/>
									</td>
								</tr>
							</tbody>
						</table>
					</c:if>
           			<p><spring:message code="workflow.item.description.${workItem.type}"/></p>
           			<workflow-ui:adminAccess>
	           			<c:url value="/console/workflow/instances/${workItem.woinRefNum}" var="url"/>
	           			<a href="${url}" class="btn-01"><spring:message code="workflow.item.cancel"/></a>
						<button><spring:message code="workflow.item.action.${workItem.type}"/></button>
					</workflow-ui:adminAccess>
				</form:form>
	        </div>
		</div>
	</div>
</div>

<%@ include file="../../footer.jsp" %>