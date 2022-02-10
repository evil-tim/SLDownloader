package com.sldlt.downloader.entity.response;

import java.io.Serializable;

public class FundResponse implements Serializable {

    private static final long serialVersionUID = 9213789333751617917L;

    private String fundCode;
    private String fundName;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

}
