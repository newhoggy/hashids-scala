package org.hashids

import org.specs2.mutable._

class SpecHashids extends Specification {
  "One number should encode then decode" >> {
    var a: Hashids = null
    var expected = "NkK9"
    var res = ""
    val num_to_hash = 12345L;
    var res2: Seq[Long] = null;
    try {
      a = new Hashids("this is my salt");
    } catch {
      case e: Exception => e.printStackTrace()
    }
    res = a.encode(num_to_hash);
    res must_== expected
    res2 = a.decode(expected);
    res2.length must_== 1
    res2(0) must_== num_to_hash
  }

  "Several numbers should encode then decode" >> {
    var a: Hashids = null
    val expected = "aBMswoO2UB3Sj"
    var res = ""
    val num_to_hash = Array[Long](683L, 94108L, 123L, 5L)
    var res2: Seq[Long] = null
    try {
      a = new Hashids("this is my salt");
    } catch {
      case e: Exception => e.printStackTrace()
    }
    res = a.encode(num_to_hash: _*)
    res must_== expected
    res2 = a.decode(expected);
    res2.length must_== num_to_hash.length
    res2.toList must_== num_to_hash.toList
  }

  "One number should encode then decode with salt" >> {
    var a: Hashids = null
    val expected = "gB0NV05e"
    var res = ""
    val num_to_hash = 1L
    var res2: Seq[Long] = null
    try {
      a = new Hashids("this is my salt", 8);
    } catch {
      case e: Exception => e.printStackTrace()
    }
    res = a.encode(num_to_hash);
    res must_== expected
    res2 = a.decode(expected);
    res2.length must_== 1
    res2(0) must_== num_to_hash
  }

  "Should be random" >> {
    var a: Hashids = null
    val expected = "1Wc8cwcE"
    var res = ""
    val num_to_hash = Array[Long](5L, 5L, 5L, 5L)
    var res2: Seq[Long] = null
    try {
      a = new Hashids("this is my salt");
    } catch {
      case e: Exception => e.printStackTrace()
    }
    res = a.encode(num_to_hash: _*);
    res must_== expected
    res2 = a.decode(expected);
    res2.length must_== num_to_hash.length
    res2.toList must_== num_to_hash.toList
  }

  "Hash for sequence of consecutive numbers should appear random" >> {
    var a: Hashids = null
    val expected = "kRHnurhptKcjIDTWC3sx"
    var res = "";
    val num_to_hash = List[Long](1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
    var res2: Seq[Long] = null
    try {
      a = new Hashids("this is my salt")
    } catch {
      case e: Exception => e.printStackTrace()
    }
    res = a.encode(num_to_hash: _*)
    res must_== expected
    res2 = a.decode(expected);
    res2.length must_== num_to_hash.length
    res2.toList must_== num_to_hash.toList
  }

  "Sequence of hashes for consecutive numbers should appear random" >> {
    var a: Hashids = null
    try {
      a = new Hashids("this is my salt");
    } catch {
      case e: Exception => e.printStackTrace()
    }
    a.encode(1L) must_== "NV"
    a.encode(2L) must_== "6m"
    a.encode(3L) must_== "yD"
    a.encode(4L) must_== "2l"
    a.encode(5L) must_== "rD"
  }

  "Max long value should encode" >> {
    var a = new Hashids("this is my salt")

    a.encode(9876543210123L) must_== "Y8r7W1kNN"
  }

  "Special number encode and decode" >> {
    val expected = "3kK3nNOe"
    val num_to_hash = 75527867232L
    val a = new Hashids("this is my salt");
    val res = a.encode(num_to_hash);
    res must_== expected
    val res2 = a.decode(expected);
    res2.length must_== 1
    res2(0) must_== num_to_hash
  }
}
