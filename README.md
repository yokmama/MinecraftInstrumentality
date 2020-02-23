# MinecraftInstrumentality

=== setup

1 git clone https://github.com/yokmama/MinecraftInstrumentality.git

2 IntelliJでクローンしたフォルダを開く

3 Gradleタスクのbuildでモジュールを作成

=== debug
1 create remote 設定
2 vmの設定に　-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005　を追加
3 下記コマンドでspigotを起動
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar spigot-1.15.2.jar nogui