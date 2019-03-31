package ui;

import atm.AtmMachine;
import atm.BankManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class RestockMenu extends SubMenu {
    private final AtmMachine machine;
    private JTextField stockInfo;
    private JComboBox<Integer> cashTypeSelection, amountSelection;
    private JButton restockButton;

    RestockMenu(BankManager manager) {
        super("Restock Menu");

        machine = manager.getMachineList().get(0);

        cashTypeSelection = new JComboBox<>(machine.getValidCashTypes().toArray(new Integer[0]));
        amountSelection = new JComboBox<>(new Integer[]{1, 5, 10, 50, 100, 200, 500});

        stockInfo = new JTextField(20);
        stockInfo.setEditable(false);
        stockInfo.setFont(new Font("Serif", Font.PLAIN, 18));
        stockInfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        restockButton = new JButton("Restock");
        restockButton.addActionListener(e -> {
            Integer cashType = (Integer) cashTypeSelection.getSelectedItem();
            Integer amount = (Integer) amountSelection.getSelectedItem();

            if (cashType != null && amount != null) {
                TreeMap<Integer, Integer> restockInfo = new TreeMap<>();

                restockInfo.put(cashType, amount);

                manager.restockMachine(restockInfo);

                MainFrame.showInfoMessage("Restock successful!", "Success");
            } else {
                MainFrame.showErrorMessage("Cash type or amount not selected!");
            }

            updateStockInfo();
        });

        updateStockInfo();

        defaultRowsLayout(new LinkedHashMap<JComponent, String>() {{
            put(stockInfo, null);
            put(cashTypeSelection, "Select cash type: ");
            put(amountSelection, "Select restock amount: ");
            put(restockButton, null);
        }});

        setVisible(true);
    }

    private void updateStockInfo() {
        cashTypeSelection.removeAllItems();
        StringBuilder stockText = new StringBuilder();

        SortedMap<Integer, Integer> stock = machine.getStock();

        for (int cashType : stock.keySet()) {
            cashTypeSelection.addItem(cashType);

            stockText.append(String.format("$%d-%d ", cashType, stock.get(cashType)));
        }

        stockInfo.setText(stockText.toString());
    }
}
