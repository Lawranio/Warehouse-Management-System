package com.company;


import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.crypto.Data;


/*

Класс содержит описание пользователя "Работник магазина":

- интерфейс, состоящий из вложенного класса ShopInterface

- функции пользователя (кнопки)

 */


class ShopEmployee {

    public static String shopName;

    public ShopEmployee(Connection connection, String shop) {
        ShopInterface app = new ShopInterface(connection);
        app.setVisible(true);
        shopName = "'" + shop + "'";
    }

    private static class ShopInterface extends JFrame {

        ResultSet resultSet;
        String SQL;

        // Создать запрос на товар
        JButton requestSupply = new JButton(new AbstractAction("Запрос на поставку") {

            @Override
            public void actionPerformed(ActionEvent e) {

                boolean nameCheck = false;  // проверка названия
                String stuff_id = "";            // код товара
                String name = "";           // название товара
                String amount;              // количество товара
                String deliveryDates;        // дата поставки
                String price = "";               // стоимость доставки
                String shop_id = null;             // код магазина
                String shop_address = null;        // адрес магазина

                // Проверка: Существует ли товар с таким названием
                while (!nameCheck) {

                    name = JOptionPane.showInputDialog("Введите название продукта");
                    name = "'" + name + "'";
                    if (name.equals("'null'")) break;

                    try {

                        if (DatabaseHandler.searchName(connection, name)) nameCheck = true;
                        else JOptionPane.showMessageDialog(null, "Товара с таким названием нет", "Ошибка", JOptionPane.WARNING_MESSAGE);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }

                // Ввод количества товара и даты
                amount = JOptionPane.showInputDialog("Введите количество продукта");
                deliveryDates = JOptionPane.showInputDialog("Введите дату доставки в формате 'год-месяц-дата'");
                deliveryDates = "'" + deliveryDates + "', GETDATE(), ";


                // TODO: Проверка количества товара на складе
                // Поиск кода товара и формирование стоимости
                try {

                    SQL = "select * from Product where product_name = " + name;
                    resultSet = DatabaseHandler.doSelect(connection, SQL);
                    resultSet.next();
                    stuff_id = resultSet.getString(1) + ", ";
                    price = Integer.toString(resultSet.getInt(4) * Integer.parseInt(amount));
                    amount = ", " + amount;
                    price = ", " + price + ", ";

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }


                // Поиск идентификатора магазина и адреса
                SQL = "select * from Shop where shop_name = " + shopName;
                try {

                    resultSet = DatabaseHandler.doSelect(connection, SQL);
                    resultSet.next();
                    shop_id = resultSet.getString(1) + ", ";
                    shop_address = ", '" + resultSet.getString(3) + "'" + ", ";

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }


                // Добавление запроса в базу данных
                SQL = "insert into Delivery values (" + stuff_id + name + amount + price + deliveryDates + shop_id + shopName + shop_address + "'Создано', null, null)";
                System.out.println(SQL);

                try {

                    DatabaseHandler.doUpdate(connection, SQL);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            }
        });

        Connection connection;

        ShopInterface(Connection con) {

            super("Склад - работник магазина");
            connection = con;
            this.setBounds(600, 400, 900, 450);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container container = this.getContentPane();
            container.setLayout(new GridLayout(2, 4, 25, 25));

            container.add(requestSupply);

        }
    }

}
