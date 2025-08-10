package big.modules.screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputDialog {


    public static String getInputFromDialog() {
        final String[] result = new String[1];
        final JDialog dialog = new JDialog((Frame) null, "send message", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(null);

        final JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 20));


        JButton submitButton = new JButton("submit");


        ActionListener submitAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result[0] = textField.getText();
                dialog.dispose();
            }
        };

        submitButton.addActionListener(submitAction);


        textField.addActionListener(submitAction);


        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        inputPanel.add(new JLabel("Type in:"));
        inputPanel.add(textField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


        dialog.setVisible(true);

        return result[0] != null ? result[0] : "";
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            String userInput = getInputFromDialog();
            System.out.println("user input: " + userInput);
            

            JOptionPane.showMessageDialog(null, "you input: " + userInput);
        });
    }
}