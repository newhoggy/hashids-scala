package org.hashids

object Hashids {
  val DEFAULT_ALPHABET: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
}

class Hashids(
    var salt: String = "",
    var minHashLength: Int = 0,
    var alphabet: String = Hashids.DEFAULT_ALPHABET) {
  var seps: String = "cfhistuCFHISTU"
  var sepDiv: Double = 3.5
  var guardDiv: Int = 12
  var minAlphabetLength: Int = 16
  var guards: String = ""

  minHashLength = minHashLength max 0

  alphabet = {
    var uniqueAlphabet: String = ""

    for (i <- 0 until alphabet.length) {
      if (!uniqueAlphabet.contains("" + alphabet(i))) {
        uniqueAlphabet += "" + alphabet(i)
      }
    }

    uniqueAlphabet
  }

  if (alphabet.length < this.minAlphabetLength) {
    throw new IllegalArgumentException(s"alphabet must contain at least $minAlphabetLength unique characters")
  }

  if (alphabet.contains(" ")) {
    throw new IllegalArgumentException("alphabet cannot contains spaces");
  }

  // seps should contain only characters present in alphabet;
  // alphabet should not contains seps
  for (i <- 0 until seps.length) {
    val j = alphabet.indexOf(seps(i))

    if (j == -1) {
      seps = seps.substring(0, i) + " " + seps.substring(i + 1)
    } else {
      alphabet = alphabet.substring(0, j) + " " + alphabet.substring(j + 1)
    }
  }

  this.alphabet = this.alphabet.replaceAll("\\s+", "");
  this.seps = this.seps.replaceAll("\\s+", "");
  this.seps = this.consistentShuffle(this.seps, this.salt);

  if ((this.seps.equals("")) || ((this.alphabet.length() / this.seps.length()) > this.sepDiv)) {
    var seps_len: Int = (this.alphabet.length() / this.sepDiv).ceil.toInt

    if (seps_len == 1) {
      seps_len += 1
    }

    if (seps_len > this.seps.length()) {
      val diff = seps_len - this.seps.length()
      this.seps += this.alphabet.substring(0, diff)
      this.alphabet = this.alphabet.substring(diff)
    } else {
      this.seps = this.seps.substring(0, seps_len)
    }
  }

  this.alphabet = this.consistentShuffle(this.alphabet, this.salt);

  // use double to round up
  val guardCount = (this.alphabet.length.toDouble / this.guardDiv).ceil.toInt

  if (this.alphabet.length() < 3) {
    this.guards = this.seps.substring(0, guardCount)
    this.seps = this.seps.substring(guardCount)
  } else {
    this.guards = this.alphabet.substring(0, guardCount)
    this.alphabet = this.alphabet.substring(guardCount)
  }

  /**
   * Encrypt numbers to string
   *
   * @param numbers the numbers to encrypt
   * @return the encrypt string
   */
  def encode(numbers: Long*): String = {
    var retval = ""

    if (numbers.length == 0) {
      return retval
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
    if (hash.equals(""))
      return Seq.empty;

    return this._decode(hash, this.alphabet)
  }

  /**
   * Encrypt hexa to string
   *
   * @param hexa the hexa to encrypt
   * @return the encrypt string
   */
  def encodeHex(hexa: String): String = {
      if (!hexa.matches("^[0-9a-fA-F]+$"))
        return ""

      val matched = new java.util.ArrayList[Long]
      val matcher = java.util.regex.Pattern.compile("[\\w\\W]{1,12}").matcher(hexa);

      while (matcher.find())
          matched.add(java.lang.Long.parseLong("1" + matcher.group(), 16))

      // conversion
      val result = new Array[Long](matched.size)

      for (i <- 0 until matched.size) {
        result(i) = matched.get(i)
      }

      return this._encode(result: _*)
  }

  /**
   * Decrypt string to numbers
   *
   * @param hash the encrypt string
   * @return decryped numbers
   */
  def decodeHex(hash: String): String = {
    var result = ""
    val numbers = this.decode(hash)

    for (number <- numbers) {
      result += java.lang.Long.toHexString(number).substring(1)
    }

    return result;
  }

  private def _encode(numbers: Long*): String = {
    var numberHashInt = 0

    for (i <- 0 until numbers.size) {
      numberHashInt += (numbers(i) % (i + 100)).toInt
    }

    var alphabet = this.alphabet
    val ret = alphabet.toCharArray()(numberHashInt % alphabet.length())
    val lottery = ret
    var num = 0L
    var sepsIndex = 0
    var guardIndex = 0
    var buffer = ""
    var ret_str = ret + ""
    var guard: Char = '\0'

    for (i <- 0 until numbers.length) {
      num = numbers(i)
      buffer = lottery + this.salt + alphabet;

      alphabet = this.consistentShuffle(alphabet, buffer.substring(0, alphabet.length()));
      val last = this.hash(num, alphabet)

      ret_str += last;

      if(i + 1 < numbers.length){
        num %= (last.toCharArray()(0) + i).toInt
        sepsIndex = (num % this.seps.length()).toInt
        ret_str += this.seps.toCharArray()(sepsIndex)
      }
    }

    if(ret_str.length() < this.minHashLength){
      guardIndex = (numberHashInt + (ret_str.toCharArray()(0)).toInt) % this.guards.length();
      guard = this.guards.toCharArray()(guardIndex)

      ret_str = guard + ret_str;

      if(ret_str.length() < this.minHashLength){
        guardIndex = (numberHashInt + (ret_str.toCharArray()(2)).toInt) % this.guards.length();
        guard = this.guards.toCharArray()(guardIndex)

        ret_str += guard;
      }
    }

    val halfLen = alphabet.length() / 2;
    while(ret_str.length() < this.minHashLength){
      alphabet = this.consistentShuffle(alphabet, alphabet);
      ret_str = alphabet.substring(halfLen) + ret_str + alphabet.substring(0, halfLen);
      val excess = ret_str.length() - this.minHashLength;
      if (excess > 0) {
        val start_pos = excess / 2;
        ret_str = ret_str.substring(start_pos, start_pos + this.minHashLength);
      }
    }

    return ret_str;
  }

  private def _decode(hash: String, inAlphabet: String): Seq[Long] = {
    var alphabet = inAlphabet
    val ret = new java.util.ArrayList[Long]()

    var i = 0
    val regexp = "[" + this.guards + "]"
    var hashBreakdown = hash.replaceAll(regexp, " ")
    var hashArray = hashBreakdown.split(" ")

    if(hashArray.length == 3 || hashArray.length == 2){
      i = 1;
    }

    hashBreakdown = hashArray(i)

    val lottery = hashBreakdown.toCharArray()(0)
    hashBreakdown = hashBreakdown.substring(1)
    hashBreakdown = hashBreakdown.replaceAll("[" + this.seps + "]", " ")
    hashArray = hashBreakdown.split(" ")

    var subHash = ""
    var buffer = ""

    for (aHashArray <- hashArray) {
      subHash = aHashArray
      buffer = lottery + this.salt + alphabet
      alphabet = this.consistentShuffle(alphabet, buffer.substring(0, alphabet.length()))
      ret.add(this.unhash(subHash, alphabet))
    }

    // Transform from List<Long> to long[]
    val arr = new Array[Long](ret.size)

    for (k <- 0 until arr.length) {
      arr(k) = ret.get(k)
    }

    return arr
  }

  /* Private methods */
  private def consistentShuffle(inAlphabet: String, salt: String): String = {
    if (salt.length() <= 0) {
      return inAlphabet
    }

    var alphabet = inAlphabet
    val arr = salt.toCharArray()
    var asc_val = 0
    var j = 0
    var tmp = '\0'

    var v = 0
    var p = 0

    for (i <- (alphabet.length - 1) until 0 by -1) {
      v %= salt.length();
      asc_val = arr(v).toInt
      p += asc_val;
      j = (asc_val + v + p) % i;

      tmp = alphabet.charAt(j);
      alphabet = alphabet.substring(0, j) + alphabet.charAt(i) + alphabet.substring(j + 1);
      alphabet = alphabet.substring(0, i) + tmp + alphabet.substring(i + 1);

      v += 1
    }

    return alphabet;
  }

  private def hash(inInput: Long, alphabet: String): String = {
    var hash = ""
    val alphabetLen = alphabet.length()
    val arr = alphabet.toCharArray()
    var input = inInput

    do {
      hash = arr((input % alphabetLen).toInt) + hash
      input /= alphabetLen
    } while (input > 0)

    return hash
  }

  private def unhash(input: String, alphabet: String): Long = {
    var number = 0L
    var pos = 0L
    val input_arr = input.toCharArray()

    for (i <- 0 until input.length) {
      pos = alphabet.indexOf(input_arr(i))
      number += (pos * scala.math.pow(alphabet.length(), input.length() - i - 1)).toLong
    }

    return number;
  }

  def checkedCast(value: Long): Int = {
    var result = value.toInt

    if (result != value) {
      // don't use checkArgument here, to avoid boxing
      throw new IllegalArgumentException(s"Out of range: $value")
    }

    return result
  }

  /**
   * Get version
   *
   * @return version
   */
  def getVersion(): String = {
    return "1.0.0"
  }
}
