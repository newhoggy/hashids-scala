package org.hashids

import scala.annotation.tailrec
import org.hashids.impl._

class Hashids(
    salt: String = "",
    minHashLength: Int = 0,
    alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890") {
  private val distinctAlphabet = alphabet.distinct

  require(distinctAlphabet.length >= 16, "alphabet must contain at least 16 unique characters")
  require(distinctAlphabet.indexOf(" ") < 0, "alphabet cannot contains spaces")

  private val sepDiv = 3.5
  private val guardDiv = 12

  val impl = {
    val filteredSeps = "cfhistuCFHISTU".filter(x => distinctAlphabet.contains(x))
    val filteredAlphabet = distinctAlphabet.filterNot(x => filteredSeps.contains(x))
    val shuffledSeps = consistentShuffle(filteredSeps, salt)

    val (tmpSeps, tmpAlpha) = {
      if (shuffledSeps.isEmpty || ((filteredAlphabet.length / shuffledSeps.length) > sepDiv)) {
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
      } else (shuffledSeps, filteredAlphabet)
    }

    val guardCount = Math.ceil(tmpAlpha.length.toDouble / guardDiv).toInt
    val shuffledAlpha = consistentShuffle(tmpAlpha, salt)

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

  def consistentShuffle(alphabet: String, salt: String): String = {
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
