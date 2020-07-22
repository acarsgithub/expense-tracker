
This success branch actually has all tests at the bottom passing perfectly! This is the solution to the master branch.

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
* Vulnerabilities: none


### /login
* Description: allow user to login accounts, redirect to /
* Parameters: None
* Accept Get Request
* Roles permitted: permit all
* Vulnerabilities: none


### /create-new-user
* Description: this url will allow you to create a new manager
* Parameters: 
    *jsonStr* - this is a jsonObject RequestBody parameter, which should include two key-value pairs
        * *username* - the username of the account created
        * *password* - the password of the account created
* Post Mapping
* Roles permitted: permit all
* Vulnerabilities: none
   

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
* Vulnerabilities: none      


### /all-users
* Description: this url will allow the admin to see all managers/users within the database as well as their respective net-worth
* Parameters:
    * *admin-username* - the username of the admin wanting to access the account
* Accept Get Request
* Roles Permitted: admin
* Vulnerabilities: none

 
### /transaction-history/{username} 
* Description: this url will allow for a user to see all transactions they have made throughout the history of their account creation
* Parameters:
    * *username* - this is a path variable indicating the user you want to access the transactions of
    * *user* - this is a required request parameter indicating the name of the user
* Accept Get Request
* Roles Permitted: admin, user
* Vulnerabilities: none
    
    
### /total-net-worth/{username} 
* Description: this url will allow for a user to see their total net worth, taking into consideration all accounts they have created
* Parameters:
    * *username* - this is a path variable indicating the username of the manager you are attempting to access the net worth of
* Accept Get Request
* Roles Permitted: admin, user
* Vulnerabilities: none


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
* Vulnerabilities: none   
 
 
 
## Expense Tracker vulnerability design

### Brute Force Attack on login information
* Description: On /login page, an attacker can try as many username-password pairs as it wants
* Demo: see test fucnction
* Test function: testBFA()
* Location: N/A 
* Solution: Implement an authenticationFailureListener class that keep track of login failure
information using spring security framework. Sample solution see AuthenticationFailureClass and
MyUserDetails class. For more detailed solution see https://www.baeldung
.com/spring-security-block-brute-force-authentication-attempts

### Improper api design (exposing admin authorities to users)
* Description: On /index page, a user is allowed to see the link for /admin page whhich is a page only authorized to ADMIN
* Demo: 
* Test function: none
* Location: HomeResource.java, Line: 19-21
* Solution: After login, direct users with different roles to corresponding index pages that only list the corresponding authorities of that role.

### Invalid inputs that cause sql server error
* Description: On /get_product_info_by_id page, an attacker can enter anything rather than a product id which is an integer. This will cause server error.
* Demo: see test fucnction
* Test function: testInvalidSqlInput()
* Location: HomeResource.java, Line: 48-50
* Solution: Implement code that sanitize user input, see the source code comment for sample solution.

### Unauthorized access of system by anyone
* Description: /admin page can be accessed by an attacker without being authenticated
* Demo: see test fucnction
* Test function: testUnauthenticatedAccess()
* Location: SecurityConfiguration.java, Line: 36 
* Solution: Refine authority assigned to each role, see the source code comment for sample solution.

### CSRF attack
* Description: On /get_product_info_by_id page, an attacker cause user to carry out a query on product information unintentionally.
* Demo: see test fucnction
* Test function: testCSRF()
* Location: SecurityConfiguration.java, Line: 49
* Solution: Enable csrf protection provided by spring security, see the source code comment for sample solution.

### Expose of sql source code
* Description: In the HomeSource.java file, the sql quey used by /get_product_info_by_id is exposed.
* Demo:
* Test function: none
* Location: HomeResource.java, Line: 69-71
* Solution: Implement mysql procedure in the database management system and call procedure in source code instead, see the source code comment for sample solution.

### Sql injection attack
* Description: On /get_product_info_by_id page, an attacker can enter additional sql query element along with a product id. This will expose data in the database.
* Demo: see test fucnction
* Test function: testSQLInjection()
* Location: HomeResource.java, Line: 81
* Solution: Implement code that sanitize user input, see the source code comment for sample solution.

### XSS attack
* Description: On /add_comment page, an attacker can enter malicious html code that cause server
error
* Demo: see test fucnction
* Test function: testSQLInjection()
* Location: HomeResource.java, Line: 81
* Solution: Implement code that sanitize user input, see the source code in HtmlUtils.java.


## Test description and grading rubrics

### testHome(), testPageAdmin(), testGetProductInfo(), testAddComment()
* Should pass if the original functionalities of the application remain.
* Grading rubrics: 10 pts each should be deducted if each of them fails.

### testUser(), testWrongUser()
* Should pass if the login authentication remains.
* Grading rubrics: 10 pts should be deducted if any of them fails.

### testUnauthenticatedAccess()
* Should pass if /admin is secured against unauthenticaed users.
* Grading rubrics: completely ignoring the vulnerbility -10; code partialy implemented but fails the test -5; correct -0;

### testUnauthorizedAccess()
* Should pass if /admin is secured against unauthorized users, such as the role 'USER'.
* Grading rubrics: completely ignoring the vulnerbility -10; code partialy implemented but fails the test -5; correct -0;

### testSQLInjection()
* Should pass if /get_product_info_by_id api is secured against sql injection attack such as
'id=1 and username = user2'
* Grading rubrics: completely ignoring the vulnerbility -10; code partialy implemented but fails the test -5; correct -0;

### testInvalidSqlInput()
* Should pass if /get_product_info_by_id api is secured against invalid inputs such as 'id=asfsda'.
* Grading rubrics: completely ignoring the vulnerbility -10; code partialy implemented but fails the test -5; correct -0;

### testXSS()
* Should pass if /add_comment api is secured against XSS attack.
* Grading rubrics: completely ignoring the vulnerbility -10; code partialy implemented but fails the test -5; correct -0;

### testCSRF(), testCSRF_1()
* Should pass if CSRF protection is enabled in spring security so that only request sent with csrf
token will be processed.
* Grading rubrics: completely ignoring the vulnerbility -10; code partialy implemented but fails the test -5; correct -0;

### testBFA()
* Should pass if the application is secured against brute force attack in login. Student should
implement solution to BFA such that if a user tries a wrong password for less than 10 times the api
will redirect the user to /login?error each time login fails. However, if more than 10 times, the
user account will be locked. The user can not tries any more password and can not even login with
valid credentials.
* Grading rubrics: Bonus, completely ignoring the vulnerbility -0; code partialy implemented but
fails the test +5; correct +10;