package org.hashids

import org.hashids.HashidsSyntax._
import org.scalacheck._
import org.specs2._

case class ZeroOrPosLong(value: Long)

class CheckHashids extends org.specs2.Specification with org.specs2.ScalaCheck {
  import CheckHashids.arbitraryZeroOrPosLong

  def is = {
    "List of random zero or positive longs should encode then decode" ! {
      check { (a: List[ZeroOrPosLong], salt: String) =>
        implicit val hashid = Hashids(salt)

        a.raw.toHashid.fromHashid must_== a.raw
      }
    } ^ {
      end
    }
  }

  implicit class RichListZeroOrPosLong(self: List[ZeroOrPosLong]) {
    def raw = self.map(_.value)
  }
}

object CheckHashids {
  implicit val arbitraryZeroOrPosLong: Arbitrary[ZeroOrPosLong] = Arbitrary {
    Gen.chooseNum(0L, Long.MaxValue, 2L, 75527867232L).map(ZeroOrPosLong(_))
  }
}
