package org.hashids

import org.hashids.syntax._
import org.scalacheck._
import org.specs2._

case class ZeroOrPosLong(value: Long)

case class Size(size: Int)

class CheckHashids extends org.specs2.Specification with org.specs2.ScalaCheck {
  import CheckHashids._

  def is = {
    "List of random zero or positive longs should encode then decode" ! {
      check { (a: List[ZeroOrPosLong], salt: String) =>
        implicit val hashid = Hashids(salt)

        a.raw.encodeHashid.decodeHashid must_== a.raw
      }
    } ^
    "List of random zero or positive longs should encode then decode" ! {
      check { (a: List[ZeroOrPosLong], salt: String) =>
        implicit val hashid = Hashids(salt = salt)

        a.raw.encodeHashid.decodeHashid must_== a.raw
      }
    } ^
    "List of random zero or positive longs should encode then decode and honour min hash length" ! {
      check { (a: List[ZeroOrPosLong], salt: String, minHashLength: Size) =>
        implicit val hashid = Hashids(salt = salt, minHashLength = minHashLength.size)

        val hash = a.raw.encodeHashid

        hash.decodeHashid must_== a.raw
        hash.length must be >= minHashLength.size when !a.isEmpty
      }
    } ^ end
  }
}

object CheckHashids {
  implicit val arbitraryZeroOrPosLong: Arbitrary[ZeroOrPosLong] = Arbitrary {
    Gen.chooseNum(0L, Long.MaxValue, 2L, 75527867232L).map(ZeroOrPosLong(_))
  }

  implicit val arbitrarySize: Arbitrary[Size] = Arbitrary {
    Gen.chooseNum(0, 50).map(Size(_))
  }

  implicit class RichListZeroOrPosLong(self: List[ZeroOrPosLong]) {
    def raw = self.map(_.value)
  }
}
