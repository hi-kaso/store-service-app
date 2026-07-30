// gcc server.c -o server.exe -lws2_32

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>

#pragma comment(lib,"ws2_32.lib")

typedef int socklen_t;

#define PORT 8080
#define CLOSE_SOCKET(s) closesocket(s)
#define MAX_MENU 100

typedef struct{
    char name[100];
    int count;
} MenuCount;

void updateOrderCount(char *data);
void saveCSV(void);
void showHistory(void);

MenuCount menuList[MAX_MENU];
int menuSize = 0;

int main(void)
{
    WSADATA wsa;
    SOCKET server_fd;
    SOCKET client_fd;
    SOCKET order_fd = INVALID_SOCKET;
    SOCKET display_fd = INVALID_SOCKET;

    struct sockaddr_in server_addr;
    struct sockaddr_in client_addr;

    if(WSAStartup(MAKEWORD(2,2), &wsa) != 0){
        printf("WSAStartup失敗\n");
        return 1;
    }

    server_fd = socket(AF_INET, SOCK_STREAM, 0);

    if(server_fd == INVALID_SOCKET){
        printf("socket失敗\n");
        WSACleanup();
        return 1;
    }

    int opt = 1;
    setsockopt(server_fd,
               SOL_SOCKET,
               SO_REUSEADDR,
               (const char*)&opt,
               sizeof(opt));

    memset(&server_addr,0,sizeof(server_addr));

    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.s_addr = INADDR_ANY;

    if(bind(server_fd,
            (struct sockaddr*)&server_addr,
            sizeof(server_addr)) < 0){

        perror("bind");
        return 1;
    }

    if(listen(server_fd,5) < 0){
        perror("listen");
        return 1;
    }

    printf("サーバ起動 ポート%d\n",PORT);

    //-----------------------------------
    // ORDERとDISPLAYが接続するまで待機
    //-----------------------------------

    while(order_fd == INVALID_SOCKET ||
          display_fd == INVALID_SOCKET)
    {
        socklen_t len = sizeof(client_addr);

        client_fd = accept(server_fd,
                           (struct sockaddr*)&client_addr,
                           &len);

        if(client_fd == INVALID_SOCKET)
            continue;

        printf("接続:%s:%d\n",
               inet_ntoa(client_addr.sin_addr),
               ntohs(client_addr.sin_port));

        char type[32];

        memset(type,0,sizeof(type));

        int n = recv(client_fd,
                     type,
                     sizeof(type)-1,
                     0);

        if(n <= 0){
            CLOSE_SOCKET(client_fd);
            continue;
        }

        type[n]='\0';

        // 改行を除去
        type[strcspn(type,"\r\n")] = '\0';

        if(strcmp(type,"ORDER")==0){

            order_fd = client_fd;
            printf("注文受付用として登録\n");

        }else if(strcmp(type,"DISPLAY")==0){

            display_fd = client_fd;
            printf("注文表示用として登録\n");

        }else{

            printf("不明な接続:%s\n",type);
            CLOSE_SOCKET(client_fd);

        }

    }

    printf("\n===== 準備完了 =====\n");

    //-----------------------------------
    // 注文受付→表示へ転送
    //-----------------------------------

    while(1)
    {
        char buffer[1024];
        char sendbuf[1100];

        memset(buffer,0,sizeof(buffer));

        int size =
            recv(order_fd,
                 buffer,
                 sizeof(buffer)-1,
                 0);

        if(size <= 0){

            printf("注文受付が切断\n");
            break;
        }

        buffer[size]='\0';

        if(strncmp(buffer,"ORDER_CART:",11)==0)
        {
            updateOrderCount(buffer + 11);
            saveCSV();
        }
        else if(strcmp(buffer,"LOG")==0){
            showHistory();
        }
        if(strcmp(buffer,"RESET_ORDER_NUMBER")==0)
        {
            send(display_fd,
                "RESET_ORDER_NUMBER\n",
                strlen("RESET_ORDER_NUMBER\n"),0);

            printf("注文番号リセット要求\n");

            continue;
        }

        printf("注文:%s\n",buffer);

        sprintf(sendbuf,"%s\n",buffer);

        if(send(display_fd,
                sendbuf,
                strlen(sendbuf),
                0) == SOCKET_ERROR){

            printf("表示機への送信失敗\n");
            break;
        }

        printf("表示へ転送しました\n");
    }

    CLOSE_SOCKET(order_fd);
    CLOSE_SOCKET(display_fd);
    CLOSE_SOCKET(server_fd);

    WSACleanup();

    return 0;
}





/*----------------------------------
 商品の注文数を更新
 mode = 1  → 注文追加
 mode = -1 → 注文取消
----------------------------------*/
void updateOrderCount(char *data)
{
    char temp[1024];

    strcpy(temp,data);


    char *token = strtok(temp,",");


    while(token != NULL)
    {

        char name[100];
        int count;


        sscanf(token,"%[^x]x%d",name,&count);


        int found = 0;


        for(int i=0;i<menuSize;i++)
        {
            if(strcmp(menuList[i].name,name)==0)
            {
                menuList[i].count += count;
                found = 1;
                break;
            }
        }


        if(!found)
        {
            strcpy(menuList[menuSize].name,name);
            menuList[menuSize].count=count;
            menuSize++;
        }


        token=strtok(NULL,",");
    }
}

void saveCSV(void)
{
    FILE *fp = fopen("order_history.csv","w");

    if(fp == NULL)
    {
        printf("CSV保存失敗\n");
        return;
    }

    fprintf(fp,"商品名,注文数\n");

    for(int i=0;i<menuSize;i++)
    {
        fprintf(fp,"%s,%d\n",
                menuList[i].name,
                menuList[i].count);
    }

    fclose(fp);
}


void showHistory(void)
{
    printf("\n===== 注文履歴 =====\n");

    for(int i=0;i<menuSize;i++)
    {
        printf("%s : %d\n",
               menuList[i].name,
               menuList[i].count);
    }

    printf("====================\n");
}