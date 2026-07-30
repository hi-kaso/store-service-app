import javax.swing.JPanel;

public abstract class OrderCardFactory {

    public abstract JPanel createCard(String rawData, int orderCount);

}