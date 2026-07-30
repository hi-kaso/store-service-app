import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class Order_client extends JFrame {

    private CommunicationStrategy strategy;

    // メニューと単価の定義
    private final Map<String, Integer> menuPrices = new HashMap<>() {{
        put("ハンバーガー", 300);
        put("チーズバーガー", 380);
        put("ポテト(M)", 250);
        put("ドリンク(M)", 200);
    }};

    private DefaultTableModel tableModel;
    private JLabel totalAmountLabel;

    public Order_client() {
        setTitle("注文入力システム (GUI)");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        strategy = new TcpCommunicationStrategy();

        // 画面レイアウト構築
        initUI();

        // サーバー接続 (IPアドレスは環境に合わせて書き換えてください)
        try {
            strategy.connect("127.0.0.1", 8080);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "サーバー接続エラー: " + e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // --- メニュー選択パネル（上部） ---
        JPanel menuPanel = new JPanel(new FlowLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("メニュー追加"));

        JComboBox<String> menuComboBox = new JComboBox<>(menuPrices.keySet().toArray(new String[0]));
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JButton addButton = new JButton("カートに追加");

        menuPanel.add(new JLabel("商品:"));
        menuPanel.add(menuComboBox);
        menuPanel.add(new JLabel("数量:"));
        menuPanel.add(qtySpinner);
        menuPanel.add(addButton);

        add(menuPanel, BorderLayout.NORTH);

        // --- カート（テーブル）（中央） ---
        String[] columnNames = {"商品名", "単価", "数量", "小計"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // テーブルの直接編集は不可
            }
        };
        JTable cartTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 操作・合計金額・送信パネル（下部） ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel calcPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalAmountLabel = new JLabel("合計金額: 0 円");
        totalAmountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        calcPanel.add(totalAmountLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("選択した商品を削除");
        JButton clearButton = new JButton("カートを空にする");
        JButton sendButton = new JButton("注文を送信");
        JButton resetButton = new JButton("注文番号をリセット");
        sendButton.setBackground(new Color(60, 179, 113));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnPanel.add(removeButton);
        btnPanel.add(clearButton);
        btnPanel.add(sendButton);
        btnPanel.add(resetButton);

        bottomPanel.add(calcPanel, BorderLayout.NORTH);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- イベント処理 ---
        // 追加ボタン
        addButton.addActionListener((ActionEvent e) -> {
            String selectedItem = (String) menuComboBox.getSelectedItem();
            int qty = (Integer) qtySpinner.getValue();
            int price = menuPrices.get(selectedItem);

            // 既にカートにある場合は個数を更新
            boolean exists = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(selectedItem)) {
                    int currentQty = (Integer) tableModel.getValueAt(i, 2);
                    int newQty = currentQty + qty;
                    tableModel.setValueAt(newQty, i, 2);
                    tableModel.setValueAt(newQty * price, i, 3);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                tableModel.addRow(new Object[]{selectedItem, price, qty, price * qty});
            }
            updateTotal();
        });

        // 削除ボタン
        removeButton.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
                updateTotal();
            }
        });

        // 全クリアボタン
        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            updateTotal();
        });

        // 注文送信ボタン
        sendButton.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "カートが空です。");
                return;
            }

            try {
                // カート内の全商品を「ADD:商品名x個数」形式の文字列にして送信
                StringBuilder orderMsg = new StringBuilder("ORDER_CART:");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String name = (String) tableModel.getValueAt(i, 0);
                    int qty = (Integer) tableModel.getValueAt(i, 2);
                    orderMsg.append(name).append("x").append(qty).append(",");
                }
                // 末尾のカンマを削除
                if (orderMsg.charAt(orderMsg.length() - 1) == ',') {
                    orderMsg.deleteCharAt(orderMsg.length() - 1);
                }

                strategy.send(orderMsg.toString());

                JOptionPane.showMessageDialog(this, "注文を送信しました！");
                tableModel.setRowCount(0);
                updateTotal();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "送信エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        });

        resetButton.addActionListener(e -> {

            try {

                strategy.send("RESET_ORDER_NUMBER");

                JOptionPane.showMessageDialog(
                    this,
                    "次回注文から番号をリセットします"
                );

            } catch(Exception ex){

                JOptionPane.showMessageDialog(
                    this,
                    "送信エラー"
                );
            }

        });

        // ウインドウ閉じ時の切断処理
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    strategy.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 合計金額の更新処理
    private void updateTotal() {
        int total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (Integer) tableModel.getValueAt(i, 3);
        }
        totalAmountLabel.setText("合計金額: " + total + " 円");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Order_client().setVisible(true);
        });
    }
}