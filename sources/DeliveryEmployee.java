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

Класс содержит описание пользователя "Работник службы доставки":

- интерфейс, состоящий из вложенного класса DeliveryInterface

- функции пользователя (кнопки)

 */


class DeliveryEmployee {

    public static String deliveryName;

    public DeliveryEmployee(Connection connection, String delivery) {
        deliveryName = "'" + delivery + "'";
        DeliveryInterface app = new DeliveryInterface(connection);
        app.setVisible(true);
    }

    private static class DeliveryInterface extends JFrame {

        ResultSet resultSet;
        String SQL;

        JButton showSupply = new JButton(new AbstractAction("Поставки") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame showSupplyWindow = new JFrame("Поставки");
                showSupplyWindow.setVisible(true);
                showSupplyWindow.setBounds(700, 400, 1000, 500);

                final Vector<String> name = new Vector<>();
                final Vector<String> phone = new Vector<>();

                String SQL2 = "select shop_name, phone from Shop";
                try {
                    ResultSet resultSet2 = DatabaseHandler.doSelect(connection, SQL2);

                    while (resultSet2.next()) {

                        name.add(resultSet2.getString(1));
                        phone.add(resultSet2.getString(2));
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                final JTable supplyTable = new JTable() {};

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

                        if (column == 5) {

                            String tempName = (String) supplyTable.getValueAt(row, column);
                            int index = name.indexOf(tempName);
                            supplyTable.setToolTipText("Телефон магазина: " + phone.get(index));

                        }
                        else supplyTable.setToolTipText(null);
                    }
                });

                supplyTable.setModel(tableModel);
                tableModel.addColumn("Код товара");
                tableModel.addColumn("Название товара");
                tableModel.addColumn("Количество");
                tableModel.addColumn("Дата поставки");
                tableModel.addColumn("Код магазина");
                tableModel.addColumn("Название магазина");
                tableModel.addColumn("Адрес магазина");
                tableModel.addColumn("Статус поставки");
                tableModel.addColumn("Номер поставки");

                SQL = "select top 10 * from Delivery where delivery_name = " + deliveryName;
                final int[] counter = {0};

                // Вывод первых 10 записей
                try {

                    resultSet = DatabaseHandler.doSelect(connection, SQL);
                    if (!resultSet.next()) JOptionPane.showMessageDialog(null, "Поставок нет.", "Информация", JOptionPane.PLAIN_MESSAGE);
                    else {

                        resultSet.beforeFirst();
                        while (resultSet.next()) {

                            tableModel.addRow(new Object[] { resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                                                             resultSet.getString(5), resultSet.getString(7), resultSet.getString(8),
                                                             resultSet.getString(9), resultSet.getString(10), resultSet.getString(13)});
                        }

                        SwingUtilities.updateComponentTreeUI(showSupplyWindow);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                final JButton takeDelivery = new JButton(new AbstractAction("Принять поставку в реализацию") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        int row = supplyTable.getSelectedRow();

                        try {

                            resultSet.absolute(row + 1);
                            SQL = "update Delivery set delivery_status = 'Доставляется' where delivery_number = " + resultSet.getString(13);
                            supplyTable.setValueAt("Доставляется", row, 7);
                            System.out.println(SQL);
                            DatabaseHandler.doUpdate(connection, SQL);

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                Container container = showSupplyWindow.getContentPane();
                container.setLayout(new GridLayout(3, 1, 5, 40));
                container.add(new JScrollPane(supplyTable));
                container.add(takeDelivery);
            }

        });

        Connection connection;

        DeliveryInterface(Connection con) {

            super("Склад - работник службы доставки " + deliveryName);
            connection = con;
            this.setBounds(600, 400, 900, 450);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container container = this.getContentPane();
            container.setLayout(new GridLayout(2, 4, 25, 25));
            container.add(showSupply);

        }
    }
}
