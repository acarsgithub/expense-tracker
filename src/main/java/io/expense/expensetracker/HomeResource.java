package io.expense.expensetracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
public class HomeResource {

    private String username;

    @GetMapping("/")
    public String home(){
        return("<h2><center>Welcome! Please log in to update or see your current expenses. " +
                "If you're already logged in, then congratulations!</center></h2>");
    }


    // THIS NEEDS TO BE A POST METHOD
    // SQL Injection Link:
    // http://localhost:8080/add-new-account/acarary?category=Investment&amount=1&acc_name=M2Finance%27%29%3B+INSERT+INTO+expenses%28%60username%60%2C+%60expense_category%60%2C+%60expense_value%60%2C+%60expense_acc_name%60%29+VALUES+%28%27acarary%27%2C+%27Loan%27%2C+%27-10000%27%2C+%27SQLInjection
    @GetMapping("/add-new-account/{username}")
    public String addNewAccountToManager(@PathVariable("username") String username,
                                         @RequestParam("category") String category,
                                         @RequestParam("amount") long amount,
                                         @RequestParam("acc_name") String acc_name){
        Connection conn = null;
        Statement stmt = null;

        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker?allowMultiQueries=true", "root", "");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // Determines if the user already has the account they are attempting to add
            String checkCurrAccNames = "SELECT expense_acc_name FROM expenses WHERE username='" + username + "'";
            ResultSet setOfCurrAccNames = stmt.executeQuery(checkCurrAccNames);
            while(setOfCurrAccNames.next()){
                if(setOfCurrAccNames.getString("expense_acc_name").equals(acc_name)){
                    return("<h2><center>The account you are attempting to create already exists!</center></h2>");
                }
            }

            // If it is a new account, add to database
            String properId = "INSERT INTO expenses(`username`, `expense_category`, `expense_value`, `expense_acc_name`) " +
                        " VALUES ('" + username + "', '" + category + "', '" + amount + "', '" + acc_name + "')";
            stmt.executeUpdate(properId);


            // Handle JDBC errors
        } catch (Exception se) {
            se.printStackTrace();
        }
        //close resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        //model.addAttribute("networth", total);
        //model.addAttribute("username", username);
        return "<h2><center>Added account to manager successfully!</center></h2>";
    }



    // XSS Injection Cybersecurity Issue
    // Link to apply:
    // http://localhost:8080/all-users?admin-username=%3Cscript%3Ealert(%27XSS!%27)%3C/script%3E
    @GetMapping("/all-users")
    public String obtainAllUsers(@RequestParam("admin-username") String username){
        Connection conn = null;
        Statement stmt = null;
        String data = "";

        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", "");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String properId = "SELECT username FROM  manager";

            if(stmt.execute(properId)) {
                ResultSet idCheck = stmt.executeQuery(properId);
                while (idCheck.next()) {
                    data += "<center>" + getTotalNetWorth(idCheck.getString("username")) + "</center><br>";
                }
                idCheck.close();
            }

            // Handle JDBC errors
        } catch (Exception se) {
            se.printStackTrace();
        }
        //close resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        //model.addAttribute("networth", total);
        //model.addAttribute("username", username)
        System.out.println("Here");
        return "<h2><center>Welcome " + username + ". Here's all users' information. </center></h2><br>" + data;
    }


    @GetMapping("/transaction-history/{username}")
    public String getTransactionHistory(@PathVariable("username") String username){
        Connection conn = null;
        Statement stmt = null;
        String data = "";

        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", "");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String properId = "SELECT * FROM  transactions WHERE username = '" + username + "'";

            if(stmt.execute(properId)) {
                ResultSet idCheck = stmt.executeQuery(properId);
                while (idCheck.next()) {
                    data += "<h2><center>Account Name: " + idCheck.getString("trans_acc_name") + "</center><br>"
                            + "<center>Transaction Type: " + idCheck.getString("trans_type") + "</center><br>"
                            + "<center>Transaction Date: " + idCheck.getString("trans_date") + "</center><br>"
                            + "<center>Transaction Amount: " + idCheck.getLong("trans_amount") + "</center></h2><br><br>";
                }
                idCheck.close();
            }


            // Handle JDBC errors
        } catch (Exception se) {
            se.printStackTrace();
        }
        //close resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        //model.addAttribute("networth", total);
        //model.addAttribute("username", username);
        return data;
    }


    @GetMapping("/total-net-worth/{username}")
    public String getTotalNetWorth(@PathVariable("username") String username/*,
                                       Principal principal, Model model*/){

        /*if (principal.getName().equals(username)) {*/
            Connection conn = null;
            Statement stmt = null;
            long total = 0;

            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Open connection and execute query
                conn = DriverManager
                        .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", "");
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                String properId = "SELECT expense_value, expense_category FROM expenses WHERE username = '" + username + "'";

                try {

                    if (stmt.execute(properId)) {
                        ResultSet idCheck = stmt.executeQuery(properId);
                        while (idCheck.next()) {
                            total += idCheck.getLong("expense_value");
                        }
                        idCheck.close();
                    }
                } catch (Exception se){
                    return "<h2><center>Error occurred! Could not fetch data for " + username + "</center></h2>";
                }


                // Handle JDBC errors
            } catch (Exception se) {
                se.printStackTrace();
            }
            //close resources
            try {
                if (conn != null && stmt != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            //model.addAttribute("networth", total);
            //model.addAttribute("username", username);
            return "<h2><center>User: " + username + "   ----   Networth: " + total + "</center></h2>";
            /*
        } else {
            return ("no");
        }
        */

    }

    @GetMapping("/modify-account/{username}")
    public String modifyAccount(@PathVariable("username") String username,
                            @RequestParam(defaultValue = "-1", value = "accountID", required = false) int accountID,
                            @RequestParam(value= "trans_type", required=false) String trans_type,
                            @RequestParam(value= "trans_amount", required=false) Long trans_amount){

        Connection conn = null;
        Statement stmt = null;
        String data = "";
        ResultSet idCheck = null;
        String properId = "";
        long value = 0;
        String expense_acc_name = "";
        String update;

        try {
            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", "");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            if(accountID != -1) {
                ResultSet rs2 = stmt.executeQuery("SELECT * FROM expenses WHERE expense_id=" + accountID);
                while(rs2.next()){
                    value = rs2.getLong("expense_value");
                    expense_acc_name = rs2.getString("expense_acc_name");
                }
                if(trans_type.equals("DEPOSIT")) {
                    value = value + trans_amount;
                    update = "UPDATE expenses SET expense_value=" + value + " WHERE expense_id=" + accountID;
                } else {
                    value = value - trans_amount;
                    update = "UPDATE expenses SET expense_value=" + value + " WHERE expense_id=" + accountID;
                }
                stmt.executeUpdate(update);

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                System.out.println(dateFormat.format(cal.getTime()));

                update = "INSERT INTO transactions(`expense_id`, `trans_date`, `trans_acc_name`, `trans_amount`, `trans_type`, `username`) " +
                        " VALUES ('" + accountID + "', '" + dateFormat.format(cal.getTime()) + "', '"
                        + expense_acc_name + "', '" + trans_amount + "', '" + trans_type + "', '" + username + "')";
                stmt.executeUpdate(update);

            }

            properId = "SELECT * FROM expenses WHERE username = '" + username + "'";
            idCheck = stmt.executeQuery(properId);
            return viewTable(idCheck, "<h2><b>All Personal Accounts</b><h2>");
            /*
            while (idCheck.next()) {
                data += "<p><center>Account Name: " + idCheck.getString("expense_acc_name") + "</center><br>"
                        + "<center>Account ID: " + idCheck.getString("expense_id") + "</center><br>"
                        + "<center>Account Value: " + idCheck.getString("expense_value") + "</center><br>"
                        + "<center>Account Type: " + idCheck.getString("expense_category") + "</center><br><br></p>";
            }

            idCheck.close();
             */

            // Handle JDBC errors
        } catch (Exception se) { se.printStackTrace(); }
        //close resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) { se.printStackTrace(); }

        return(data);
    }




    public String viewTable(ResultSet rs, String title) throws SQLException {
        StringBuilder result = new StringBuilder();


        result.append("<table border=\"1\" \nalign=\"center\"> \n<caption>").append(title).append("</caption>");
        ResultSetMetaData rsmd = rs.getMetaData();

        int columnsNumber = rsmd.getColumnCount();

        result.append("<tr>");
        for (int i = 1; i <= columnsNumber; i++) {
            result.append("<th>").append(rsmd.getColumnName(i)).append("</th>");
        }
        result.append("</tr>");


        while (rs.next()) {
            result.append("<tr>");
            for (int i = 1; i <= columnsNumber; i++) {
                result.append("<td><center>").append(rs.getString(i)).append("</center</td>");
            }
            result.append("</tr>");
        }

        result.append("</table>");
        return result.toString();
    }

}
