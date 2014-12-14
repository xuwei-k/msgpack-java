package org.msgpack.value

import java.io.ByteArrayOutputStream

import org.msgpack.core.{MessagePack, MessagePackSpec}
import org.msgpack.value.holder.ValueHolder
import org.msgpack.value.impl.RawValueImpl


class RawValueImplTest extends MessagePackSpec {

  "RawValueImple" should {
    "toString shouldn't return empty value" in {
      val str = "aaa"
      def newRawStr() = ValueFactory.newRawString(str.getBytes("UTF-8"))

      def pack(v: Value): Array[Byte] = {
        val out = new ByteArrayOutputStream()
        val packer = MessagePack.newDefaultPacker(out)
        packer.packValue(v)
        packer.close()
        out.toByteArray
      }

      {
        val rawStr = newRawStr()
        pack(rawStr)
        rawStr.toString() shouldBe str
      }

      {
        val rawStr = newRawStr()
        pack(rawStr)
        rawStr.asString().toString shouldBe str
      }
    }

    "RawStringValueImpl#equals" in {
      def printRawValueImplInternal(v: Value): Unit = {
        val field = classOf[RawValueImpl].getDeclaredField("byteBuffer")
        field.setAccessible(true)
        println(field.get(v).asInstanceOf[java.nio.ByteBuffer])
      }

      val str = ValueFactory.newRawString("aaa".getBytes("UTF-8"))
      val bytes = createMessagePackData(_.packValue(str))
      val unpacker = MessagePack.newDefaultUnpacker(bytes)
      val holder = new ValueHolder
      unpacker.unpackValue(holder)
      val unpackedValue = holder.get

      unpackedValue.toString shouldBe str.toString

      // java.nio.HeapByteBuffer[pos=0 lim=3 cap=3]
      printRawValueImplInternal(unpackedValue)

      // java.nio.HeapByteBuffer[pos=3 lim=3 cap=3]
      printRawValueImplInternal(str)

      unpackedValue shouldBe str // failure
    }
  }
}
