/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import javax.swing.*;
import javax.xml.transform.Result;
import java.sql.*; //imports JDBC classes
import java.text.DateFormat;
import java.util.ArrayList;

public class LibraryModel {

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;

    //connection object used to connect to the sql database
    Connection con;

    //url to the sql database you want to connect to
    String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/rhodedavi_jdbc";

    public LibraryModel(JFrame parent, String userid, String password) {
	    dialogParent = parent;

	    try {
            //Register a PostgreSQL Driver
            Class.forName("org.postgresql.Driver");
            //Establish a Connection
            con = DriverManager.getConnection(url, userid, password);
        } catch (SQLException | ClassNotFoundException cnfe){
            System.out.println("Can not find"+
                    "the driver class: "+
                    "\nEither I have not installed it"+
                    "properly or \n postgresql.jar "+
                    " file is not in my CLASSPATH");
            System.out.println(cnfe.getMessage());
            cnfe.printStackTrace();
        }
    }

    public String bookLookup(int isbn) { //needs to show isbn:bookname, edition: number, Num of Copies: x - copies left: x, authors: x

        //variables related to the columns of the Book table
        String Title = "";
        ArrayList<String> AuthorsLastName= new ArrayList<>();
        int Edition_No = 0;
        int NumOfCop = 0;
        int NumLeft = 0;
        int NumOfBooksWithISBN = 0;

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tBOOK LOOKUP \n");

        //sql query to be executed
        String query = "SELECT bk.ISBN, bk.Title, au.Surname, bk.Edition_No, bk.NumOfCop, bk.NumLeft " +
                "FROM Book as bk " +
                "LEFT JOIN Book_Author AS bkAu ON bk.ISBN = bkAu.ISBN " + // gets book author
                "LEFT JOIN Author as au ON au.AuthorID = bkAu.AuthorID " + //uses the book author ID number to get the Author name and surname
                "WHERE bk.ISBN = " + isbn +  // filters the table to only display those that match the input ISBN number, because we are not doing agg function we need to look through the returned statements using rs.next() later.
                " ORDER BY bkAu.AuthorSeqNo;";
        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);

            //need to do to account for more than one author!
            while (rs.next()){
                NumOfBooksWithISBN++;
                //if(rs.getString("Title") != null) Title = rs.getString("Title");
                Title = rs.getString("Title");
                //if you dont trim them the result will have massive spaces, also this if statement accounts for when some books dont have authors
                if(rs.getString("Title") != null) AuthorsLastName.add(rs.getString("Surname").trim());
                Edition_No = rs.getInt("Edition_No");
                NumOfCop = rs.getInt("NumOfCop");
                NumLeft = rs.getInt("Numleft");
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        //cancels lookup if there are no books matching the ISBN
        if(NumOfBooksWithISBN == 0){
            returnResult.append("No book with the ISBN " + isbn + " exists");
            return returnResult.toString();
        }
        //Builds the result to be returned to the user
        returnResult.append(isbn + ": ").append(Title).append("\n\tEdition: ").append(Edition_No);
        returnResult.append("\n\tNumber of Copies Total: ").append(NumOfCop);
        returnResult.append("\n\tCopies Left: ").append(NumLeft + "\n");

        //loop to display all authors
        if(AuthorsLastName.isEmpty()){
            returnResult.append("\t No Authors in System");
        } else {
            //changes formating depending no number of authors
            if(AuthorsLastName.size() > 1){
                returnResult.append("\tAuthors: ");
            } else {
                returnResult.append("\tAuthor: ");
            }
            //displays all authors in the author last name array
            for (int i = 0; i < AuthorsLastName.size(); i ++){
                returnResult.append(AuthorsLastName.get(i));
                if ((AuthorsLastName.size() - 1) != i){
                    returnResult.append(", ");
                }
            }
        }
        return returnResult.toString();
    }







