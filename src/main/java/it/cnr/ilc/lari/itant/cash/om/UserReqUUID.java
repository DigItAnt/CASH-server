package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

// Bean di utilità per tutte le volte che serve user-id e requestUUID
public class UserReqUUID {
    String requestUUID;
    
    @JsonProperty("user-id")
    int userId;

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    
}
