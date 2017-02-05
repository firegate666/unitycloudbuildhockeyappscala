import java.util.{Date, Locale}
import java.text.DateFormat._

object FrenchDate4Java {
  def main(args: Array[String]) = {
    val now = new Date
    val df = getDateInstance(LONG, Locale.FRANCE)
    println(df format now)
    printf(df.format(now))
  }
}