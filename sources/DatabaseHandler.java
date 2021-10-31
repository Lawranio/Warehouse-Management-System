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


    public DatabaseHandler(Connection connection, Statement statement){

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
            connection = DriverManager.getConnection(connectionURL);

            if (login.equals("user=warehouse;")) { WarehouseEmployee employee = new WarehouseEmployee(connection, statement); }
        }

        catch (SQLException ex) {
            //TODO: Сделать так, чтобы программу не нужно было перезапускать
            JOptionPane.showMessageDialog(null, "Неправильные данные для входа. Перезапустите программу и повторите ввод.", "Авторизация", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
    }
}
