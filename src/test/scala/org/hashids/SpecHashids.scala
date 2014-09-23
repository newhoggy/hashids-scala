package org.hashids

import org.hashids.syntax._
import org.specs2.mutable._
import org.scalacheck._

class SpecHashids extends Specification {
  "One number should encode then decode" >> {
    implicit val hashids = Hashids("this is my salt")
    val expected = "NkK9"
    val data = 12345L
    val hashed = data.toHashid
    val unhashed = hashed.fromHashid
    hashed must_== expected
    unhashed must_== List(data)
  }

  "Several numbers should encode then decode" >> {
    implicit val hashids = Hashids("this is my salt")
    val expected = "aBMswoO2UB3Sj"
    val data = List[Long](683L, 94108L, 123L, 5L)
    val hashed = data.toHashid
    val unhashed = hashed.fromHashid
    hashed must_== expected
    unhashed must_== data
  }

  "One number should encode then decode with salt" >> {
    implicit val hashids = Hashids("this is my salt", 8)
    val expected = "gB0NV05e"
    val data = 1L
    val hashed = data.toHashid
    val unhashed = hashed.fromHashid
    hashed must_== expected
    unhashed must_== List(data)
  }

  "Characters should not be disallowed in sep just because they happend to have special meaning in regexes" >> {
    implicit val hashids = Hashids(seps = "[asdf")
    val data = 1L
    val hashed = data.toHashid
    val unhashed = hashids.decode(hashed)
    unhashed must_== List(data)
  }

  "Should be random" >> {
    implicit val hashids = Hashids("this is my salt")
    val expected = "1Wc8cwcE"
    val data = List[Long](5L, 5L, 5L, 5L)
    val hashed = data.toHashid
    val unhashed = hashed.fromHashid
    hashed must_== expected
    unhashed must_== data
  }

  "Hash for sequence of consecutive numbers should appear random" >> {
    implicit val hashids = Hashids("this is my salt")
    val expected = "kRHnurhptKcjIDTWC3sx"
    val data = List[Long](1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
    val hashed = data.toHashid
    val unhashed = hashed.fromHashid
    hashed must_== expected
    unhashed must_== data
  }

  "Sequence of hashes for consecutive numbers should appear random" >> {
    implicit val hashids = Hashids("this is my salt")
    1L.toHashid must_== "NV"
    2L.toHashid must_== "6m"
    3L.toHashid must_== "yD"
    4L.toHashid must_== "2l"
    5L.toHashid must_== "rD"
  }

  "Max long value should encode" >> {
    implicit val hashids = Hashids("this is my salt")
    9876543210123L.toHashid must_== "Y8r7W1kNN"
  }

  "Special number encode and decode" >> {
    implicit val hashids = Hashids("this is my salt")
    val expected = "3kK3nNOe"
    val data = 75527867232L
    val hashed = data.toHashid
    val unhashed = hashed.fromHashid
    hashed must_== expected
    unhashed must_== List(data)
  }
}
