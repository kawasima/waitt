package com.nablarch.example.app.web.form;

import nablarch.core.util.annotation.Published;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * プロジェクト一括更新フォーム
 *
 * @author Nabu Rakutaro
 */
@Published
public class ProjectBulkForm implements Serializable {

    /** シリアルバージョンUID */
    private static final long serialVersionUID = 1L;

    /** プロジェクト情報のリスト */
    @Valid
    private List<InnerProjectForm> projectList = new ArrayList<>();

    /**
     * プロジェクト情報のリストを返す。
     *
     * @return プロジェクト情報のリスト
     */
    public List<InnerProjectForm> getProjectList() {
        return projectList;
    }

    /**
     * プロジェクト情報のリストを設定する。
     *
     * @param projectList 設定したいプロジェクト情報のリスト
     */
    public void setProjectList(List<InnerProjectForm> projectList) {
        this.projectList = projectList;
    }
}
