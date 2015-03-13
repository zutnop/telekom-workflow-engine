<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="../../header.jsp" %>
<workflow-ui:menu activeTab="workflows" />

<div id="search">
    <form:form method="POST" commandName="instanceSearchForm" action="instances">
        <div class="inner clear">
            <p class="main">
                <span class="fields">
                    <label><spring:message code="workflow.search.name" /></label>
                    <form:select path="workflowName" cssClass="important" multiple="false">
                        <form:option value=""><spring:message code="workflow.search.name.all" /></form:option>
                        <c:forEach items="${graphNames}" var="name">
                            <form:option value="${name}">${name}</form:option>
                        </c:forEach>
                    </form:select>
                    <label><spring:message code="workflow.search.status" /></label>
                    <form:select path="status" cssClass="important" multiple="false">
                        <form:option value=""><spring:message code="workflow.search.status.all" /></form:option>
                        <c:forEach items="${workflowStatuses}" var="status">
                            <form:option value="${status}"><spring:message code="workflowinstance.status.facade.${status}" /></form:option>
                        </c:forEach>
                    </form:select>
                    <label><spring:message code="workflow.search.id" /></label><form:input path="id" cssClass="text important" />
                    <label><spring:message code="workflow.search.label1" /></label><form:input path="label1" cssClass="text important" />
                    <label><spring:message code="workflow.search.label2" /></label><form:input path="label2" cssClass="text important" />
                </span>
                <button onclick="$('#instancesTable').DataTable().page('first');$('#instancesTable').DataTable().state.save();"><spring:message
                        code="workflow.search.search" /></button>
            </p>
        </div>
    </form:form>
