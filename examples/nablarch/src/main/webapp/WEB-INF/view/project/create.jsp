<%@page import="com.nablarch.example.app.web.common.code.ProjectClass"%>
<%@page import="com.nablarch.example.app.web.common.code.ProjectType"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="n" uri="http://tis.co.jp/nablarch" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
    <head>
        <%-- javascript --%>
        <n:script type="text/javascript" src="/javascripts/lib/jquery-1.11.2.min.js"></n:script>
        <n:script type="text/javascript" src="/javascripts/projectInput.js"></n:script>
        <title>プロジェクト登録画面</title>
    </head>

    <body>
        <div class="mainContents">
            <n:include path="/WEB-INF/view/common/menu.jsp" />
            <n:include path="/WEB-INF/view/common/header.jsp" />
        </div>
        <section>
            <n:form useToken="true">
                <div class="title-nav">

                    <span class="page-title">プロジェクト登録画面</span>
                    <div class="button-nav">
                        <n:forInputPage>
                            <div class="button-block real-button-block">
                                <n:button uri="/action/project/confirmOfCreate" cssClass="btn btn-raised btn-success">登録</n:button>
                            </div>
                            <div class="button-block link-button-block">
                                <n:a id="topBackLink" href="#" cssClass="btn btn-raised btn-default">戻る</n:a>
                            </div>
                        </n:forInputPage>
                        <n:forConfirmationPage>
                            <n:button uri="/action/project/backToNew" cssClass="btn btn-raised btn-default">入力へ戻る</n:button>
                            <n:button uri="/action/project/create" cssClass="btn btn-raised btn-success" allowDoubleSubmission="false">確定</n:button>
                        </n:forConfirmationPage>
                    </div>
                </div>
                <div class="message-area margin-top">
                    <n:errors filter="global" cssClass="message-error"/>
                </div>
                <h2 class="font-group">
                    プロジェクト情報
                </h2>
                <table class="table">
                    <tbody>
                        <tr>
                            <th class="width-250 required">
                                プロジェクト名
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:text name="form.projectName" maxlength="64" cssClass="form-control width-300" errorCss="input-error" />
                                    <n:error errorCss="message-error" name="form.projectName" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th class="required">
                                プロジェクト種別
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:set var="projectTypeList" value="<%= ProjectType.values() %>"/>
                                    <n:select name="form.projectType"
                                              listName="projectTypeList"
                                              elementValueProperty="code"
                                              elementLabelProperty="label"
                                              elementLabelPattern="$LABEL$"
                                              cssClass="form-control"/>
                                    <n:error errorCss="message-error" name="form.projectType" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th class="required">
                                プロジェクト分類
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:set var="projectClassList" value="<%= ProjectClass.values() %>"/>
                                    <n:select name="form.projectClass"
                                              listName="projectClassList"
                                              elementValueProperty="code"
                                              elementLabelProperty="label"
                                              elementLabelPattern="$LABEL$"
                                              cssClass="form-control"/>
                                    <n:error errorCss="message-error" name="form.projectClass" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                プロジェクトマネージャー
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:text name="form.projectManager" maxlength="64" cssClass="form-control width-300" errorCss="input-error"/>
                                    <n:error errorCss="message-error" name="form.projectManager" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                プロジェクトリーダー
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:text name="form.projectLeader" maxlength="64" cssClass="form-control width-300" errorCss="input-error"/>
                                    <n:error errorCss="message-error" name="form.projectLeader" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th class="required">
                                顧客名
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:text name="form.clientId" maxlength="10" readonly="true" cssClass="form-control input-label" tabindex="-1" />
                                    <n:text name="form.clientName" maxlength="64" readonly="true" cssClass="form-control  input-label" tabindex="-1" />
                                </div>
                                <n:forInputPage>
                                    <n:a href="/action/client/index" id="client_pop"><n:img src="/images/glass.png" alt="glass"/></n:a>
                                </n:forInputPage>
                                <n:error errorCss="message-error" name="form.clientId" />
                                <n:error errorCss="message-error" name="form.clientName" />
                            </td>
                        </tr>
                        <tr>
                            <th>
                                プロジェクト開始日
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:text name="form.projectStartDate" nameAlias="form.date" valueFormat="exampleDateTime{yyyy/MM/dd}" cssClass="form-control datepicker" errorCss="input-error"/>
                                    <n:error errorCss="message-error" name="form.projectStartDate" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                プロジェクト終了日
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:text name="form.projectEndDate" nameAlias="form.date" valueFormat="exampleDateTime{yyyy/MM/dd}" cssClass="form-control datepicker" errorCss="input-error" />
                                    <n:error errorCss="message-error" name="form.projectEndDate" />
                                    <n:error errorCss="message-error" name="form.validProjectPeriod" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                備考
                            </th>
                            <td>
                                <div class="form-group">
                                    <n:textarea rows="5" cols="50" name="form.note" cssClass="form-control" errorCss="input-error"/>
                                    <n:error errorCss="message-error" name="form.note" />
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                売上高
                            </th>
                            <td>
                                <n:forInputPage>
                                <div class="form-group">
                                    <n:text name="form.sales" maxlength="9" cssClass="form-control width-200" errorCss="input-error" style="float:left;" />
                                    <div style="display:table-cell;height:30px;vertical-align:bottom;">千円</div>
                                    <div style="clear:left;"><n:error errorCss="message-error" name="form.sales" /></div>
                                </div>
                                </n:forInputPage>
                                <n:forConfirmationPage>
                                    <n:write name="form.sales" valueFormat="decimal{###,###,### 千円}" />
                                </n:forConfirmationPage>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                売上原価
                            </th>
                            <td>
                                <n:forInputPage>
                                <div class="form-group">
                                    <n:text name="form.costOfGoodsSold" maxlength="9" cssClass="form-control width-200" errorCss="input-error" style="float:left;" />
                                    <div style="display:table-cell;height:30px;vertical-align:bottom;">千円</div>
                                    <div style="clear:left;"><n:error errorCss="message-error" name="form.costOfGoodsSold" /></div>
                                </div>
                                </n:forInputPage>
                                <n:forConfirmationPage>
                                    <n:write name="form.costOfGoodsSold" valueFormat="decimal{###,###,### 千円}" />
                                </n:forConfirmationPage>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                販管費
                            </th>
                            <td>
                                <n:forInputPage>
                                <div class="form-group">
                                    <n:text name="form.sga" maxlength="9" cssClass="form-control width-200" errorCss="input-error" style="float:left;" />
                                    <div style="display:table-cell;height:30px;vertical-align:bottom;">千円</div>
                                    <div style="clear:left;"><n:error errorCss="message-error" name="form.sga" /></div>
                                </div>
                                </n:forInputPage>
                                <n:forConfirmationPage>
                                    <n:write name="form.sga" valueFormat="decimal{###,###,### 千円}" />
                                </n:forConfirmationPage>
                            </td>
                        </tr>
                        <tr>
                            <th>
                                本社配賦
                            </th>
                            <td>
                                <n:forInputPage>
                                <div class="form-group">
                                    <n:text name="form.allocationOfCorpExpenses" maxlength="9" cssClass="form-control width-200" errorCss="input-error" style="float:left;" />
                                    <div style="display:table-cell;height:30px;vertical-align:bottom;">千円</div>
                                    <div style="clear:left;"><n:error errorCss="message-error" name="form.allocationOfCorpExpenses" /></div>
                                </div>
                                </n:forInputPage>
                                <n:forConfirmationPage>
                                    <n:write name="form.allocationOfCorpExpenses" valueFormat="decimal{###,###,### 千円}" />
                                </n:forConfirmationPage>
                            </td>
                        </tr>
                        <n:forConfirmationPage>
                            <tr>
                                <td>
                                    売上総利益
                                </td>
                                <td>
                                    <n:write name="form.grossProfit" valueFormat="decimal{###,###,### 千円}" />
                                    <n:error errorCss="message-error" name="form.grossProfit" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    配賦前利益
                                </td>
                                <td>
                                    <n:write name="form.profitBeforeAllocation" valueFormat="decimal{###,###,### 千円}" />
                                    <n:error errorCss="message-error" name="form.profitBeforeAllocation" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    配賦前利益率
                                </td>
                                <td>
                                    <n:write name="form.profitRateBeforeAllocation" valueFormat="decimal{##0.0 %}" />
                                    <n:error errorCss="message-error" name="form.profitRateBeforeAllocation" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    営業利益
                                </td>
                                <td>
                                    <n:write name="form.operatingProfit" valueFormat="decimal{###,###,### 千円}" />
                                    <n:error errorCss="message-error" name="form.operatingProfit" />
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    営業利益率
                                </td>
                                <td>
                                    <n:write name="form.operatingProfitRate" valueFormat="decimal{##0.0 %}" />
                                     <n:error errorCss="message-error" name="form.operatingProfitRate" />
                                </td>
                            </tr>
                        </n:forConfirmationPage>
                    </tbody>
                </table>

                <div class="title-nav page-footer">
                    <div class="button-nav">
                        <n:forInputPage>
                            <div class="button-block real-button-block">
                                <n:button uri="/action/project/confirmOfCreate" cssClass="btn btn-raised btn-success">登録</n:button>
                            </div>
                            <div class="button-block link-button-block">
                                <n:a id="bottomBackLink" href="#" cssClass="btn btn-raised btn-default">戻る</n:a>
                            </div>
                        </n:forInputPage>
                        <n:forConfirmationPage>
                            <n:button uri="/action/project/backToNew" cssClass="btn btn-raised btn-default">入力へ戻る</n:button>
                            <n:button uri="/action/project/create" cssClass="btn btn-raised btn-success" allowDoubleSubmission="false">確定</n:button>
                        </n:forConfirmationPage>
                    </div>
                </div>
            </n:form>
        </section>
        <n:include path="/WEB-INF/view/common/footer.jsp" />
        <n:forInputPage>
            <n:script type="text/javascript">
                $(function(){
                    setListUrlTo("topBackLink");
                    setListUrlTo("bottomBackLink");
                });
            </n:script>
        </n:forInputPage>
    </body>
</html>
