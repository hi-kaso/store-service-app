//javac display_client.java
//java display_client
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class display_client {

    // 親機(Cサーバー)のIPアドレス
    private static final String SERVER_IP = "127.0.0.1";

    // ポート番号
    private static final int PORT = 8080;

    public static void main(String[] args) {

        // Strategyパターン
        DisplayStrategy strategy = new FancyDisplay();

        try (
                Socket socket = new Socket(SERVER_IP, PORT);

                PrintWriter writer =
                        new PrintWriter(socket.getOutputStream(), true);

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream(),
                                "UTF-8"));
        ) {

            // 自分は注文表示用であることを親機へ通知
            writer.println("DISPLAY");

            System.out.println("親機へ接続しました");
            System.out.println("注文待機中...");

            String order;

            while ((order = reader.readLine()) != null) {

                strategy.display(order);

            }

            System.out.println("親機との接続が切断されました");

        } catch (IOException e) {

            System.out.println("通信エラー");
            e.printStackTrace();

        }

    }

}

/*=============================
  Strategyパターン
=============================*/

// 表示方法のインターフェース
interface DisplayStrategy {

    void display(String order);

}

// 表示方法の実装
class FancyDisplay implements DisplayStrategy {

    @Override
    public void display(String order) {

        System.out.println();
        System.out.println("====================");
        System.out.println("     新しい注文");
        System.out.println("--------------------");
        System.out.println("注文内容：" + order);
        System.out.println("====================");

    }

}