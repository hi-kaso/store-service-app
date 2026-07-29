import java.io.*;//add
import java.net.Socket;//add

public class TcpCommunicationStrategy implements CommunicationStrategy {

    private Socket socket;//add
    private BufferedReader reader;//add
    private BufferedWriter writer;//add

    @Override
    public void connect(String host, int port) throws Exception {
        System.out.println("[strategy]を使用しています...");
        System.out.println("[strategy]:"+ host + ":"+port);

        socket = new Socket(host, port);

        InputStream in = socket.getInputStream();

        InputStreamReader isr = new InputStreamReader(in, "UTF-8");
        reader = new BufferedReader(isr);

        OutputStream ou = socket.getOutputStream();

        OutputStreamWriter osw = new OutputStreamWriter(ou, "UTF-8");
        writer = new BufferedWriter(osw);

        // 自分は注文受付用であることを通知
        writer.write("ORDER");
        writer.newLine();
        writer.flush();
    }

    @Override
    public void send(String message) throws Exception {
        writer.write(message);
        writer.newLine();     // 改行を送る
        writer.flush();       // 送信確定
    }

    @Override
    public void disconnect() throws Exception {
        if (writer != null) {
                writer.close();
        }
        if (reader != null) {
                reader.close();
        }
        if (socket != null) {
                socket.close();
        }
        System.out.println("[disconnect] 接続を終了しました。");
}
    @Override
    public String getName() {
        return "TCP";
    }
}
