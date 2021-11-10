package com.company;

import javax.swing.*;
import java.sql.*;
import java.sql.Connection;


/*

Класс содержит описание хэндлера базы данных:
- установление соединения

- создание объекта авторизовавшегося пользователя

 */


public class DatabaseHandler {

    String connectionURL = "jdbc:sqlserver://localhost:1433;databaseName=Warehouse;";
    String login;
    String password;


    public DatabaseHandler(){

        /*
            Авторизация - ввод логина и пароля
         */

        login = JOptionPane.showInputDialog("Логин");
        password = JOptionPane.showInputDialog("Пароль");

        login = "user=" + login + ";";
        password = "password=" + password;
        connectionURL = connectionURL + login + password;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        /*
            Создание объекта авторизовавшегося пользователя
         */

        try {
            Connection connection = DriverManager.getConnection(connectionURL);

            if (login.equals("user=warehouse;")) { WarehouseEmployee employee = new WarehouseEmployee(connection); }
        }

        catch (SQLException ex) {
            //TODO: Сделать так, чтобы программу не нужно было перезапускать
            JOptionPane.showMessageDialog(null, "Неправильные данные для входа. Перезапустите программу и повторите ввод.", "Авторизация", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
    }

    // Статический метод - выполняет запрос SELECT в базу данных
    public static ResultSet doSelect(Connection connection, String SQL) throws SQLException {

        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(SQL);

    }

    // Статический метод - выполняет запросы UPDATE, INSERT, DELETE в базу данных
    public static void doUpdate(Connection connection, String SQL) throws SQLException {

        Statement statement = connection.createStatement();
        statement.execute(SQL);

    }
}
