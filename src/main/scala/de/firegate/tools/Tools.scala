package de.firegate.tools

import scala.util.Random

object Tools {

  def randomString(prefix: String = "", suffix: String = "", nameSize: Int = 20): String = {
    val alphabet = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ ("_")
    (1 to nameSize).map(_ => alphabet(Random.nextInt(alphabet.size))).mkString
  }

  def tmpDir(): String = {
    sys.props.getOrElse("java.io.tmpdir", "NULL")
  }

  def tmpFileName(prefix: String = "", suffix: String = ""): String = {
    tmpDir() + randomString(prefix, suffix)
  }
}
