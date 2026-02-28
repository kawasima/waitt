package com.nablarch.example.app.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ユーザ エンティティ。
 */
@Entity
@Table(name = "USERS")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "KANJI_NAME", length = 128)
    private String kanjiName;

    @Column(name = "KANA_NAME", length = 128)
    private String kanaName;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getKanjiName() {
        return kanjiName;
    }

    public void setKanjiName(String kanjiName) {
        this.kanjiName = kanjiName;
    }

    public String getKanaName() {
        return kanaName;
    }

    public void setKanaName(String kanaName) {
        this.kanaName = kanaName;
    }
}
