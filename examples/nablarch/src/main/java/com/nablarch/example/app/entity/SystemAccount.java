package com.nablarch.example.app.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * システムアカウント エンティティ。
 */
@Entity
@Table(name = "SYSTEM_ACCOUNT")
public class SystemAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "LOGIN_ID", length = 20, nullable = false)
    private String loginId;

    @Column(name = "USER_PASSWORD", length = 44, nullable = false)
    private String userPassword;

    @Column(name = "USER_ID_LOCKED", nullable = false)
    private boolean userIdLocked;

    @Column(name = "PASSWORD_EXPIRATION_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date passwordExpirationDate;

    @Column(name = "FAILED_COUNT", nullable = false)
    private Short failedCount;

    @Column(name = "EFFECTIVE_DATE_FROM")
    @Temporal(TemporalType.DATE)
    private Date effectiveDateFrom;

    @Column(name = "EFFECTIVE_DATE_TO")
    @Temporal(TemporalType.DATE)
    private Date effectiveDateTo;

    @Column(name = "LAST_LOGIN_DATE_TIME")
    private Timestamp lastLoginDateTime;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public boolean isUserIdLocked() {
        return userIdLocked;
    }

    public void setUserIdLocked(boolean userIdLocked) {
        this.userIdLocked = userIdLocked;
    }

    public Date getPasswordExpirationDate() {
        return passwordExpirationDate;
    }

    public void setPasswordExpirationDate(Date passwordExpirationDate) {
        this.passwordExpirationDate = passwordExpirationDate;
    }

    public Short getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Short failedCount) {
        this.failedCount = failedCount;
    }

    public Date getEffectiveDateFrom() {
        return effectiveDateFrom;
    }

    public void setEffectiveDateFrom(Date effectiveDateFrom) {
        this.effectiveDateFrom = effectiveDateFrom;
    }

    public Date getEffectiveDateTo() {
        return effectiveDateTo;
    }

    public void setEffectiveDateTo(Date effectiveDateTo) {
        this.effectiveDateTo = effectiveDateTo;
    }

    public Timestamp getLastLoginDateTime() {
        return lastLoginDateTime;
    }

    public void setLastLoginDateTime(Timestamp lastLoginDateTime) {
        this.lastLoginDateTime = lastLoginDateTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
