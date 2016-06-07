package org.hashids

import scala.annotation.tailrec

package object impl {
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

  def hash(input: Long, alphabet: String): String = {
    val alphaSize = alphabet.length.toLong

    @tailrec
    def doHash(in: Long, hash: String): String = {
      if (in <= 0) {
        hash
      } else {
        val newIn = in / alphaSize
        val newHash = alphabet.charAt((in % alphaSize).toInt) + hash
        doHash(newIn, newHash)
      }
    }

    doHash(input / alphaSize, alphabet.charAt((input % alphaSize).toInt).toString)
  }

  def unhash(input: String, alphabet: String): Long = {
    input.zipWithIndex.foldLeft[Long](0L) { case (acc, (in, i)) =>
      acc + (alphabet.indexOf(in) * Math.pow(alphabet.length, input.length - 1 - i)).toLong
    }
  }

  def encode(numbers: Long*)(effectiveAlphabet: String, minHashLength: Int, salt: String, seps: String, guards: String): String = {
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

    val provisionalResult = {
      if (tmpResult.length < minHashLength) {
        val guardIndex = (numberHash + tmpResult.codePointAt(0)) % guards.length
        val guard = guards.charAt(guardIndex)

        val provResult = guard + tmpResult

        if (provResult.length < minHashLength) {
          val guardIndex = (numberHash + provResult.codePointAt(2)) % guards.length
          val guard = guards.charAt(guardIndex)
          provResult + guard
        } else {
          provResult
        }
      } else tmpResult
    }

    val halfLen = tmpAlpha.length / 2

    @tailrec
    def respectMinHashLength(alpha: String, res: String): String = {
      if (res.length >= minHashLength) {
        res
      } else {
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

  def decode(hash: String)(effectiveAlphabet: String, salt: String, seps: String, guards: String): List[Long] = {
    val hashArray = hash.split(s"[$guards]")
    val i = if (hashArray.length == 3 || hashArray.length == 2) 1 else 0
    if (hashArray.nonEmpty) {
      val hashArrayI = hashArray(i)
      if (hashArrayI.nonEmpty) {
        val lottery = hashArray(i).charAt(0)
        val hashBreakdown = hashArray(i).substring(1).split(s"[$seps]")

        @tailrec
        def doDecode(
            in: List[String], buff: String,
            alpha: String, result: List[Long]): List[Long] = in match {
          case Nil => result.reverse
          case x :: tail =>
            val newAlpha = consistentShuffle(alpha, buff.substring(0, alpha.length))
            doDecode(tail, lottery + salt + newAlpha, newAlpha, unhash(x, newAlpha) :: result)
        }

        doDecode(hashBreakdown.toList, lottery + salt + effectiveAlphabet, effectiveAlphabet, Nil)
      } else {
        List.empty
      }
    } else {
      List.empty
    }
  }
}
