package com.company;


import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;


/*

Класс содержит описание пользователя "Работник склада":
- интерфейс, состоящий из вложенного класса WarehouseInterface и
  дополнительного класса ButtonEventListener для работы кнопок

- функции пользователя (кнопки)

 */


class WarehouseEmployee {

    public WarehouseEmployee(Connection connection, Statement statement) {
        WarehouseInterface app = new WarehouseInterface(connection, statement);
        app.setVisible(true);
    }


    private class WarehouseInterface extends JFrame{

        // Создать карточку товара
        JButton newStuffButton = new JButton(new AbstractAction("Создать карточку товара") {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean nameCheck = false;  // для проверки названия
                String SQL;          // запрос в базу данных
                String name = null;         // название товара
                String amount;              // количество товара
                String price;               // стоимость товара


                // Проверка: Существует ли товар с таким названием
                while (!nameCheck) {
                    nameCheck = false;
                    name = JOptionPane.showInputDialog("Введите название продукта");
                    name = "'" + name + "'";
                    SQL = "select * from Product where product_name = " + name;

                    try {

                        statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(SQL);

                        // Если результат запроса НЕпустой
                        if (resultSet.next()) { JOptionPane.showMessageDialog(null, "Товар с таким названием уже существует", "Информация", JOptionPane.PLAIN_MESSAGE); }
                        else nameCheck = true;

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                }


                try {

                    amount = JOptionPane.showInputDialog("Введите количество продукта");
                    amount = ", " + amount;
                    price = JOptionPane.showInputDialog("Введите цену за одну единицу товара");
                    price = ", " + price + ");";
                    SQL = "insert into Product values (" + name + amount + price;

                    statement = connection.createStatement();
                    statement.execute(SQL);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                //TODO: Сделать так, чтобы надпись не выводилась, если ввод данных отменили
                text.append("Создана карточка товара " + name);
            }
        });

        JButton editStuffButton = new JButton(new AbstractAction("Редактировать карточку товара") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame editStuffWindow = new JFrame("Редактирование карточки товара");
                editStuffWindow.setVisible(true);
                editStuffWindow.setBounds(700, 400, 600, 250);

                final JTextField inputField = new JTextField("", 2);
                JCheckBox stuff_id = new JCheckBox("Код товара", false);
                JCheckBox stuff_name = new JCheckBox("Название товара", false);
                JCheckBox stuff_amount = new JCheckBox("Количество товара на складе", false);
                JCheckBox stuff_price = new JCheckBox("Стоимость единицы товара", false);

                Container container = editStuffWindow.getContentPane();
                container.setLayout(new GridLayout(3, 1, 2, 3));

                JButton confirm = new JButton(new AbstractAction("Подтверждение") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        editStuffWindow.dispose();
                        System.out.println(inputField.getText());
                        inputField.setText("");
                    }
                });

                container.add(stuff_name);
                container.add(stuff_amount);
                container.add(stuff_id);
                container.add(stuff_price);
                container.add(inputField);
                container.add(confirm);
            }
        });

        JButton createSupply = new JButton("Сформировать поставку");

        JTextArea text = new JTextArea();

        Connection connection;
        Statement statement;

        WarehouseInterface(Connection con, Statement state) {
            super("Склад - работник склада");
            connection = con;
            statement = state;
            this.setBounds(600, 400, 300, 250);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container container = this.getContentPane();
            container.setLayout(new GridLayout(2, 1, 50, 50));

            text.setEditable(false);

            container.add(newStuffButton);
            container.add(editStuffButton);
            container.add(createSupply);
            container.add(text);

            //createSupply.addActionListener(new ButtonEventListener("Выбрано формирование поставки\n"));

        }
    }
}



