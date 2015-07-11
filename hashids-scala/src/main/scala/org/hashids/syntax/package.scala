package org.hashids

package object syntax {
  implicit class HashidsLongOps(x: Long) {
    def hashid(implicit hashids: Hashids): String = hashids.encode(x)
  }

  implicit class HashidsSeqLongOps(val self: Seq[Long]) {
    def hashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class HashidsArrayLongOps(val self: Array[Long]) {
    def encodeHashid(implicit hashids: Hashids): String = hashids.encode(self: _*)
  }

  implicit class HashidsStringOps(x: String) {
    def unhashid(implicit hashids: Hashids): Seq[Long] = hashids.decode(x)

    def hashidHex(implicit hashids: Hashids): String = hashids.encodeHex(x)

    def unhashidHex(implicit hashids: Hashids): String = hashids.decodeHex(x)
  }
}
