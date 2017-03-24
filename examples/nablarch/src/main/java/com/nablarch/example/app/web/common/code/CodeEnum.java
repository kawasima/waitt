package com.nablarch.example.app.web.common.code;

import nablarch.core.util.annotation.Published;

/**
 * コード値を定義したEnumが実装するインタフェース。
 */
@Published(tag = "architect")
public interface CodeEnum {
    /**
     * ラベルを返却する。
     * @return ラベル
     */
    String getLabel();

    /**
     * コード値を返却する。
     * @return コード値
     */
    String getCode();
}
