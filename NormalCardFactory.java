import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NormalCardFactory extends OrderCardFactory {

    @Override
    public JPanel createCard(String rawData, int orderCount){
        
        String itemsText = rawData;

        if(itemsText.startsWith("ORDER_CART:")){
            itemsText = itemsText.substring("ORDER_CART:".length());
        }

        if(rawData.startsWith("DELETE:")){
            return null;
        }

        if(itemsText.startsWith("ADD:")){
            itemsText = itemsText.substring(4);
        }

        String[] items = itemsText.split(",");

        DefaultListModel<String> listModel = new DefaultListModel<>();

        for(String item : items){

            String trimmedItem = item.trim();

            if(!trimmedItem.isEmpty()){
                listModel.addElement(" • " + trimmedItem);
            }
        }

        if(listModel.isEmpty()){
            return null;
        }

        String timeStr =
            new SimpleDateFormat("HH:mm:ss")
            .format(new Date());

        JPanel card =
            new JPanel(new BorderLayout(5,5));

        card.setMaximumSize(
            new Dimension(Integer.MAX_VALUE,180)
        );

        final String originalTitle =
            "注文 #" + orderCount +
            " (" + timeStr + ")";

        javax.swing.border.TitledBorder border =
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                originalTitle
            );

        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5,5,5,5),
                border
            )
        );

        Color normalBg =
            new Color(240,248,255);

        card.setBackground(normalBg);

        JList<String> itemList =
            new JList<>(listModel);

        itemList.setFont(
            new Font("SansSerif",Font.PLAIN,14)
        );

        itemList.setBackground(normalBg);

        JScrollPane listScroll =
            new JScrollPane(itemList);

        listScroll.setBorder(null);

        card.add(listScroll,BorderLayout.CENTER);

        JPanel actionPanel =
            new JPanel(new FlowLayout(FlowLayout.RIGHT));

        actionPanel.setOpaque(false);

        JButton completeButton =
            new JButton("完了にする");

        completeButton.setFocusable(false);

        completeButton.addActionListener(e -> {

            boolean isCompleted =
                completeButton.getText().equals("元に戻す");

            if(!isCompleted){

                border.setTitle(
                    originalTitle + " [完了]"
                );

                card.setBackground(
                    new Color(220,220,220)
                );

                itemList.setBackground(
                    new Color(220,220,220)
                );

                completeButton.setText("元に戻す");

            }else{

                border.setTitle(originalTitle);

                card.setBackground(normalBg);

                itemList.setBackground(normalBg);

                completeButton.setText("完了にする");
            }

            card.repaint();
        });

        actionPanel.add(completeButton);

        card.add(actionPanel,BorderLayout.SOUTH);

        return card;
    }
}