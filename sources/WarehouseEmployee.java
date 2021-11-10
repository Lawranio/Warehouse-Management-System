package com.company;


import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;


/*

Класс содержит описание пользователя "Работник склада":
- интерфейс, состоящий из вложенного класса WarehouseInterface и
  дополнительного класса ButtonEventListener для работы кнопок

- функции пользователя (кнопки)

 */


class WarehouseEmployee {

    public WarehouseEmployee(Connection connection) {
        WarehouseInterface app = new WarehouseInterface(connection);
        app.setVisible(true);
    }


    private static class WarehouseInterface extends JFrame{

        // Создать карточку товара
        JButton newStuffButton = new JButton(new AbstractAction("Создать карточку товара") {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean nameCheck = false;  // для проверки названия
                String SQL;                 // запрос в базу данных
                String name = "";         // название товара
                String amount;              // количество товара
                String price;               // стоимость товара
                ResultSet resultSet;        // результат запроса


                // Проверка: Существует ли товар с таким названием
                while (!nameCheck) {

                    name = JOptionPane.showInputDialog("Введите название продукта");
                    name = "'" + name + "'";
                    SQL = "select * from Product where product_name = " + name;

                    try {

                        resultSet = DatabaseHandler.doSelect(connection, SQL);

                        // Если результат запроса НЕпустой
                        if (resultSet.next()) { JOptionPane.showMessageDialog(null, "Товар с таким названием уже существует", "Информация", JOptionPane.PLAIN_MESSAGE); }
                        else nameCheck = true;

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                }


                // Вставка нового продукта
                try {

                    amount = JOptionPane.showInputDialog("Введите количество продукта");
                    amount = ", " + amount;
                    price = JOptionPane.showInputDialog("Введите цену за одну единицу товара");
                    price = ", " + price + ");";
                    SQL = "insert into Product values (" + name + amount + price;

                    DatabaseHandler.doUpdate(connection, SQL);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                if (!name.equals("'null'")) textActionLog.append("Создана карточка товара " + name + "\n");
            }
        });

        JButton editStuffButton = new JButton(new AbstractAction("Редактировать карточку товара") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame editStuffWindow = new JFrame("Редактирование карточки товара");
                editStuffWindow.setVisible(true);
                editStuffWindow.setBounds(700, 400, 600, 250);

                final JTextField inputField = new JTextField("", 2);
                final JCheckBox stuff_id = new JCheckBox("Код товара", false);
                final JCheckBox stuff_name = new JCheckBox("Название товара", false);
                final JCheckBox stuff_amount = new JCheckBox("Количество товара на складе", false);
                final JCheckBox stuff_price = new JCheckBox("Стоимость единицы товара", false);
                final JTextArea textArea = new JTextArea();

                final String[] row = new String[1];              // номер строки товара для изменения
                final ResultSet[] resultSet = new ResultSet[1];  // результат запроса

                // Поиск товара для редактирования
                JButton search = new JButton(new AbstractAction("Найти товар") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        textArea.setText("");

                        boolean resultCheck = false;
                        String SQL = "select * from Product where ";
                        if (stuff_id.isSelected()) {

                            SQL = SQL + "product_id = " + inputField.getText();
                            stuff_id.setSelected(false);
                        }

                        if (stuff_amount.isSelected()) {

                            SQL = SQL + "amount = " + inputField.getText();
                            stuff_amount.setSelected(false);
                        }

                        if (stuff_name.isSelected()) {

                            SQL = SQL + "product_name = " + "'" + inputField.getText() + "'";
                            stuff_name.setSelected(false);
                        }

                        if (stuff_price.isSelected()) {

                            SQL = SQL + "price = " + inputField.getText();
                            stuff_price.setSelected(false);
                        }

                        inputField.setText("");

                        try {

                            resultSet[0] = DatabaseHandler.doSelect(connection, SQL);
                            int i = 1;

                            while (resultSet[0].next()) {

                                resultCheck = true;
                                SQL = resultSet[0].getString(1) + " " + resultSet[0].getString(2) + " " + resultSet[0].getString(3) + " " + resultSet[0].getString(4);
                                SQL = "#" + i + " - " + SQL;
                                textArea.append(SQL + "\n");
                                i++;
                            }

                            if (!resultCheck) textArea.append("Ничего не найдено\n");


                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                // Выбор товара и редактирование
                JButton confirm = new JButton(new AbstractAction("Выбрать товар") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        row[0] = inputField.getText();
                        System.out.println(row[0]);
                        inputField.setText("");

                        String info;
                        String SQL = "update Product set ";

                        try {
                            resultSet[0].absolute(Integer.parseInt(row[0]));

                            if (stuff_amount.isSelected()) {
                                info = JOptionPane.showInputDialog("Изменение количества на складе товара '" + resultSet[0].getString(2) + "'. Сейчас - " + resultSet[0].getString(3));
                                SQL = SQL + "amount = " + info + " where product_id = " + resultSet[0].getString(1);
                                System.out.println(SQL);
                                DatabaseHandler.doUpdate(connection, SQL);
                                editStuffWindow.dispose();
                                textActionLog.append("Изменено количество на складе товара '" + resultSet[0].getString(2) + "' с " + resultSet[0].getString(3) + " на " + info);
                            }

                            if (stuff_name.isSelected()) {
                                info = JOptionPane.showInputDialog("Изменение названия товара '" + resultSet[0].getString(2) + "'");
                                SQL = SQL + "product_name = '" + info + "' where product_id = " + resultSet[0].getString(1);
                                System.out.println(SQL);
                                DatabaseHandler.doUpdate(connection, SQL);
                                editStuffWindow.dispose();
                                textActionLog.append("Изменено название товара '" + resultSet[0].getString(2) + "' на '" + info + "'");
                            }

                            if (stuff_price.isSelected()) {
                                info = JOptionPane.showInputDialog("Изменение стоимости товара '" + resultSet[0].getString(2) + "'. Сейчас - " + resultSet[0].getString(4));
                                SQL = SQL + "price = " + info + " where product_id = " + resultSet[0].getString(1);
                                System.out.println(SQL);
                                DatabaseHandler.doUpdate(connection, SQL);
                                editStuffWindow.dispose();
                                textActionLog.append("Изменена стоимость товара '" + resultSet[0].getString(2) + "' с " + resultSet[0].getString(4) + " на " + info);
                            }

                            if (stuff_id.isSelected()) {
                                JOptionPane.showMessageDialog(null, "Редактирование кода товара невозможно", "Ошибка", JOptionPane.WARNING_MESSAGE);
                                stuff_id.setSelected(false);
                            }


                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                Container container = editStuffWindow.getContentPane();
                container.setLayout(new GridLayout(4, 1, 2, 3));

                container.add(stuff_name);
                container.add(stuff_amount);
                container.add(stuff_id);
                container.add(stuff_price);
                container.add(inputField);
                container.add(search);
                container.add(textArea);
                container.add(confirm);
                textArea.setEditable(false);

            }
        });

        JButton createSupply = new JButton("Сформировать поставку");

        JTextArea textActionLog = new JTextArea();

        Connection connection;

        WarehouseInterface(Connection con) {

            super("Склад - работник склада");
            connection = con;
            this.setBounds(600, 400, 650, 450);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container container = this.getContentPane();
            container.setLayout(new GridLayout(2, 1, 50, 50));

            textActionLog.setEditable(false);

            container.add(newStuffButton);
            container.add(editStuffButton);
            container.add(createSupply);
            container.add(textActionLog);

        }
    }
}



