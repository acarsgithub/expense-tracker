# Expense Tracker Application


### General purpose of application
This application will allow individuals to track and manage their finances and expenses. 
Users will have the ability to sign up and create a 'manager'. Each user can add as many financial accounts as
they wish to keep track of within the manager, but the account types can only be credit, loans, investments, checking, or savings. 
You can deposit or withdraw money from accounts, keep track of your total net worth, and see all 
transactions that you have made throughout the lifespan of your manager. The admin can also additionally 
see all users who have created an account and their respective net-worth. 


### Directions on using the application
There are several url links that you can utilize within the application...

* **/** - this homepage will give you acces to information on how to correctly use the application 



* **/login** - this url will allow you to login after entering the correct username and password



* **/create-new-user** - this url will allow you to create a new manager
    * *jsonStr* - this is a jsonObject RequestBody parameter, which should include two key-value pairs
        * *username* - the username of the account created
        * *password* - the password of the account created
   

* **/add-new-account/{username}** - this url will allow you to add a new financial account to the manager of the specified username
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
        

* **/all-users** - this url will allow the admin to see all managers/users within the database as well as their respective net-worth

* **/transaction-history/{username}** - this url will allow for a user to see all transactions they have made throughout the history of their account creation
    * *username* - this is a path variable indicating the user you want to access the transactions of
    * *user* - this is a required request parameter indicating the name of the user
    
* **/total-net-worth/{username}** - this url will allow for a user to see their total net worth, taking into consideration all accounts they have created
    * *username* - this is a path variable indicating the username of the manager you are attempting to access the net worth of
 
* **/modify-account/{username}** - this url will allow for a user to modify any of the accounts they currently have in their manager, or 
if they decide to not input the optional request parameters, they will simply see all of their accounts and account information stored within their manager
    * *username* - this is a path variable indicating the username of the manager you want to modify an account for
    * *accountID* - this is an optional request parameter  indicating the accountID number of the account you wish to modify (if you input this, then you SHOULD input the next two optional parameters as well)
    * *trans_type* - this is an optional request parameter indicating the type of transaction you would like to perform on the account (choose form the following two options)
        1. DEPOSIT
        2. WITHDRAWAL
    * *trans_amount* - this is an optional request parameter indicating the amount of amount you would like to withdraw or deposit into the account
    

### Testing Directions
There are 12 tests, 4 of the tests pass, but 8 of the tests fail. The 8 tests that fail require modifications to be made revolving around
not allowing SQL injections, not allowing XSS JS injections, not users to access accounts of different users, not allowing specific
users to admin only links/information, etc. Your job is to fix these errors and make them all pass. 

##### Passing Tests
* *testHome* - works perfectly and as it should!

* *testForcesUserLogin* - this test passes perfectly and ensures that a random individual with no account cannot access an admin link

* *testAdminCredentialsOnAdminController* - this test passes and ensures an admin can get into a page designed for themselves

* *testCreateNewUser* - this test passes and ensures a new user can properly be made when needed

##### Failing Tests
* *testXSS* - this test FAILS because it allows for an XSS injection to occur rather than throw a 400 error, this needs 
to be accounted for within the 'all-users' controller --- the solution can be approached in various ways, but it involves
checking the accounts of the parameter and insuring that nothing gets executed unless it is a valid username within the database

* *testAdminNeededForController* - this test attempts to ensure that a user privilege cannot access admin information, but it FAILS and
the solution involves changing the security configuration

* *testXSS2* - this tests against an XSS Injection in the transaction history controller and it FAILS to not allow the XSS Injection, and the 
solution revolves around verifying and checking the contents of the parameter 

* *testSQLInjection* - this tests against a SQL Injection in the add-new-account controller and FAILS to prevent a SQL Injection
from occurring, and the solution revolves around verifying and checking the contents of the json object values (ALL OBJECT VALUES SHOULD BE TESTED AGAINST)

* *testAccessingDifferentUser* - this tests that a different user from the one currently logged in cannot add-new-account for
another user and it FAILS to prevent this, the solution is to check the principal and compare to the username of the path variable 

* *testDifferentUserAccessingTransactionHistory* - this tests against a different user attempting to access the transaction 
history of a different user's account and FAILS to prevent this from happening, and the solution revolves around using 
redirectView.setUrl in conjunction with a check on the current principal/ logged in user

* *testDifferentUserAccessingModifyAccount* - this tests against a different user attempting to access the modify account page
of a different user's account and FAILS to prevent this from happening, and the solution revolves around using 
redirectView.setUrl in conjunction with a check on the current principal/ logged in user

* *testCreateNewDuplicateUser* - this tests against a duplicate user being created and FAILS to prevent duplicates with the same username,
and the solution revolves around verifying the username and checkign against the database and returning that the account already
exists if the username matches one in the database