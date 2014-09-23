package org.hashids

package object syntax {
  implicit class RichStringForHashids(val self: String) extends AnyVal {
    def fromHashid(implicit hashids: Hashids): List[Long] = hashids.decode(self).toList
  }

  implicit class RichSeqLongForHashids(val self: Seq[Long]) extends AnyVal {
    def toHashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class RichArrayLongForHashids(val self: Array[Long]) extends AnyVal {
    def toHashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class RichLongForHashids(val self: Long) extends AnyVal {
    def toHashid(implicit hashids: Hashids): String = hashids.encode(self)
  }
}
