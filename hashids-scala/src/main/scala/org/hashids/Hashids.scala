package org.hashids

import scala.annotation.tailrec
import org.hashids.impl._

class Hashids(
    salt: String = "",
    minHashLength: Int = 0,
    alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
    seps: String,
    guards: String,
    effectiveAlphabet: String) {
  def encode(numbers: Long*): String = {
    if (numbers.isEmpty) {
      ""
    } else {
      org.hashids.impl.encode(numbers:_*)(effectiveAlphabet, minHashLength, salt, seps, guards)
    }
  }

  def encodeHex(in: String): String = {
    require(in.matches("^[0-9a-fA-F]+$"), "Not a HEX string")

    val matcher = "[\\w\\W]{1,12}".r.pattern.matcher(in)

    @tailrec
    def doSplit(result: List[Long]): List[Long] = {
      if (matcher.find())
        doSplit(java.lang.Long.parseLong("1" + matcher.group, 16) :: result)
      else
        result
    }

    org.hashids.impl.encode(doSplit(Nil):_*)(effectiveAlphabet, minHashLength, salt, seps, guards)
  }

  def decode(hash: String): List[Long] = hash match {
    case "" => Nil
    case x =>
      val res = org.hashids.impl.decode(x)(effectiveAlphabet, salt, seps, guards)

      if (res.exists(_ < 0)) {
        List.empty
      } else if (encode(res: _*) == hash) {
        res
      } else {
        Nil
      }
  }

  def decodeHex(hash: String): String = decode(hash).map(_.toHexString.substring(1).toUpperCase).mkString

  def version = "1.0.0"
}

object Hashids {
  @deprecated("Use `Hashids.legacyJiecao` or `Hashids.reference` instead.  See compatibility note in README.md", "1.1.1")
  def apply(
      salt: String = "",
      minHashLength: Int = 0,
      alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"): Hashids = ???

  def legacyJiecao(
      salt: String = "",
      minHashLength: Int = 0,
      alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890") = {
    val (seps, guards, effectiveAlphabet) = {
      val distinctAlphabet = alphabet.distinct

      require(distinctAlphabet.length >= 16, "alphabet must contain at least 16 unique characters")
      require(distinctAlphabet.indexOf(" ") < 0, "alphabet cannot contains spaces")

      val sepDiv = 3.5
      val guardDiv = 12
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
        (seps, guards, shuffledAlpha)
      } else {
        val guards = shuffledAlpha.substring(0, guardCount)
        val alpha = shuffledAlpha.substring(guardCount)
        (tmpSeps, guards, alpha)
      }
    }

    new Hashids(salt, minHashLength, alphabet, seps, guards, effectiveAlphabet)
  }

  def reference(
      salt: String = "",
      minHashLength: Int = 0,
      alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890") = {
    val (seps, guards, effectiveAlphabet) = {
      val distinctAlphabet = alphabet.distinct

      require(distinctAlphabet.length >= 16, "alphabet must contain at least 16 unique characters")
      require(distinctAlphabet.indexOf(" ") < 0, "alphabet cannot contains spaces")

      val sepDiv = 3.5
      val guardDiv = 12
      val filteredSeps = "cfhistuCFHISTU".filter(x => distinctAlphabet.contains(x))
      val filteredAlphabet = distinctAlphabet.filterNot(x => filteredSeps.contains(x))
      val shuffledSeps = consistentShuffle(filteredSeps, salt)

      val (tmpSeps, tmpAlpha) = {
        if (shuffledSeps.isEmpty || (Math.ceil(filteredAlphabet.length.toDouble / shuffledSeps.length) > sepDiv)) {
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
        (seps, guards, shuffledAlpha)
      } else {
        val guards = shuffledAlpha.substring(0, guardCount)
        val alpha = shuffledAlpha.substring(guardCount)
        (tmpSeps, guards, alpha)
      }
    }

    new Hashids(salt, minHashLength, alphabet, seps, guards, effectiveAlphabet)
  }
}
