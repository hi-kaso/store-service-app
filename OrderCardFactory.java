import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderCardFactory {
    public JPanel createCard(String rawData, int orderCount){
        
        if (rawData.startsWith("DELETE:")) {
            return null;
        }

        String itemsText = rawData;

        if (itemsText.startsWith("ADD:")) {
            itemsText = itemsText.substring(4);
        }

        // カンマ区切りで商品を取り出し
        String[] items = itemsText.split(",");

        DefaultListModel<String> listModel = new DefaultListModel<>();

        for (String item : items) {
            String trimmedItem = item.trim();
            // 空文字でない場合のみ追加（「・」だけのカート防止）
            if (!trimmedItem.isEmpty()) {
                listModel.addElement(" • " + trimmedItem);
            }
        }

        // 表示する商品が1つもない場合はカードを作成しない
        if (listModel.isEmpty()) {
            return null;
        }

        String timeStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
    
        // --- カード（パネル）の作成 ---
        JPanel card = new JPanel(new BorderLayout(5, 5));
        // カード全体の最大サイズを設定（BoxLayoutでのレイアウト崩れ防止）
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        final String originalTitle = "注文 #" + orderCount + " (" + timeStr + ")";
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), originalTitle);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                border
        ));
        // 通常時の背景色（ライトブルー系）
        Color normalBg = new Color(240, 248, 255);
        card.setBackground(normalBg);

        // 商品リスト表示
        JList<String> itemList = new JList<>(listModel);
        itemList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        itemList.setBackground(normalBg);
        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setBorder(null);
        card.add(listScroll, BorderLayout.CENTER);

        // --- 完了ボタン・ステータスエリア ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        JButton completeButton = new JButton("完了にする");
        completeButton.setFocusable(false);
        // 完了ボタンが押された時の処理
        completeButton.addActionListener(e -> {
            boolean isCompleted = completeButton.getText().equals("元に戻す");

            if (!isCompleted) {
                // 完了状態へ変更
                border.setTitle(originalTitle + " [完了]");
                card.setBackground(new Color(220, 220, 220)); // 灰色に変更
                itemList.setBackground(new Color(220, 220, 220));
                completeButton.setText("元に戻す");
            } else {
                // 未完了状態へ戻す
                border.setTitle(originalTitle);
                card.setBackground(normalBg);
                itemList.setBackground(normalBg);
                completeButton.setText("完了にする");
            }
            card.repaint();
        });

        actionPanel.add(completeButton);
        card.add(actionPanel, BorderLayout.SOUTH);

        return card;
    }
}