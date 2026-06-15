#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
TCP エコークライアント（子機）
キーボード入力 → ポート 8080 に送信 → 返信を表示
"""

import socket

HOST = "127.0.0.1"  # 親機の IP（同一 PC なら 127.0.0.1、LAN なら親機の IP に変更）
PORT = 8080
BUFFER_SIZE = 1024


def main():
    print(f"接続先: {HOST}:{PORT}")
    print("文字列を入力して Enter。終了は quit または Ctrl+C")
    print("-" * 40)

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.connect((HOST, PORT))
            print("親機に接続しました")

            while True:
                text = input("送信> ")
                if text.strip().lower() == "quit":
                    print("終了します")
                    break

                # 文字列をバイト列にして送信
                sock.sendall(text.encode("utf-8"))

                # 親機からエコーされたデータを受信
                data = sock.recv(BUFFER_SIZE)
                if not data:
                    print("親機が接続を閉じました")
                    break

                print(f"受信> {data.decode('utf-8')}")

    except ConnectionRefusedError:
        print("接続できません。親機（echo_server.exe）が起動しているか確認してください。")
    except KeyboardInterrupt:
        print("\n終了します")
    except OSError as e:
        print(f"通信エラー: {e}")


if __name__ == "__main__":
    main()