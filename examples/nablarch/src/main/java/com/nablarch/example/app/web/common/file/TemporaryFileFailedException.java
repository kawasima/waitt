package com.nablarch.example.app.web.common.file;

import nablarch.core.util.annotation.Published;

/**
 * 一時ファイルの操作に失敗した場合に発生する例外。
 *
 * @author Nabu Rakutaro
 */
@Published
public class TemporaryFileFailedException extends RuntimeException {

    /**
     * コンストラクタ。
     *
     * @param cause 起因例外
     */
    public TemporaryFileFailedException(Throwable cause) {
        super(cause);
    }
}