    public String showCatalogue() { //needs to show isbn:bookname, edition: number, Num of Copies: x - copies left: x, authors: x

        //variables related to the columns of the Book table
        ArrayList<String> Title = new ArrayList<>();
        ArrayList<String> AuthorsLastName = new ArrayList<>();
        ArrayList<String> Edition_No = new ArrayList<>();
        ArrayList<String> NumOfCop = new ArrayList<>();
        ArrayList<String> NumLeft = new ArrayList<>();
        ArrayList<String> ISBN = new ArrayList<>();
        int NumOfBooks = 0;


        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        //generic message we display when the this function is used
        returnResult.append("\t\t\t\t\t\t\t\t\tSHOW CATALOGUE \n");

        //sql query to be executed
        String query = "SELECT bk.ISBN, bk.Title, string_agg(au.Surname,  ', ' ORDER BY bkAu.AuthorSeqNo) as Surname, bk.Edition_No, bk.NumOfCop, bk.NumLeft " +
                "FROM Book as bk " +
                "LEFT JOIN Book_Author AS bkAu ON bk.ISBN = bkAu.ISBN " + // gets book author
                "LEFT JOIN Author as au ON au.AuthorID = bkAu.AuthorID " +//uses the book author ID number to get the Author name and surname
                "GROUP BY bk.ISBN, bk.Title, bk.Edition_No, bk.NumOfCop, bk.NumLeft;"; // need to this to when using agg function on select query

        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()){
                NumOfBooks++;
                if(rs.getString("Title") != null) Title.add(rs.getString("Title").trim());
                //if you dont trim them the result will have massive spaces, also this if statement accounts for when some books dont have authors
                AuthorsLastName.add(rs.getString("Surname"));
                Edition_No.add(Integer.toString(rs.getInt("Edition_No")));
                NumOfCop.add(Integer.toString(rs.getInt("NumOfCop")));
                NumLeft.add(Integer.toString(rs.getInt("Numleft")));
                ISBN.add(Integer.toString(rs.getInt("ISBN")));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        //loop to populate the books list
        for (int i = 0; i < NumOfBooks; i++){
            //Builds the result to be returned to the user
            returnResult.append(ISBN.get(i) + ": " + Title.get(i)).append("\n\tEdition: ").append(Edition_No.get(i));
            returnResult.append("\n\tNumber of Copies Total: ").append(NumOfCop.get(i));
            returnResult.append("\n\tCopies Left: ").append(NumLeft.get(i) + "\n");

            //loop to display all authors
            if(AuthorsLastName.get(i) == null){
                returnResult.append("\tNo Authors in System");
            } else if (AuthorsLastName.get(i).contains(",")){
                returnResult.append("\tAuthors: ");
                returnResult.append(AuthorsLastName.get(i));
            } else {
                returnResult.append("\tAuthor: ");
                returnResult.append(AuthorsLastName.get(i));
            }
            returnResult.append("\n\n");
        }
        return returnResult.toString();
    }


//returnResult.append("\t No Authors in System");
//} else if (AuthorsLastName.get(i).contains(",")){
//        returnResult.append("\tAuthors: ");
//        } else{
//        returnResult.append("\tAuthor: ");
//        returnResult.append(AuthorsLastName.get(i));
//        }
//        }


