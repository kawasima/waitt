package com.nablarch.example.app.web.common.code;

import nablarch.core.util.annotation.Published;

/**
 * プロジェクトのソートキーを定義したEnum。
 */
@Published
public enum ProjectSortKey implements CodeEnum {
    /** プロジェクトID */
    ID("id", "プロジェクトＩＤ"),
    /** プロジェクト名 */
    NAME("name", "プロジェクト名"),
    /** プロジェクト開始日 */
    SDATE("sdate", "プロジェクト開始日"),
    /** プロジェクト終了日 */
    EDATE("edate", "プロジェクト終了日");

    /** ソートキーのラベル */
    private String label;
    /** ソートキーのコード */
    private String code;

    /**
     * コンストラクタ。
     * @param code コード値
     * @param label ラベル
     */
    ProjectSortKey(String code, String label) {
        this.label = label;
        this.code = code;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getCode() {
        return code;
    }
}
