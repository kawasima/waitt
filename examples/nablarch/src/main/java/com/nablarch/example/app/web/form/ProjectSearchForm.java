package com.nablarch.example.app.web.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ee.Domain;

/**
 * プロジェクト検索一覧フォーム
 *
 * @author Nabu Rakutaro
 */
@Published
public class ProjectSearchForm extends SearchFormBase implements Serializable {

    /** シリアルバージョンUID */
    private static final long serialVersionUID = 1L;

    /** 顧客ID */
    @Domain("id")
    private String clientId;

    /** 顧客名 */
    @Domain("clientName")
    private String clientName;

    /** プロジェクト名 */
    @Domain("projectName")
    private String projectName;

    /** プロジェクト種別 */
    @Domain("projectType")
    private String projectType;

    /** プロジェクト分類 */
    @Valid
    private List<ProjectClass> projectClass;

    /** プロジェクト開始日（FROM） */
    @Domain("date")
    private String projectStartDateBegin;

    /** プロジェクト開始日（TO） */
    @Domain("date")
    private String projectStartDateEnd;

    /** プロジェクト終了日（FROM） */
    @Domain("date")
    private String projectEndDateBegin;

    /** プロジェクト終了日（TO） */
    @Domain("date")
    private String projectEndDateEnd;

    /** 並び順項目 */
    @Domain("projectAlignmentItem")
    private String sortKey;

    /** 並び順 */
    @Domain("alignment")
    private String sortDir;

    /**
     * プロジェクト検索結果を受け取るためのフォーム
     */
    public ProjectSearchForm() {
    }

    /**
     * 顧客IDを取得する。
     * @return 顧客ID
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * 顧客IDを設定する。
     * @param clientId 顧客ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * 顧客名を取得する。
     * @return 顧客名
     */
    public String getClientName() {
        return this.clientName;
    }

    /**
     * 顧客名を設定する。
     * @param clientName 顧客名
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * プロジェクト名を取得する。
     * @return プロジェクト名
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * プロジェクト名を設定する。
     * @param projectName プロジェクト名
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * プロジェクト種別を取得する。
     * @return プロジェクト種別
     */
    public String getProjectType() {
        return this.projectType;
    }

    /**
     * プロジェクト種別を設定する。
     * @param projectType プロジェクト種別
     */
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    /**
     * プロジェクト分類を取得する。
     * @return プロジェクト分類
     */
    public String[] getProjectClass() {

        if (projectClass == null) {
            return null;
        }

        List<String> stringList = new ArrayList<String>();
        for (ProjectClass bean: projectClass) {
            stringList.add(bean.getProjectClass());
        }

        return stringList.toArray(new String[stringList.size()]);
    }

    /**
     * プロジェクト分類を設定する。
     * @param projectClass プロジェクト分類
     */
    public void setProjectClass(String[] projectClass) {

        if (projectClass == null) {
            this.projectClass = null;
            return;
        }

        boolean hasValue = false;

        this.projectClass = new ArrayList<ProjectSearchForm.ProjectClass>();
        for (String project : projectClass) {
            if (StringUtil.hasValue(project)) {
                hasValue = true;
            }
            ProjectClass bean = new ProjectClass();
            bean.setProjectClass(project);
            this.projectClass.add(bean);
        }

        // 配列内に空文字のみの場合はnullを設定(検索条件に空文字が設定されるのを回避)
        if (!hasValue) {
            this.projectClass = null;
        }
    }

    /**
     * プロジェクト開始日（FROM）を返す。
     *
     * @return プロジェクト開始日（FROM）
     */
    public String getProjectStartDateBegin() {
        return this.projectStartDateBegin;
    }

    /**
     * プロジェクト開始日（FROM）文字列をセットする。
     *
     * @param projectStartDateBegin プロジェクト開始日（FROM）
     */
    public void setProjectStartDateBegin(String projectStartDateBegin) {
        this.projectStartDateBegin = projectStartDateBegin;
    }