    public String showLoanedBooks() { //needs to show isbn:bookname, edition: number, Num of Copies: x - copies left: x, authors: x WHERE LOANED = x;

        //variables related to the columns of the Book table
        ArrayList<String> Title = new ArrayList<>();
        ArrayList<String> AuthorsLastName = new ArrayList<>();
        ArrayList<String> Edition_No = new ArrayList<>();
        ArrayList<String> NumOfCop = new ArrayList<>();
        ArrayList<String> NumLeft = new ArrayList<>();
        ArrayList<String> ISBN = new ArrayList<>();
        ArrayList<String> firstName = new ArrayList<>();
        ArrayList<String> lastName = new ArrayList<>();
        ArrayList<String> City = new ArrayList<>();
        ArrayList<String> CustomerId = new ArrayList<>();
        ArrayList<String> ISBNtoBorrower = new ArrayList<>(); // array which contains all the unique books
        ArrayList<String> BorrowerName = new ArrayList<>();
        int NumOfBooks = 0;
        int Checker = 0;

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        //generic message we display when the this function is used
        returnResult.append("\t\t\t\t\t\t\t\t\tSHOW LOANED BOOKS \n");

        //sql query to be executed
        String query = "SELECT loaned.ISBN, bk.Title, bk.Edition_No, bk.NumOfCop, bk.NumLeft, string_agg(au.Surname,  ', ') AS Surname, loaned.CustomerId, cus.L_Name, cus.F_Name, cus.City " +
                "FROM Cust_Book AS loaned " +
                "LEFT JOIN Book AS bk ON loaned.ISBN = bk.ISBN " +
                "LEFT JOIN Book_Author AS bkAu ON loaned.ISBN = bkAu.ISBN " +
                "LEFT JOIN Author AS  au ON bkAu.AuthorId = au.AuthorId " +
                "LEFT JOIN Customer AS cus ON loaned.CustomerId = cus.CustomerId " +
                "GROUP BY loaned.ISBN, bk.Title, bk.Edition_No, bk.NumOfCop, bk.NumLeft, loaned.CustomerId, cus.L_Name, cus.F_Name, cus.City;";

        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()){
                NumOfBooks++;
                if(rs.getString("Title") != null) Title.add(rs.getString("Title").trim());
                AuthorsLastName.add(rs.getString("Surname"));
                Edition_No.add(Integer.toString(rs.getInt("Edition_No")));
                NumOfCop.add(Integer.toString(rs.getInt("NumOfCop")));
                NumLeft.add(Integer.toString(rs.getInt("Numleft")));
                ISBN.add(Integer.toString(rs.getInt("ISBN")));
                firstName.add(rs.getString("F_Name").trim());
                lastName.add(rs.getString("L_Name").trim());
                City.add(rs.getString("City"));
                CustomerId.add(Integer.toString(rs.getInt("CustomerId")));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        //method that checks the current index and compares it to all other index for match. Removes the match
        Checker = NumOfBooks;
        ISBNtoBorrower = ISBN;

//        for (int i = 0; i < Checker; i++){
//            String CurrentISBN = ISBN.get(i);
//
//            for(int j = 0; j <ISBNtoBorrower.size(); j++)
//            //makes sure that we dont access an array index that does not exist
//            if (i >= ISBN.size() -1){
//                //compares current isbn to the proceding one
//                if(CurrentISBN.equals(ISBN.get(i+1))){
//                    ISBNtoBorrower.add
//
//                }
//            }
//            ISBNtoBorrower.add("\n");
//        }
        //loop to populate the books list
        for (int i = 0; i < NumOfBooks; i++){
            //Builds the result to be returned to the user
            returnResult.append(ISBN.get(i) + ": " + Title.get(i)).append("\n\tEdition: ").append(Edition_No.get(i));
            returnResult.append("\n\tNumber of Copies Total: ").append(NumOfCop.get(i));
            returnResult.append("\n\tCopies Left: ").append(NumLeft.get(i) + "\n");

            //loop to display all authors
            if(AuthorsLastName.isEmpty()){
                returnResult.append("\t No Authors in System \n");
            } else {
                returnResult.append("\tAuthor(s): ");
                returnResult.append(AuthorsLastName.get(i));
                returnResult.append("\n");
            }
            returnResult.append("\tLoaned To: " + CustomerId.get(i) + " - " + firstName.get(i) + " " + lastName.get(i) + "\n\n");
        }

        return returnResult.toString();
    }


