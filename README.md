# Hashids.scala [![Circle CI](https://circleci.com/gh/newhoggy/hashids-scala.svg?style=svg)](https://circleci.com/gh/newhoggy/hashids-scala)

A small Scala library to generate YouTube-like hashes from one or many numbers.

Ported from Java [hashids-java](https://github.com/jiecao-fm/hashids-java) by [John Ky](https://github.com/newhoggy)

**NOTE** This project is moving to https://github.com/pico-works/pico-hashids

For the latest version, please go there instead.

## What is it?

hashids (Hash ID's) creates short, unique, decryptable hashes from unsigned (long) integers.

It was designed for websites to use in URL shortening, tracking stuff, or making pages private (or at least unguessable).

This algorithm tries to satisfy the following requirements:

1. Hashes must be unique and decryptable.
2. They should be able to contain more than one integer (so you can use them in complex or clustered systems).
3. You should be able to specify minimum hash length.
4. Hashes should not contain basic English curse words (since they are meant to appear in public places - like the URL).

Instead of showing items as `1`, `2`, or `3`, you could show them as `U6dc`, `u87U`, and `HMou`.
You don't have to store these hashes in the database, but can encrypt + decrypt on the fly.

All (long) integers need to be greater than or equal to zero.

## Getting hashids-scala

`hashids-scala` is currently under development.

If you're using SBT, add the following lines to your build file:

    resolvers += "dl-john-ky" at "http://dl.john-ky.io/maven/releases"

    libraryDependencies += "io.john-ky" %% "hashids-scala" % "1.1.2-2974446"

## Compatibility note

In version `1.1.0`, the library contained a bug that caused it to generate hashids that were incompatibility with the
Javascript reference implementation under some situations.  Applications upgrading from `1.1.0` to `1.1.1` will need to
decide which of cross-language compatibility or backwards compatibility is more important.

To ensure that the application writers make this decision carefully, the `Hashids.apply` method is deprecated and will
now throw `NotImplementedException`.

Application writers are must replace calls to `Hashids.apply` with one of the following:
 
* `Hashids.legacyJiecao` - if backwards compatibility is important.
* `Hashids.reference` - if cross-language compatibility is important.

## Usage

#### Import the package

```scala
import org.hashids._
```

#### Encrypting one number

You can pass a unique salt value so your hashes differ from everyone else's.  "this is my salt" is used as an example.

```scala

val hashids = Hashids.reference("this is my salt")
val hash = hashids.encode(12345L)
```

`hash` is now going to be:

    "NkK9"

#### Decrypting

Notice during decryption, same salt value is used:

```scala

val hashids = Hashids.reference("this is my salt")
val numbers = hashids.decode("NkK9")
```

`numbers` is now going to be:

    List(12345L): Seq[Long]

#### Decrypting with different salt

Decryption will not work if salt is changed:

```scala

val hashids = Hashids.reference("this is my pepper")
val numbers = hashids.decode("NkK9")
```

`numbers` is now going to be:

    List(): Seq[Long]

#### Encrypting several numbers

```scala

val hashids = Hashids.reference("this is my salt")
val hash = hashids.encode(683L, 94108L, 123L, 5L)
```

`hash` is now going to be:

    "aBMswoO2UB3Sj"

#### Decrypting is done the same way

```scala

val hashids = Hashids.reference("this is my salt")
val numbers = hashids.decode("aBMswoO2UB3Sj")
```

`numbers` is now going to be:

    List(683L, 94108L, 123L, 5L): Seq[Long]

#### Encrypting and specifying minimum hash length

Here we encrypt integer 1, and set the minimum hash length to **8** (by default it's **0** -- meaning hashes will be the shortest possible length).

```scala

val hashids = Hashids.reference("this is my salt", 8)
val hash = hashids.encode(1L)
```

`hash` is now going to be:

    "gB0NV05e"

#### Decrypting

```scala

val hashids = Hashids.reference("this is my salt", 8)
val numbers = hashids.decode("gB0NV05e")
```

`numbers` is now going to be:

    List(1L): Seq[Long]

#### Specifying custom hash alphabet

Here we set the alphabet to consist of only four letters: "0123456789abcdef"

```scala

val hashids = Hashids.reference("this is my salt", 0, "0123456789abcdef")
val hash = hashids.encode(1234567L)
```

`hash` is now going to be:

    "b332db5"

## Randomness

The primary purpose of hashids is to obfuscate ids. It's not meant or tested to be used for security purposes or compression.
Having said that, this algorithm does try to make these hashes unguessable and unpredictable:

#### Repeating numbers

```scala

val hashids = Hashids.reference("this is my salt")
val hash = hashids.encode(5L, 5L, 5L, 5L)
```

You don't see any repeating patterns that might show there's 4 identical numbers in the hash:

    "1Wc8cwcE"

Same with incremented numbers:

```scala

val hashids = Hashids.reference("this is my salt")
val hash = hashids.encode(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
```

`hash` will be :

    "kRHnurhptKcjIDTWC3sx"

### Incrementing number hashes:

```scala

val hashids = Hashids.reference("this is my salt")
val hash1 = hashids.encode(1L) /* NV */
val hash2 = hashids.encode(2L) /* 6m */
val hash3 = hashids.encode(3L) /* yD */
val hash4 = hashids.encode(4L) /* 2l */
val hash5 = hashids.encode(5L) /* rD */
```

## Implicit Scala Syntax

`hashids-scala` also supports Scala idiomatic syntax for obtaining the hashids codec object from
implicit scope.  Import `org.hashids.Hashids._` to enable this support.

In the following examples, the `Long` and `Seq[Long]` is lifted to support the
`hashid` method while `String` is lifted to support the `unhashid`, `hashidHex` and `unhashidHex` methods.

```scala
import org.hashids.Hashids
import org.hashids.syntax._

implicit val hashids = Hashids.reference("this is my salt")
val hash1 = 12345L.hashid
val hash2 = List(1L, 2L, 3L).hashid
val unhashed = "NkK9".unhashid
```

In future the library will be updated to implement this syntax using type traits.

## Bad hashes

This library was written with the intent of placing these hashes in visible places - like the URL. If a unique hash is created for each user, it would be unfortunate if the hash ended up accidentally being a bad word. Imagine auto-creating a URL with hash for your user that looks like this - `http://example.com/user/a**hole`

Therefore, this algorithm tries to avoid generating most common English curse words with the default alphabet. This is done by never placing the following letters next to each other:

    c, C, s, S, f, F, h, H, u, U, i, I, t, T

## Scaladoc
[Scala API docs](http://newhoggy.github.io/hashids/api/scala_2.11/1.0.0)

## Contact

Follow me [@newhoggy](https://twitter.com/newhoggy) or [@IvanAkimov](http://twitter.com/ivanakimov)

## License

MIT License. See the `LICENSE` file.
