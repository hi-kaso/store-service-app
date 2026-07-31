import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpCommunication implements CommunicationStrategy {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    @Override
    public void connect(String host, int port) throws Exception {
        System.out.println("[TcpCommunication] 接続開始: " + host + ":" + port);

        socket = new Socket(host, port);

        // C言語サーバー(server.c)と文字化けなく通信するため UTF-8 を指定
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        // ★ サーバー側(server.c)が「注文受付用端末」と認識するために "ORDER" を通知
        writer.write("ORDER");
        writer.newLine();
        writer.flush();
    }

    @Override
    public void send(String message) throws Exception {
        if (writer == null) {
            throw new IllegalStateException("未接続です。先にconnectを実行してください。");
        }
        writer.write(message);
        writer.newLine(); // C言語サーバーの改行（\n）区切り用
        writer.flush();   // 即座にパケットを送出
    }

    @Override
    public void disconnect() throws Exception {
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("[TcpCommunication] 接続を終了しました。");
    }

    @Override
    public String getName() {
        return "TCP/IP Socket";
    }
}