    public String showAuthor(int authorID) { //needs to show authorId - Name, books written: list books
        ArrayList<String> BookTitle = new ArrayList<>();
        ArrayList<String> BookISBN = new ArrayList<>();
        int ColumnsFound = 0;
        String firstname = "";
        String surname = "";

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        //generic message we display when the this function is used
        returnResult.append("\t\t\t\t\t\t\t\t\tSHOW AUTHOR \n");

        //sql query to be executed
        String query = "SELECT au.AuthorId, bk.Title, au.Surname, au.Name, bk.ISBN, bk.Title " +
                "FROM Author as au " +
                "LEFT JOIN Book_Author AS bkAu ON au.AuthorID = bkAu.AuthorID " + // gets book author
                "LEFT JOIN Book as bk ON bk.ISBN = bkAu.ISBN " + //uses the book author ID number to get the Author name and surname
                "WHERE au.AuthorId = " + authorID + ";"; // filters the table to only display those that match the input ISBN number, because we are not doing agg function we need to look through the returned statements using rs.next() later.

        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()){
                firstname = rs.getString("Name").trim();
                surname = rs.getString("Surname").trim();
                ColumnsFound++;
                if(rs.getString("Title") != null) BookTitle.add(rs.getString("Title").trim());
                BookISBN.add(Integer.toString(rs.getInt("ISBN")));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        //cancels lookup if there are no books matching the ISBN
        if(ColumnsFound == 0){
            returnResult.append("No author with ID " + authorID + " exists");
            return returnResult.toString();
        }

        //Builds the result to be returned to the user
        returnResult.append("\t" + authorID + ": ").append(firstname + " " + surname + "\n");

        //if no books found then this is returned
        if(ColumnsFound == 0 || BookTitle.isEmpty()){
            returnResult.append("\tThis author has no books written");
            return returnResult.toString();
        }

        //loop to display all authors
        // displays all authors in the author last name array
        for (int i = 0; i < ColumnsFound; i ++) {
            returnResult.append("\t" + BookISBN.get(i));
            returnResult.append(" - " + BookTitle.get(i));
            if ((BookISBN.size() - 1) != i) {
                returnResult.append("\n");
            }
        }
            return returnResult.toString();
    }



    public String showAllAuthors() { //show all authors ... authorId: lastname, name

        //variables related to the columns of the Book table
        ArrayList<String> Name = new ArrayList<>();
        ArrayList<String> AuthID = new ArrayList<>();
        int ColumnsFound = 0;
        String firstname = "";
        String surname = "";

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tSHOW ALL AUTHORS \n");

        //sql query to be executed
        String query = "SELECT * FROM Author ORDER BY AuthorId ASC;"; // filters the table to only display those that match the input ISBN number, because we are not doing agg function we need to look through the returned statements using rs.next() later.

        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);

            //need to do to account for more than one author!
            while (rs.next()){
                ColumnsFound++;
                firstname = rs.getString("Name").trim();
                surname = rs.getString("Surname").trim();
                Name.add(surname + ", " + firstname);
                AuthID.add(Integer.toString(rs.getInt("AuthorId")));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        if(ColumnsFound == 0){
            returnResult.append("No authors in the database");
            return returnResult.toString();
        }

        for (int i = 0; i < ColumnsFound; i ++) {
            returnResult.append("\t" + AuthID.get(i));
            returnResult.append(": " + Name.get(i));
            if ((AuthID.size() - 1) != i) {
                returnResult.append("\n");
            }
        }
        return returnResult.toString();
    }



