package org.msgpack.value

import org.msgpack.core.{MessagePack, MessagePackSpec}
import org.msgpack.value.holder.ValueHolder
import org.msgpack.value.impl.RawValueImpl

class BinaryValueImplTest extends MessagePackSpec {

  "BinaryValueImpl" should {
    "equals and hashCode" in {
      def printRawValueImplInternal(v: Value): Unit = {
        val field = classOf[RawValueImpl].getDeclaredField("byteBuffer")
        field.setAccessible(true)
        println(field.get(v))
      }

      val binaryValue = ValueFactory.newBinary(Array[Byte](0x41, 0x42, 0x43))

      val bytes = createMessagePackData { packer =>
        packer.packValue(binaryValue)
        packer.close()
      }

      val unpacker = MessagePack.newDefaultUnpacker(bytes)
      val holder = new ValueHolder
      unpacker.unpackValue(holder)
      val unpackedValue = holder.get

      printRawValueImplInternal(binaryValue)
      printRawValueImplInternal(unpackedValue)

      binaryValue.shouldEqual(unpackedValue)
      binaryValue.hashCode().shouldEqual(unpackedValue.hashCode())
    }
  }
}
