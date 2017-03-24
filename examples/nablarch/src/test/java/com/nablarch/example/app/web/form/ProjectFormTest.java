package com.nablarch.example.app.web.form;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.test.core.db.DbAccessTestSupport;

import org.junit.Test;

/**
 * {@link ProjectForm} のテストクラス。
 *
 * @author Nabu Rakutaro
 */
public class ProjectFormTest extends DbAccessTestSupport {

    /** テスト対象クラス */
    private ProjectForm target = new ProjectForm();

    /**
     * {@link ProjectForm#getGrossProfit()} のテスト。
     */
    @Test
    public final void 売上総利益の取得テスト() {
        String sheetName ="売上総利益の取得テスト";
        String params = "params";

        for (Map<String, String> param : getListMap(sheetName, params)) {
            setUpTargetForm(param);

            Long actual = target.getGrossProfit();
            assertThat("no=" + param.get("no"), actual, is(convertToLong(param.get("expected"))));
        }
    }

    /**
     * {@link ProjectForm#getProfitBeforeAllocation()} のテスト。
     */
    @Test
    public final void 配賦前利益の取得テスト() {
        String sheetName ="配賦前利益の取得テスト";
        String params = "params";

        for (Map<String, String> param : getListMap(sheetName, params)) {
            setUpTargetForm(param);

            Long actual = target.getProfitBeforeAllocation();
            assertThat("no=" + param.get("no"), actual, is(convertToLong(param.get("expected"))));
        }
    }

    /**
     * {@link ProjectForm#getProfitRateBeforeAllocation()} のテスト。
     */
    @Test
    public final void 配賦前利益率の取得テスト() {
        String sheetName ="配賦前利益率の取得テスト";
        String params = "params";

        for (Map<String, String> param : getListMap(sheetName, params)) {
            setUpTargetForm(param);

            BigDecimal actual = target.getProfitRateBeforeAllocation();
            assertThat("no=" + param.get("no"), actual, is(convertToBigDecimal(param.get("expected"))));
        }
    }

    /**
     * {@link ProjectForm#getOperatingProfit()} のテスト。
     */
    @Test
    public final void 営業利益の取得テスト() {
        String sheetName ="営業利益の取得テスト";
        String params = "params";

        for (Map<String, String> param : getListMap(sheetName, params)) {
            setUpTargetForm(param);

            Long actual = target.getOperatingProfit();
            assertThat("no=" + param.get("no"), actual, is(convertToLong(param.get("expected"))));
        }
    }

    /**
     * {@link ProjectForm#getOperatingProfitRate()} のテスト。
     */
    @Test
    public final void 営業利益率の取得テスト() {
        String sheetName ="営業利益率の取得テスト";
        String params = "params";

        for (Map<String, String> param : getListMap(sheetName, params)) {
            setUpTargetForm(param);

            BigDecimal actual = target.getOperatingProfitRate();
            assertThat("no=" + param.get("no"), actual, is(convertToBigDecimal(param.get("expected"))));
        }
    }

    /**
     * テスト対象クラスに準備データを設定する。
     *
     * @param param {@link DbAccessTestSupport#getListMap(String, String)} の結果
     */
    private void setUpTargetForm(Map<String, String> param) {
        target.setSales(param.get("sales"));
        target.setCostOfGoodsSold(param.get("costOfGoodsSold"));
        target.setSga(param.get("sga"));
        target.setAllocationOfCorpExpenses(param.get("allocationOfCorpExpenses"));
    }

    /**
     * 文字列を数値に変換する。<br />
     * 入力値がnullの場合は、nullを返却する。
     *
     * @param input 入力値
     * @return 変換した値
     */
    private Long convertToLong(String input) {
        return StringUtil.isNullOrEmpty(input) ? null : Long.parseLong(input);
    }

    /**
     * 文字列を数値に変換する。<br />
     * 入力値がnullの場合は、nullを返却する。
     *
     * @param input 入力値
     * @return 変換した値
     */
    private BigDecimal convertToBigDecimal(String input) {
        return StringUtil.isNullOrEmpty(input) ? null : new BigDecimal(input);
    }
}
