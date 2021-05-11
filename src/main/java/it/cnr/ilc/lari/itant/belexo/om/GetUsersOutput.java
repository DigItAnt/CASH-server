package it.cnr.ilc.lari.itant.belexo.om;

import java.util.List;

public class GetUsersOutput {
    private String requestUUID;
    private Integer results;
    private List<User> users;


    public Integer getResults() {
        return results;
    }
    public void setResults(Integer results) {
        this.results = results;
    }

    public String getRequestUUID() {
        return requestUUID;
    }
    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }
    public List<User> getUsers() {
        return users;
    }
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
