//gcc echo_server.c -o echo_server.exe -lws2_32 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib")
typedef int socklen_t;

/*使用するTCPポート番号*/
#define PORT 8080
/*ソケットを閉じるための共通マクロ*/
#define CLOSE_SOCKET(s) closesocket(s)

int main(void)
{
    /*サーバー用ソケットのファイルディスクリプタ*/
    int server_fd;
    int client_fd;//クライアント用

    /*サーバーのIPアドレスとポートを格納する構造体*/
    struct sockaddr_in server_addr;
    struct sockaddr_in client_addr;//クライアント用

    /*Winsockライブラリの初期化。これはWindowsのみで必要*/
    WSADATA wsa_data;
    if(WSAStartup(MAKEWORD(2,2), &wsa_data) != 0 ){
        fprintf(stderr, "WSAStartupに失敗しました\n");
        return 1;
    }

    /*手順１：ソケットの作成
    socket()は通信の「入口」をカーネルに要求する関数
    引数：
        AF_INET：IPv4を使用することを指定する引数
        SOCK_STREAM：TCPを指定
        0：プロトコルを自動選択（TCPの場合はIPPROTO_TCP）
    
    戻り値：
        成功時：ソケットのファイルディスクリプタ(0以上の整数)
        失敗時：-1
    */
    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0){
        perror("socketの作成に失敗しました");
        WSACleanup();
        return 1;
    }
    printf("ソケットを作成しました。(fd = %d)\n", server_fd);

    /*---オプション：SO_REUSEADDR---
    サーバー再起動時に「Address already in use」エラーを防ぐため、
    直前に使用していたポートをすぐ再利用できるようにします。
    */
    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, (const char *)&opt, sizeof(opt));

    /*手順２：アドレス情報の設定とバインド*/

    /*sockaddr_in構造体を0で初期化（ゴミデータを防ぐため）*/
    memset(&server_addr, 0, sizeof(server_addr));

    //ファミリー：IPv4
    server_addr.sin_family = AF_INET;

    //IPアドレス：INADDR_ANY(0.0.0.0)
    server_addr.sin_addr.s_addr = INADDR_ANY;

    //ポート番号：8080
    //htons() = Host TO Network Short
    //ネットワークバイトオーダー(ビックエンディアン)に変換する必要がある
    server_addr.sin_port = htons(PORT);

    /*bind()の引数
    server_fd：バインド対象のソケット
    (struct sockaddr *)&sever_addr：アドレス情報へのポインタ
    sizeof(server_addr)：アドレス構造体のサイズ
    
    戻り値：成功 0 / 失敗 -1
    */

    if(bind(server_fd, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0 ){
        perror("bindに失敗しました");
        CLOSE_SOCKET(server_fd);
        WSACleanup();
        return 1;
    }
    printf("ポート %d にバインドしました\n", PORT);

    /*手順３：接続待ち状態にする(listen)
    listen() はソケットを「パッシブ（待ち受け）モード」に切り替えます。
    これにより、クライアント（子機）からの接続要求 (connect) を
    受け付ける準備が整います。
    引数：
        server_fd：待ち受けに使うソケット
        5：バッグログ（同時に処理待ちできる接続要求の最大数）
        
    戻り値：成功 0 / 失敗 -1
    */
    if(listen(server_fd, 5) < 0){
        perror("lisetenに失敗しました");
        CLOSE_SOCKET(server_fd);
        WSACleanup();
        return 1;
    }
    printf("ポート %d で接続待ち...\n", PORT);

    //ココまでで「待ち受け準備」は完了
    //次の段階：accept()　→　recv()　→　send()のループでエコー処理

    while (1){
        //accept：子機の接続を待つ
        socklen_t client_addr_len = sizeof(client_addr);

        client_fd = accept(server_fd, (struct sockaddr *)&client_addr, &client_addr_len);
        if(client_fd < 0 ){
            perror("accept");
            continue;
        }
        printf("クライアント接続: %s:%d\n", inet_ntoa(client_addr.sin_addr),ntohs(client_addr.sin_port));

        while(1){
            char buffer[1024];
            int bytes_received;

            memset(buffer, 0, sizeof(buffer));//ゴミデータ掃除

            //recv：子機から受信
            bytes_received = recv(client_fd, buffer, sizeof(buffer) - 1, 0 );
            
            if(bytes_received < 0 ){
                perror("recv");
                break;
            }

            if(bytes_received == 0){
                printf("クライアントが切断しました\n");
                break;
            }
            
            buffer[bytes_received] = '\0';
            printf("受信:%s\n", buffer);

            //send：そのまま返す
            if(send(client_fd, buffer, bytes_received, 0) < 0){
                perror("send");
                break;
            }
            printf("送信： %s\n", buffer);
        }

        CLOSE_SOCKET(client_fd);
        printf("接続を閉じました。次の接続を待ちます...\n");
    }

    //通常while(1)なので来ることはない
    CLOSE_SOCKET(server_fd);
    WSACleanup();
    return 0;

}



