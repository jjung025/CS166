/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
         System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + " ");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql,authorisedUser);
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql,authorisedUser); break;
                       case 6: ViewOrderStatus(esql,authorisedUser); break;
                       case 7: UpdateUserInfo(esql, authorisedUser); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql,authorisedUser); break;
                       case 7: UpdateUserInfo(esql,authorisedUser); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql,authorisedUser); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                         \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static String find_type(Cafe esql,String userName){ //done
   	try{
      	String query = String.format("SELECT type FROM Users WHERE login = '%s'", userName);
      	List<List<String>> z = esql.executeQueryAndReturnResult(query);
      	List<String> temp = z.get(0);
      	String user = temp.get(0);
      	System.out.println(user);
      	return user;
      }
   	catch(Exception e){
   	System.err.println (e.getMessage());
   	return null;
   	}
   }

   public static void BrowseMenuName(Cafe esql){ //done
   	try{
      	Scanner reader = new Scanner(System.in); //Reads from System in, user input
      	System.out.println("Enter Item");
      	String Input = reader.nextLine();
      	String query = String.format("SELECT * FROM Menu WHERE ItemName = '%s'", Input);
      	int MenItems = esql.executeQueryAndPrintResult(query);
      //	for(int i = 0;i < 13;i++)
      //	{
      ///		List<String> temp = MenItems.get(i);
      /*		String item = temp.get(0);
      		item = item.trim(); //trims the whitespaces
      		System.out.print(", ");
      		System.out.print(item);
      	}
      */
      	System.out.printf("%n");
      }
	catch(Exception e){
	System.err.println (e.getMessage());
	}

   }//end

   public static void BrowseMenuType(Cafe esql){ //done
   	try{
      	Scanner reader = new Scanner(System.in); //Reads from user
      	System.out.println("Enter Item Type");
      	String Input = reader.nextLine();
      	String query = String.format("SELECT ItemName FROM Menu WHERE Type = '%s'", Input);
      //	System.out.println(query);
      	int z = esql.executeQueryAndPrintResult(query);
      
      //	List<List<String>> MenItems = esql.executeQueryAndReturnResult(query);
      //	List<String> temp = MenItems.get(0);
      //	String item = temp.get(0);
      //	System.out.println(item);
      /*	for(int i = 0;i < MenItems.size();i++)
      	{
      		List<String> temp = MenItems.get(i);
      		String item = temp.get(0);
      		item = item.trim(); //trims the whitespaces
      		System.out.print(", ");
      		System.out.print(item);
      	}
      */
      	System.out.printf("%n");
      }
	catch(Exception e){
//	System.err.println (e.getMessage());
	}
   }//end

   public static Integer AddOrder(Cafe esql){  //FIND ORDERID OF THE FOOD// JACKIE DO THIS

	//Following is dead code, tried this but coudln't get it to work.
/*
	try{
	Scanner reader = new Scanner(System.in); //Reads from user
	System.out.println("What do you want to order?");
	String Input = reader.nextLine();
	//INSERT INTO Orders VALUES(OrderID, login,paid,timeStamp,total)
//	String query = String.format("SELECT '%s' FROM Menu", OrderID, user, false, timestamp,total );
}
	catch(Exception e){
//	System.err.println (e.getMessage());
	}
*/
      /*try{
         Scanner reader = new Scanner(System.in); //Reads from user
   	   System.out.println("What do you want to order?");
   	   String Input = reader.nextLine();
   	   INSERT INTO Orders(orderid,login,paid,timeStamp,total) VALUES (%d,'%s',false,CURRENT_TIMESTAMP,%f);
   	   String query = String.format("")
      }
   	catch(Exception e){
      System.err.println (e.getMessage());
   	}*/
   }//end 

   public static void UpdateOrder(Cafe esql){ //Customer can update Comments for their orders 
      // Your code goes here.
     try{
   	Scanner reader = new Scanner(System.in); //Reads from user
   	System.out.println("Enter Order ID you want to update the comments of.");
   	int ID = reader.nextInt();
   	System.out.println("Enter your new comments.");
   	int commentin = reader.nextLine();
   	System.out.print("Your comments are now updated.");
   	System.out.printf("%n");
   	String query = String.format("UPDATE ItemStatus SET comments = '%s' WHERE orderID = '%s'",commentin, ID);
   	esql.executeUpdate(query);
   	String Test = String.format("SELECT orderID, comments FROM ItemStatus WHERE orderID = '%s'", ID);
   	int z = esql.executeQueryAndPrintResult(Test);
     }
     catch(Exception e){
	  System.err.println (e.getMessage());
	  }
   	
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){
	try{
   	Scanner reader = new Scanner(System.in); //Reads from user
   	System.out.println("Enter 1 if you want to update Paid status.");
   	System.out.println("\nEnter 2 if you want to update item status.");
   	int Choice = reader.nextInt();
   	
   	if(Choice == 1)
   	{
   		//Update Paid
   		System.out.println("Enter Order ID you want to change.");
   		int ID = reader.nextInt();
   		String query = String.format("UPDATE Orders SET paid = true WHERE orderID = '%s'",  ID);
   		String Test1 = String.format("SELECT * FROM Orders WHERE orderID = '%s' ", ID);
   		 esql.executeUpdate(query);
   		int z = esql.executeQueryAndPrintResult(Test1);
   	}
   	else if (Choice == 2)
   	{
   		//update item status
   		System.out.println("Enter Order ID you want to change.");
   		int ID = reader.nextInt();
   		System.out.println("Enter the new status.");
   		String statusin = reader.nextLine();
   		String query = string.format("UPDATE ItemStatus SET status = '%s' WHERE orderID = '%s'",statusin, ID);
   		String Test2 = String.format("SELECT * FROM ItemStatus WHERE orderID = '%s'",ID);
   		esql.executeUpdate(query);
   	   int z = esql.executeQueryAndPrintResult(Test2);
   	}
	
	}
	catch(Exception e){
	System.err.println (e.getMessage());
	}

   }//end

   public static void ViewOrderHistory(Cafe esql,String userName){//NEED TO SEE THE MOST RECENT 5 BUT MOSTLY DONE.
   	try{
      	String query = String.format("SELECT * FROM ItemStatus NATURAL JOIN Orders WHERE Login = '%s'", userName);
      	int z = esql.executeQueryAndPrintResult(query);
   	}
   	catch(Exception e){
   	System.err.println (e.getMessage());
   	}
   }//end

   public static void UpdateUserInfo(Cafe esql, String userName){ //DONE.
      // USE executeUpdate(sql);
      try{
      	 System.out.print("What do you want to change your favorite item to?");
               String favItems = in.readLine();
      
      
      	String query = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'" , favItems, userName);
      	 esql.executeUpdate(query);
      
      	System.out.print("Your info is now updated: ");
      	System.out.printf("%n");
      	String Test = String.format("SELECT * FROM Users WHERE login = '%s'", userName);
      	int z = esql.executeQueryAndPrintResult(Test);	
      }	

	catch(Exception e){
	System.err.println(e.getMessage());
}

   }//end

   public static void ManagerUpdateUserInfo(Cafe esql){//DONE.

	try{
	System.out.print("What user do you want to update?");
         String user = in.readLine();
         System.out.print("To what position? (Customer or Employee)");
         String position = in.readLine();

	System.out.print("Your info is now updated: ");
	System.out.printf("%n");
	String query = String.format("UPDATE Users SET Type = '%s' WHERE login = '%s'", position, user);
	esql.executeUpdate(query);
	String Test = String.format("SELECT login, type FROM Users WHERE login = '%s'", user);
	int c = esql.executeQueryAndPrintResult(Test);
	}	

	catch(Exception e){
	System.err.println(e.getMessage());
}

   }//end

   public static void UpdateMenu(Cafe esql){
      // Your code goes here.
      	try{
      	   canner reader = new Scanner(System.in); //Reads from user
   	      System.out.println("Enter 1 if you want to add item to Menu.");
   	      System.out.println("\nEnter 2 if you want to delete item from Menu.");
   	      System.out.println("\nEnter 3 if you want to update an item from Menu.");
   	      int Choice = reader.nextInt();
   	      
   	      if (Choice == 1)
   	      {
   	         
   	      }
   	      else if (Choice == 2)
   	      {
   	         System.out.println("Enter the item name you want to delete.");
   	         String namein = reader.nextLine();
   	         String query = String.format("DELETE FROM Menu WHERE itemName = '%s'",namein);
   	      }
   	      else if (Choice ==3)
   	      {
   	         
   	      }
      	}	

	   catch(Exception e){
   	System.err.println(e.getMessage());
      }
   }//end

   public static void ViewOrderStatus(Cafe esql,String userName){ //Done
	try{
	System.out.print("What order do you want to look at?");
         String order = in.readLine();

	String query = String.format("SELECT ItemName, Status FROM ItemStatus NATURAL JOIN Orders WHERE OrderID = '%s'", order);
	int z = esql.executeQueryAndPrintResult(query);
	}
	catch(Exception e){
	System.err.println (e.getMessage());
	}

   }//end

   public static void ViewCurrentOrder(Cafe esql){//Looks at all orders within 24 hours that aren't paid.
      // Your code goes here.
      try{
	      	String query = String.format("SELECT OrderID,timeStamp FROM Orders WHERE timestamp > (now() - interval '24 hours') AND paid = false");
	      	int z = esql.executeQueryAndPrintResult(query);
      }
	catch(Exception e){
	System.err.println (e.getMessage());
	}
      
   }//end

   public static void Query6(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Cafe
