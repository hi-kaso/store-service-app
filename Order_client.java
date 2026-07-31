import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class display_client extends JFrame {

    private static final String SERVER_IP = "172.17.208.1"; // サーバーIP
    private static final int PORT = 8080;

    private JPanel ordersContainer;
    private int orderCount = 0;
    private OrderCardFactory factory = new NormalCardFactory();

    public display_client() {
        setTitle("注文表示モニター (GUI)");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ordersContainer = new JPanel();
        ordersContainer.setLayout(new BoxLayout(ordersContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(ordersContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);

        // 別スレッドでネットワーク受信を待機
        new Thread(this::startNetworkClient).start();
    }

    private void startNetworkClient() {
        try (
            Socket socket = new Socket(SERVER_IP, PORT);
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            writer.println("DISPLAY");

            String message;
            while ((message = reader.readLine()) != null) {
                final String rawData = message.trim();

                if (rawData.equals("RESET_ORDER_NUMBER")) {
                    SwingUtilities.invokeLater(() -> {
                        orderCount = 0;
                        JOptionPane.showMessageDialog(this, "注文番号をリセットしました");
                    });
                    continue;
                }

                SwingUtilities.invokeLater(() -> addOrderCard(rawData));
            }

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, "通信エラー: " + e.getMessage())
            );
        }
    }

    private void addOrderCard(String rawData) {
        JPanel card = factory.createCard(rawData, orderCount + 1);

        if (card != null) {
            orderCount++;
            ordersContainer.add(card);
            ordersContainer.revalidate();
            ordersContainer.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(display_client::new);
    }
}