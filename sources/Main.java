package com.company;


import java.sql.Connection;
import java.sql.Statement;

public class Main {

    public static void main(String[] args){

        Connection connection = null;
        Statement statement = null;
        DatabaseHandler databaseHandler = new DatabaseHandler(connection, statement);
    }
}