    /**
     * プロジェクト開始日（TO）を返す。
     *
     * @return プロジェクト開始日（TO）
     */
    public String getProjectStartDateEnd() {
        return this.projectStartDateEnd;
    }

    /**
     * プロジェクト開始日（TO）文字列をセットする。
     *
     * @param projectStartDateEnd プロジェクト開始日（TO）
     */
    public void setProjectStartDateEnd(String projectStartDateEnd) {
        this.projectStartDateEnd = projectStartDateEnd;
    }

    /**
     * プロジェクト終了日（FROM）を返す。
     *
     * @return プロジェクト終了日（FROM）
     */
    public String getProjectEndDateBegin() {
        return this.projectEndDateBegin;
    }

    /**
     * プロジェクト終了日（FROM）文字列をセットする。
     *
     * @param projectEndDateBegin プロジェクト終了日（FROM）
     */
    public void setProjectEndDateBegin(String projectEndDateBegin) {
        this.projectEndDateBegin = projectEndDateBegin;
    }

    /**
     * プロジェクト終了日（TO）を返す。
     *
     * @return プロジェクト終了日（TO）
     */
    public String getProjectEndDateEnd() {
        return this.projectEndDateEnd;
    }

    /**
     * プロジェクト終了日（TO）文字列をセットする。
     *
     * @param projectEndDateEnd プロジェクト終了日（TO）
     */
    public void setProjectEndDateEnd(String projectEndDateEnd) {
        this.projectEndDateEnd = projectEndDateEnd;
    }

    /**
     * 並び順項目を取得する。
     * @return 並び順項目
     */
    public String getSortKey() {
        return this.sortKey;
    }

    /**
     * 並び順項目を設定する。
     * @param sortKey 並び順項目
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * sortKeyとsortDirの値からソートIDを作成し、設定する。
     * sortKeyとsortDirに正常な値が格納されていない場合は"idAsc"を設定する。
     *
     * @return ソートID
     */
    public String getSortId() {
        String sortId = "idAsc";
        if (StringUtil.hasValue(sortKey) && StringUtil.hasValue(sortDir)) {
            if ("id".equals(sortKey)) {
                sortId = "id";
            } else if ("name".equals(sortKey)) {
                sortId = "name";
            } else if ("sdate".equals(sortKey)) {
                sortId = "startDate";
            } else if ("edate".equals(sortKey)) {
                sortId = "endDate";
            } else {
                sortId = "id";
            }
            sortId += sortDir.equals("desc") ? "Desc" : "Asc";
        }
        return sortId;
    }

    /**
     * ソートIDの値からsortKeyとsortDirを判定し、設定する。
     *
     * @param sortId ソートID
     */
    public void setSortId(String sortId) {
        this.sortDir = "asc";
        this.sortKey = "name";

        if (StringUtil.hasValue(sortId)) {
            if (sortId.startsWith("id")) {
                this.sortKey = "id";
            } else if (sortId.startsWith("name")) {
                this.sortKey = "name";
            } else if (sortId.startsWith("startDate")) {
                this.sortKey = "sdate";
            } else if (sortId.startsWith("sortKey")) {
                this.sortKey = "edate";
            }
            this.sortDir = sortId.endsWith("Desc") ? "desc" : "asc";
        }
    }

    /**
     * 並び順を取得する。
     * @return 並び順
     */
    public String getSortDir() {
        return this.sortDir;
    }

    /**
     * 並び順を設定する。
     * @param sortDir 並び順
     */
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    /**
     * プロジェクト分類Beanクラス。
     * @author Nabu Rakutaro
     */
    static class ProjectClass implements Serializable {

        /** シリアルバージョンUID */
        private static final long serialVersionUID = 1L;

        /**
         * プロジェクト分類
         */
        @Domain("projectClass")
        private String projectClass;

        /**
         * プロジェクト分類を取得する。
         * @return プロジェクト分類
         */
        public String getProjectClass() {
            return projectClass;
        }

        /**
         * プロジェクト分類を設定する。
         * @param projectClass プロジェクト分類
         */
        public void setProjectClass(String projectClass) {
            this.projectClass = projectClass;
        }
    }

}
