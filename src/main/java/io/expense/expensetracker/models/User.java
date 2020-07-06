package io.expense.expensetracker.models;

import javax.persistence.*;

@Entity
@Table(name="manager")
public class User {

    // each column in the database extends to these variables created here
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String username;
    private boolean active;
    private String password;
    private String roles;

    public int getId() {
        return id;
    }

    public String getUser_name() {
        return username;
    }


    public boolean isActive() {
        return active;
    }

    public String getPassword() {
        return password;
    }

    public String getRoles() {
        return roles;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setUser_name(String user_name) {
        this.username = user_name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

}
