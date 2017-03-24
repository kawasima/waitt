package com.nablarch.example.app.web.formatter;

import nablarch.common.web.tag.DateTimeFormatter;
import nablarch.common.web.tag.ValueFormatter;
import nablarch.common.web.tag.YYYYMMDDFormatter;
import nablarch.core.util.annotation.Published;

import javax.servlet.jsp.PageContext;
import java.util.Date;

/**
 * 日付のフォーマットを行うクラス。
 * <p/>
 * フォーマット対象のオブジェクトが{@link Date}型であれば、{@link DateTimeFormatter#format(PageContext, String, Object, String)}、
 * {@link Date}型以外であれば{@link YYYYMMDDFormatter#format(PageContext, String, Object, String)}に処理を委譲する。
 *
 * @author Nabu Rakutaro
 */
@Published(tag = "architect")
public class ExampleDateTimeFormatter implements ValueFormatter {

    /**
     * @{link Date}オブジェクトを日付文字列に変換するフォーマッター
     */
    private DateTimeFormatter dateTimeFormatter = new DateTimeFormatter();

    /**
     * 文字列を日付文字列に変換するフォーマッター
     */
    private YYYYMMDDFormatter yyyymmddFormatter = new YYYYMMDDFormatter();

    @Override
    public String format(PageContext pageContext, String name, Object value, String pattern) {
        return value instanceof Date
                ? dateTimeFormatter.format(pageContext, name, value, pattern)
                : yyyymmddFormatter.format(pageContext, name, value, pattern);
    }
}
