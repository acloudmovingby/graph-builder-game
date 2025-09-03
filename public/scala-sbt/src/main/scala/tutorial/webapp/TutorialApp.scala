package tutorial.webapp

import graphi.Test

object TutorialApp {
  def main(args: Array[String]): Unit = {
    println(s"Hello world!!! ${new Test().saySomething()}")
  }
}