    public String showCustomer(int customerID) { //cusid: lastname, name - city, IF BOOKS BORROREWED
        int ColumnsFound = 0;
        String firstname = "";
        String surname = "";
        String city = "";
        ArrayList<String> BookTitle = new ArrayList<>();
        ArrayList<Integer> BookISBN = new ArrayList<>();

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        //generic message we display when the this function is used
        returnResult.append("\t\t\t\t\t\t\t\t\tSHOW CUSTOMER \n");

        //sql query to be executed
        String query = "SELECT cus.F_Name, cus.L_Name, cus.City, cus.CustomerID, bk.ISBN, bk.Title " +
                "FROM Customer AS cus " +
                "LEFT JOIN Cust_Book AS cusBk ON cus.CustomerID = cusBk.CustomerID " + // gets book author
                "LEFT JOIN Book AS bk ON bk.ISBN = cusBk.ISBN " + //uses the book author ID number to get the Author name and surname
                "WHERE cus.CustomerID = " + customerID + ";"; // filters the table to only display those that match the input ISBN number, because we are not doing agg function we need to look through the returned statements using rs.next() later.

        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()){
                firstname = rs.getString("F_Name").trim();
                surname = rs.getString("L_Name").trim();
                city = rs.getString("City").trim();
                ColumnsFound++;
                if(rs.getString("Title") != null) BookTitle.add(rs.getString("Title").trim());
                BookISBN.add(rs.getInt("ISBN"));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        //cancels lookup if there are no books matching the ISBN
        if(ColumnsFound == 0){
            returnResult.append("No Customer with ID " + customerID + " exists");
            return returnResult.toString();
        }

        //Builds the result to be returned to the user
        returnResult.append("\t" + customerID + ": ").append(surname + " " + firstname + "\n");
        //if no books found then this is returned
        if(BookTitle.size() == 0){
            returnResult.append("\tThis customer has no books borrowed");
            return returnResult.toString();
        }

        //loop to display all authors
        //displays all authors in the author last name array
        returnResult.append("\t--Books Borrowed-- \n");
        for (int i = 0; i < BookISBN.size(); i ++) {
            returnResult.append("\t" + BookISBN.get(i));
            returnResult.append(" - " + BookTitle.get(i));
            if (i < (BookISBN.size() - 1) ) {
                returnResult.append("\n");
            }
        }
        return returnResult.toString();
    }



