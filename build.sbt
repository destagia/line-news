name := """line-news"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

initialCommands in console := """
  import util._
  import scala.concurrent.{Await, Future}
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val sample =
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/">
<channel>
<atom:link href="http://news.livedoor.com/rss/topics/int.xml" rel="self" type="application/rss+xml" />
<language>ja</language>
<title>ライブドアニュース - 海外トピックス</title>
<link>http://news.livedoor.com</link>
<generator>http://news.livedoor.com</generator>
<description>ライブドアニュース - 海外トピックス</description>
<lastBuildDate>Fri, 12 Jun 2015 22:59:34 +0900</lastBuildDate>
<item>
    <title>南シナ海問題 フィリピンで「中国は出ていけ」と抗議デモ</title>
    <link>http://news.livedoor.com/article/detail/10225287/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;フィリピンで12日、南シナ海問題に関して中国への抗議デモがあった&lt;/li&gt;
        &lt;li&gt;市民団体メンバーらが「中国はフィリピン水域から出ていけ」などと主張&lt;/li&gt;
        &lt;li&gt;アキノ大統領も式典で、人工島建設を進める中国を暗に批判した&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10225287/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>0</mobile>
    <pubDate>Fri, 12 Jun 2015 21:14:59 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10225287/</guid>
</item>
<item>
    <title>ジンバブエが自国通貨を廃止 ジンバブエドルを米ドルに交換へ</title>
    <link>http://news.livedoor.com/article/detail/10224062/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;ジンバブエは11日、事実上価値のなくなった自国通貨を廃止すると発表&lt;/li&gt;
        &lt;li&gt;銀行口座に残っているジンバブエドルは、米ドルに交換するという&lt;/li&gt;
        &lt;li&gt;同国は08年にハイパーインフレを経験した後、自国通貨の使用をやめていた&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10224062/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>0</mobile>
    <pubDate>Fri, 12 Jun 2015 16:23:41 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10224062/</guid>
</item>
<item>
    <title>韓国が世界遺産の登録阻止へ外交攻勢 「歴史知らしめる」</title>
    <link>http://news.livedoor.com/article/detail/10225001/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;韓国の外相が12日、ベルリンを訪問しドイツの外相と会談した&lt;/li&gt;
        &lt;li&gt;「明治日本の産業革命遺産」の世界遺産登録反対を働きかけたとみられる&lt;/li&gt;
        &lt;li&gt;「国際社会に歴史的事実を明確に知らしめる」と韓国の議員は主張している&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10225001/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>0</mobile>
    <pubDate>Fri, 12 Jun 2015 19:49:59 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10225001/</guid>
</item>
<item>
    <title>西アフリカのエボラ出血熱感染者が再び増加 流行いまだ終わらず</title>
    <link>http://news.livedoor.com/article/detail/10222151/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;西アフリカのエボラ出血熱感染者が、再び増加に転じていると伝えている&lt;/li&gt;
        &lt;li&gt;6月第1週の間に、新たに31人の感染が確認されたという&lt;/li&gt;
        &lt;li&gt;「流行はまだ終わっていない」と国連は12日までに発表した声明で強調した&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10222151/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>1</mobile>
    <pubDate>Fri, 12 Jun 2015 09:38:00 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10222151/</guid>
</item>
<item>
    <title>韓国MERSの感染拡大に大病院の落とし穴 業界規模に比べ弱いシステム</title>
    <link>http://news.livedoor.com/article/detail/10224431/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;MERS感染拡大は、大病院の落とし穴が一因になっているという指摘がある&lt;/li&gt;
        &lt;li&gt;都市の大病院では、入院希望患者が救急病棟で待たされることもあるという&lt;/li&gt;
        &lt;li&gt;「病院業界の規模に比べて、システムがまだ弱い」と韓国の病院関係者&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10224431/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>0</mobile>
    <pubDate>Fri, 12 Jun 2015 18:27:16 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10224431/</guid>
</item>
<item>
    <title>テキサス州で地震急増 石油の掘削説あがるも石油企業は否定</title>
    <link>http://news.livedoor.com/article/detail/10225004/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;アメリカのテキサス州でマグニチュード3以上の地震が急増している&lt;/li&gt;
        &lt;li&gt;研究では石油や天然ガスの掘削活動に伴う人為的な地震だとする見方が有力&lt;/li&gt;
        &lt;li&gt;石油企業は関連を否定し、住民の不安と石油産業の利害が絡み合っている&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10225004/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>1</mobile>
    <pubDate>Fri, 12 Jun 2015 17:40:22 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10225004/</guid>
</item>
<item>
    <title>テイラー・スウィフトがInstagramで彼氏とのラブラブ写真を公開 </title>
    <link>http://news.livedoor.com/article/detail/10223520/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;テイラー・スウィフトが彼氏と白鳥型フロートに乗っている写真を公開した&lt;/li&gt;
        &lt;li&gt;ビキニ姿で左手を高く上げ、右手を彼氏の肩に乗せてとても親密な雰囲気&lt;/li&gt;
        &lt;li&gt;写真を見たファンたちは次々と祝福のコメントを書き込んだ&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10223520/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>1</mobile>
    <pubDate>Fri, 12 Jun 2015 14:10:27 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10223520/</guid>
</item>
<item>
    <title>習近平国家主席を狙った暗殺未遂事件 検査用の注射器に毒</title>
    <link>http://news.livedoor.com/article/detail/10224043/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;習近平国家主席を狙ったこれまでの暗殺未遂事件を、特派員がレポート&lt;/li&gt;
        &lt;li&gt;12年には健康診断のため訪れた病院で、検査用の注射器に毒を入れられていた&lt;/li&gt;
        &lt;li&gt;14年にウルムチで起きた爆発事件も習氏暗殺が目的の可能性が高いといわれる&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10224043/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>1</mobile>
    <pubDate>Fri, 12 Jun 2015 16:00:26 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10224043/</guid>
</item>
<item>
    <title>黒字金額の多い国は？ 1位はノルウェー、2位はブラジルに</title>
    <link>http://news.livedoor.com/article/detail/10224660/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;黒字金額の多い国ランキングをCIAのデータをもとに紹介している&lt;/li&gt;
        &lt;li&gt;1位は「ノルウェー」となり、約7兆6662億円の黒字となっている&lt;/li&gt;
        &lt;li&gt;約3兆2443億円で「ブラジル」、約1兆9225億円で「ドイツ」が続いた&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10224660/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>1</mobile>
    <pubDate>Fri, 12 Jun 2015 18:04:56 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10224660/</guid>
</item>
<item>
    <title>朴槿恵政権はMERS心理戦に敗退 大統領はなぜ記者会見をしないのか</title>
    <link>http://news.livedoor.com/article/detail/10223573/</link>
    <description>
    ざっくり言うと
    &lt;br /&gt;
      &lt;ul&gt;
        &lt;li&gt;MERS危機において、朴槿恵政権は心理戦に失敗していると筆者が分析している&lt;/li&gt;
        &lt;li&gt;大統領が記者会見をすることで、国民を安心させる効果があるという&lt;/li&gt;
        &lt;li&gt;国民の前で説明しないことには、政府に対する不信は容易には減じないとした&lt;/li&gt;
      &lt;/ul&gt;
    &lt;a href=&quot;http://news.livedoor.com/article/detail/10223573/&quot;&gt;記事を読む&lt;/a&gt;
    </description>
    <mobile>1</mobile>
    <pubDate>Fri, 12 Jun 2015 13:32:11 +0900</pubDate>
    <guid>http://news.livedoor.com/article/detail/10223573/</guid>
</item>
</channel>
</rss>

"""

