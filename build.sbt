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
<ResultSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:yahoo:jp:jlp:KeyphraseService"
  xsi:schemaLocation="urn:yahoo:jp:jlp:KeyphraseService http://jlp.yahooapis.jp/KeyphraseService/V1/extract.xsd">
  <Result>
    <Keyphrase>東京ミッドタウン</Keyphrase>
    <Score>100</Score>
  </Result>
  <Result>
    <Keyphrase>国立新美術館</Keyphrase>
    <Score>54</Score>
  </Result>
  <Result>
    <Keyphrase>5分</Keyphrase>
    <Score>9</Score>
  </Result>
</ResultSet>

"""

