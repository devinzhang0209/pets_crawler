package com.zhuyinline.pets.crawler.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "user_cert_t")
public class UserCert implements Serializable {

    @ApiModelProperty(hidden=true)
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)

    private Integer certId;

    private Integer userId;

    private String certName;

    private String certImage;

    private String certOrg;

    private String certPassTime;

    @ApiModelProperty(hidden=true)
    private String createTime;

    private Integer status;

    private static final long serialVersionUID = 1L;

    public Integer getCertId() {
        return certId;
    }

    public void setCertId(Integer certId) {
        this.certId = certId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName == null ? null : certName.trim();
    }

    public String getCertImage() {
        return certImage;
    }

    public void setCertImage(String certImage) {
        this.certImage = certImage == null ? null : certImage.trim();
    }

    public String getCertOrg() {
        return certOrg;
    }

    public void setCertOrg(String certOrg) {
        this.certOrg = certOrg == null ? null : certOrg.trim();
    }

    public String getCertPassTime() {
        return certPassTime;
    }

    public void setCertPassTime(String certPassTime) {
        this.certPassTime = certPassTime == null ? null : certPassTime.trim();
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}