package org.hashids

import scala.annotation.tailrec
import org.hashids.impl._

class Hashids(
    salt: String = "",
    minHashLength: Int = 0,
    alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890") {
  private def reorder(string: String, salt: String): String = {
    val len_salt = salt.length

    if (len_salt == 0) {
      string
    } else {
      var i = string.length - 1
      var index = 0
      var integer_sum = 0
      var temp_string = string

      while (i > 0) {
        index %= len_salt
        val integer = salt(index).toInt
        integer_sum += integer
        val j = (integer + index + integer_sum) % i

        val temp = temp_string(j)
        val trailer = if (j + 1 < temp_string.length) temp_string.substring(j + 1) else ' '
        temp_string = temp_string.substring(0, j) + temp_string(i) + trailer
        temp_string = temp_string.substring(0, i) + temp + temp_string.substring(i + 1)

        i -= 1
        index += 1
      }

      temp_string
    }
  }

  val impl = {
    val distinctAlphabet = alphabet.distinct

    require(distinctAlphabet.length >= 16, "alphabet must contain at least 16 unique characters")
    require(distinctAlphabet.indexOf(" ") < 0, "alphabet cannot contains spaces")

    val sepDiv = 3.5
    val guardDiv = 12

    val filteredSeps = "cfhistuCFHISTU".filter(x => distinctAlphabet.contains(x))
    val filteredAlphabet = distinctAlphabet.filterNot(x => filteredSeps.contains(x))
    val shuffledSeps = reorder(filteredSeps, salt)

    val (tmpSeps, tmpAlpha) = {
      // len_separators: 8
      // Assigning min_separators: 8

      // int(ceil(float(dividend) / divisor))

      if (shuffledSeps.isEmpty || (sepDiv < Math.ceil(filteredAlphabet.length.toFloat / shuffledSeps.length.toFloat))) {
        val sepsTmpLen = Math.ceil(filteredAlphabet.length / sepDiv).toInt
        val sepsLen = if (sepsTmpLen == 1) 2 else sepsTmpLen

        if (sepsLen > shuffledSeps.length) {
          val diff = sepsLen - shuffledSeps.length
          val seps = shuffledSeps + filteredAlphabet.substring(0, diff)
          val alpha = filteredAlphabet.substring(diff)
          (seps, alpha)
        } else {
          val seps = shuffledSeps.substring(0, sepsLen)
          val alpha = filteredAlphabet
          (seps, alpha)
        }
      } else {
        (shuffledSeps, filteredAlphabet)
      }
    }

    val guardCount = Math.ceil(tmpAlpha.length.toDouble / guardDiv).toInt
    val shuffledAlpha = reorder(tmpAlpha, salt)

    if (shuffledAlpha.length < 3) {
      val guards = tmpSeps.substring(0, guardCount)
      val seps = tmpSeps.substring(guardCount)

      HashidsImpl(
        salt = salt,
        minHashLength = minHashLength,
        effectiveAlphabet = shuffledAlpha,
        seps = seps,
        guards = guards)
    } else {
      val guards = shuffledAlpha.substring(0, guardCount)
      val alpha = shuffledAlpha.substring(guardCount)

      HashidsImpl(
        salt = salt,
        minHashLength = minHashLength,
        effectiveAlphabet = alpha,
        seps = tmpSeps,
        guards = guards)
    }
  }

  def encode(numbers: Long*): String = impl.encode(numbers: _*)

  def encodeHex(in: String): String = impl.encodeHex(in)

  def decode(hash: String): List[Long] = impl.decode(hash)

  def decodeHex(hash: String): String = impl.decodeHex(hash)

  def consistenShuffle(alphabet: String, salt: String): String = {
    @tailrec
    def doShuffle(i: Int, v: Int, p: Int, result: String): String = {
      if (i <= 0) {
        result
      } else {
        val newV = v % salt.length;
        val ascii = salt.codePointAt(newV)
        val newP = p + ascii
        val j = (ascii + newV + newP) % i
        val tmp = result.charAt(j)

        val alphaSuff = result.substring(0, j) + result.charAt(i) + result.substring(j + 1)
        val res = alphaSuff.substring(0, i) + tmp + alphaSuff.substring(i + 1)

        doShuffle(i - 1, newV + 1, newP, res)
      }
    }

    if (salt.length <= 0) alphabet else doShuffle(alphabet.length - 1, 0, 0, alphabet)
  }

  def version = "1.0.0"
}

object Hashids {
  def apply(salt: String) =
    new Hashids(salt)

  def apply(salt: String, minHashLength: Int) =
    new Hashids(salt, minHashLength)

  def apply(salt: String, minHashLength: Int, alphabet: String) =
    new Hashids(salt, minHashLength, alphabet)
}
