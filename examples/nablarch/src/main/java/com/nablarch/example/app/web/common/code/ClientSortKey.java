package com.nablarch.example.app.web.common.code;

import nablarch.core.util.annotation.Published;

/**
 * 顧客を定義したEnum。
 */
@Published
public enum ClientSortKey implements CodeEnum {
    /** 顧客ID */
    ID("id", "顧客ＩＤ"),
    /** 顧客名 */
    NAME("name", "顧客名");

    /** 顧客のラベル */
    private String label;
    /** 顧客のコード */
    private String code;

    /**
     * コンストラクタ。
     * @param code コード値
     * @param label ラベル
     */
    ClientSortKey(String code, String label) {
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
