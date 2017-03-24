package com.nablarch.example.app.web.action;

import nablarch.common.web.session.SessionUtil;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.HttpRequestWrapper;

/**
 * ログイン状態チェックハンドラ 。
 *
 * @author Nabu Rakutaro
 */
@Published
public class LoginUserPrincipalCheckHandler implements Handler<Object, Object> {

    /**
     * セッションからユーザ情報を取得できなかった場合は、ログイン画面を表示。
     *
     * @param data リクエストデータ
     * @param context 実行コンテキスト
     * @return HTTPレスポンス
     */
    @Override
    public Object handle(Object data, ExecutionContext context) {
        String requestPath = ((HttpRequestWrapper) context.getCurrentRequestObject()).getRequestPath();
        if (SessionUtil.orNull(context, "userContext") == null && !requestPath.equals("/action/login")) {
            return new HttpResponse("/WEB-INF/view/login/index.jsp");
        }
        return context.handleNext(data);
    }
}
