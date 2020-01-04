# 如何处理脏数据:以Scala+play-json为例

数据采集是数据处理的第一站,由于采集过程中数据来源复杂,需要经常处理有脏数据的情况.我们在采集中将各种意外情况妥善处理后,才能为后续的用户用更简单可靠的方式处理数据提供可能性(让只会SQL的人也能处理数据).

## 整体原则

* 任务不崩溃
  * 任务崩溃之后需要人为恢复并牺牲后续依赖它的系统运行的时间,**造成经济损失**
  * 除了硬件和系统故障之外,我们有充分的手段不让任务因内容异常而崩溃
* 异常有统一记录点
  * 异常记录分散的情况消耗排查者过多时间(牺牲后续依赖它的系统运行的时间,**造成经济损失**),并增加新人学习成本
* 异常记录足够详细
  * 异常记录过于宽泛会消耗排查者过多时间,甚至要变更代码重新发布来排查异常(牺牲后续依赖它的系统运行的时间,**造成经济损失**)

## 不良示范

### 藏匿异常(最糟糕的动作)

例子

```scala
    val div_result = try {
      someHeavyJob()
    } catch {
      case e: Exception =>
        ""
    }
```

直接无视异常返回一个后续可接受的值,这个是非常危险的动作,可能会导致以下情形:

* 异常发生次数和持续时间完全不可观测,系统丧失告警能力
* 异常排查时定位问题消耗时间较长,需要重新发布,甚至多次重新发布才能成功排查问题

### 合并异常

一行日志的字段异常可能有以下几种情况:

* 日志中应当有某个键,却没有
* 日志中的某个键应该是某种数据类型,却不是
* 日志中的键对应的值应当在某个范围内,却不在

对应可能造成这些异常的原因:

* 日志中应当有某个键,却没有
  * 常见于客户端的处理逻辑有bug,或者使用了第三方代码库时对第三方系统了解不充分
* 日志中的某个键应该是某种数据类型,却不是
  * 常见于弱类型语言操作不当(JavaScript,PHP等)
* 日志中的键对应的值应当在某个范围内,却不在
  * 常见于客户端采集用户输入时没有充分约束

一个合并异常的例子:

```scala
  def merge_exception_demo() = {
    val map = Map("key_a" -> "val_a", "key_b" -> "val_b")
    val valC = map.getOrElse("key_c","")
  }
```

这段示例代码合并了第一项和第三项异常,合并异常会造成以下问题:

* 排查链路延长,异常发生时,从只需要客户端排查异常,延伸到了客户端和ETL层都要排查异常.
* 异常发现节点后推,本来可以在ETL层发现的异常,可能会后推倒应用层才能发现.
* 上游(客户端)异常排查范围增加,由于异常合并,上游需要在更多的可能性中找到问题根源,排查时间随之增加.

### 不捕获异常

对可能发生异常的地方不捕获异常,会在异常发生时导致程序崩溃,造成直接损失.

同时,也容易造成异常发生时处理方法过于仓促,处理过程不够干净,导致异常处理代码进入[藏匿异常,合并异常,异常范围过宽]的陷阱中.

如何避免这种情况:

* 充分考虑方法的各种异常可能性,尤其是认真查看相关代码的签名和注释根据情况增加处理代码

### 异常范围过宽

样例:

```scala
  def wide_exception_demo() = try {
    //...... 大量可能抛出不同异常的代码
  } catch {
    case e: Exception =>
      // ..... 不分异常类型的简单处理
  }
```

这种做法只完成了三项基本原则中的一项,任务不崩溃,后续两项都没做到,常见于上一项问题[不捕获异常]发生后的懒人方案.这种方案造成的问题基本上相当于[合并异常]

## 利用类型系统排错和提升效率

### 代数数据类型 Algebraic Data Type

这里先介绍下代数数据类型的概念,代数数据类型一个简单描述是: 该类型(在Scala中为sealed trait)的所有实例(在Scala中为case class)都可以用已经定义的实例排列组合而成.一个典型的代数数据类型就是Json,我们看看play-json是怎么用代数数据类型定义Json的.

```scala
sealed trait JsValue

case object JsNull extends JsValue
sealed abstract class JsBoolean(val value: Boolean)
case object JsTrue extends JsBoolean(true)
case object JsFalse extends JsBoolean(false)
case class JsNumber(value: BigDecimal) extends JsValue
case class JsString(value: String) extends JsValue
case class JsArray(value: IndexedSeq[JsValue] = Array[JsValue]()) extends JsValue
case class JsObject(
    private[json] val underlying: Map[String, JsValue]
) extends JsValue
```



### 利用ADT 确认逻辑完整性

### 利用类型系统消除空指针异常

### 利用类型系统合理设计逻辑精细度

### 利用类型系统确保错误处理

### 利用类型系统做短路逻辑

