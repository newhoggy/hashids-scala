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

  def decode(hash: String, effectiveAlphabet: String, salt: String, seps: String, guards: String): List[Long] = {
    val hashArray = hash.split(s"[$guards]")
    val i = if (hashArray.length == 3 || hashArray.length == 2) 1 else 0
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
  }
}
