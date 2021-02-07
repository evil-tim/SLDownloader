package com.sldlt.downloader.entity.response;

import java.io.Serializable;

public class NAVPSResponse implements Serializable {

    private static final long serialVersionUID = -4242167103608895394L;

    private String fundCode;
    private String fundDesc;
    private String fundName;
    private String fundValDate;
    private String fundNetVal;
    private String fundYoyVal;
    private String fundYtdVal;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getFundDesc() {
        return fundDesc;
    }

    public void setFundDesc(String fundDesc) {
        this.fundDesc = fundDesc;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getFundValDate() {
        return fundValDate;
    }

    public void setFundValDate(String fundValDate) {
        this.fundValDate = fundValDate;
    }

    public String getFundNetVal() {
        return fundNetVal;
    }

    public void setFundNetVal(String fundNetVal) {
        this.fundNetVal = fundNetVal;
    }

    public String getFundYoyVal() {
        return fundYoyVal;
    }

    public void setFundYoyVal(String fundYoyVal) {
        this.fundYoyVal = fundYoyVal;
    }

    public String getFundYtdVal() {
        return fundYtdVal;
    }

    public void setFundYtdVal(String fundYtdVal) {
        this.fundYtdVal = fundYtdVal;
    }

}
