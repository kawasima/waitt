<%@page import="com.nablarch.example.app.web.common.code.ClientSortKey"%>
<%@page import="com.nablarch.example.app.web.common.code.SortOrder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="n" uri="http://tis.co.jp/nablarch" %>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags/listSearchResult" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%-- javascript --%>
        <n:script type="text/javascript" src="/javascripts/lib/jquery-1.11.2.min.js"></n:script>
        <n:script type="text/javascript" src="/javascripts/lib/jquery-ui.min.js"></n:script>
        <n:script type="text/javascript" charset="UTF-8" src="/javascripts/lib/jquery-ui-datepicker-ja.js"></n:script>
        <n:script type="text/javascript" charset="UTF-8" src="/javascripts/common.js"></n:script>
        <n:script type="text/javascript" charset="UTF-8" src="/javascripts/clientList.js"></n:script>
        <!-- Bootstrap Core CSS -->
        <link href="//cdn.jsdelivr.net/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
        <link href="//cdn.jsdelivr.net/bootstrap.material-design/0.5.6/css/bootstrap-material-design.css" rel="stylesheet">
        <link href="//cdn.jsdelivr.net/bootstrap.material-design/0.5.6/css/ripples.min.css" rel="stylesheet">
        <%-- stylesheet --%>
        <n:link rel="stylesheet" type="text/css" href="/stylesheets/project.css" />
        <n:link rel="stylesheet" type="text/css" href="/stylesheets/common.css" />
        <n:link rel="stylesheet" href="/stylesheets/jquery-ui.css" />
        <title>顧客検索一覧画面</title>
    </head>
    <body>
        <n:include path="/WEB-INF/view/common/noscript.jsp" />
        <div class="mainContents">
            <n:include path="/WEB-INF/view/common/subheader.jsp" />
            <div class="dialogContents">
                <n:form method="GET" action="/action/client/list">
                    <div class="title-nav">
                        <span>顧客検索一覧画面</span>
                        <div class="button-nav">
                            <button class="btn btn-raised btn-success clearButton">クリア</button>
                            <button class="btn btn-raised btn-default closeButton">閉じる</button>
                        </div>
                    </div>
                    <div class="floatClear"></div>
                    <br/>
                    <h4 class="font-group">検索条件</h4>
                    <div id="searchCondition">
                        <table class="table">
                            <col style="width: 30%;" />
                            <col style="width: 70%;" />
                            <tbody>
                                <tr>
                                    <th class="item-norequired">顧客名</th>
                                    <td>
                                        <n:text name="form.clientName" size="40" maxlength="64" cssClass="form-control" errorCss="input-error input-text" />
                                        <n:error errorCss="message-error" name="form.clientName" />
                                    </td>
                                </tr>
                                <tr>
                                    <th class="item-norequired">業種</th>
                                    <td>
                                        <n:select
                                            listName="industries"
                                            elementValueProperty="industryCode"
                                            elementLabelProperty="industryName"
                                            name="form.industryCode"
                                            withNoneOption="true"
                                            cssClass="btn dropdown-toggle"
                                            errorCss="input-error input-select" />
                                        <n:error errorCss="message-error" name="form.industryCode" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <div class="sort-nav">
                            <n:set var="sortKeyList" value="<%= ClientSortKey.values() %>"/>
                            <n:select
                                    id="sortKey"
                                    name="form.sortKey"
                                    listName="sortKeyList"
                                    elementValueProperty="code"
                                    elementLabelProperty="label"
                                    elementLabelPattern="$LABEL$"
                                    cssClass="btn dropdown-toggle"/>
                            <n:set var="sortOrderList" value="<%= SortOrder.values() %>"/>
                            <n:select
                                    id="sortDir"
                                    name="form.sortDir"
                                    listName="sortOrderList"
                                    elementValueProperty="code"
                                    elementLabelProperty="label"
                                    elementLabelPattern="$LABEL$"
                                    cssClass="btn dropdown-toggle"/>
                            <input type="submit" class="btn btn-raised btn-default" value="検索"/>
                            <input type="hidden" name="form.pageNumber" value="1" />
                        </div>
                    </div>
                    <br />
                </n:form>
                <span class="font-group">
                    検索結果
                </span>
                <span class="search-result-count">
                    <c:if test="${not empty clients}">
                        <n:write name="clients.pagination.resultCount" />
                    </c:if>
                        <c:if test="${empty clients}">
                            0
                    </c:if>
                </span>
                <n:form method="GET">
                    <c:url value="/action/client/list" var="uri">
                        <c:param name="form.clientName" value="${form.clientName}"/>
                        <c:param name="form.industryCode" value="${form.industryCode}"/>
                        <c:param name="form.sortKey" value="${form.sortKey}"/>
                        <c:param name="form.sortDir" value="${form.sortDir}"/>
                    </c:url>
                    <app:listSearchResult
                        currentPageNumberCss="form-control"
                        pagingCss="paging"
                        usePageNumberSubmit="true"
                        prevSubmitLabel="«"
                        nextSubmitLabel="»"
                        listSearchInfoName="form"
                        searchUri="${uri}"
                        resultSetName="clients"
                        useResultCount="false"
                        prevSubmitCss="prev-page-link"
                        nextSubmitCss="next-page-link"
                        resultSetCss="table table-striped table-hover">
                        <jsp:attribute name="headerRowFragment">
                            <tr>
                                <th>顧客ID</th>
                                <th>顧客名</th>
                                <th>業種</th>
                            </tr>
                        </jsp:attribute>
                        <jsp:attribute name="bodyRowFragment">
                            <tr class="list-table x_color_row_table_target">
                                <td>
                                    <!-- .clientLinkのonClickイベントによって親画面に値が設定される -->
                                    <a href="#" class="clientLink">
                                        <input type="hidden" class="clientId" value="<n:write name='row.clientId' />" />
                                        <input type="hidden" class="clientName" value="<n:write name='row.clientName' />" />
                                        <n:write name="row.clientId"/>
                                    </a>
                                </td>
                                <td>
                                    <n:write name="row.clientName"/>
                                </td>
                                <td>
                                    <n:write name="row.industryName" />
                                </td>
                            </tr>
                        </jsp:attribute>
                    </app:listSearchResult>
                    <br/>
                    <div class="title-nav">
                        <div class="button-nav">
                            <button class="btn btn-raised btn-success clearButton">クリア</button>
                            <button class="btn btn-raised btn-default closeButton">閉じる</button>
                        </div>
                    </div>
                </n:form>
            </div>
            <n:include path="/WEB-INF/view/common/footer.jsp" />
        </div>
    </body>
</html>