package org.hashids

import scala.annotation.tailrec

class Hashids(
  salt: String = "",
  minHashLength: Int = 0,
  alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
) {

  require(alphabet.length == alphabet.distinct.length , "check your alphabet for duplicates")
  require(alphabet.length >= 16, "alphabet must contain at least 16 characters")
  require(alphabet.indexOf(" ") < 0, "alphabet cannot contains spaces")

  private val sepDiv = 3.5
  private val guardDiv = 12

  private val (seps, guards, effectiveAlphabet) = {
    val filteredSeps = "cfhistuCFHISTU".filter(x => alphabet.contains(x))
    val filteredAlphabet = alphabet.filterNot(x => filteredSeps.contains(x))
    val shuffledSeps = consistentShuffle(filteredSeps, salt)

    val (tmpSeps, tmpAlpha) =
      if (shuffledSeps.isEmpty || ((filteredAlphabet.length / shuffledSeps.length) > sepDiv)) {
        val sepsTmpLen = Math.ceil(filteredAlphabet.length / sepDiv).toInt
        val sepsLen = if(sepsTmpLen == 1) 2 else sepsTmpLen

        if(sepsLen > shuffledSeps.length) {
          val diff = sepsLen - shuffledSeps.length
          val seps =  shuffledSeps + filteredAlphabet.substring(0, diff)
          val alpha = filteredAlphabet.substring(diff)
          (seps, alpha)
        } else {
          val seps = shuffledSeps.substring(0, sepsLen)
          val alpha = filteredAlphabet
          (seps, alpha)
        }
      } else (shuffledSeps, filteredAlphabet)

    val guardCount = Math.ceil(tmpAlpha.length.toDouble / guardDiv).toInt
    val shuffledAlpha = consistentShuffle(tmpAlpha, salt)

    if(shuffledAlpha.length < 3) {
      val guards = tmpSeps.substring(0, guardCount)
      val seps = tmpSeps.substring(guardCount)
      (seps, guards, shuffledAlpha)
    } else {
      val guards = shuffledAlpha.substring(0, guardCount)
      val alpha = shuffledAlpha.substring(guardCount)
      (tmpSeps, guards, alpha)
    }
  }

  def encode(numbers: Long*): String =
    if(numbers.isEmpty) "" else _encode(numbers:_*)

  def encodeHex(in: String): String = {
    require(in.matches("^[0-9a-fA-F]+$"), "Not a HEX string")

    val matcher = "[\\w\\W]{1,12}".r.pattern.matcher(in)

    @tailrec
    def doSplit(result: List[Long]): List[Long] = {
      if (matcher.find()) doSplit(java.lang.Long.parseLong("1" + matcher.group, 16) :: result)
      else result
    }

    _encode(doSplit(Nil):_*)
  }

  private def _encode(numbers: Long*): String = {
    val indexedNumbers = numbers.zipWithIndex
    val numberHash = indexedNumbers
      .foldLeft[Int](0){ case (acc, (x, i)) =>
        acc + (x % (i+100)).toInt
    }
    val lottery = effectiveAlphabet.charAt(numberHash % effectiveAlphabet.length).toString

    val (tmpResult, tmpAlpha) =
      indexedNumbers.foldLeft[(String, String)]((lottery, effectiveAlphabet)) {
        case ((result, alpha), (x, i)) =>
          val buffer = lottery + salt + alpha
          val newAlpha = consistentShuffle(alpha, buffer.substring(0, alpha.length))
          val last = hash(x, newAlpha)
          val newResult = result + last

          if (i + 1 < numbers.size) {
            val num = x % (last.codePointAt(0) + i)
            val sepsIndex = (num % seps.length).toInt
            (newResult + seps.charAt((num % seps.length).toInt), newAlpha)
          } else {
            (newResult, newAlpha)
          }
      }

    val provisionalResult = if(tmpResult.length < minHashLength) {
      val guardIndex = (numberHash + tmpResult.codePointAt(0)) % guards.length
      val guard = guards.charAt(guardIndex)

      val provResult = guard + tmpResult

      if(provResult.length < minHashLength) {
        val guardIndex = (numberHash + provResult.codePointAt(2)) % guards.length
        val guard = guards.charAt(guardIndex)
        provResult + guard
      } else {
        provResult
      }
    } else tmpResult

    val halfLen = tmpAlpha.length / 2

    @tailrec
    def respectMinHashLength(alpha: String, res: String): String = {
      if (res.length >= minHashLength) res
      else {
        val newAlpha = consistentShuffle(alpha, alpha);
        val tmpRes = newAlpha.substring(halfLen) + res + newAlpha.substring(0, halfLen);
        val excess = tmpRes.length - minHashLength
        val newRes = if(excess > 0) {
          val startPos = excess / 2
          tmpRes.substring(startPos, startPos + minHashLength)
        } else tmpRes
        respectMinHashLength(newAlpha, newRes)
      }
    }

    respectMinHashLength(tmpAlpha, provisionalResult)
  }

  def decode(hash: String): List[Long] = hash match {
    case "" => Nil
    case x =>
      val res = _decode(x, effectiveAlphabet)
      if (encode(res:_*) == hash) res
      else throw new IllegalStateException(s"Hash $hash was generated with different Hashids salt/alphabet")
  }

  def decodeHex(hash: String): String =
    decode(hash).map(x => x.toHexString.substring(1).toUpperCase).mkString

  private def _decode(hash: String, alphabet: String): List[Long] = {
    val hashArray = hash.split(s"[$guards]")
    val i = if(hashArray.length == 3 || hashArray.length == 2) 1 else 0
    val lottery = hashArray(i).charAt(0)
    val hashBreakdown = hashArray(i).substring(1).split(s"[$seps]")

    @tailrec
    def doDecode(in: List[String], buff: String,
      alpha: String, result: List[Long]): List[Long] = in match {
      case Nil => result.reverse
      case x :: tail =>
        val newBuf = lottery + salt + alpha
        val newAlpha = consistentShuffle(alpha, buff.substring(0, alpha.length))
        doDecode(tail, lottery + this.salt + newAlpha, newAlpha, unhash(x, newAlpha) :: result)
    }

    doDecode(hashBreakdown.toList, lottery + salt + effectiveAlphabet, effectiveAlphabet, Nil)
  }


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

    if(salt.length <= 0) alphabet
    else doShuffle(alphabet.length - 1, 0, 0, alphabet)
  }

  private def hash(input: Long, alphabet: String): String = {
    val alphaSize = alphabet.length.toLong

    @tailrec
    def doHash(in: Long, hash: String): String = {
      if (in <= 0) hash
      else {
        val newIn = in / alphaSize
        val newHash = alphabet.charAt((in % alphaSize).toInt) + hash
        doHash(newIn, newHash)
      }
    }

    doHash(input / alphaSize, alphabet.charAt((input % alphaSize).toInt).toString)
  }

  private def unhash(input: String, alphabet: String): Long =
    input.zipWithIndex.foldLeft[Long](0L){case (acc, (in, i)) =>
      acc + (alphabet.indexOf(in) *
        Math.pow(alphabet.length, input.length - 1 - i)).toLong
    }

  def version = "1.0.0"

}

object Hashids {

  implicit class HashidsLongOps(x: Long) {
    def hashid(implicit hashids: Hashids): String = hashids.encode(x)
  }

  implicit class HashidsSeqLongOps(val self: Seq[Long]) {
    def hashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class HashidsArrayLongOps(val self: Array[Long]) {
    def encodeHashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class HashidsStringOps(x: String) {
    def unhashid(implicit hashids: Hashids)   : Seq[Long] = hashids.decode(x)
    def hashidHex(implicit hashids: Hashids)  : String = hashids.encodeHex(x)
    def unhashidHex(implicit hashids: Hashids): String = hashids.decodeHex(x)
  }

  def apply(salt: String) =
    new Hashids(salt)

  def apply(salt: String, minHashLength: Int) =
    new Hashids(salt, minHashLength)

  def apply(salt: String, minHashLength: Int, alphabet: String) =
    new Hashids(salt, minHashLength, alphabet)

}
