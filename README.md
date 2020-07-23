This master branch actually has a majority of the tests failing! 

### Testing Directions
There are 12 tests, 4 of the tests pass, but 8 of the tests fail. The 8 tests that fail require modifications to be made revolving around
not allowing SQL injections, not allowing XSS JS injections, not users to access accounts of different users, not allowing specific
users to admin only links/information, etc. Your job is to fix these errors and make them all pass. 


# expense-tracker application
## Instruction
* Clone/download this project to your local machine
* Open project with Intellij
* Setup and connect to your Mamp/Wamp server
* Run the sql file src/main/test/java/travis.sql with Mysql Workbench 
* Configure your connection to the database server in src/main/resources/application.properties.
* To run the existing test, go to 
src/test/java/io.expense.expensetracker/ExpenseTrackerApplicationTests.java and run the test file
* To run the api: run src/main/java/io.expense.expensetracker/ExpenseTrackerApplication.java
* Open your browser and enter request: http://localhost:8080, this address will allow you to access different resources in the api


## API design
This application will allow individuals to track and manage their finances and expenses. 
Users will have the ability to sign up and create a 'manager'. Each user can add as many financial accounts as
they wish to keep track of within the manager, but the account types can only be credit, loans, investments, checking, or savings. 
You can deposit or withdraw money from accounts, keep track of your total net worth, and see all 
transactions that you have made throughout the lifespan of your manager. The admin can also additionally 
see all users who have created an account and their respective net-worth. 


### /
* Description: this homepage will give you access to information on how to correctly use the application 
* Parameters: None
* Accept Get Request
* Roles permitted: permit all


### /login
* Description: allow user to login accounts, redirect to /
* Parameters: None
* Accept Get Request
* Roles permitted: permit all


### /create-new-user
* Description: this url will allow you to create a new manager
* Parameters: 
    *jsonStr* - this is a jsonObject RequestBody parameter, which should include two key-value pairs
        * *username* - the username of the account created
        * *password* - the password of the account created
* Post Mapping
* Roles permitted: permit all
   

### /add-new-account/{username}
* Description: this url will allow you to add a new financial account to the manager of the specified username
* Parameters: 
    * *username* - this is a path variable signifying the user to add the account for
    * *jsonStr* - this is a jsonObject RequestBody parameter, which should include three key-value pairs
        * *category* - this is a required request parameter that will indicate the type of account you are adding (choose from the following options)
            1. Checking
            2. Saving
            3. Investment
            4. Loan
            5. Credit
        * *amount* - this is a required request parameter indicating the initial amount tha account will start off with (may be negative or positive values)
        * *acc_name* - this is a required request parameter indicating the name of the account that you want to open (RobinHood, Wells Fargo, etc.)
* Post Mapping
* Roles permitted: user, admin    


### /all-users
* Description: this url will allow the admin to see all managers/users within the database as well as their respective net-worth
* Parameters:
    * *admin-username* - the username of the admin wanting to access the account
* Accept Get Request
* Roles Permitted: admin

 
### /transaction-history/{username} 
* Description: this url will allow for a user to see all transactions they have made throughout the history of their account creation
* Parameters:
    * *username* - this is a path variable indicating the user you want to access the transactions of
    * *user* - this is a required request parameter indicating the name of the user
* Accept Get Request
* Roles Permitted: admin, user
    
    
### /total-net-worth/{username} 
* Description: this url will allow for a user to see their total net worth, taking into consideration all accounts they have created
* Parameters:
    * *username* - this is a path variable indicating the username of the manager you are attempting to access the net worth of
* Accept Get Request
* Roles Permitted: admin, user


### /modify-account/{username}
* Description: this url will allow for a user to modify any of the accounts they currently have in their manager, or if they decide to not input the optional request parameters, they will simply see all of their accounts and account information stored within their manager
* Parameters:
    * *username* - this is a path variable indicating the username of the manager you want to modify an account for
    * *accountID* - this is an optional request parameter  indicating the accountID number of the account you wish to modify (if you input this, then you SHOULD input the next two optional parameters as well)
    * *trans_type* - this is an optional request parameter indicating the type of transaction you would like to perform on the account (choose form the following two options)
        1. DEPOSIT
        2. WITHDRAWAL
    * *trans_amount* - this is an optional request parameter indicating the amount of amount you would like to withdraw or deposit into the account
* Accept Get Request
* Roles Permitted: admin, user



## Test description and grading rubrics

### testHome, testForcesUserLogin, testAdminCredentialsOnAdminController, testCreateNewUser
* Should pass if the original functionalities of the application remain.
* Grading rubrics: 5 pts each should be deducted if each of them fails.


### testXSS
* Should pass if /all-users api is secured against XSS injection attack
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this test FAILS because it allows for an XSS injection to occur, this needs to be accounted for within the 'all-users' controller --- the solution can be approached in various ways, but it involves
  checking the accounts of the parameter and insuring that nothing gets executed unless it is a valid username within the database


### testAdminNeededForController
* Should pass if /all-users is secured against unauthenticaed users.
* this test attempts to ensure that a user privilege cannot access admin information, but it FAILS and the solution involves changing the security configuration
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;


### testXSS2
* Should pass if /transaction-history api is secured against XSS injection attack
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this tests against an XSS Injection in the transaction history controller, and it FAILS to not allow the XSS Injection, and the 
    solution revolves around verifying and checking the contents of the parameter 
    
    
### testSQLInjection
* Should pass if /add-new-account api is secured against sql injection attack
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this tests against a SQL Injection in the add-new-account controller and FAILS to prevent a SQL Injection from 
  occurring, and the solution revolves around verifying and checking the contents of the json object values (ALL OBJECT VALUES SHOULD BE TESTED AGAINST)


### testAccessingDifferentUser 
* Should pass if /add-new-account api is secured against other users who shouldn't have access to another user's info
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this tests that a different user from the one currently logged in cannot add-new-account for
    another user, and it FAILS to prevent this, the solution is to check the principal and compare to the username of the path variable
    
     
### testDifferentUserAccessingTransactionHistory
* Should pass if /transaction-history api is secured against other users who shouldn't have access to another user's info
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this tests against a different user attempting to access the transaction history of a different user's account 



### testDifferentUserAccessingModifyAccount
* Should pass if /modify-account api is secured against other users who shouldn't have access to another user's info
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this tests against a different user attempting to access the modify account page of a different user's account


### testCreateNewDuplicateUser 
* Should pass if user is prohibited from making same account (same username)
* Grading rubrics: completely ignoring the vulnerability -10; code partially implemented but fails the test -5; correct -0;
* this tests against a duplicate user creations and FAILS to prevent duplicates with the same username, and the solution revolves around verifying the username and checking against the database and returning that the account already
exists if the username matches one in the database
