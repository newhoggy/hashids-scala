package org.hashids

import org.hashids.syntax._
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

case class ZeroOrPosLong(value: Long)

case class Size(size: Int)

class CheckHashids extends Specification with ScalaCheck {
  import CheckHashids._

  "List of random zero or positive longs should encode then decode" in {
    prop { (a: List[ZeroOrPosLong], salt: String) =>
      implicit val hashid = Hashids.reference(salt)

      a.raw.hashid.unhashid ==== a.raw
    }
  }

  "List of random zero or positive longs should encode then decode" in {
    prop { (a: List[ZeroOrPosLong], salt: String) =>
      implicit val hashid = Hashids.reference(salt = salt)

      a.raw.hashid.unhashid ==== a.raw
    }
  }

  "List of random zero or positive longs should encode then decode and honour min hash length" in {
    prop { (a: List[ZeroOrPosLong], salt: String, minHashLength: Size) =>
      implicit val hashid = Hashids.reference(salt = salt, minHashLength = minHashLength.size)

      val hash = a.raw.hashid

      hash.unhashid ==== a.raw
      hash.length must be >= minHashLength.size when !a.isEmpty
    }
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
