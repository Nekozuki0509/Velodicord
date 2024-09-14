[![Modrinth Version](https://img.shields.io/modrinth/v/FVtm4KDD?logo=modrinth&color=1bd768)![Modrinth Downloads](https://img.shields.io/modrinth/dt/FVtm4KDD?logo=modrinth&color=1bd768)![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/FVtm4KDD?logo=modrinth&color=1bd768)](https://modrinth.com/plugin/velodicord)
[![Discord](https://img.shields.io/discord/1241236305741090836?logo=discord&color=5765f2)](https://discord.gg/352Cdy8MjV)
[![Static Badge](https://img.shields.io/badge/litlink-Nekozuki0509-9594f9)](https://lit.link/nekozuki0509)

[![Static Badge](https://img.shields.io/badge/github-fabdicord-blue?logo=github)](https://github.com/Nekozuki0509/Fabdicord)
[![Static Badge](https://img.shields.io/badge/github-V4S4J-blue?logo=github)](https://github.com/Nekozuki0509/V4S4J)

# Velodicord
## 説明
これはvelocity専用のプラグインで、discord-minecraft間のチャット共有、それの日本語化、それの読み上げ、velocityとバックエンドサーバーの起動・停止通知、プレイヤーの入退出・サーバー移動通知、プレイヤーの死亡通知、プレイヤーの進捗達成通知、プレイヤーのコマンド実行通知、/サーバー名でのサーバー移動、/posでの現在いる座標のdiscordへの通知、/playerで現在参加しているプレイヤー通知(discordからも可)、discordからのコマンド実行ができるとにかくプロキシサーバーに必要と思われる機能を全部つぎ込んだものです
> [!CAUTION]
> **fabricサーバー側に[Fabdicord](https://modrinth.com/project/fabdicord)も必要です**(もし別ver.が必要なら[discordサーバー](https://discord.gg/352Cdy8MjV)のrequestまで)
## 使い方
1. discord botを作る
   - 必要なパーミッション
     - Bot
       - Privileged Gateway Intents
       - PRESENCE INTENT
       - SERVER MEMBERS INTENT
       - MESSAGE CONTENT INTENT
     - OAuth2
       - OAuth2 URL Generator
         - SCOPES
           - applications.commands
           - bot
         - BOT PERMISSIONS
           - Administer
1. velocityサーバーのpluginsフォルダにこのプラグインを入れて再起動
1. plugins/Velodicord/config.jsonを編集
1. 楽しみましょう!
## コマンド
   - discord側
     - join
       - ボイスチャンネルへの参加
     - leave
       - ボイスチャンネルからの退出
     - dic (辞書関係)
       - show
         - 辞書に登録されている単語
       - add [word: String] [read: String]
         - 辞書に新たな単語を登録・登録されている単語の読み方を変更(正規表現可)
       - del [word: String]
         - 辞書に登録されている単語の削除
     - ch (チャンネル関連)
       - show
         - 設定されているチャンネル
       - set [name: String] [channel: Channel]
         - チャンネルを設定
       - del_log
         - ログチャンネルを削除
     - commandrole (コマンドロール関連)
       - show
         - 設定されているロール
       - set [role: Role]
         - ロールを設定
     - detectbot (発言を無視しないbot関連)
       - show
         - 登録されている発言を無視しないbot
       - add [bot: User]
         - 新たに発言を無視しないbotを登録
       - del [bot: User]
         - 登録されている発言を無視しないbotの削除
     - speaker (話者関連)
       - show
         - all
           - 話者の種類とID
         - your
           - 設定されている話者
         - default
           - デフォルトの話者
       - set [which: String] [id: Integer]
         - 話者を設定
     - ignorecommand (通知しないコマンド関連)
       - show
         - 登録されている通知しないコマンド
       - add [command: String]
         - 新たに通知しないコマンドを登録
       - del [command: String]
         - 登録されている通知しないコマンドの削除
     - mentionable (メンション可能ロール関係)
       - show
         - 登録されているメンション可能ロール
       - set [role1: String] [role2: String] [role3 :String]
         - メンション可能ロールの設定
     - server (マイクラサーバー関連)
       - info
         - 各サーバーの情報
       - command [name: String] [command: String]
         - マイクラコマンド実行
     - admincommand (管理者コマンド関連)
       - show
       - add [which: String] [command: String]
         - 新たに管理者コマンドを登録
       - del [which: String] [command: String]
         - 登録されている管理者コマンドの削除
   - マイクラ側
     - playerlist : 現在参加しているプレイヤー一覧
     - [サーバー名] : サーバーへ接続
     - pos : 現在いる座標の共有。引数を渡すことでその名前でposチャンネルに保存する
## configファイル
```
{
  "_bcomment_" : "discordbotのtoken",
  "BotToken" : "aaaaaa",
  "_mcomment_" : "discordのメインチャンネルID",
  "MainChannelID" : "000000",
  "_pmcomment_" : "discordのプラグインメッセージチャンネルID",
  "PMChannelID" : "000000",
  "_lcomment_" : "マイクラサーバーのログ**フォーラム**チャンネルID(オプション)",
  "LogChannelID" : "000000",
  "_ncomment_" : "discordの入退出などの通知チャンネルID(オプション)",
  "NoticeChannelID" : "000000",
  "_pocomment_" : "discordのPOSチャンネルID(オプション)",
  "PosChannelID" : "000000",
  "_cccomment_" : "discordのコマンドチャンネルID(オプション)",
  "CommandChannelID" : "000000",
  "_vcomment_" : "VOICEVOXのタイプ CPU : 1 DirectML : 2 CUDA : 3",
  "VOICEVOX-type" : "1",
  "_dcomment_" : "デフォルトの読み上げの声のID",
  "DefaultSpeakerID" : "3",
  "_crcomment_" : "管理者コマンドを実行できるロールID",
  "CommandRoleID" : "aaaaaa"
}
```
