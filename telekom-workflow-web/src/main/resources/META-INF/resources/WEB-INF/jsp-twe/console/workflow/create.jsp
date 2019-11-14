<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ include file="../../header.jspf" %>
<workflow-ui:menu activeTab="create" />

<div id="page">
	<div class="inner clear">

		<c:choose>
			<c:when test="${not empty error}">
				<div class="message error">
					<spring:message code="workflow.create.single.error.unknown" arguments="${error}" />
				</div>
			</c:when>
			<c:when test="${not empty form.refNum}">
				<div class="message success">
					<c:url value="${urlPrefix}/console/workflow/instances/${form.refNum}" var="url"/>
					<spring:message code="workflow.create.single.success" />&nbsp;<a href="${url}"><c:out value="${form.refNum}"/></a>
				</div>
			</c:when>
			<c:when test="${not empty batchForm.refNums}" >
				<div class="message success">
					<spring:message code="workflow.create.batch.success" />&nbsp;<c:forEach items="${batchForm.refNums}" var="refNum" varStatus="status"><c:url value="${urlPrefix}/console/workflow/instances/${refNum}" var="url"/><c:if test="${!status.first}">, </c:if><a href="${url}"><c:out value="${refNum}"/></a></c:forEach>
				</div>
			</c:when>
		</c:choose>

		<div class="box">
			<h1><spring:message code="workflow.create.single.title" /></h1>
			<form:form method="POST" modelAttribute="form" action="create">
				<table class="form">
					<tbody>
						<tr>
							<th>
								<label class="form-header ${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('workflowName') ? 'error' : ''}" for="i01">
									<spring:message code="workflowinstance.name"/>:
									<span class="req">*</span>
								</label>
							</th>
							<td>
								<form:select path="workflowName" id="i01" items="${workflowNames}" >
									<c:forEach items="${workflowNames}" var="name">
										<c:choose>
	            							<c:when test="${form.workflowName eq name}">
												<form:option value="${name}" selected="true">${name}</form:option>
		                            		</c:when>
			                            	<c:otherwise>
		                            			<form:option value="${name}">${name}</form:option>
			                            	</c:otherwise>
	                            		</c:choose>
	                    		  	</c:forEach>
								</form:select>
								<form:errors path="workflowName" element="p" cssClass="message error inline"/>
							</td>
						</tr>
						<tr>
							<th>
								<label class="form-header ${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('workflowVersion') ? 'error' : ''}" for="i02">
									<spring:message code="workflowinstance.version"/>:
								</label>
							</th>
							<td>
								<form:input path="workflowVersion" id="i02" cssClass="text ${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('workflowVersion') ? 'error' : ''}"/>
								<form:errors path="workflowVersion" element="p" cssClass="message error inline"/>
							</td>
						</tr>
						<tr>
							<th>
								<label class="form-header" for="i03">
									<spring:message code="workflowinstance.label1"/>:
								</label>
							</th>
							<td><form:input path="label1" id="i03" cssClass="text"/></td>
						</tr>
						<tr>
							<th>
								<label class="form-header" for="i04">
									<spring:message code="workflowinstance.label2"/>:
								</label>
							</th>
							<td><form:input path="label2" id="i04" cssClass="text"/></td>
						</tr>
						<tr>
							<th>
								<label class="form-header ${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('arguments') ? 'error' : ''}" for="i05">
									<spring:message code="workflowinstance.arguments"/>:
								</label>
							</th>
							<td>
								<form:textarea path="arguments" id="i05" cssClass="${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('arguments') ? 'error' : ''}"/>
								<form:errors path="arguments" element="p" cssClass="message error inline"/>
							</td>
						</tr>
					</tbody>
				</table>
                <p><spring:message code="workflow.create.single.syntax" /></p>
			    <button><spring:message code="workflow.create.single.action"/></button>
			</form:form>
		</div>

		<div class="box">
			<h1><spring:message code="workflow.create.batch.title" /></h1>
			<form:form method="POST" modelAttribute="batchForm" action="batchCreate">
				<table class="form">
					<tbody>
						<tr>
							<th>
								<label class="form-header ${requestScope['org.springframework.validation.BindingResult.batchForm'].hasFieldErrors('batchRequest') ? 'error' : ''}" for="i01">
									<spring:message code="workflow.create.batch.request"/>:
									<span class="req">*</span>
								</label>
							</th>
							<td>
								<form:textarea path="batchRequest" id="i05" cssClass="${requestScope['org.springframework.validation.BindingResult.form'].hasFieldErrors('batchRequest') ? 'error' : ''}"/>
								<form:errors path="batchRequest" element="p" cssClass="message error inline"/>
							</td>
						</tr>
					</tbody>
				</table>
				<p><spring:message code="workflow.create.batch.syntax" /></p>
			    <button><spring:message code="workflow.create.batch.action" /></button>
			</form:form>
		</div>
	</div>
</div>

<%@ include file="../../footer.jspf" %>