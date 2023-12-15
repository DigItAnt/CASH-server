package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class BiblioRequest extends UserReqUUID {
    List<BiblioFilter> filters;

    public BiblioRequest() {
    }

    public BiblioRequest(List<BiblioFilter> filters) {
        this.filters = filters;
    }
    
    public List<BiblioFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<BiblioFilter> filters) {
        this.filters = filters;
    }

    
}
