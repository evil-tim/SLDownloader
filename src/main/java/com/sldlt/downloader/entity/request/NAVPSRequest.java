package com.sldlt.downloader.entity.request;

import java.io.Serializable;

public class NAVPSRequest implements Serializable {

    private static final long serialVersionUID = 5726025298700818953L;

    private String fundCode;
    private String dateFrom;
    private String dateTo;

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

}
