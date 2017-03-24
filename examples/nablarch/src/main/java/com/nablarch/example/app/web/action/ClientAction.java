package com.nablarch.example.app.web.action;

import nablarch.common.dao.EntityList;
import nablarch.common.dao.UniversalDao;
import nablarch.common.web.interceptor.InjectForm;
import nablarch.core.beans.BeanUtil;
import nablarch.core.message.ApplicationException;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.interceptor.OnError;

import com.nablarch.example.app.entity.Industry;
import com.nablarch.example.app.web.dto.ClientDto;
import com.nablarch.example.app.web.dto.ClientSearchDto;
import com.nablarch.example.app.web.form.ClientSearchForm;

/**
 * 顧客検索機能。
 *
 * @author Nabu Rakutaro
 *
 */
@Published
public class ClientAction {

    /**
     * 顧客検索画面を表示。
     *
     * @param request HTTPリクエスト
     * @param context 実行コンテキスト
     * @return HTTPレスポンス
     */
    public HttpResponse index(HttpRequest request, ExecutionContext context) {
        // プルダウンに使用する業種一覧を取得する。
        EntityList<Industry> industries = UniversalDao.findAll(Industry.class);
        context.setRequestScopedVar("industries", industries);
        return new HttpResponse("/WEB-INF/view/client/index.jsp");
    }

    /**
     * 検索結果を表示。
     *
     * @param request HTTPリクエスト
     * @param context 実行コンテキスト
     * @return HTTPレスポンス
     */
    @InjectForm(form = ClientSearchForm.class, prefix = "form")
    @OnError(type = ApplicationException.class, path = "forward://index")
    public HttpResponse list(HttpRequest request, ExecutionContext context) {
        ClientSearchForm form = context.getRequestScopedVar("form");
        ClientSearchDto condition = BeanUtil.createAndCopy(ClientSearchDto.class, form);

        EntityList<ClientDto> clients = UniversalDao
                .page(Integer.parseInt(form.getPageNumber()))
                .per(20L)
                .findAllBySqlFile(ClientDto.class, "SEARCH_CLIENT", condition);

        // プルダウンに使用する業種一覧を取得する。
        EntityList<Industry> industries = UniversalDao.findAll(Industry.class);
        context.setRequestScopedVar("industries", industries);

        context.setRequestScopedVar("clients", clients);
        return new HttpResponse("/WEB-INF/view/client/index.jsp");
    }
}
