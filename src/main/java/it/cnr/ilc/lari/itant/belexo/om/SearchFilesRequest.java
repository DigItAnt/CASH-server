package it.cnr.ilc.lari.itant.belexo.om;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class SearchFilesRequest {
    String requestUUID;

    String searchText;

    boolean startWith;

    boolean contains;
    
    int userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.S")
    Date date;

    boolean exactDate;

    boolean fromDate;

    boolean untilDate;

    Map<String, String> metadata;

    @JsonProperty("search-text")
    public String getSearchText() {
        return searchText;
    }

    @JsonProperty("search-text")
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @JsonProperty("start-with")
    public boolean isStartWith() {
        return startWith;
    }

    @JsonProperty("start-with")
    public void setStartWith(boolean startWith) {
        this.startWith = startWith;
    }

    public boolean isContains() {
        return contains;
    }

    public void setContains(boolean contains) {
        this.contains = contains;
    }

    @JsonProperty("user-id")
    public int getUserId() {
        return userId;
    }

    @JsonProperty("user-id")
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @JsonProperty("import-date")
    public Date getDate() {
        return date;
    }

    @JsonProperty("import-date")
    public void setDate(Date date) {
        this.date = date;
    }

    @JsonProperty("exact-date")
    public boolean isExactDate() {
        return exactDate;
    }

    @JsonProperty("exact-date")
    public void setExactDate(boolean exactDate) {
        this.exactDate = exactDate;
    }

    @JsonProperty("from-date")
    public boolean isFromDate() {
        return fromDate;
    }

    @JsonProperty("from-date")
    public void setFromDate(boolean fromDate) {
        this.fromDate = fromDate;
    }

    @JsonProperty("util-date")
    public boolean isUntilDate() {
        return untilDate;
    }

    @JsonProperty("util-date")
    public void setUntilDate(boolean untilDate) {
        this.untilDate = untilDate;
    }

    //@JsonSerialize(using = MetadataSerializer.class)
    public Map<String, String> getMetadata() {
        return metadata;
    }

    //@JsonDeserialize(using = MetadataDeserializer.class)
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    
}
