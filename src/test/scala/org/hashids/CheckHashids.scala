package org.hashids

import org.specs2._
import org.scalacheck._

case class ZeroOrPosLong(value: Long)

class CheckHashids extends org.specs2.Specification with org.specs2.ScalaCheck {
  import CheckHashids.arbitraryZeroOrPosLong

  implicit val hashid = Hashids("salt")

  def is = {
    "List of random zero or positive longs should encode then decode" ! check { (a: List[ZeroOrPosLong]) => a.raw.toHashid.fromHashid must_== a.raw } ^ end
  }

  implicit class RichListZeroOrPosLong(self: List[ZeroOrPosLong]) {
    def raw = self.map(_.value)
  }

  implicit class RichListInt(self: List[Long]) {
    def toHashid(implicit hashids: Hashids): String = {
      hashids.encode(self: _*)
    }
  }

  implicit class RichString(self: String) {
    def fromHashid(implicit hashids: Hashids): List[Long] = {
      hashids.decode(self).toList
    }
  }
}

object CheckHashids {
  implicit val arbitraryZeroOrPosLong: Arbitrary[ZeroOrPosLong] = Arbitrary {
    Gen.chooseNum(0L, Long.MaxValue).map(ZeroOrPosLong(_))
  }
}