</div>
<div id="page">
    <div class="inner clear">
        <c:choose>
            <c:when test="${not empty actionError}">
                <div class="message error"><spring:message code="${actionError}" /></div>
            </c:when>
            <c:when test="${not empty actionMessage}">
                <div class="message"><spring:message code="${actionMessage}" /></div>
            </c:when>
        </c:choose>
        <div class="box">
            <h1><spring:message code="workflow.instances.title" /></h1>
            <workflow-ui:adminAccess>
	            <div id="massOperations" style="display:none">
	                <p><spring:message code="workflow.instances.action.selected.label" arguments='<span id="selectedInstances"></span>' /></p>
	                <a id="batchAbort" href="javascript:submit('abort', '<spring:message code="workflow.instances.action.abort.confirm"/>')" class="batch btn-01">
	                    <spring:message code="workflow.instance.action.abort" />
	                </a>
	                <a id="batchSuspend" href="javascript:submit('suspend')" class="batch btn-01"><spring:message code="workflow.instance.action.suspend" /></a>
	                <a id="batchResume" href="javascript:submit('resume')" class="batch btn-01"><spring:message code="workflow.instance.action.resume" /></a>
	                <a id="batchRetry" href="javascript:submit('retry')" class="batch btn-01"><spring:message code="workflow.instance.action.retry" /></a>
	            </div>
            </workflow-ui:adminAccess>
            <c:choose>
                <c:when test="${not empty error}">
                    <div class="message error"><spring:message code="${error}" /></div>
                </c:when>
                <c:when test="${not empty warning}">
                    <div class="message warning"><spring:message code="${warning}" /></div>
                </c:when>
                <c:otherwise>
                    <table id="instancesTable" class="data">
                        <thead>
                        <tr>
                            <workflow-ui:adminAccess>
                               <th class="w1p"><input type="checkbox" class="toggleAll" /></th>
                            </workflow-ui:adminAccess>
                            <th class="w1p right"><spring:message code="workflowinstance.refnum" /></th>
                            <th><spring:message code="workflowinstance.nameandversion" /></th>
                            <th><spring:message code="workflowinstance.label1" /></th>
                            <th><spring:message code="workflowinstance.label2" /></th>
                            <th><spring:message code="workflowinstance.created" /></th>
                            <th><spring:message code="workflow.instances.nextduedate" /></th>
                            <th><spring:message code="workflow.instances.humantask" /></th>
                            <th><spring:message code="workflowinstance.status" /></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>

                    <c:url value="/console/workflow/instances/" var="urlPrefix" />
                    <form method="post" action="${urlPrefix}action" id="actionForm">
                        <input type="hidden" name="action" id="action" />
                        <input type="hidden" name="refNums" />
                    </form>

                    <script type="text/javascript">
                        $(document).ready(function () {
                            var urlPrefix = '${urlPrefix}';
                            $('#instancesTable').dataTable({
                                lengthMenu: [20, 100, 1000],
                                pageLength: 20,
                                sDom: 'rt<"dataTables-length"l><"dataTables-pager"ip>',
                                processing: true,
                                serverSide: true,
                                ajax: {
                                    url: "${urlPrefix}search",
                                    type: "POST"
                                },
                                columns: [
                                    <workflow-ui:adminAccess>
	                                    {data: null, orderable: false, render: function (data, type, row, meta) {
	                                        return '<input type="checkbox" class="toggle" data-status="' + row.status + '"  data-refnum="' + row.refNum + '" />'
	                                    }},
                                    </workflow-ui:adminAccess>
                                    {data: "refNum", sClass: "w1p right", render: function (refNum, type, full) {
                                        return '<a href="' + urlPrefix + refNum + '">' + refNum + '</a>';
                                    }},
                                    {data: "workflowNameWithVersion"},
                                    {data: "label1"},
                                    {data: "label2"},
                                    {data: "dateCreatedText"},
                                    {data: "nextTimerDueDateText"},
                                    {data: "hasActiveHumanTask"},
                                    {data: "displayStatus"}
                                ],
                                order: [
                                    [ 5, "desc" ]
                                ],
                                stateSave: true,
                                stateDuration: -1
                            });

                            $('#instancesTable').on('order.dt', function () {
                                clearAndHidePossibleActions();
                            }).on('page.dt', function () {
                                clearAndHidePossibleActions();
                            });

                            $('.toggleAll').click(function (event) {
                                if (this.checked) {
                                    $('.toggle').each(function () {
                                        this.checked = true;
                                    });
                                } else {
                                    $('.toggle').each(function () {
                                        this.checked = false;
                                    });
                                }
                                displayActionButtons();
                            });
                            $(document).on('click', '.toggle', function (event) {
                                displayActionButtons();
                            });
                        });
                        function submit(action, confirmMsg) {
                            if (!confirmMsg || confirm(confirmMsg)) {
                                var actionForm = $("#actionForm");
                                $("#actionForm, input[name=action]").val(action);
                                var refNums = '';
                                $('.toggle').each(function () {
                                    if (this.checked) {
                                        var instanceStatus = $(this).attr('data-status');
                                        if (instanceStatus == 'ABORT' || instanceStatus == 'ABORTING') {
                                            return true;
                                        }
                                        if (refNums.length > 0) {
                                            refNums = refNums + ',';
                                        }
                                        refNums = refNums + $(this).attr('data-refNum');
                                    }
                                });
                                $("#actionForm, input[name=refNums]").val(refNums);
                                $("#actionForm").submit();
                            }
                        }
                        function clearAndHidePossibleActions() {
                            $('.toggleAll').attr('checked', false);
                            $('#massOperations').hide();
                        }
                        function displayActionButtons() {
                            var abort = ['NEW', 'STARTING', 'STARTING_ERROR', 'EXECUTING', 'EXECUTING_ERROR', 'SUSPENDED'];
                            var retry = ['STARTING_ERROR', 'EXECUTING_ERROR', 'ABORTING_ERROR'];
                            var selectedInstancesCount = $('input[class="toggle"]:checked').length;
                            if (selectedInstancesCount > 0) {
                                $(".batch").hide();
                                $('#selectedInstances').html(selectedInstancesCount);
                                $('.toggle').each(function () {
                                    if (this.checked) {
                                        var instanceStatus = $(this).attr('data-status');
                                        if ($.inArray(instanceStatus, abort) != -1) {
                                            $("#batchAbort").show();
                                        }
                                        if (instanceStatus == 'SUSPENDED') {
                                            $("#batchResume").show();
                                        }
                                        if (instanceStatus == 'EXECUTING') {
                                            $("#batchSuspend").show();
                                        }
                                        if ($.inArray(instanceStatus, retry) != -1) {
                                            $("#batchRetry").show();
                                        }
                                    }
                                });
                                $('#massOperations').show();
                            } else {
                                $('#massOperations').hide();
                            }
                        }
                    </script>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../../footer.jsp" %>
