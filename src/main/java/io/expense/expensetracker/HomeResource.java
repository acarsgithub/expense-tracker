package io.expense.expensetracker;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Controller
public class HomeResource {

    private String pass = "";

    /*
        Method: home
        Purpose: This method will implement the index.html file and describe how to use the application
        Parameters: None
        Returns: index.html thymeleaf file
     */
    @GetMapping("/")
    public String home(){
        // Access index.html thymeleaf file
        return("index");
    }


    /*
        Method: createNewUser
        Purpose: This post method will allow an individual to create a new account/manager to track their finances
        RequestBody JSON Object/String: json string object with two parameters ----
                                        - username of manager being created
                                        - password of manager being created
        Returns: either a string indicating the user was created successfully, or that the user already exists

     */
    @PostMapping("/create-new-user")
    @ResponseBody
    public String createNewUser(@RequestBody String jsonStr) throws JSONException {

        // parsing json object
        JSONObject json = new JSONObject(jsonStr);
        String username = json.getString("username");
        String password = json.getString("password");

        Connection conn = null;
        Statement stmt = null;

        try {
            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker?allowMultiQueries=true", "root", pass);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);


            // Determines if the user already has the account they are attempting to add
            String checkCurrUserNames = "SELECT username FROM manager";
            ResultSet setOfCurrUserNames = stmt.executeQuery(checkCurrUserNames);
            while(setOfCurrUserNames.next()){
                if(setOfCurrUserNames.getString("username").equals(username)){
                    return("<h2><center>The manager you are attempting to create already exists!</center></h2>");
                }
            }


            // If it is a new account, add to database
            String properId = "INSERT INTO manager(`username`, `active`, `password`, `roles`) " +
                    " VALUES ('" + username + "', TRUE, '" + password + "', 'ROLE_USER')";
            stmt.executeUpdate(properId);

        } catch (Exception se) { se.printStackTrace(); }
        // Close Resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        // Account added successfully
        return "<h2><center>Created new manager successfully!</center></h2>";

    }


    /*
        Method: addNewAccountToManager
        Purpose: This method will add a new financial account to the specified username's manager and will allow the
                individual to track the account in the future
        Path Variable: the username of the person adding the account
        RequestBody JSON Object/String: json string object with three parameters ----
                                        - category (Investment, Loan, Credit, Checking, Saving)
                                        - amount (positive or negative value)
                                        - acc_name (name of the account)
        Returns: either a string indicating the account was created successfully, or that the account already exists
     */
    @PostMapping("/add-new-account/{username}")
    @ResponseBody
    public String addNewAccountToManager(@PathVariable("username") String username, @RequestBody String jsonStr, Principal principal)
            throws JSONException {

        // Parsing JSON Object
        JSONObject json = new JSONObject(jsonStr);
        String category = json.getString("category");
        String amount = json.getString("amount");
        String acc_name = json.getString("acc_name");

        String loggedInUser = principal.getName();
        if(!loggedInUser.equals(username)){
            return "<h2><center>You cannot access another individual's account!</center></h2>";
        }

        Connection conn = null;
        Statement stmt = null;

        try {
            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker?allowMultiQueries=true", "root", pass);
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

        } catch (Exception se) { se.printStackTrace(); }
        // Close Resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        // Account added successfully
        return "<h2><center>Added account to manager successfully!</center></h2>";
    }


