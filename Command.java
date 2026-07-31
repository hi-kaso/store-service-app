import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

// ボタン操作のインターフェースと、注文送信処理（SendOrderCommand）をまとめたファイル

public interface Command {
    void execute();
}

class SendOrderCommand implements Command {
    private final DefaultTableModel cartModel;
    private final CommunicationStrategy strategy;

    public SendOrderCommand(DefaultTableModel cartModel, CommunicationStrategy strategy) {
        this.cartModel = cartModel;
        this.strategy = strategy;
    }

    @Override
    public void execute() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "カートが空です。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("ORDER_CART:");
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (i > 0) sb.append(",");
            String name = (String) cartModel.getValueAt(i, 0);
            Object qty = cartModel.getValueAt(i, 2);
            sb.append(name).append("x").append(qty);
        }

        try {
            strategy.send(sb.toString());
            JOptionPane.showMessageDialog(null, "注文を送信しました:\n" + sb.toString());
            cartModel.setRowCount(0); // 送信完了後にカートをクリア
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "送信に失敗しました: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }
}