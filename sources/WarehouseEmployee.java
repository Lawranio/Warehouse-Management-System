package com.company;



import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;


/*

Класс содержит описание пользователя "Работник склада":

- интерфейс, состоящий из вложенного класса WarehouseInterface

- функции пользователя (кнопки)

 */


class WarehouseEmployee {

    public WarehouseEmployee(Connection connection) {
        WarehouseInterface app = new WarehouseInterface(connection);
        app.setVisible(true);
    }


    private static class WarehouseInterface extends JFrame{

        ResultSet resultSet;
        String SQL;

        // Создать карточку товара
        JButton newStuffButton = new JButton(new AbstractAction("Создать карточку товара") {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean nameCheck = false;  // для проверки названия
                String name = "";         // название товара
                String amount;              // количество товара
                String price;               // стоимость товара

                // Проверка: Существует ли товар с таким названием
                while (!nameCheck) {

                    name = JOptionPane.showInputDialog("Введите название продукта");
                    name = "'" + name + "'";

                    try {

                        if (!DatabaseHandler.searchName(connection, name)) nameCheck = true;

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

        // Редактировать карточку товара
        JButton editStuffButton = new JButton(new AbstractAction("Редактировать карточку товара") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame editStuffWindow = new JFrame("Редактирование карточки товара");
                editStuffWindow.setVisible(true);
                editStuffWindow.setBounds(700, 400, 800, 450);

                final Vector<Vector<String>> tableData = new Vector<>();
                final Vector<String> tableRowInfo = new Vector<>();
                final Vector<String> tableHeader = new Vector<>();

                tableHeader.add("Код товара");
                tableHeader.add("Название товара");
                tableHeader.add("Количество");
                tableHeader.add("Стоимость");

                final JTable productTable = new JTable();
                DefaultTableModel tableModel = new DefaultTableModel(tableData, tableHeader) {

                    // Если таблица была изменена, то выполняется этот метод
                    @Override
                    public void fireTableCellUpdated(int row, int column) {
                        super.fireTableCellUpdated(row, column);
                        SQL = "update Product set ";

                        try {
                            resultSet.first();

                            // Изменение кода товара
                            if (column == 0) {

                                tableRowInfo.setElementAt(resultSet.getString(1), 0);
                                tableData.remove(0);
                                tableData.add(tableRowInfo);
                                JOptionPane.showMessageDialog(null, "Изменить код товара невозможно. Изменения не будут записаны.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                            }

                            // Изменение названия товара
                            if (column == 1) {

                                String name = getValueAt(0, 1).toString();
                                name = "'" + name + "'";

                                // Проверка: есть ли товар с таким же названием
                                if (!DatabaseHandler.searchName(connection, name)) {

                                    SQL = SQL + "product_name = '" + getValueAt(0, 1) + "' where product_id = " + resultSet.getString(1);
                                    System.out.println(SQL);
                                    DatabaseHandler.doUpdate(connection, SQL);
                                    textActionLog.append("Изменено название товара '" + resultSet.getString(2) + "' на '" + getValueAt(0, 1) + "'\n");
                                }
                                else {

                                    tableRowInfo.setElementAt(resultSet.getString(2), 1);
                                    tableData.remove(0);
                                    tableData.add(tableRowInfo);
                                }

                            }

                            // Изменение количества товара
                            if (column == 2) {

                                SQL = SQL + "amount = " + getValueAt(0, 2) + " where product_id = " + resultSet.getString(1);
                                System.out.println(SQL);
                                DatabaseHandler.doUpdate(connection, SQL);
                                textActionLog.append("Изменено количество на складе товара '" + resultSet.getString(2) + "' с " + resultSet.getString(3) + " на " + getValueAt(0, 2) + "\n");
                            }

                            // Изменение стоимости товара
                            if (column == 3) {

                                SQL = SQL + "price = " + getValueAt(0, 3) + " where product_id = " + resultSet.getString(1);
                                System.out.println(SQL);
                                DatabaseHandler.doUpdate(connection, SQL);
                                textActionLog.append("Изменена стоимость товара '" + resultSet.getString(2) + "' с " + resultSet.getString(4) + " на " + getValueAt(0, 3) + "\n");
                            }


                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }

                };
                productTable.setModel(tableModel);

                final JTextField inputField = new JTextField("", 2);
                final JCheckBox stuff_id = new JCheckBox("Код товара", false);
                final JCheckBox stuff_name = new JCheckBox("Название товара", false);

                // Поиск товара для редактирования
                JButton search = new JButton(new AbstractAction("Найти товар") {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        SQL = "select * from Product where ";
                        if (stuff_id.isSelected()) {

                            SQL = SQL + "product_id = " + inputField.getText();
                            stuff_id.setSelected(false);
                        }

                        if (stuff_name.isSelected()) {

                            SQL = SQL + "product_name = " + "'" + inputField.getText() + "'";
                            stuff_name.setSelected(false);
                        }

                        inputField.setText("");

                        try {

                            resultSet = DatabaseHandler.doSelect(connection, SQL);

                            if (resultSet.next()) {

                                tableData.clear();
                                tableRowInfo.add(resultSet.getString(1));
                                tableRowInfo.add(resultSet.getString(2));
                                tableRowInfo.add(resultSet.getString(3));
                                tableRowInfo.add(resultSet.getString(4));
                                tableData.add(tableRowInfo);
                                SwingUtilities.updateComponentTreeUI(editStuffWindow);
                                System.out.println(tableData);
                                System.out.println(tableRowInfo);

                            }
                            else JOptionPane.showMessageDialog(null, "Ничего не найдено", "Информация", JOptionPane.PLAIN_MESSAGE);


                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                Container container = editStuffWindow.getContentPane();
                container.setLayout(new GridLayout(3, 1, 2, 3));

                container.add(stuff_name);
                container.add(stuff_id);
                container.add(inputField);
                container.add(search);
                container.add(new JScrollPane(productTable));
            }
        });

        // Сформировать поставку
        JButton createSupply = new JButton("Сформировать поставку");

        // Просмотр товаров
        JButton showStuff = new JButton(new AbstractAction("Просмотр товаров") {
            @Override
            public void actionPerformed(ActionEvent e) {


            }
        });

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



