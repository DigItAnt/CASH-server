package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

// Bean di utilità per tutte le volte che serve user-id e requestUUID
public class UserReqUUID {
    String requestUUID;
    
    @JsonProperty("user-id")
    String userId;

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    
}
