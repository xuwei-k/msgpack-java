//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.jackson.dataformat.benchmark

import com.fasterxml.jackson.databind.ObjectMapper
import org.msgpack.jackson.dataformat.MessagePackDataformatTestBase
import org.msgpack.jackson.dataformat.MessagePackDataformatTestBase.{NormalPojo, Suit}
import org.openjdk.jmh.annotations.Benchmark
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.math.BigInteger

object MessagePackDataformatPojoBenchmarkTest {
  private final val LOOP_MAX = 1000
  private val pojos = new Array[MessagePackDataformatTestBase.NormalPojo](LOOP_MAX)
  private val pojosSerWithOrig = new Array[Array[Byte]](LOOP_MAX)
  private val pojosSerWithMsgPack = new Array[Array[Byte]](LOOP_MAX)

  locally {
    val origObjectMapper = new ObjectMapper
    val msgpackObjectMapper = new ObjectMapper(new MessagePackFactory)
    var i = 0
    while (i < LOOP_MAX) {
      val pojo = new MessagePackDataformatTestBase.NormalPojo
      pojo.i = i
      pojo.l = i
      pojo.f = i.toFloat
      pojo.d = i.toDouble
      pojo.setS(String.valueOf(i))
      pojo.bool = i % 2 == 0
      pojo.bi = BigInteger.valueOf(i)
      i % 4 match {
        case 0 =>
          pojo.suit = Suit.SPADE
        case 1 =>
          pojo.suit = Suit.HEART
        case 2 =>
          pojo.suit = Suit.DIAMOND
        case 3 =>
          pojo.suit = Suit.CLUB
      }
      pojo.b = Array[Byte](i.toByte)
      pojos(i) = pojo
      i += 1
    }

    i = 0
    while (i < LOOP_MAX) {
      pojosSerWithOrig(i) = origObjectMapper.writeValueAsBytes(pojos(i))
      i += 1
    }

    i = 0
    while (i < LOOP_MAX) {
      pojosSerWithMsgPack(i) = msgpackObjectMapper.writeValueAsBytes(pojos(i))
      i += 1
    }
  }

  private val origObjectMapper = new ObjectMapper
  private val msgpackObjectMapper = new ObjectMapper(new MessagePackFactory)
}

class MessagePackDataformatPojoBenchmarkTest {
  import MessagePackDataformatPojoBenchmarkTest._

  @Benchmark
  def serializePojoWithJSON(): Unit = {
    var i = 0
    while (i < LOOP_MAX) {
      origObjectMapper.writeValueAsBytes(pojos(i))
      i += 1
    }
  }

  @Benchmark
  def serializePojoWithMessagePack(): Unit = {
    var i = 0
    while (i < LOOP_MAX) {
      msgpackObjectMapper.writeValueAsBytes(pojos(i))
      i += 1
    }
  }

  @Benchmark
  def deserializePojoWithJSON(): Unit = {
    var i= 0
    while (i < LOOP_MAX) {
      origObjectMapper.readValue(pojosSerWithOrig(i), classOf[NormalPojo])
      i += 1
    }
  }

  @Benchmark
  def deserializePojoWithMessagePack(): Unit = {
    var i = 0
    while (i < LOOP_MAX) {
      msgpackObjectMapper.readValue(pojosSerWithMsgPack(i), classOf[NormalPojo])
      i += 1
    }
  }
}