    public String showAllCustomers() {//cusid: lastname, name - city

        //variables related to the columns of the Book table
        ArrayList<String> Name = new ArrayList<>();
        ArrayList<String> CustID = new ArrayList<>();
        ArrayList<String> City = new ArrayList<>();
        int ColumnsFound = 0;
        String firstname = "";
        String surname = "";

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tSHOW ALL CUSTOMERS \n");

        //sql query to be executed
        String query = "SELECT * FROM Customer ORDER BY CustomerID ASC;"; // filters the table to only display those that match the input ISBN number, because we are not doing agg function we need to look through the returned statements using rs.next() later.

        try {
            //opens a connection with the sql server
            Statement stmt = con.createStatement();
            //sql statement object, if this fails then an exception is thrown
            ResultSet rs = stmt.executeQuery(query);

            //need to do to account for more than one author!
            while (rs.next()){
                ColumnsFound++;
                firstname = rs.getString("F_Name").trim();
                surname = rs.getString("L_Name").trim();
                Name.add(surname + ", " + firstname);
                CustID.add(Integer.toString(rs.getInt("CustomerID")));
                City.add(rs.getString("City"));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }

        if(ColumnsFound == 0){
            returnResult.append("No customers in the database");
            return returnResult.toString();
        }

        for (int i = 0; i < ColumnsFound; i ++) {
            returnResult.append("\t" + CustID.get(i));
            returnResult.append(": " + Name.get(i));
            if (City.get(i) == null){
                returnResult.append(" - (NO CITY FOUND)");
            } else {
                returnResult.append(" - " + City.get(i));
            }
            if ((CustID.size() - 1) != i) {
                returnResult.append("\n");
            }
        }
        return returnResult.toString();
    }


    //NOTE IF THE PERSON HAS THE SAME BOOK IT HAS BEEN IGNORED. PEOPLE CAN TAKE 2 BOOKS OUT IF THEY WANT, I.E ONE BOOK FOR EACH CHILD
    public String borrowBook(int isbn, int customerID,
			     int day, int month, int year) {

        int bookFound = 0;
        int custFound = 0;
        String firstname = "";
        String surname = "";
        int numLeft = 0;
        int numOfCop = 0;
        String Title = "";
        String date = "" + year +"-"+ month +"-"+ day;
        String sqlDate = "TO_DATE(\'" + date + "\', \'YYYY-MM-DD\')";


        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tBORROW BOOK \n");

        //sql queries to check existance of books and customer
        String checkBook ="SELECT * FROM Book WHERE ISBN = " + isbn + " FOR UPDATE;";
        String checkCustomer = "SELECT * FROM Customer WHERE CustomerID = " + customerID +" FOR UPDATE;";

        //update sql statements to be used
        String updateBook = "UPDATE Book SET NumLeft = NumLeft - 1 WHERE ISBN =" + isbn +";";
        String updateCust_book = "INSERT INTO Cust_Book (CustomerId, DueDate, ISBN) VALUES (" + customerID + "," + sqlDate + "," + isbn + ");";

        try {
            //prevents the database from commiting any changes until i want it to
            con.setAutoCommit(false);

            //opens a connection with the sql server
            Statement stmt = con.createStatement();

            //sql statement object, if this fails then an exception is thrown
            ResultSet checkCus = stmt.executeQuery(checkCustomer);

            //method of checking if the customer exists
            while (checkCus.next()){
                firstname = checkCus.getString("F_Name").trim();
                surname = checkCus.getString("L_Name").trim();
                custFound++;
            }
            if (custFound == 0){
                returnResult.append("No customer with the ID" + customerID + " exists");
                return returnResult.toString();
            }
            ResultSet checkBk = stmt.executeQuery(checkBook);
            //Checks to see if the book exists and if there is any copies for it to be borrowed
            while (checkBk.next()){
                Title = checkBk.getString("Title");
                numLeft = checkBk.getInt("NumLeft");
                numOfCop = checkBk.getInt("NumOfCop");
                bookFound ++;
            }
            if(bookFound == 0){
                returnResult.append("No book with the ISBN " + isbn + " exists");
                return returnResult.toString();
            }
            if (numLeft < 1){
                returnResult.append("Sorry, there are no books left in stock with the ISBN " + isbn);
                return returnResult.toString();
            }
            //warns the user that the database is going to update
            showError();
            //locks the tuples being updated on from being changed by another user
            stmt.executeUpdate(updateBook);
            stmt.executeUpdate(updateCust_book);

            //commits the updates to the database
            con.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
            returnResult.append("Error: Either you entered an INVALID date of the month. Please restart the program to reset the connection to the database.");
            return returnResult.toString();
        }
    returnResult.append("\tBook: " + isbn + " - " + Title +"\n");
        returnResult.append("\tLoaned to: " + customerID + " - " + firstname + " " + surname + "\n"); //what the hell... the IDE wont let me indent correctly
        returnResult.append("\tDue Date: " + year + "-" + month + "-" + day);
	return returnResult.toString();
    }



    public String returnBook(int isbn, int customerid) {

        int bookFound = 0;
        int custFound = 0;
        String firstname = "";
        String surname = "";
        int numLeft = 0;
        int numOfCop = 0;
        String Title = "";


        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tBOOK RETURNED \n");

        //sql queries to check existance of books and customer
        String checkBook ="SELECT * FROM Book WHERE ISBN = " + isbn + " FOR UPDATE";
        String checkCustomer = "SELECT * FROM Customer WHERE CustomerID = " + customerid +" FOR UPDATE;";

        //update sql statements to be used
        String updateBook = "UPDATE Book SET NumLeft = NumLeft + 1 WHERE ISBN =" + isbn +";";
        //String updateCust_book = "INSERT INTO Cust_Book (CustomerId, DueDate, ISBN) VALUES (" + customerID + ", " + isbn + ", " + year + "-" + month + "-" + day +");";
        String deleteCust_book = "DELETE FROM Cust_Book WHERE CustomerId = " + customerid  + " AND ISBN = " + isbn + ";";

        try {
            //prevents the database from commiting any changes until i want it to
            con.setAutoCommit(false);

            //opens a connection with the sql server
            Statement stmt = con.createStatement();

            //sql statement object, if this fails then an exception is thrown
            ResultSet checkCus = stmt.executeQuery(checkCustomer);

            //method of checking if the customer exists
            while (checkCus.next()){
                firstname = checkCus.getString("F_Name").trim();
                surname = checkCus.getString("L_Name").trim();
                custFound++;
            }
            if (custFound == 0){
                returnResult.append("No customer with the ID" + customerid + " exists");
                return returnResult.toString();
            }

            //Checks to see if the book exists and if there is any copies for it to be borrowed
            ResultSet checkBk = stmt.executeQuery(checkBook); //turns out only one executeQuery statement can be done at a time
            while (checkBk.next()){
                Title = checkBk.getString("Title");
                numLeft = checkBk.getInt("NumLeft");
                numOfCop = checkBk.getInt("NumOfCop");
                bookFound ++;
            }
            if(bookFound == 0){
                returnResult.append("No book with the ISBN " + isbn + " exists");
                return returnResult.toString();
            }

            //warns the user that the database is going to update
            showError();
            //locks the tuples being updated on from being changed by another user

            stmt.executeUpdate(updateBook);
            stmt.executeUpdate(deleteCust_book);

            //commits the updates to the database
            con.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }
        //creates output to be displayed once the row has been deleted/updated
        returnResult.append("\tBook: " + isbn + " - " + Title +"\n");
        returnResult.append("\tLoaned to: " + customerid + " - " + firstname + " " + surname + "\n\n"); //what the hell... the IDE wont let me indent correctly
        return returnResult.toString();
    }



    public void closeDBConnection() {
        try {
            con.close();
            System.out.println("CONNECTION TO DATABASE HAS BEEN TERMINATED");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    public String deleteCus(int customerID) {

        int bookFound = 0;
        int custFound = 0;
        String firstname = "";
        String surname = "";
        String Title = "";
        int ISBN = 0;


        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tCUSTOMER DELETE \n");

        //sql queries to check existance of books and customer
        String checkIfBookBorrowed ="SELECT cusBk.CustomerId as CustomerId, bk.ISBN, bk.Title " +
                "FROM Cust_book as cusBk "+
                "NATURAL JOIN Book AS bk WHERE CustomerId = " + customerID + ";";
        String checkCustomer = "SELECT * FROM Customer WHERE CustomerID = " + customerID +";";

        try {
            //prevents the database from commiting any changes until i want it to
            con.setAutoCommit(false);

            //opens a connection with the sql server
            Statement stmt = con.createStatement();

            //sql statement object, if this fails then an exception is thrown
            ResultSet checkCus = stmt.executeQuery(checkCustomer);

            //method of checking if the customer exists
            while (checkCus.next()){
                firstname = checkCus.getString("F_Name").trim();
                surname = checkCus.getString("L_Name").trim();
                custFound++;
            }
            if (custFound == 0){
                returnResult.append("No customer with the ID" + customerID + " exists");
                return returnResult.toString();
            }

            //Checks to see if the book exists and if there is any copies for it to be borrowed
            ResultSet checkBk = stmt.executeQuery(checkIfBookBorrowed); //turns out only one executeQuery statement can be done at a time
            while (checkBk.next()){
                Title = checkBk.getString("Title");
                ISBN = checkBk.getInt("ISBN");
                bookFound ++;
            }
            if(bookFound > 0){
                returnResult.append("This customer still currently has a book out, no deletion can be made until they return it");
                return returnResult.toString();
            }

            //update sql statements to be used
            String deleteCust_book = "DELETE FROM Customer WHERE CustomerId = " + customerID +";";

            //warns the user that the database is going to update
            showError();
            //locks the tuples being updated on from being changed by another user
            stmt.executeUpdate(deleteCust_book);

            //commits the updates to the database
            con.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }
        //creates output to be displayed once the row has been deleted/updated
        returnResult.append("\tCustomer with the ID " + customerID + " has been DELETED\n");
        return returnResult.toString();
    }


//when deleted, any value is set to default. So only needed to delete the AuthorId
    public String deleteAuthor(int authorID) {

        int authFound = 0;

        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tAUTHOR DELETE \n");

        //sql queries to check existance of books and customer
        String checkIfExists ="SELECT AuthorId " +
                "FROM Author "+
                "WHERE AuthorId = " + authorID + ";";

        try {
            //prevents the database from commiting any changes until i want it to
            con.setAutoCommit(false);

            //opens a connection with the sql server
            Statement stmt = con.createStatement();

            //sql statement object, if this fails then an exception is thrown
            ResultSet checkExistance = stmt.executeQuery(checkIfExists);

            //method of checking if the author exists and gives error if there is none
            while (checkExistance.next()){
                authFound++;
            }
            if (authFound == 0){
                returnResult.append("\tNo Author with the ID " + authorID + " exists");
                return returnResult.toString();
            }

            //update sql statements to be used
            String deleteCust_book = "DELETE FROM Author WHERE AuthorId = " + authorID + ";";

            //warns the user that the database is going to update
            showError();
            //locks the tuples being updated on from being changed by another user
            stmt.executeUpdate(deleteCust_book);

            //commits the updates to the database
            con.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }
        //creates output to be displayed once the row has been deleted/updated
        returnResult.append("\tAuthor with ID " + authorID + " was DELETED\n\n");
        return returnResult.toString();
    }





    //when deleted, any value is set to default. So only needed to delete the ISBN
    public String deleteBook(int isbn) {
        //have to stop deletion of book if there is the customer has a book out

        int bookFound = 0;
        int loanFound = 0;
        String Title = "";
        int ISBN = 0;


        //string we want to return as the result
        StringBuilder returnResult = new StringBuilder();
        returnResult.append("\t\t\t\t\t\t\t\t\tBOOK DELETE \n");

        //sql queries to check existance of books and customer
        String checkIfBookBorrowed ="SELECT cusBk.CustomerId as CustomerId, bk.ISBN, bk.Title " +
                "FROM Cust_book as cusBk "+
                "NATURAL JOIN Book AS bk WHERE ISBN = " + isbn + ";";
        String checkBookExists = "SELECT * FROM Book WHERE ISBN = " + isbn +";";

        try {
            //prevents the database from commiting any changes until i want it to
            con.setAutoCommit(false);

            //opens a connection with the sql server
            Statement stmt = con.createStatement();

            //sql statement object, if this fails then an exception is thrown
            ResultSet checkBook = stmt.executeQuery(checkBookExists);

            //method of checking if the customer exists
            while (checkBook.next()){
                Title = checkBook.getString("Title");
                bookFound++;
            }
            if (bookFound == 0){
                returnResult.append("No book with the ISBN" + isbn + " exists");
                return returnResult.toString();
            }

            //Checks to see if the book is borrowed. An extra function/button would need to be added to fully clear out the book if the library truely wants to delete the book and forget about it.
            //Though, I imagine that deleting a book wouldnt be an issue because the defualt ISBN value of 0 would be used thus it would still track users who have borrowed books without returning them.
            //simply solution is to just log that the book is returned, then delete it. This method prevents you from deleting a book accidently that is loaned.
            ResultSet checkBk = stmt.executeQuery(checkIfBookBorrowed); //turns out only one executeQuery statement can be done at a time
            while (checkBk.next()){
                loanFound ++;
            }
            if(loanFound > 0){
                returnResult.append("A customer still has this book on loan, no deletion can be made until all copies are returned");
                return returnResult.toString();
            }

            //update sql statements to be used
            String deleteCust_book = "DELETE FROM Book WHERE ISBN = " + isbn  + ";";

            //warns the user that the database is going to update
            showError();
            //locks the tuples being updated on from being changed by another user
            stmt.executeUpdate(deleteCust_book);

            //commits the updates to the database
            con.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getSQLState());
            System.out.println(e.getErrorCode());
            System.out.println("An exception"+
                    "while creating a statement,"+
                    "probably means I am no longer"+
                    "connected");
        }
        //creates output to be displayed once the row has been deleted/updated
        returnResult.append("\t" + ISBN + " - " + Title +"\n");
        returnResult.append("\tHas been DELETED from the database\n\n"); //what the hell... the IDE wont let me indent correctly
        return returnResult.toString();
    }



    //warning box that is displayed when a database modification is about to take place
    private void showError(){
        JOptionPane.showMessageDialog(LibraryUI.getFrames()[0], " Locked the tuple(s), ready to update. Click OK to continue", "Database Locked", JOptionPane.WARNING_MESSAGE);
    }

}