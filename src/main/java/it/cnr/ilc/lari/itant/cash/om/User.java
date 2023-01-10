package it.cnr.ilc.lari.itant.cash.om;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private int userId;
    private String firstName;
    private String lastName;

    @JsonProperty("user-id")
    public int getUserId() {
        return userId;
    }

    @JsonProperty("user-id")
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @JsonProperty("first-name")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("first-name")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("last-name")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("last-name")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    
}
