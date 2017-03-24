package com.nablarch.example.app.web.common.code;

import nablarch.core.util.annotation.Published;

/**
 * 並び順を定義したEnum。
 */
@Published
public enum SortOrder implements CodeEnum {
    /** 昇順 */
    ASC("asc", "昇順"),
    /** 降順 */
    DESC("desc", "降順");

    /** 並び順のラベル */
    private String label;
    /** 並び順のコード */
    private String code;

    /**
     * コンストラクタ。
     * @param code コード値
     * @param label ラベル
     */
    SortOrder(String code, String label) {
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
