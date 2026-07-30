import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VerticalCardFactory extends OrderCardFactory {

    @Override
    public JPanel createCard(String rawData, int orderCount) {

        if (rawData.startsWith("DELETE:")) {
            return null;
        }

        String itemsText = rawData;

        if (itemsText.startsWith("ORDER_CART:")) {
            itemsText = itemsText.substring("ORDER_CART:".length());
        }
        else if (itemsText.startsWith("ADD:")) {
            itemsText = itemsText.substring(4);
        }

        String[] items = itemsText.split(",");

        DefaultListModel<String> listModel = new DefaultListModel<>();

        for (String item : items) {
            String trimmedItem = item.trim();

            if (!trimmedItem.isEmpty()) {
                listModel.addElement("• " + trimmedItem);
            }
        }

        if (listModel.isEmpty()) {
            return null;
        }

        String timeStr = new SimpleDateFormat("HH:mm:ss").format(new Date());

        // -------------------------
        // カード作成（縦長）
        // -------------------------
        JPanel card = new JPanel(new BorderLayout(5,5));

        // 縦長サイズ
        card.setPreferredSize(new Dimension(180, 300));
        card.setMaximumSize(new Dimension(180, 300));

        final String originalTitle =
                "注文 #" + orderCount + "\n" + timeStr;

        javax.swing.border.TitledBorder border =
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(),
                        originalTitle);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5,5,5,5),
                border));

        Color normalBg = new Color(240,248,255);
        card.setBackground(normalBg);

        // 商品一覧
        JList<String> itemList = new JList<>(listModel);
        itemList.setFont(new Font("SansSerif", Font.PLAIN, 16));
        itemList.setBackground(normalBg);

        JScrollPane scroll = new JScrollPane(itemList);
        scroll.setBorder(null);

        card.add(scroll, BorderLayout.CENTER);

        // 完了ボタン
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);

        JButton completeButton = new JButton("完了");

        completeButton.addActionListener(e -> {

            boolean done = completeButton.getText().equals("戻す");

            if (!done) {

                border.setTitle(originalTitle + "【完了】");

                card.setBackground(new Color(220,220,220));
                itemList.setBackground(new Color(220,220,220));

                completeButton.setText("戻す");

            } else {

                border.setTitle(originalTitle);

                card.setBackground(normalBg);
                itemList.setBackground(normalBg);

                completeButton.setText("完了");
            }

            card.repaint();
        });

        bottom.add(completeButton);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }
}