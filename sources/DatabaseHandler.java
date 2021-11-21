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

            // Создание работника склада
            if (login.equals("user=склад;")) { WarehouseEmployee employee = new WarehouseEmployee(connection); }

            // Создание работников магазина
            if (login.equals("user=магазинОвен;")) { ShopEmployee employee = new ShopEmployee(connection, "Овен"); }

            // Создание работников доставки
            if (login.equals("user=доставкаСдек;")) { DeliveryEmployee employee = new DeliveryEmployee(connection, "Сдек"); }
        }

        catch (SQLException ex) {
            //TODO: Сделать так, чтобы программу не нужно было перезапускать
            JOptionPane.showMessageDialog(null, "Неправильные данные для входа. Перезапустите программу и повторите ввод." +
                                                " При повторяющейся ошибке обратитесь к системному администратору.", "Авторизация", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    // Выполняет запрос SELECT в базу данных
    public static ResultSet doSelect(Connection connection, String SQL) throws SQLException {

        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(SQL);

    }


    // Выполняет запросы UPDATE, INSERT, DELETE в базу данных
    public static void doUpdate(Connection connection, String SQL) throws SQLException {

        Statement statement = connection.createStatement();
        statement.execute(SQL);

    }


    /*

        Проверка: есть ли товар с таким названием

        @return
            true - такой товара есть
            false - такого товара нет

     */
    public static boolean searchName(Connection connection, String name) throws SQLException {

        String SQL = "select * from Product where product_name = " + name;
        ResultSet resultSet = DatabaseHandler.doSelect(connection, SQL);
        return resultSet.next();
    }

}
