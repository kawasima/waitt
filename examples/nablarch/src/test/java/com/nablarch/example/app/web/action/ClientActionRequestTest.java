package com.nablarch.example.app.web.action;

import com.nablarch.example.app.test.ExampleTestCaseInfo;
import com.nablarch.example.app.test.advice.SignedInAdvice;
import com.nablarch.example.app.test.ExampleHttpRequestTestTemplate;
import nablarch.fw.ExecutionContext;
import org.junit.Test;

/**
 * {@link ClientAction}のリクエスト単体テストクラス。
 *
 * @author Nabu Rakutaro
 */
public class ClientActionRequestTest extends ExampleHttpRequestTestTemplate {

    @Override
    protected String getBaseUri() {
        return "/action/client/";
    }

    /**
     * 顧客検索画面初期表示正常系ケース。
     */
    @Test
    public void indexNormal() {

        execute("indexNormal", new SignedInAdvice() {

            @Override
            public void afterExecute(ExampleTestCaseInfo testCaseInfo,
                                     ExecutionContext context) {
                assertBeanList(testCaseInfo.getSheetName(), "industry",
                        "industries", testCaseInfo, context);
            }
        });
    }

    /**
     * 顧客検索正常系ケース。
     */
    @Test
    public void listNormal() {

        execute("listNormal", new SignedInAdvice() {

            @Override
            public void afterExecute(ExampleTestCaseInfo testCaseInfo,
                                     ExecutionContext context) {
                assertBeanList(testCaseInfo.getSheetName(), "client",
                        "clients", testCaseInfo, context);
            }
        });
    }

    /**
     * 顧客検索異常系ケース。
     */
    @Test
    public void listAbNormal() {
        execute("listAbNormal", new SignedInAdvice());
    }
}
