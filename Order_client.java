import java.util.Scanner;
 
public class Order_client {
    public static void main(String[] args) {
        CommunicationStrategy strategy = new TcpCommunicationStrategy();
        Scanner scanner = new Scanner(System.in);
 
        try {
            // アプリ起動時に1回だけ接続
            strategy.connect("127.0.0.1", 8080);
 
            System.out.println("=== 注文入力システム ===");
            System.out.println("注文データ（例: ORDER:101:ハンバーガー）を入力してください。");
            System.out.println("'exit' と入力すると終了します。");
 
            while (true) {
                System.out.print("注文入力 > ");
                String input = scanner.nextLine();
 
                // 'exit' と入力されたら繰り返しを抜ける
                if ("exit".equalsIgnoreCase(input.trim())) {
                    break;
                }
 
                if (input.trim().isEmpty()) {
                    continue;
                }
 
                // 接続を維持したまま何度でも送信！
                try {
                    strategy.send(input);
                } catch (Exception e) {
                    System.err.println("送信エラーが発生しました: " + e.getMessage());
                    // 必要に応じてここで再接続処理（strategy.connect）を呼び出す
                }
            }
 
        } catch (Exception e) {
            System.err.println("通信エラー: " + e.getMessage());
        } finally {
            // アプリ終了時に接続を切断する
            try {
                strategy.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            scanner.close();
            System.out.println("アプリを終了しました。");
        }
    }
}