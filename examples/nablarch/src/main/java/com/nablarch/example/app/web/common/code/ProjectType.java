package com.nablarch.example.app.web.common.code;

import nablarch.core.util.annotation.Published;

/**
 * プロジェクト種別を定義したEnum。
 */
@Published
public enum ProjectType implements CodeEnum {
    /** 新規開発PJ */
    DEVELOPMENT("development", "新規開発PJ"),
    /** 保守PJ */
    MAINTENANCE("maintenance", "保守PJ");

    /** プロジェクト種別のラベル */
    private String label;
    /** プロジェクト種別のコード */
    private String code;

    /**
     * コンストラクタ。
     * @param code コード値
     * @param label ラベル
     */
    ProjectType(String code, String label) {
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
