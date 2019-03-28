package ui;

import atm.BankManager;
import atm.User;
import atm.UserNotExistException;
import atm.WrongPasswordException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginValidation extends JDialog {
    private JButton loginButton;
    private JTextField usernameField, passwordField;
    private Container container;

    LoginValidation(JDialog parent, LoginType type, BankManager manager, boolean onlyValidate) {
        super(MainFrame.mainFrame, "Login as " + type.toString(), true);

        usernameField = new JTextField(10);
        passwordField = new JTextField(10);
        loginButton = new JButton("login");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText(), password = passwordField.getText();
            switch (type) {
                case USER:
                    User user = manager.validateUserLogin(username, password);
                    if (user != null) {
                        MainFrame.showMessage("Login success!");
                        this.dispose();
                        if (parent != null) {
                            parent.dispose();
                        }

                        if (!onlyValidate) {
                            new UserMainMenu();
                        }

                    }
                    break;

                case MANAGER:
                    try {
                        manager.login(username, password);
                        this.dispose();

                        if (parent != null) {
                            parent.dispose();
                        }

                        if (!onlyValidate) {
                            new ManagerMainMenu();
                        }
                    } catch (WrongPasswordException | UserNotExistException ex) {
                        MainFrame.showMessage(ex.getMessage());
                    }
                    break;
            }
        });

        container = getContentPane();

        initializeLayout();

        setBounds(10, 10, 350, 250);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(LoginValidation.this,
                        "Are you sure to cancel this login?", "Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    if (onlyValidate) {
                        System.exit(0);
                    } else {
                        LoginValidation.this.dispose();
                    }
                }
            }
        });
        setMinimumSize(new Dimension(350, 200));
        setVisible(true);
    }

    private void initializeLayout() {
        JPanel usernamePanel = new JPanel();
        usernamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        usernamePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        usernamePanel.add(new JLabel("username: "));
        usernamePanel.add(usernameField);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        passwordPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.add(new JLabel("password: "));
        passwordPanel.add(passwordField);

        JPanel infoPanel = new JPanel();
        usernamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoPanel.setLayout(new GridLayout(1, 2));
        infoPanel.add(usernamePanel);
        infoPanel.add(passwordPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(loginButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));
        mainPanel.add(infoPanel);
        mainPanel.add(buttonPanel);

        container.add(mainPanel);
    }

}
