package com.sldlt.downloader.entity.response;

import java.io.Serializable;
import java.util.List;

public class FundsResponse implements Serializable {

    private static final long serialVersionUID = 6342764562409039127L;

    private List<FundResponse> pesoList;
    private List<FundResponse> dollarList;

    public List<FundResponse> getPesoList() {
        return pesoList;
    }

    public void setPesoList(List<FundResponse> pesoList) {
        this.pesoList = pesoList;
    }

    public List<FundResponse> getDollarList() {
        return dollarList;
    }

    public void setDollarList(List<FundResponse> dollarList) {
        this.dollarList = dollarList;
    }

}
