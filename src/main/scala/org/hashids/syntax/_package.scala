package org.hashids

package object syntax {
  implicit class RichStringForHashids(val self: String) extends AnyVal {
    def fromHashid(implicit hashids: Hashids): List[Long] = hashids.decode(self).toList
  }

  implicit class RichListLongForHashids(val self: List[Long]) extends AnyVal {
    def toHashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class RichLongForHashids(val self: Long) extends AnyVal {
    def toHashid(implicit hashids: Hashids): String = hashids.encode(self)
  }
}
