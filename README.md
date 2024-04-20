# Velodicord
## 説明
これはvelocity専用のプラグインで、discord-minecraft間のチャット共有、それの日本語化、それの読み上げ、velocityサーバーの起動・停止通知、プレイヤーの入退出・サーバー移動通知、プレイヤーの死亡通知、プレイヤーの進捗達成通知、プレイヤーのコマンド実行通知、/サーバー名でのサーバー移動、/posでの現在いる座標のdiscordへの通知、/playerで現在参加しているプレイヤー通知(discordからも可)ができるとにかくプロキシサーバーに必要と思われる機能を全部つぎ込んだものです
> [!CAUTION]
> **プレイヤーの死亡通知、プレイヤーの進捗達成通知、プレイヤーのコマンド実行通知、/posでの現在いる座標のdiscordへの通知、チャットが二回送られることを防ぐにはfabricサーバー側に[Fabdicord](https://modrinth.com/project/fabdicord)も必要です**
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
## 機能
   - マイクラ、discord間チャット共有(ローマ字日本語化)
     - discord側
       - /showdetectbot : 登録されている発言を無視しないbot一覧
       - /adddetectbot : 新たに発言を無視しないbotを登録
       - /deletedetectbot : 登録されている発言を無視しないbotの削除
       - /showignorecommand : 登録されている通知しないコマンド一覧
       - /addignorecommand : 新たに通知しないコマンドを登録
       - /deleteignorecommand : 登録されている通知しないコマンドの削除
     - マイクラ側
       - velocityサーバーの起動・停止通知
       - プレイヤーの入退出・サーバー移動通知
       - プレイヤーの死亡通知
       - プレイヤーの進捗達成通知
       - プレイヤーのコマンド実行通知
   - 読み上げ機能
     - discord側
       - /join : 実行者が現在入室しているボイスチャンネルに参加
       - /leave : 現在参加しているボイスチャンネルから退出
       - /showdic : 辞書に登録している単語一覧
       - /adddic : 辞書に新たな単語を登録・登録されている単語の読み方を編集
       - /deletedic : 辞書に登録されている単語の削除
       - /showspeaker : 話者の種類とID
       - /setdefaultspeaker : デフォルトの話者を設定
       - /setspeaker : 話者を設定
     - マイクラ側
       - /setspeaker : 話者を設定
   - その他
     - discord側
       - /player : 現在参加しているプレイヤー
       - /showchannel : 設定されているチャンネル
       - /setmain : メインチャンネルを設定
       - /setlog : ログチャンネルを設定
       - /setpos : POSチャンネルを設定
       - /setcommand : コマンドチャンネルを設定
     - マイクラ側
       - /playerlist : 現在参加しているプレイヤー一覧
       - /[サーバー名] : サーバーへ接続
       - /pos : 現在いる座標の共有。引数を渡すことでその名前でposチャンネルに保存する
## configファイル
```
{
  "_bcomment_" : "discordbotのtoken",
  "BotToken" : "123456",
  "_mcomment_" : "discordのメインチャンネルID",
  "MainChannelID" : "123456",
  "_lcomment_" : "discordの入退出などの通知チャンネルID(オプション)",
  "LogChannelID" : "000000",
  "_pcomment_" : "discordのPOSチャンネルID(オプション)",
  "PosChannelID" : "000000",
  "_ccomment_" : "discordのコマンドチャンネルID(オプション)",
  "CommandChannelID" : "000000",
  "_vcomment_" : "VOICEVOXのタイプ CPU : 1 DirectML : 2 CUDA : 3",
  "VOICEVOX-type" : "1",
  "_dcomment_" : "デフォルトの読み上げの声のID",
  "DefaultSpeakerID" : "3"
}
```
## [modrinth](https://modrinth.com/project/velodicord)で公開しています
