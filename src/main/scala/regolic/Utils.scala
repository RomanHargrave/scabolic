package regolic

object Utils {

  /* if c is digit in radix r (1 < r <= 36) */
  def isDigit(c: Char, r: Int): Boolean = {
    require(r > 1 && r <= 36)
    val d = (c - '0').toInt
    if(d < 10 && d >= 0)
      d < r
    else {
      val ld = (c.toLower - 'a').toInt
      ld >= 0 && ld < r - 10
    }
  }

}
