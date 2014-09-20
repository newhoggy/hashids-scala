package org.hashids

import org.specs2.mutable._

class SpecHashids extends Specification {
  "One number should encode then decode" >> {
    val expected = "NkK9"
    val num_to_hash = 12345L
    val a = new Hashids("this is my salt")
    val res = a.encode(num_to_hash)
    val res2 = a.decode(expected)
    res must_== expected
    res2.length must_== 1
    res2(0) must_== num_to_hash
  }

  "Several numbers should encode then decode" >> {
    val expected = "aBMswoO2UB3Sj"
    val num_to_hash = Array[Long](683L, 94108L, 123L, 5L)
    val a = new Hashids("this is my salt")
    val res = a.encode(num_to_hash: _*)
    val res2 = a.decode(expected)
    res must_== expected
    res2.length must_== num_to_hash.length
    res2.toList must_== num_to_hash.toList
  }

  "One number should encode then decode with salt" >> {
    val expected = "gB0NV05e"
    val num_to_hash = 1L
    val a = new Hashids("this is my salt", 8)
    val res = a.encode(num_to_hash)
    val res2 = a.decode(expected)
    res must_== expected
    res2.length must_== 1
    res2(0) must_== num_to_hash
  }

  "Should be random" >> {
    val expected = "1Wc8cwcE"
    val num_to_hash = Array[Long](5L, 5L, 5L, 5L)
    val a = new Hashids("this is my salt")
    val res2 = a.decode(expected)
    val res = a.encode(num_to_hash: _*)
    res must_== expected
    res2.length must_== num_to_hash.length
    res2.toList must_== num_to_hash.toList
  }

  "Hash for sequence of consecutive numbers should appear random" >> {
    val expected = "kRHnurhptKcjIDTWC3sx"
    val num_to_hash = List[Long](1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
    val a = new Hashids("this is my salt")
    val res = a.encode(num_to_hash: _*)
    val res2 = a.decode(expected)
    res must_== expected
    res2.length must_== num_to_hash.length
    res2.toList must_== num_to_hash.toList
  }

  "Sequence of hashes for consecutive numbers should appear random" >> {
    val a = new Hashids("this is my salt")
    a.encode(1L) must_== "NV"
    a.encode(2L) must_== "6m"
    a.encode(3L) must_== "yD"
    a.encode(4L) must_== "2l"
    a.encode(5L) must_== "rD"
  }

  "Max long value should encode" >> {
    val a = new Hashids("this is my salt")
    a.encode(9876543210123L) must_== "Y8r7W1kNN"
  }

  "Special number encode and decode" >> {
    val expected = "3kK3nNOe"
    val num_to_hash = 75527867232L
    val a = new Hashids("this is my salt")
    val res = a.encode(num_to_hash)
    val res2 = a.decode(expected)
    res must_== expected
    res2.length must_== 1
    res2(0) must_== num_to_hash
  }
}
