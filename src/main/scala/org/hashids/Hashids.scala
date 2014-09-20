package org.hashids

object Hashids {
  val defaultAlphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"

  def apply(
      salt: String = "",
      inMinHashLength: Int = 0,
      inAlphabet: String = Hashids.defaultAlphabet): Hashids = {
    var seps: String = "cfhistuCFHISTU"
    val sepDiv: Double = 3.5
    val guardDiv: Int = 12
    val minAlphabetLength: Int = 16
    var guards: String = ""

    val minHashLength = inMinHashLength max 0

    var alphabet = {
      var uniqueAlphabet: String = ""

      for (i <- 0 until inAlphabet.length) {
        if (!uniqueAlphabet.contains("" + inAlphabet(i))) {
          uniqueAlphabet += "" + inAlphabet(i)
        }
      }

      uniqueAlphabet
    }

    if (alphabet.length < minAlphabetLength) {
      throw new IllegalArgumentException(s"alphabet must contain at least $minAlphabetLength unique characters")
    }

    if (alphabet.contains(" ")) {
      throw new IllegalArgumentException("alphabet cannot contains spaces")
    }

    // seps should contain only characters present in alphabet
    // alphabet should not contains seps
    for (i <- 0 until seps.length) {
      val j = alphabet.indexOf(seps(i))

      if (j == -1) {
        seps = seps.substring(0, i) + " " + seps.substring(i + 1)
      } else {
        alphabet = alphabet.substring(0, j) + " " + alphabet.substring(j + 1)
      }
    }

    alphabet = alphabet.replaceAll("\\s+", "")
    seps = seps.replaceAll("\\s+", "")
    seps = consistentShuffle(seps, salt)

    if (seps == "" || (alphabet.length / seps.length) > sepDiv) {
      var seps_len: Int = (alphabet.length / sepDiv).ceil.toInt

      if (seps_len == 1) {
        seps_len += 1
      }

      if (seps_len > seps.length) {
        val diff = seps_len - seps.length
        seps += alphabet.substring(0, diff)
        alphabet = alphabet.substring(diff)
      } else {
        seps = seps.substring(0, seps_len)
      }
    }

    alphabet = consistentShuffle(alphabet, salt)

    // use double to round up
    val guardCount = (alphabet.length.toDouble / guardDiv).ceil.toInt

    if (alphabet.length < 3) {
      guards = seps.substring(0, guardCount)
      seps = seps.substring(guardCount)
    } else {
      guards = alphabet.substring(0, guardCount)
      alphabet = alphabet.substring(guardCount)
    }

    new Hashids(
        salt,
        minHashLength,
        alphabet,
        seps,
        guards)
  }

  /* Private methods */
  private def consistentShuffle(inAlphabet: String, salt: String): String = {
    if (salt.length <= 0) {
      return inAlphabet
    }

    var alphabet = inAlphabet
    val arr = salt.toCharArray()
    var v = 0
    var p = 0

    for (i <- (alphabet.length - 1) until 0 by -1) {
      v %= salt.length
      val asc_val = arr(v).toInt
      p += asc_val
      val j = (asc_val + v + p) % i

      val tmp = alphabet.charAt(j)
      alphabet = alphabet.substring(0, j) + alphabet.charAt(i) + alphabet.substring(j + 1)
      alphabet = alphabet.substring(0, i) + tmp + alphabet.substring(i + 1)

      v += 1
    }

    return alphabet
  }

  private def hash(inInput: Long, alphabet: String): String = {
    var hash = ""
    val alphabetLen = alphabet.length
    val arr = alphabet.toCharArray()
    var input = inInput

    do {
      hash = arr((input % alphabetLen).toInt) + hash
      input /= alphabetLen
    } while (input > 0)

    return hash
  }

  private def unhash(input: String, alphabet: String): Long = {
    val input_arr = input.toCharArray()

    var number = 0L

    for (i <- 0 until input.length) {
      val pos = alphabet.indexOf(input_arr(i))
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
   * Encrypt numbers to string
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
   * Decrypt string to numbers
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
    val ret = alphabet.toCharArray()(numberHashInt % alphabet.length)
    val lottery = ret
    var ret_str = ret + ""

    for (i <- 0 until numbers.length) {
      var num = numbers(i)
      val buffer = lottery + this.salt + alphabet

      alphabet = Hashids.consistentShuffle(alphabet, buffer.substring(0, alphabet.length))
      val last = Hashids.hash(num, alphabet)

      ret_str += last

      if (i + 1 < numbers.length) {
        num %= (last.toCharArray()(0) + i).toInt
        val sepsIndex = (num % this.seps.length).toInt
        ret_str += this.seps.toCharArray()(sepsIndex)
      }
    }

    if (ret_str.length < this.minHashLength) {
      var guardIndex = (numberHashInt + ret_str.toCharArray()(0).toInt) % this.guards.length
      var guard = this.guards.toCharArray()(guardIndex)

      ret_str = guard + ret_str

      if (ret_str.length < this.minHashLength) {
        guardIndex = (numberHashInt + ret_str.toCharArray()(2).toInt) % this.guards.length
        guard = this.guards.toCharArray()(guardIndex)

        ret_str += guard
      }
    }

    val halfLen = alphabet.length / 2
    while (ret_str.length < this.minHashLength) {
      alphabet = Hashids.consistentShuffle(alphabet, alphabet)
      ret_str = alphabet.substring(halfLen) + ret_str + alphabet.substring(0, halfLen)
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
    val ret = new java.util.ArrayList[Long]()

    var i = 0
    val regexp = "[" + this.guards + "]"
    var hashBreakdown = hash.replaceAll(regexp, " ")
    var hashArray = hashBreakdown.split(" ")

    if (hashArray.length == 3 || hashArray.length == 2) {
      i = 1
    }

    hashBreakdown = hashArray(i)

    val lottery = hashBreakdown.toCharArray()(0)
    hashBreakdown = hashBreakdown.substring(1)
    hashBreakdown = hashBreakdown.replaceAll("[" + this.seps + "]", " ")
    hashArray = hashBreakdown.split(" ")

    for (aHashArray <- hashArray) {
      val subHash = aHashArray
      val buffer = lottery + this.salt + alphabet
      alphabet = Hashids.consistentShuffle(alphabet, buffer.substring(0, alphabet.length))
      ret.add(Hashids.unhash(subHash, alphabet))
    }

    // Transform from List<Long> to long[]
    val arr = new Array[Long](ret.size)

    for (k <- 0 until arr.length) {
      arr(k) = ret.get(k)
    }

    return arr
  }

  /**
   * Get version
   *
   * @return version
   */
  def version: String = "1.0.0"
}
