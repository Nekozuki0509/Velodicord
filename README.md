# Velodicord
## 説明
これはvelocity専用のプラグインで、discord-minecraft間のチャット共有、それの日本語化、velocityサーバーの起動・停止通知、プレイヤーの入退出・サーバー移動通知、プレイヤーの死亡通知、プレイヤーの進捗達成通知、プレイヤーのコマンド実行通知、/サーバー名でのサーバー移動、/posでの現在いる座標のdiscordへの通知、/playerで現在参加しているプレイヤー通知(discordからも可)ができるとにかくプロキシサーバーに必要と思われる機能を全部つぎ込んだものです
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
1. plugins/Velodicord/config.yamlを編集
1. 楽しみましょう!
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