    /*
        Method: obtainAllUsers
        Purpose: This method will allow the admin to see all users and all of their net-worth
        Required Request Parameter: the admin username
        Returns: user information and net worth

        CYBERSECURITY: XSS Injection Cybersecurity Issue through username
        http://localhost:8080/all-users?admin-username=%3Cscript%3Ealert(%27XSS!%27)%3C/script%3E
     */
    @GetMapping("/all-users")
    @ResponseBody
    public String obtainAllUsers(@RequestParam("admin-username") String username){

        Connection conn = null;
        Statement stmt = null;
        String data = "";

        try {
            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", pass);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);


            String checkUsername = "SELECT username FROM manager WHERE roles = 'ROLE_ADMIN'";
            ResultSet userCheck = stmt.executeQuery(checkUsername);
            boolean properUser = false;
            while (userCheck.next()){
                if(userCheck.getString("username").equals(username)){
                    properUser = true;
                }
            }

            if(!properUser){
                return "<h2><center>That username is not valid!</center></h2>";
            }


            String properId = "SELECT username FROM  manager";

            // Obtain total net worth of all users in the database
            if(stmt.execute(properId)) {
                ResultSet idCheck = stmt.executeQuery(properId);
                while (idCheck.next()) {
                    data += "<center>" + getTotalNetWorth(idCheck.getString("username")) + "</center><br>";
                }
                idCheck.close();
            }

        } catch (Exception se) { se.printStackTrace(); }

        // Close Resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        // XSS Injection occurs here with the username
        return "<h2><center>Welcome, " + username + ". Here's all users' information. </center></h2><br>" + data;
    }



    /*
        Method: getTransactionHistory
        Purpose: This method will show the user their transaction history for all accounts
        Path Variable: username of the person's account to show the transaction history of
        Required Request Parameter: the name of the user (for displaying purposes)
        Returns: all transactions ensued by the user

        CYBERSECURITY: XSS Injection Cybersecurity Issue through 'user' variable
        Link: http://localhost:8080/transaction-history/acarary?xss=%3Cscript%3Ealert(%27XSS%27)%3C/script%3E
     */

    @GetMapping("/transaction-history/{username}")
    @ResponseBody
    public String getTransactionHistory(@PathVariable("username") String username, @RequestParam("user") String user){

        // Needed variables for connection and SQL statements
        Connection conn = null;
        Statement stmt = null;
        String transactionData = "";

        try {
            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", pass);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);


            String checkUsername = "SELECT username FROM manager WHERE roles = 'ROLE_USER'";
            ResultSet userCheck = stmt.executeQuery(checkUsername);
            boolean properUser = false;
            while (userCheck.next()){
                if(userCheck.getString("username").equals(username)){
                    properUser = true;
                }
            }

            if(!properUser){
                return "<h2><center>That username is not valid!</center></h2>";
            }

            // SQL Query to obtain user's information from transaction database
            String transactionInfo = "SELECT * FROM  transactions WHERE username = '" + username + "'";

            if(stmt.execute(transactionInfo)) {
                // Obtaining user data from transaction database
                ResultSet transactionSQL = stmt.executeQuery(transactionInfo);
                System.out.println(transactionSQL);

                // Accessing helper method to create table
                transactionData = viewTable(transactionSQL, "<h2><center>Full Transaction History</center></h2>");
                transactionSQL.close();
            }

        } catch (Exception se) { se.printStackTrace(); }

        // Close resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        // XSS Injection occurs here with user variable
        return "<h2><center>" + user  + "<br>" + transactionData + "</center></h2>";
    }



