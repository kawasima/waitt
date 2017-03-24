package com.nablarch.example.app.web.form;

import java.io.Serializable;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.PropertyName;
import nablarch.core.validation.ee.Domain;
import nablarch.core.validation.ee.Required;

/**
 * ログイン入力フォーム。
 *
 * @author Nabu Rakutaro
 */
@Published
public class LoginForm implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /** ログインID */
    @Required
    @Domain("loginId")
    private String loginId;

    /** パスワード */
    @Required
    @Domain("password")
    private String userPassword;

    /**
     * ログインIDを取得する。
     * @return ログインID
     */
    public String getLoginId() {
        return loginId;
    }

    /**
     * ログインIDを設定する。
     * @param loginId ログインID
     */
    @PropertyName("ログインID")
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    /**
     * パスワードを取得する。
     * @return パスワード
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * パスワードを設定する。
     * @param userPassword パスワード
     */
    @PropertyName("パスワード")
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

}
