package com.nablarch.example.app.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * プロジェクト エンティティ。
 */
@Entity
@Table(name = "PROJECT")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PROJECT_ID")
    private Integer projectId;

    @Column(name = "PROJECT_NAME", length = 128)
    private String projectName;

    @Column(name = "PROJECT_TYPE", length = 2)
    private String projectType;

    @Column(name = "PROJECT_CLASS", length = 2)
    private String projectClass;

    @Column(name = "PROJECT_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date projectStartDate;

    @Column(name = "PROJECT_END_DATE")
    @Temporal(TemporalType.DATE)
    private Date projectEndDate;

    @Column(name = "CLIENT_ID")
    private Integer clientId;

    @Column(name = "PROJECT_MANAGER", length = 128)
    private String projectManager;

    @Column(name = "PROJECT_LEADER", length = 128)
    private String projectLeader;

    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "NOTE", length = 512)
    private String note;

    @Column(name = "SALES")
    private Integer sales;

    @Column(name = "COST_OF_GOODS_SOLD")
    private Integer costOfGoodsSold;

    @Column(name = "SGA")
    private Integer sga;

    @Column(name = "ALLOCATION_OF_CORP_EXPENSES")
    private Integer allocationOfCorpExpenses;

    @Version
    @Column(name = "VERSION")
    private Long version;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getProjectClass() {
        return projectClass;
    }

    public void setProjectClass(String projectClass) {
        this.projectClass = projectClass;
    }

    public Date getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    public Date getProjectEndDate() {
        return projectEndDate;
    }

    public void setProjectEndDate(Date projectEndDate) {
        this.projectEndDate = projectEndDate;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getProjectManager() {
        return projectManager;
    }

    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public String getProjectLeader() {
        return projectLeader;
    }

    public void setProjectLeader(String projectLeader) {
        this.projectLeader = projectLeader;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(Integer costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }

    public Integer getSga() {
        return sga;
    }

    public void setSga(Integer sga) {
        this.sga = sga;
    }

    public Integer getAllocationOfCorpExpenses() {
        return allocationOfCorpExpenses;
    }

    public void setAllocationOfCorpExpenses(Integer allocationOfCorpExpenses) {
        this.allocationOfCorpExpenses = allocationOfCorpExpenses;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