    /*
        Method: getTotalNetWorth
        Purpose: This method will allow a user to see their total net worth, based on all accounts in their manager
        Path Variable: username of account you want to check
        Returns: total net worth of individual
     */
    @GetMapping("/total-net-worth/{username}")
    @ResponseBody
    public String getTotalNetWorth(@PathVariable("username") String username){

            // Needed variables for connection and SQL statement/data
            Connection conn = null;
            Statement stmt = null;
            long total = 0;

            try {
                // Open connection and execute query
                conn = DriverManager
                        .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", pass);
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                String properId = "SELECT expense_value, expense_category FROM expenses WHERE username = '" + username + "'";

                try {
                    // Obtain the total net worth from the database
                    if (stmt.execute(properId)) {
                        ResultSet idCheck = stmt.executeQuery(properId);
                        while (idCheck.next()) {
                            total += idCheck.getLong("expense_value");
                        }
                        idCheck.close();
                    }
                } catch (Exception se){

                    // Couldn't find username in the database
                    return "<h2><center>Error occurred! Could not fetch data for " + username + "</center></h2>";
                }

            } catch (Exception se) { se.printStackTrace(); }

            // Close Resources
            try {
                if (conn != null && stmt != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }

            return "<h3><center>User: " + username + "   ----   Networth: " + total + "</center></h3>";
    }


    /*
        Method: modifyAccount
        Purpose: This method will allow a user to see all accounts within their manager, and will allow them to modify
                an account by withdrawing or depositing money
         Optional Request Parameters: the accountID associated with the account to modify
         Optional Request Parameters: the transaction type (must be 'DEPOSIT' or 'WITHDRAWAL')
         Optional Request Parameters: the transaction amount to deposit or withdraw
         Returns: all account information for the user as a string

         Post method should be
     */
    @GetMapping("/modify-account/{username}")
    @ResponseBody
    public String modifyAccount(@PathVariable("username") String username,
                            @RequestParam(defaultValue = "-1", value = "accountID", required = false) int accountID,
                            @RequestParam(value= "trans_type", required=false) String trans_type,
                            @RequestParam(value= "trans_amount", required=false) Long trans_amount){

        // Needed variables
        Connection conn = null;
        Statement stmt = null;
        ResultSet idCheck = null;
        String properId = "";
        long value = 0;
        String expense_acc_name = "";
        String update = "";

        try {

            // Open connection and execute query
            conn = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/expensetracker", "root", pass);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // Determine if the user entered the accountID and intends to modify an actual acccount
            if(accountID != -1) {

                // Obtain expense value and account name from SQL database
                ResultSet rs2 = stmt.executeQuery("SELECT * FROM expenses WHERE expense_id=" + accountID);
                while(rs2.next()){
                    value = rs2.getLong("expense_value");
                    expense_acc_name = rs2.getString("expense_acc_name");
                }

                // Determine if the transaction was a deposit or withdrawal adn update in the database accordingly
                if(trans_type.equals("DEPOSIT")) {
                    value = value + trans_amount;
                    update = "UPDATE expenses SET expense_value=" + value + " WHERE expense_id=" + accountID;
                } else {
                    value = value - trans_amount;
                    update = "UPDATE expenses SET expense_value=" + value + " WHERE expense_id=" + accountID;
                }
                stmt.executeUpdate(update);

                // Obtain the current date and time of the transaction
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                System.out.println(dateFormat.format(cal.getTime()));

                // Update into the transaction table
                update = "INSERT INTO transactions(`expense_id`, `trans_date`, `trans_acc_name`, `trans_amount`, `trans_type`, `username`) " +
                        " VALUES ('" + accountID + "', '" + dateFormat.format(cal.getTime()) + "', '"
                        + expense_acc_name + "', '" + trans_amount + "', '" + trans_type + "', '" + username + "')";
                stmt.executeUpdate(update);

            }

            // Print out new account values
            properId = "SELECT * FROM expenses WHERE username = '" + username + "'";
            idCheck = stmt.executeQuery(properId);
            return viewTable(idCheck, "<h2><b>All Personal Accounts</b><h2>");

        } catch (Exception se) { se.printStackTrace(); }

        // Close Resources
        try {
            if (conn != null && stmt != null)
                conn.close();
        } catch (SQLException se) { se.printStackTrace(); }

        // Should never access here anyways
        return "";
    }


    /*
        Method: viewTable
        Purpose: This method will take in a result set and modify the data in a nice table format
        Parameter: result set which holds the data
        Parameter: title of the table that will store the user information
        Returns: a nice format for the information in the SQL table
     */
    public String viewTable(ResultSet rs, String title) throws SQLException {

        // Set up for creating table
        StringBuilder result = new StringBuilder();
        result.append("<table border=\"1\" \nalign=\"center\"> \n<caption>").append(title).append("</caption>");
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        // Getting column names from database as headers of table
        result.append("<tr>");
        for (int i = 1; i <= columnsNumber; i++) {
            result.append("<th>").append(rsmd.getColumnName(i)).append("</th>");
        }
        result.append("</tr>");

        // Obtaining database information stored in columns
        while (rs.next()) {
            result.append("<tr>");
            for (int i = 1; i <= columnsNumber; i++) {
                result.append("<td><center>").append(rs.getString(i)).append("</center</td>");
            }
            result.append("</tr>");
        }
        result.append("</table>");

        // Return filled table
        return result.toString();
    }

}
