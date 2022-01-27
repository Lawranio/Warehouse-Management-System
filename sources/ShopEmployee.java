package com.company;


import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


/*

Класс содержит описание пользователя "Работник магазина":

- интерфейс, состоящий из вложенного класса ShopInterface

- функции пользователя (кнопки)

 */


class ShopEmployee {

    public static String shopName;

    public ShopEmployee(Connection connection, String shop) {
        shopName = "'" + shop + "'";
        ShopInterface app = new ShopInterface(connection);
        app.setVisible(true);
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

        // Просмотр поставок
        JButton showSupply = new JButton(new AbstractAction("Поставки") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame showSupplyWindow = new JFrame("Поставки");
                showSupplyWindow.setVisible(true);
                showSupplyWindow.setBounds(700, 400, 1150, 500);

                final Vector<String> name = new Vector<>();
                final Vector<String> phone = new Vector<>();
                final Vector<String> web = new Vector<>();

                String SQL2 = "select delivery_name, phone, web from DeliveryService";
                try {

                    ResultSet resultSet2 = DatabaseHandler.doSelect(connection, SQL2);

                    while (resultSet2.next()) {

                        name.add(resultSet2.getString(1));
                        phone.add(resultSet2.getString(2));
                        web.add(resultSet2.getString(3));
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                final JTable supplyTable = new JTable();
                final DefaultTableModel tableModel = new DefaultTableModel() {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                supplyTable.addMouseMotionListener(new MouseMotionAdapter() {

                    @Override
                    public void mouseMoved(MouseEvent e) {

                        Point point = e.getPoint();
                        int row = supplyTable.rowAtPoint(point);
                        int column = supplyTable.columnAtPoint(point);

                        if (column == 7) {

                            String tempName = (String) supplyTable.getValueAt(row, column);
                            int index = name.indexOf(tempName);
                            if (index > -1) {
                                supplyTable.setToolTipText("Телефон доставки: " + phone.get(index) + "   Сайт доставки: " + web.get(index));
                            } else {
                                supplyTable.setToolTipText(null);
                            }
                        }
                        else supplyTable.setToolTipText(null);
                    }
                });

                supplyTable.setModel(tableModel);
                tableModel.addColumn("Код товара");
                tableModel.addColumn("Название товара");
                tableModel.addColumn("Количество");
                tableModel.addColumn("Стоимость");
                tableModel.addColumn("Дата поставки");
                tableModel.addColumn("Статус поставки");
                tableModel.addColumn("Код доставки");
                tableModel.addColumn("Название доставки");
                tableModel.addColumn("Номер поставки");

                SQL = "select * from Delivery where shop_name = " + shopName;

                // Вывод
                try {

                    resultSet = DatabaseHandler.doSelect(connection, SQL);
                    if (!resultSet.next()) JOptionPane.showMessageDialog(null, "Поставок нет.", "Информация", JOptionPane.PLAIN_MESSAGE);
                    else {

                        resultSet.beforeFirst();
                        while (resultSet.next()) {

                            tableModel.addRow(new Object[] {resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
                                                            resultSet.getString(5), resultSet.getString(10), resultSet.getString(11), resultSet.getString(12),
                                                            resultSet.getString(13)});
                        }
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                // Подтвердить получение поставки
                final JButton confirmDelivery = new JButton(new AbstractAction("Подтвердить получение поставки") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        int row = supplyTable.getSelectedRow();

                        try {

                            resultSet.absolute(row + 1);
                            String statusCheck = (String) supplyTable.getValueAt(row, 5);
                            if (statusCheck.equals("Доставляется")) {

                                SQL = "update Delivery set delivery_status = 'Доставлено' where delivery_number = " + resultSet.getString(13);
                                supplyTable.setValueAt("Доставлено", row, 5);
                                DatabaseHandler.doUpdate(connection, SQL);
                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                Container container = showSupplyWindow.getContentPane();
                container.setLayout(new GridLayout(2, 1, 20, 40));
                container.add(new JScrollPane(supplyTable));
                container.add(confirmDelivery);

            }
        });

        // Просмотр товаров
        JButton showStuff = new JButton(new AbstractAction("Просмотр товаров") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame showStuffWindow = new JFrame("Просмотр товаров");
                showStuffWindow.setVisible(true);
                showStuffWindow.setBounds(700, 400, 800, 500);

                final JTable productTable = new JTable();
                final DefaultTableModel tableModel = new DefaultTableModel() {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                productTable.setModel(tableModel);
                tableModel.addColumn("Код товара");
                tableModel.addColumn("Название товара");
                tableModel.addColumn("Количество");
                tableModel.addColumn("Стоимость");


                SQL = "select * from Product";

                // Вывод
                try {
                    resultSet = DatabaseHandler.doSelect(connection, SQL);

                    while (resultSet.next()) {

                        tableModel.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)});

                    }

                    SwingUtilities.updateComponentTreeUI(showStuffWindow);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                Container container = showStuffWindow.getContentPane();
                container.setLayout(new GridLayout(1, 1, 5, 40));
                container.add(new JScrollPane(productTable));
            }
        });

        Connection connection;

        ShopInterface(Connection con) {

            super("Склад - работник магазина " + shopName);
            connection = con;
            this.setBounds(600, 400, 900, 450);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container container = this.getContentPane();
            container.setLayout(new GridLayout(2, 4, 25, 25));

            container.add(requestSupply);
            container.add(showSupply);
            container.add(showStuff);

        }
    }

}
