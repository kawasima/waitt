package com.nablarch.example.app.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 業種 エンティティ。
 */
@Entity
@Table(name = "INDUSTRY")
public class Industry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "INDUSTRY_CODE", length = 2)
    private String industryCode;

    @Column(name = "INDUSTRY_NAME", length = 128)
    private String industryName;

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }
}
