package com.nablarch.example.app.web.form;

import java.io.Serializable;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.PropertyName;
import nablarch.core.validation.ee.Required;

/**
 * 処理対象パラメータ。
 *
 * @author Nabu Rakutaro
 */
@Published
public class ProjectTargetForm implements Serializable {

    /** シリアルバージョンUID */
    private static final long serialVersionUID = 1L;

    /** プロジェクトID */
    @Required
    private String projectId;

    /**
     * コンストラクタ
     *
     */
    public ProjectTargetForm() {
    }

    /**
     * プロジェクトIDを取得する。
     *
     * @return プロジェクトID
     */
    public String getProjectId() {
        return this.projectId;
    }

    /**
     * プロジェクトIDを設定する。
     *
     * @param projectId 設定するプロジェクトID
     *
     */
    @PropertyName("プロジェクトID")
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

}
