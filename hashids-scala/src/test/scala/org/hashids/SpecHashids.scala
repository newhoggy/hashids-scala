package org.hashids

import org.hashids.syntax._
import org.scalacheck.Gen
import org.specs2.ScalaCheck
import org.specs2.mutable._

class SpecHashids extends Specification with ScalaCheck {
  "One number should encode then decode" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    val expected = "NkK9"
    val data = 12345L
    val hashed = data.hashid
    val unhashed = hashed.unhashid
    hashed must_== expected
    unhashed must_== List(data)
  }

  "Several numbers should encode then decode" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    val expected = "aBMswoO2UB3Sj"
    val data = Seq[Long](683L, 94108L, 123L, 5L)
    val hashed = data.hashid
    val unhashed = hashed.unhashid
    hashed must_== expected
    unhashed must_== data
  }

  "One number should encode then decode with salt" >> {
    implicit val hashids = Hashids.reference("this is my salt", 8)
    val expected = "gB0NV05e"
    val data = 1L
    val hashed = data.hashid
    val unhashed = hashed.unhashid
    hashed must_== expected
    unhashed must_== List(data)
  }

  "Require alphabet with at least 16 unique chars" in {
    Hashids.reference(
      salt = "this is my salt",
      alphabet = "1123467890abcde"
    ) must throwA[IllegalArgumentException](
      message = "alphabet must contain at least 16 unique characters")
  }

  "Deny spaces in alphabet" in {
    Hashids.reference(
      salt = "this is my salt",
      alphabet = "1234567890 abcdefghijklmnopqrstuvwxyz"
    ) must throwA[IllegalArgumentException](
      message = "alphabet cannot contains spaces")
  }

  "Should be random" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    val expected = "1Wc8cwcE"
    val data = List[Long](5L, 5L, 5L, 5L)
    val hashed = data.hashid
    val unhashed = hashed.unhashid
    hashed must_== expected
    unhashed must_== data
  }

  "Hash for sequence of consecutive numbers should appear random" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    val expected = "kRHnurhptKcjIDTWC3sx"
    val data = List[Long](1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
    val hashed = data.hashid
    val unhashed = hashed.unhashid
    hashed must_== expected
    unhashed must_== data
  }

  "Sequence of hashes for consecutive numbers should appear random" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    1L.hashid must_== "NV"
    2L.hashid must_== "6m"
    3L.hashid must_== "yD"
    4L.hashid must_== "2l"
    5L.hashid must_== "rD"
  }

  "Max long value should encode" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    9876543210123L.hashid must_== "Y8r7W1kNN"
  }

  "Special number encode and decode" >> {
    implicit val hashids = Hashids.reference("this is my salt")
    val expected = "3kK3nNOe"
    val data = 75527867232L
    val hashed = data.hashid
    val unhashed = hashed.unhashid
    hashed must_== expected
    unhashed must_== List(data)
  }

  "Decoding with different salt result's in empty list" >> {
    val hashid = Hashids.reference("this is my salt")
    val hash = hashid.encode(10L)

    Hashids.reference("different salt").decode(hash) must_== Nil
  }

  "encodeHex" >> {
    val hashids = Hashids.reference("this is my salt")

    "encodes hex string" >> {
      hashids.encodeHex("FA"         ) must_== "lzY"
      hashids.encodeHex("26dd"       ) must_== "MemE"
      hashids.encodeHex("FF1A"       ) must_== "eBMrb"
      hashids.encodeHex("12abC"      ) must_== "D9NPE"
      hashids.encodeHex("185b0"      ) must_== "9OyNW"
      hashids.encodeHex("17b8d"      ) must_== "MRWNE"
      hashids.encodeHex("1d7f21dd38" ) must_== "4o6Z7KqxE"
      hashids.encodeHex("20015111d"  ) must_== "ooweQVNB"
    }

    "throw if non-hex string passed" >> {
      hashids.encodeHex("XYZ123") must throwA[IllegalArgumentException](
        message = "Not a HEX string")
    }
  }

  "decodeHex" >> {
    val hashids = Hashids.reference("this is my salt")

    "decodes hex string" >> {
      hashids.decodeHex("lzY"    ) must_== "FA"
      hashids.decodeHex("eBMrb"  ) must_== "FF1A"
      hashids.decodeHex("D9NPE"  ) must_== "12ABC"
    }
  }

  "Should be compatible with Javascript reference implementation" >> {
    "for salt = 'MyCamelCaseSalt', max length = 10, alphabet = 'ABCDEFGHIJKLMNPQRSTUVWXYZ123456789'" >> {
      val hashids = Hashids.reference("MyCamelCaseSalt", 10, "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789")

      val raw = 1145
      val encoded = hashids.encode(raw)
      val decoded = hashids.decode(encoded)
      
      encoded ==== "9Q7MJ3LVGW"
      decoded ==== List(raw)
    }
  }

  "Invalid hash should not cause out of bounds exception" >> {
    prop { (hash: String) =>
      val hashids = Hashids.reference("This is my salt")
      hashids.decodeHex(hash)
      ok
    }.setGen(Gen.alphaStr)
  }
}
