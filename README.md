# Hashids.scala ![aaa](http://img.shields.io/badge/hashids--scala-0.3.3-ff69b4.svg)  [![Stories in Ready](https://badge.waffle.io/newhoggy/hashids-scala.png?label=ready&title=Ready)](https://waffle.io/newhoggy/hashids-scala) [![Build Status](https://drone.io/github.com/newhoggy/hashids-scala/status.png)](https://drone.io/github.com/newhoggy/hashids-scala/latest)

A small Scala library to generate YouTube-like hashes from one or many numbers.

Ported from Java [hashids-java](https://github.com/jiecao-fm/hashids-java) by [John Ky](https://github.com/newhoggy)

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

## Usage

#### Import the package

```scala
import org.hashids._
import org.hashids.syntax._
```

#### Encrypting one number

You can pass a unique salt value so your hashes differ from everyone else's.  "this is my salt" is used as an example.

```scala

implicit val hashids = Hashids("this is my salt")
val hash = 12345L.toHashid
```

`hash` is now going to be:

	NkK9

#### Decrypting

Notice during decryption, same salt value is used:

```scala

implicit val hashids = Hashids("this is my salt")
val numbers = "NkK9".fromHashid
```

`numbers` is now going to be:

	List(12345)

#### Decrypting with different salt

Decryption will not work if salt is changed:

```scala

implicit val hashids = Hashids("this is my pepper")
val numbers = "NkK9".fromHashid
```

`numbers` is now going to be:

	List() // WARNING: currently not working!

#### Encrypting several numbers

```scala

implicit val hashids = Hashids("this is my salt")
val hash = List(683L, 94108L, 123L, 5L).toHashid
```

`hash` is now going to be:

	"aBMswoO2UB3Sj"

#### Decrypting is done the same way

```scala

implicit val hashids = Hashids("this is my salt")
val numbers = "aBMswoO2UB3Sj".fromHashid
```

`numbers` is now going to be:

	List(683, 94108, 123, 5)

#### Encrypting and specifying minimum hash length

Here we encrypt integer 1, and set the minimum hash length to **8** (by default it's **0** -- meaning hashes will be the shortest possible length).

```scala

implicit val hashids = Hashids("this is my salt", 8)
val hash = 1L.toHashid
```

`hash` is now going to be:

	"gB0NV05e"

#### Decrypting

```scala

implicit val hashids = Hashids("this is my salt", 8)
val numbers = "gB0NV05e".fromHashid
```

`numbers` is now going to be:

	List(1)

#### Specifying custom hash alphabet

Here we set the alphabet to consist of only four letters: "0123456789abcdef"

```scala

implicit val hashids = Hashids("this is my salt", 0, "0123456789abcdef")
val hash = 1234567L.toHashid
```

`hash` is now going to be:

	"b332db5"

## Randomness

The primary purpose of hashids is to obfuscate ids. It's not meant or tested to be used for security purposes or compression.
Having said that, this algorithm does try to make these hashes unguessable and unpredictable:

#### Repeating numbers

```scala

implicit val hashids = Hashids("this is my salt")
val hash = List(5L, 5L, 5L, 5L).toHashid
```

You don't see any repeating patterns that might show there's 4 identical numbers in the hash:

	1Wc8cwcE

Same with incremented numbers:

```scala

implicit val hashids = Hashids("this is my salt")
val hash = List(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L).toHashid
```

`hash` will be :

	kRHnurhptKcjIDTWC3sx

### Incrementing number hashes:

```scala

implicit val hashids = Hashids("this is my salt")
val hash1 = 1L.toHashid /* NV */
val hash2 = 2L.toHashid /* 6m */
val hash3 = 3L.toHashid /* yD */
val hash4 = 4L.toHashid /* 2l */
val hash5 = 5L.toHashid /* rD */
```

## Bad hashes

This library was written with the intent of placing these hashes in visible places - like the URL. If a unique hash is created for each user, it would be unfortunate if the hash ended up accidentally being a bad word. Imagine auto-creating a URL with hash for your user that looks like this - `http://example.com/user/a**hole`

Therefore, this algorithm tries to avoid generating most common English curse words with the default alphabet. This is done by never placing the following letters next to each other:

	c, C, s, S, f, F, h, H, u, U, i, I, t, T

## Contact

Follow me [@newhoggy](https://twitter.com/newhoggy) or [@IvanAkimov](http://twitter.com/ivanakimov)

## License

MIT License. See the `LICENSE` file.
-->
