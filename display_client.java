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
 
    private static final String SERVER_IP = "10.130.0.50"; // ★サーバーの物理IPを指定
    private static final int PORT = 8080;
 
    private JPanel ordersContainer;
    private int orderCount = 0;
    private OrderCardFactory factory;
 
    public display_client() {
        setTitle("注文表示モニター (GUI)");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
 
        // 注文カードを縦に並べるパネル
        ordersContainer = new JPanel();
        ordersContainer.setLayout(new BoxLayout(ordersContainer, BoxLayout.Y_AXIS));
 
        // スクロールバー付きのパネルを設定
        JScrollPane scrollPane = new JScrollPane(ordersContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // スクロール速度を少し速く調整
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
            // 表示用クライアントであることを通知[cite: 2]
            writer.println("DISPLAY");
 
            String message;
            while ((message = reader.readLine()) != null) {
                final String rawData = message.trim();
                // 空行や接続時の応答メッセージ等はスキップ
                if (rawData.isEmpty() || rawData.equals("DISPLAY") || rawData.equals("ORDER")) {
                    continue;
                }
 
                // GUIスレッドで画面を更新
                SwingUtilities.invokeLater(() -> addOrderCard(rawData));
            }
 
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, "通信エラー: " + e.getMessage())
            );
        }
    }
 
    // 新しい注文カートカードを追加するGUIメソッド
    private void addOrderCard(String rawData) {

        JPanel card = factory.createCard(rawData, orderCount+1);


        if (card != null) {
            orderCount++;
            ordersContainer.add(card, 0);
            ordersContainer.revalidate();
            ordersContainer.repaint();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(display_client::new);
    }
}