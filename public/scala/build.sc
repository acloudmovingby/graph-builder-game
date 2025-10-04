// build.sc
import mill.*, scalalib.*, scalajslib.*

object graphcontroller extends ScalaModule, ScalaJSModule {
  def scalaVersion = "3.7.3"
  def scalaJSVersion = "1.20.1"
  
  def mvnDeps = Seq(
    mvn"acloudmovingby::graphi::0.0.1" 
  )

  object test extends ScalaJSTests {
    def mvnDeps = Seq(mvn"com.lihaoyi::utest::0.9.1")
    def testFramework = "utest.runner.Framework"
  }
}
