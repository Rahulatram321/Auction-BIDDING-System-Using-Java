package RealTimeBIDDING;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class AuctionClientSwing {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JFrame frame;
    private JTextField bidField;
    private JTextArea chatArea;

    public AuctionClientSwing(String address, int port) {
        try {
            socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            setupUI();

            new Thread(new Listener()).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        frame = new JFrame("Auction Bidding Client");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        bidField = new JTextField();
        inputPanel.add(bidField, BorderLayout.CENTER);

        JButton bidButton = new JButton("Place Bid");
        inputPanel.add(bidButton, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeBid();
            }
        });

        frame.setVisible(true);
    }

    private void placeBid() {
        String bidText = bidField.getText();
        try {
            double bidAmount = Double.parseDouble(bidText);
            out.println(bidAmount);
            bidField.setText("");
        } catch (NumberFormatException e) {
            chatArea.append("Invalid bid amount. Please enter a valid number.\n");
        }
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            String messageFromServer;
            try {
                while ((messageFromServer = in.readLine()) != null) {
                    chatArea.append(messageFromServer + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuctionClientSwing("localhost", 12345));  // Connect to server running on localhost at port 12345
    }
}
