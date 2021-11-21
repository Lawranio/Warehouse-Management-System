package com.company;


import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


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
                    System.out.println(name);

                    try {

                        if (!DatabaseHandler.searchName(connection, name)) nameCheck = true;
                        else JOptionPane.showMessageDialog(null, "Товар с таким названием уже существует", "Ошибка", JOptionPane.WARNING_MESSAGE);

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
                final DefaultTableModel tableModel = new DefaultTableModel(tableData, tableHeader) {

                    // Если таблица была изменена, то выполняется этот метод
                    @Override
                    public void fireTableCellUpdated(int row, int column) {
                        //super.fireTableCellUpdated(row, column);
                        SQL = "update Product set ";

                        try {
                            resultSet.first();

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

                                    JOptionPane.showMessageDialog(null, "Товар с таким названием уже существует", "Ошибка", JOptionPane.WARNING_MESSAGE);
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

                    // Запрет на измение первого столбца - кода товара
                    @Override
                    public boolean isCellEditable(int row, int column) {

                        if (column == 0) return false;
                        return super.isCellEditable(row, column);
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
                        }

                        if (stuff_name.isSelected()) {

                            SQL = SQL + "product_name = " + "'" + inputField.getText() + "'";
                        }

                        if (!stuff_name.isSelected() & !stuff_id.isSelected()) {

                            JOptionPane.showMessageDialog(null, "Не выбран критерий поиска", "Информация", JOptionPane.WARNING_MESSAGE);
                        }

                        inputField.setText("");

                        try {

                            System.out.println(SQL);
                            resultSet = DatabaseHandler.doSelect(connection, SQL);

                            if (resultSet.next()) {

                                tableData.clear();
                                tableRowInfo.add(resultSet.getString(1));
                                tableRowInfo.add(resultSet.getString(2));
                                tableRowInfo.add(resultSet.getString(3));
                                tableRowInfo.add(resultSet.getString(4));
                                tableData.add(tableRowInfo);
                                SwingUtilities.updateComponentTreeUI(editStuffWindow);

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
        JButton createSupply = new JButton(new AbstractAction("Сформировать поставку") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame createSupplyWindow = new JFrame("Формирование поставки");
                createSupplyWindow.setVisible(true);
                createSupplyWindow.setBounds(500, 400, 1300, 450);

                final JTable supplyTable = new JTable();
                final DefaultTableModel tableModel = new DefaultTableModel() {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                supplyTable.setModel(tableModel);
                tableModel.addColumn("Код товара");
                tableModel.addColumn("Название товара");
                tableModel.addColumn("Количество");
                tableModel.addColumn("Стоимость");
                tableModel.addColumn("Дата поставки");
                tableModel.addColumn("Дата заказа");
                tableModel.addColumn("Код магазина");
                tableModel.addColumn("Название магазина");
                tableModel.addColumn("Адрес магазина");
                tableModel.addColumn("Статус поставки");
                tableModel.addColumn("Номер поставки");

                SQL = "select top 10 * from Delivery where delivery_id is null";
                final int[] counter = {0}; //TODO: Вывод следующих записей

                // Вывод первых 10 записей
                try {
                    resultSet = DatabaseHandler.doSelect(connection, SQL);
                    if (!resultSet.next()) JOptionPane.showMessageDialog(null, "Запросов нет.", "Информация", JOptionPane.PLAIN_MESSAGE);
                    else {

                        resultSet.beforeFirst();
                        while (resultSet.next()) {

                            tableModel.addRow(new Object[] {resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                                                            resultSet.getString(4), resultSet.getString(5), resultSet.getString(6),
                                                            resultSet.getString(7), resultSet.getString(8), resultSet.getString(9),
                                                            resultSet.getString(10), resultSet.getString(13)});
                        }

                        SwingUtilities.updateComponentTreeUI(createSupplyWindow);
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                // Собрать поставку
                final JButton collectOrder = new JButton(new AbstractAction("Собрать заказ") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        int row = supplyTable.getSelectedRow();

                        try {

                            resultSet.absolute(row + 1);
                            SQL = "update Delivery set delivery_status = 'Собирается' where delivery_number = " + resultSet.getString(13);
                            supplyTable.setValueAt("Собирается", row, 9);
                            DatabaseHandler.doUpdate(connection, SQL);
                            textActionLog.append("Статус поставки №" + resultSet.getString(13) + " поменялся на 'Собирается'\n");

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                // Назначить службу доставки
                final JButton appointDelivery = new JButton(new AbstractAction("Назначить службу доставки") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        SQL = "select * from DeliveryService";
                        Vector<Integer> deliveryAmounts = new Vector<>();
                        int minElement;     // Значение минимального элемента в векторе deliveryAmounts
                        int minRow;         // Строка с минимальным значением delivery_amount в таблице DeliveryService
                        String deliveryID;
                        String deliveryName;
                        String deliveryNumber;
                        int row = supplyTable.getSelectedRow();

                        try {

                            resultSet = DatabaseHandler.doSelect(connection, SQL);
                            while (resultSet.next()) {

                                deliveryAmounts.add(resultSet.getInt(5));

                            }
                            minElement = Collections.min(deliveryAmounts);
                            minRow = (deliveryAmounts.indexOf(minElement) + 1);

                            resultSet.absolute(minRow); // установка на одну из доставок с минимальным количеством поставок
                            deliveryID = resultSet.getString(1);
                            deliveryName = "'" + resultSet.getString(2) + "'";
                            deliveryNumber = (String) supplyTable.getValueAt(row, 10);

                            SQL = "update Delivery set delivery_status = 'Ожидает', " +
                                    "delivery_id = " + deliveryID +
                                    ", delivery_name = " + deliveryName + " where delivery_number = " + deliveryNumber;
                            DatabaseHandler.doUpdate(connection, SQL);
                            tableModel.removeRow(supplyTable.getSelectedRow());

                            textActionLog.append("Назначена служба доставки поставке №" + deliveryNumber + "\n");

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                Container container = createSupplyWindow.getContentPane();
                container.setLayout(new GridLayout(3, 1, 5, 40));
                container.add(new JScrollPane(supplyTable));
                container.add(collectOrder);
                container.add(appointDelivery);

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


                SQL = "select top 2 * from Product";
                final int[] counter = {0};

                // Вывод первых 10 записей
                try {
                    resultSet = DatabaseHandler.doSelect(connection, SQL);

                    while (resultSet.next()) {

                        tableModel.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)});

                    }

                    SwingUtilities.updateComponentTreeUI(showStuffWindow);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                final JButton show = new JButton(new AbstractAction("Показать следующие 2 товара") {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        tableModel.setRowCount(0);

                        counter[0] += 2;
                        SQL = "select * from Product order by product_id offset " + counter[0] + " rows fetch next 2 rows only";

                        try {
                            resultSet = DatabaseHandler.doSelect(connection, SQL);

                            while (resultSet.next()) {

                                tableModel.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)});

                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                Container container = showStuffWindow.getContentPane();
                container.setLayout(new GridLayout(2, 1, 5, 40));
                container.add(show);
                container.add(new JScrollPane(productTable));
            }
        });

        // Просмотр магазинов
        JButton showShop = new JButton(new AbstractAction("Просмотр магазинов") {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFrame showShopWindow = new JFrame("Просмотр магазинов");
                showShopWindow.setVisible(true);
                showShopWindow.setBounds(700, 400, 800, 500);

                final JTable shopTable = new JTable();
                final DefaultTableModel tableModel = new DefaultTableModel() {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                shopTable.setModel(tableModel);
                tableModel.addColumn("Идентификатор");
                tableModel.addColumn("Название магазина");
                tableModel.addColumn("Адрес");
                tableModel.addColumn("Телефон");

                SQL = "select top 2 * from Shop";
                final int[] counter = {0};

                // Вывод первых 10 записей
                try {
                    resultSet = DatabaseHandler.doSelect(connection, SQL);

                    while (resultSet.next()) {

                        tableModel.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)});

                    }

                    SwingUtilities.updateComponentTreeUI(showShopWindow);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                final JButton show = new JButton(new AbstractAction("Показать следующие 2 магазина") {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        tableModel.setRowCount(0);

                        counter[0] += 2;
                        SQL = "select * from Shop order by shop_id offset " + counter[0] + " rows fetch next 2 rows only";

                        try {
                            resultSet = DatabaseHandler.doSelect(connection, SQL);

                            while (resultSet.next()) {

                                tableModel.addRow(new Object[]{resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)});

                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                Container container = showShopWindow.getContentPane();
                container.setLayout(new GridLayout(2, 1, 5, 40));
                container.add(show);
                container.add(new JScrollPane(shopTable));
            }
        });

        // Просмотр поставок
        JButton showSupply = new JButton("Просмотр поставок");

        JTextArea textActionLog = new JTextArea();

        Connection connection;

        WarehouseInterface(Connection con) {

            super("Склад - работник склада");
            connection = con;
            this.setBounds(600, 400, 900, 450);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container container = this.getContentPane();
            container.setLayout(new GridLayout(2, 4, 25, 25));

            textActionLog.setEditable(false);

            container.add(newStuffButton);
            container.add(editStuffButton);
            container.add(createSupply);
            container.add(textActionLog);
            container.add(showStuff);
            container.add(showShop);
            container.add(showSupply);
            container.add(createSupply);

        }
    }
}



