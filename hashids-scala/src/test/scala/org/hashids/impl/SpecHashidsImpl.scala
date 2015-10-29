package org.hashids.impl

import org.hashids.syntax._
import org.specs2.mutable._
import org.hashids.impl._

class SpecHashidsImpl extends Specification {
  "Should be compatible with Javascript reference implementation" >> {
    "for salt = 'MyCamelCaseSalt', max length = 10, alphabet = 'ABCDEFGHIJKLMNPQRSTUVWXYZ123456789'" >> {
      val hashids = HashidsImpl(
        salt = "MyCamelCaseSalt",
        minHashLength = 10,
        effectiveAlphabet = "97ZP3LE8W5QKD4XRY21BVNJ",
        seps = "FUITHCSA",
        guards = "G6M")

      val raw = 1145
      val encoded = hashids.encode(raw)
      val decoded = hashids.decode(encoded)
      
      encoded ==== "9Q7MJ3LVGW"
      decoded ==== List(raw)
    }
  }
}
