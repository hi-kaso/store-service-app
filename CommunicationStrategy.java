//strategyパターン使用
public interface CommunicationStrategy {
    //private Socket socket;
    // 接続開始
    void connect(String host, int port) throws Exception;
    // メッセージ送信
    void send (String message) throws Exception;
    // 切断
    void disconnect() throws Exception;

    // Strategy名（ログ出力用）
    String getName();
}
