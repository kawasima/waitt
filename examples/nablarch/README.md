nablarch-example-web
===========================
Nablarchアプリケーションフレームワークを利用して作成したウェブExampleアプリケーションです。

## 実行手順

### 1.動作環境
実行環境に以下のソフトウェアがインストールされている事を前提とします。
* Java Version : 8
* Maven 3.0.5以降

以下は、本手順では事前準備不要です。

|ソフトウェア|説明|
|:---|:---|
|APサーバ|このアプリケーションはTomcat8を使用しています。waitt-maven-pluginはTomcat8へのアプリケーションのデプロイ、起動を行います。（起動前にエンティティクラスの作成とアプリケーションのコンパイルを別途行う必要があります。）|
|DBサーバ|このアプリケーションはH2 Database Engine(以下H2)を組み込んであるため、別途インストールの必要はありません。|

### 2. プロジェクトリポジトリの取得
Gitを使用している場合、アプリケーションを配置したいディレクトリにて「git clone」コマンドを実行してください。
以下、コマンドの例です。

    $mkdir c:\example
    $cd c:\example
    $git clone https://github.com/nablarch/nablarch-example-web.git

Gitを使用しない場合、最新のタグからzipをダウンロードし、任意のディレクトリへ展開してください。

### 3. アプリケーションのビルド
#### 3.1. データベースのセットアップ及びエンティティクラスの作成
まず、データベースのセットアップ及びエンティティクラスの作成を行います。以下のコマンドを実行してください。

    $cd nablarch-example-web
    $mvn -P gsp generate-resources
  
実行に成功すると、以下のようなログがコンソールに出力され、nablarch-example-webディレクトリの下にgsp-targetディレクトリが作成されます。

    (中略)
    [INFO] --- gsp-dba-maven-plugin:3.2.0:export-schema (default-cli) @ nablarch-example-app-web ---
    [INFO] PUBLICスキーマのExportを開始します。:c:\example\nablarch-example-web\gsp-target\output\PUBLIC.dmp
    [INFO] Building jar: c:\example\nablarch-example-web\gsp-target\output\nablarch-example-app-web-testdata-1.0.1.jar
    [INFO] PUBLICスキーマのExport完了
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    (中略)

#### 3.2. アプリケーションのビルド

次に、アプリケーションをビルドします。以下のコマンドを実行してください。

    $mvn clean compile
    
実行に成功すると、以下のようなログがコンソールに出力されます。

    (中略)
    [INFO] --- maven-compiler-plugin:3.2:compile (default-compile) @ nablarch-example-app-web ---
    [INFO] Changes detected - recompiling the module!
    [INFO] Compiling 56 source files to c:\example\nablarch-example-web\target\classes
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    (中略)


### 4. アプリケーションの起動
最後にwaitt-maven-pluginを実行し、組み込みTomcatを起動させます。以下のコマンドを実行してください。

    $mvn waitt:run

起動に成功すると、自動的にアプリケーションのログイン画面が表示されます。
以下のログインID、パスワードでログインできます。

| ログインID | パスワード |
|:------|:--------|
| 10000001 | pass123-|

### 5. DBの確認方法

1. http://www.h2database.com/html/cheatSheet.html からH2をインストールしてください。

2. {インストールフォルダ}/bin/h2.bat を実行してください(コマンドプロンプトが開く)。  
  ※h2.bat実行中はExampleアプリケーションからDBへアクセスすることができないため、Exampleアプリケーションを停止しておいてください。

3. ブラウザから http://localhost:8082 を開き、以下の情報でH2コンソールにログインしてください。
   JDBC URLの{dbファイルのパス}には、`nablarch_example.mv.db`ファイルの格納ディレクトリまでのパスを指定してください。  
  JDBC URL：jdbc:h2:{dbファイルのパス}/nablarch_example  
  ユーザ名：NABLARCH_EXAMPLE  
  パスワード：NABLARCH_EXAMPLE
