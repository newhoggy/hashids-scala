package org.hashids

object Hashids {
  val defaultAlphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
  val defaultSeps: String = "cfhistuCFHISTU"

  def apply(
      salt: String = "",
      minHashLength: Int = 0,
      alphabet: String = defaultAlphabet,
      seps: String = defaultSeps): Hashids = {
    val sepDiv: Double = 3.5
    val guardDiv: Int = 12
    val minAlphabetLength: Int = 16
    var guards: String = ""
    val myMinHashLength = minHashLength max 0

    var distinctAlphabet = alphabet.distinct

    if (distinctAlphabet.length < minAlphabetLength) {
      throw new IllegalArgumentException(s"alphabet must contain at least $minAlphabetLength unique characters")
    }

    if (distinctAlphabet.contains(" ")) {
      throw new IllegalArgumentException("alphabet cannot contains spaces")
    }

    // seps should contain only characters present in alphabet
    // alphabet should not contains seps
    var myAlphabet = distinctAlphabet diff seps
    var mySeps = distinctAlphabet intersect seps

    myAlphabet = myAlphabet.replaceAll("\\s+", "")
    mySeps = mySeps.replaceAll("\\s+", "")
    mySeps = consistentShuffle(mySeps, salt)

    if (mySeps == "" || (myAlphabet.length / mySeps.length) > sepDiv) {
      var seps_len: Int = (myAlphabet.length / sepDiv).ceil.toInt

      if (seps_len == 1) {
        seps_len += 1
      }

      if (seps_len > mySeps.length) {
        val diff = seps_len - mySeps.length
        mySeps += myAlphabet.take(diff)
        myAlphabet = myAlphabet.drop(diff)
      } else {
        mySeps = mySeps.take(seps_len)
      }
    }

    myAlphabet = consistentShuffle(myAlphabet, salt)

    val guardCount = (myAlphabet.length.toDouble / guardDiv).ceil.toInt

    if (myAlphabet.length < 3) {
      guards = mySeps.take(guardCount)
      mySeps = mySeps.drop(guardCount)
    } else {
      guards = myAlphabet.take(guardCount)
      myAlphabet = myAlphabet.drop(guardCount)
    }

    new Hashids(
        salt,
        myMinHashLength,
        myAlphabet,
        mySeps,
        guards)
  }

  private def consistentShuffle(alphabet: String, salt: String): String = {
    if (salt.length <= 0) {
      return alphabet
    }

    val as = alphabet.toCharArray
    var p = 0

    for (i <- (as.length - 1) until 0 by -1) {
      val v = (as.length - 1 - i) % salt.length
      val asc_val = salt(v).toInt
      p += asc_val
      val j = (asc_val + v + p) % i

      val tmp = as(j)
      as(j) = as(i)
      as(i) = tmp
    }

    return new String(as)
  }

  private def hash(inInput: Long, alphabet: String): String = {
    var hash = ""
    val alphabetLen = alphabet.length
    var input = inInput

    do {
      hash = alphabet((input % alphabetLen).toInt) + hash
      input /= alphabetLen
    } while (input > 0)

    return hash
  }

  private def unhash(input: String, alphabet: String): Long = {
    var number = 0L

    for (i <- 0 until input.length) {
      val pos = alphabet.indexOf(input(i))
      number += (pos * scala.math.pow(alphabet.length, input.length - i - 1)).toLong
    }

    return number
  }
}

class Hashids private (
    salt: String,
    minHashLength: Int,
    alphabet: String,
    seps: String,
    guards: String) {
  /**
   * Encode numbers to string
   *
   * @param numbers the numbers to encrypt
   * @return the encrypt string
   */
  def encode(numbers: Long*): String = {
    if (numbers.length == 0) {
      return ""
    }

    return this._encode(numbers: _*)
  }

  /**
   * Decode string to numbers
   *
   * @param hash the encrypt string
   * @return decryped numbers
   */
  def decode(hash: String): Seq[Long] = {
    if (hash == "")
      return Seq.empty

    return this._decode(hash, this.alphabet)
  }

  private def _encode(numbers: Long*): String = {
    var numberHashInt = 0

    for (i <- 0 until numbers.size) {
      numberHashInt += (numbers(i) % (i + 100)).toInt
    }

    var alphabet = this.alphabet
    val ret = alphabet(numberHashInt % alphabet.length)
    val lottery = ret
    var ret_str = ret + ""

    for (i <- 0 until numbers.length) {
      var num = numbers(i)
      val buffer = lottery + this.salt + alphabet

      alphabet = Hashids.consistentShuffle(alphabet, buffer.take(alphabet.length))
      val last = Hashids.hash(num, alphabet)

      ret_str += last

      if (i + 1 < numbers.length) {
        num %= (last(0) + i).toInt
        val sepsIndex = (num % this.seps.length).toInt
        ret_str += this.seps(sepsIndex)
      }
    }

    if (ret_str.length < this.minHashLength) {
      var guardIndex = (numberHashInt + ret_str(0).toInt) % this.guards.length
      var guard = this.guards(guardIndex)

      ret_str = guard + ret_str

      if (ret_str.length < this.minHashLength) {
        guardIndex = (numberHashInt + ret_str(2).toInt) % this.guards.length
        guard = this.guards(guardIndex)

        ret_str += guard
      }
    }

    val halfLen = alphabet.length / 2
    while (ret_str.length < this.minHashLength) {
      alphabet = Hashids.consistentShuffle(alphabet, alphabet)
      ret_str = alphabet.drop(halfLen) + ret_str + alphabet.take(halfLen)
      val excess = ret_str.length - this.minHashLength
      if (excess > 0) {
        val start_pos = excess / 2
        ret_str = ret_str.substring(start_pos, start_pos + this.minHashLength)
      }
    }

    return ret_str
  }

  private def _decode(hash: String, inAlphabet: String): Seq[Long] = {
    var alphabet = inAlphabet
    var ret = List.empty[Long]

    var i = 0
    var hashBreakdown = hash.map(c => if (guards.contains(c)) ' ' else c)
    var hashArray = hashBreakdown.split(" ")

    if (hashArray.length == 3 || hashArray.length == 2) {
      i = 1
    }

    hashBreakdown = hashArray(i)

    val lottery = hashBreakdown(0)
    hashBreakdown = hashBreakdown.drop(1)
    hashBreakdown = hashBreakdown.map(c => if (seps.contains(c)) ' ' else c)
    hashArray = hashBreakdown.split(" ")

    for (aHashArray <- hashArray) {
      val subHash = aHashArray
      val buffer = lottery + this.salt + alphabet
      alphabet = Hashids.consistentShuffle(alphabet, buffer.take(alphabet.length))
      ret ::= Hashids.unhash(subHash, alphabet)
    }

    val seq = ret.reverse

    if (encode(seq: _*) == hash) seq else Seq.empty
  }

  def version: String = "1.0.0"
}
