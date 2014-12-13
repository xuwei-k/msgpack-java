package org.msgpack.value

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import org.msgpack.core.{MessagePackSpec, MessagePack}
import org.msgpack.value.holder.ValueHolder
import org.msgpack.value.impl._
import org.scalacheck.{Gen, Arbitrary}

final class PackUnpackValueTest extends MessagePackSpec {

  private val booleanValueGen: Gen[BooleanValue] =
    Gen.oneOf(
      Gen.const(BooleanValueImpl.TRUE),
      Gen.const(BooleanValueImpl.FALSE)
    )

  private val integerValueGen: Gen[IntegerValue] =
    Gen.oneOf(
      implicitly[Arbitrary[Int]].arbitrary.map(new IntegerValueImpl(_)),
      implicitly[Arbitrary[Long]].arbitrary.map(new LongValueImpl(_)),
      Gen.choose(0L, Long.MaxValue).flatMap(i => Gen.oneOf(
        new BigIntegerValueImpl(new BigInteger(i.toString)),
        new BigIntegerValueImpl(new BigInteger(i.toString).add(new BigInteger((i - 1).toString)))
      ))
    )

  private val floatValueGen: Gen[FloatValue] =
    Gen.oneOf(
      implicitly[Arbitrary[Float]].arbitrary.map(new FloatValueImpl(_)),
      implicitly[Arbitrary[Double]].arbitrary.map(new DoubleValueImpl(_))
    )

  private val numberValueGen: Gen[NumberValue] =
    Gen.oneOf(
      integerValueGen,
      floatValueGen
    )

  private val stringToRawStringValue: String => StringValue = {
    str => new RawStringValueImpl(ByteBuffer.wrap(str.getBytes("UTF-8")))
  }

  private val stringValueGen0: Gen[StringValue] =
    Gen.oneOf(
      Gen.alphaStr.map(new StringValueImpl(_)),
      Gen.alphaStr.map(stringToRawStringValue)
    )

  private def largeString(f: String => StringValue) =
    Gen.oneOf(List(5, 8, 16).map(1 << _).flatMap(x => List(x, x - 1))).flatMap( size =>
      Gen.listOfN(size, Gen.alphaNumChar).map(charList => f(charList.mkString))
    )

  private val stringValueGen1: Gen[StringValue] =
    Gen.frequency(
      5 -> stringValueGen0,
      1 -> largeString(stringToRawStringValue),
      1 -> largeString(new StringValueImpl(_))
    )

  private val bytes2binary = (bytes: Array[Byte]) => ValueFactory.newBinary(bytes)

  private val binaryValueGen0: Gen[BinaryValue] =
    implicitly[Arbitrary[Array[Byte]]].arbitrary.map(bytes2binary)

  private val binaryValueGen1: Gen[BinaryValue] =
    Gen.frequency(
      5 -> binaryValueGen0,
      1 -> Gen.oneOf(List(8, 16).map(1 << _).flatMap(x => List(x, x - 1))).flatMap(size =>
        Gen.containerOfN[Array, Byte](size, implicitly[Arbitrary[Byte]].arbitrary).map(bytes2binary)
      )
    )

  private val valueGen0: Gen[Value] =
    Gen.frequency(
      1 -> Gen.const(ValueFactory.nilValue()),
      1 -> booleanValueGen,
      10 -> numberValueGen,
      10 -> stringValueGen0,
      3 -> binaryValueGen0
    )

  private val arrayValueGen0: Gen[ArrayValue] =
    Gen.frequency(
      5 -> Gen.containerOf[Array, Value](valueGen0).map(new ArrayValueImpl(_)),
      1 -> Gen.oneOf(List(4, 16).map(1 << _).flatMap(x => List(x, x - 1))).flatMap(size =>
        Gen.containerOfN[Array, Value](size, valueGen0).map(new ArrayValueImpl(_))
      )
    )

  private val stringMapValueGen0: Gen[MapValue] = {
    import scala.collection.convert.decorateAsJava._
    val keyValueGen = Gen.zip(stringValueGen0, valueGen0)
    Gen.frequency(
      5 -> Gen.listOf(keyValueGen).map(keyValues => ValueFactory.newMap(keyValues.toMap.asJava)),
      1 -> Gen.oneOf(List(4, 16).map(1 << _).flatMap(x => List(x, x - 1))).flatMap(size =>
        Gen.listOfN(size, keyValueGen).map(keyValues =>
          ValueFactory.newMap(keyValues.toMap.asJava)
        )
      )
    )
  }

  private val valueGen1: Gen[Value] =
    Gen.oneOf(
      valueGen0,
      arrayValueGen0,
      stringMapValueGen0,
      stringValueGen1,
      binaryValueGen1
    )

  private def roundtrip(a: Value) = {
    val out = new ByteArrayOutputStream()
    val packer = MessagePack.newDefaultPacker(out)
    packer.packValue(a)
    packer.close()
    val bytes = out.toByteArray
    val unpacker = MessagePack.newDefaultUnpacker(bytes)
    val holder = new ValueHolder
    unpacker.unpackValue(holder)
    val x = holder.get
    try{
      x shouldBe a
    }catch{
      case e: Throwable =>
        println(e)
        println(x, x.getClass)
        println(a, a.getClass)
        throw e
    }
  }

  "pack/unpack Value" should {
    "value" in {
//      forAll(valueGen1)(roundtrip)
      val a = ValueFactory.newRawString("a".getBytes("UTF-8"))
      roundtrip(a)
    }
  }
}
