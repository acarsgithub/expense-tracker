

DROP TABLE If EXISTS manager;
CREATE TABLE manager (
	  id          	int 	    		auto_increment			PRIMARY KEY,
	  username      VARCHAR(255)        NOT NULL,
	  active    	BOOLEAN    			NOT NULL,
	  password     	VARCHAR(255)   		NOT NULL,
      roles    		VARCHAR(255)   		NOT NULL
);


DROP TABLE If EXISTS expenses;
CREATE TABLE expenses (
	  expense_id            int 			auto_increment   		PRIMARY KEY,
	  username              VARCHAR(255)   	NOT NULL,
      expense_category		VARCHAR(255) 	NOT NULL  		CHECK(expense_category IN('Checking', 'Saving', 'Investment', 'Loan', 'Credit')),
	  expense_value         long   			NOT NULL,
	  expense_acc_name      VARCHAR(255)    NOT NULL
);



DROP TABLE If EXISTS transactions;
CREATE TABLE transactions (
	  trans_id           int 			 auto_increment    PRIMARY KEY,
      expense_id         int    		 NOT NULL,
	  trans_date         VARCHAR(30)     NOT NULL,
	  trans_acc_name     VARCHAR(255)    NOT NULL,
      trans_amount		 long 			 NOT NULL,
      trans_type		 VARCHAR(20) 	 NOT NULL 			CHECK(trans_type IN('DEPOSIT', 'WITHDRAWAL')),
      username			VARCHAR(255)	 NOT NULL
);