package org.msgpack.value

import org.msgpack.core.{MessagePack, MessagePackSpec}

class BinaryValueImplTest extends MessagePackSpec {

  "BinaryValueImpl" should {
    "equals and hashCode" in {

      val binaryValue = ValueFactory.newBinary(Array[Byte](0x41, 0x42, 0x43))

      val bytes = createMessagePackData { packer =>
        packer.packValue(binaryValue)
        packer.close()
      }

      val unpacker = MessagePack.newDefaultUnpacker(bytes)
      val variable = new Variable
      unpacker.unpackValue(variable)
      val unpackedValue = variable.asBinaryValue()

      binaryValue.shouldEqual(unpackedValue)
      binaryValue.hashCode().shouldEqual(unpackedValue.hashCode())
    }
  }
}
