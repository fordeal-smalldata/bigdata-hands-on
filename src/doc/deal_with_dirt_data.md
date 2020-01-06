

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

这里先介绍下代数数据类型的概念,代数数据类型一个简单描述是: 该类型(在Scala中为sealed trait)的所有实例(在Scala中为case class)都可以用已经定义的实例排列组合而成.一个典型的代数数据类型的应用场景就是Json,我们看看play-json是怎么用代数数据类型定义Json的.

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

ADT的好处都有啥,当然是能帮助大家用编译器排错啦,有了ADT之后,大家用模式匹配写代码有什么疏漏的地方,编译器就能帮你检查到.

```scala
  def try_to_int(js: JsValue): Option[Int] = {
    Option(js).flatMap {
      case JsNumber(v) => Option(v.intValue())
    }
  }
  //It would fail on the following inputs: JsArray(_), JsFalse, JsNull, JsObject(_), JsString(_), JsTrue
  //    Option(js).flatMap {
  //                       ^
  //there was one feature warning; re-run with -feature for details
  //two warnings found
```

我们看看另一个写法↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓







换成这样写就没问题啦,所有情况都照顾到了,进程不会中断,错误也能够都被保留下来(Try基本上可以看成是一个带错误信息的Option)

```scala
  def try_to_int(js: JsValue): Try[Int] = {
    Success(js).flatMap {
      case JsNumber(v) => Success(v.intValue())
      case JsString(str) => Try {
        str.toInt
      }
      case unexpected => Failure(new Exception(s"unexpected content $unexpected"))
    }
  }
```



### 利用类型系统消除空指针异常

```java
package demo;

public class NullPointerDemo {
    public static String head_by_space(String str) {
        return str.split(" ")[0];
    }

    public static void main(String[] args) {
        //IDE会给一个Weak Warning
        //Passing 'null' argument to parameter annotated as @NotNull
        //但是在业务代码的海洋里遨游的您容易熟若无睹,而且只是在显式传入常量的时候才会给警告
        //编译器成功编译通过,也没有给出警告
        System.out.println(NullPointerDemo.head_by_space(null));
    }
}
```

上述代码给出了一个编译能通过但是运行一定会报错的情况,这种情况有没有通过更合理的设计避免呢?我们先介绍下两个概念:

* [Null References: The Billion Dollar Mistake](https://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare/) Tony(不是理发师,是快速排序算法和**空指针**的发明人)对自己发明的空指针做了忏悔,生成这种设计造成了十亿美金级别的损失.
* 所以更好的方案是什么?当然是使用更加严格的类型系统啦.更严格的类型系统是什么样的?由于没找到合适的线上资料,我们来看下<深入浅出Rust>一书中的摘抄

> "类型"规定了数据可能的取值范围,规定了在这些值上可能的操作,也规定了这些数据代表的含义,还规定了这些数据存储的方式.
>
> 如果我们有一个类型 Thing, 它有一个成员函数doSometThing(),那么只要是这个类型的变量,就一定应该可以调用doSomeThing()函数,完成同样的操作,返回同样类型的返回值.
>
> 但是,null违背了这样的约定...

所以Rust的String真的不会是null啦.

但是Scala为了处理一些历史遗留问题(为了和Java交互…),String还是有可能为null,所以接收String的,我们来看一下在历史的这个阶段Scala能通过什么手段来处理空指针异常

```scala
package demo

import scala.util.{Failure, Success, Try}

object NullPointerSafeDemo {
  def head_by_space(str: Option[String]): Option[String] = {
    str.flatMap(_.split(" ").headOption) //Option不是String,所以必须通过map/flatMap/foreach来操作,None不会进入这些操作
  }

  def main(args: Array[String]): Unit = {
    println(head_by_space(Option(null))) //将null变成了None
  }
}
```

这段代码遇到传入空指针不会报错,你的可能对null进行操作而触发异常的代码都因为有类型系统的保护,不会在传入值为null时被运行了.

可能有人会觉得,上面那一版Java代码自己加一行`if(str != null)`也能解决,使用`Option`的好处在哪里呢?区别在于,我们使用`Option`的话,一个值到底会不会为空,该处理的空值有没有被遗漏这些情况我们就都可以交给编译器解决,如果编译通过了,就说明该处理的情况都被处理了,有效地降低了开发者为了空指针安全所需要承担的心智负担.

### 利用类型系统合理设计逻辑精细度

解析数据的时候,需要处理的各种可能性会很多,为了确保没有漏网之鱼,我们需要对各种情况进行全面处理,一个最详细的全面处理代码如下所示

```scala
  def most_detailed_json_lookup() = {
    val json: JsValue = ???
    json \ "key" match { //匹配一个JsLookupResult
      //有这个键,列出所有可能的情况
      case JsDefined(value) => value match {
        case JsNull => ???
        case boolean: JsBoolean => ???
        case JsNumber(value) => ???
        case JsString(value) => ???
        case JsArray(value) => ???
        case JsObject(underlying) => ???
      }
      case undefined: JsUndefined =>
        //处理没有这个键的情况,下方的处理情况是抛出了一个异常说明应该有这个键却没有
        throw new Exception(s"error occurred when try to get key from $json : ${undefined.validationError}")
    }
  }
```

如果每次解析都要这么写,未免有些太辛苦了,下面我们来看下通过不同API,获得不同类型的结果,来简化处理代码的情况

#### 数据一定要有,并且类型也是确定的

```scala
  def simplified_json_lookup() = {
    val json: JsValue = ???
    val expectedInt = (json \ "key").as[Int]
  }
```

这个方法只在有"key"这个键,并且对应Json字段能映射成`Int`时能够执行成功,否则就会抛异常,需要在外部调用侧有异常处理

#### 数据可以有可以没有,但是如果有的话,类型是确定的

```scala
  def simplified_json_lookup2() = {
    val json: JsValue = ???
    val expectedInt = (json \ "key").asOpt[Int]
  }
```

这种做法不会抛出异常,不论键有没有,类型吻不吻合

#### 数据可以有也可以没有,而且有的话类型也有多种可能性

```scala
  def simplified_json_lookup3() = {
    val json: JsValue = ???
    val expectedInt = (json \ "key").asOpt[JsValue].map {
      case JsNumber(num) => num.toInt
      case JsString(str) => str.toInt
      case unexpected => throw new Exception(unexpected.toString())
    }
  }
```

这种做法不会因为没有相应的键而抛异常,但会因为类型匹配或者处理过程中的代码而抛异常

#### 设计逻辑精度时容易出现的问题

* 行为和设计意图不符,或者设计意图没有充分表达,常见于API不够熟悉或者缺少设计意识的情况

例:

```scala
  def head_by_space_str(str: Option[String]): String = {
    str.flatMap(_.split(" ").headOption).getOrElse("")
  }
```

这段代码的设计意图应该是"head可以没有",但是函数签名上没有充分表达出来,当调用者收到一个`""`的时候,调用者不知道是真的结果是`""`,还是因为没有合适的值所以用一个`""`代替,我们换个写法

```scala
  def head_by_space(str: Option[String]): Option[String] = {
    str.flatMap(_.split(" ").headOption)
  }
```

这样写的话,如果返回值为`None`就可以表达"没有合适的值"这种情况,得到`""`就说明是真的返回值为`""`,代码有了"自文档"的能力,有了更多的信息可以告诉别人(这个别人可能就是三个月后的你自己).

小技巧: 避免使用`getOrElse("")`,``getOrElse(0)``,``getOrElse(-1)``,等操作,可以很大程度避免这种情况.

* 单纯为了避免异常而写出不会抛异常的代码,忽略了在程序不崩溃的情况下记录异常的需求

我们再审视一遍`head_by_space`

```scala
  def head_by_space(str: Option[String]): Option[String] = {
    str.flatMap(_.split(" ").headOption)
  }
```

这段代码不会抛异常,但是这个"不抛异常"到底是不是我们想要的东西呢?如果按正常情况,代码就应该返回`Some[String]`,返回`None`是不正常的情况,那么我们就应该在返回`None`的时候抛出异常,把异常的发生量作为一种监控手段,在异常大量发生的时候获得警告.函数就可以这么定义.

```scala
  def try_head_by_space_(str: Option[String]): Try[String] = Try {
    str.get.split(" ").head
  }
```

此时如果在`Try`包围的逻辑块里发生任何异常,得到的结果就会是`Failure[Exception]`,异常能被捕获,程序不会崩溃退出,类型系统还会告诉你,你得在未来的某个时候处理掉这些异常(通常是写进某个日志渠道).

### 利用类型系统确保错误处理

假如我们有多个可能产生异常的代码块,可以利用`Try`结构和`for`语句来串联代码块,这样所有的异常最终会有一个统一的出口处理,而业务代码可以当做没有发生任何异常那样串联处理.

```scala
  def try_head_by_space_(str: String): Try[String] = Try {
    str.split(" ")(1)
  }

  def try_to_int(str: String): Try[Int] = Try {
    str.toInt
  }

  def main(args: Array[String]): Unit = {
    val num =
      for (head <- try_head_by_space_("asdds asdasd");
           num <- try_to_int(head))
        yield num

    num match {
      case Success(res) => println(s"success fully get number $res")
      case Failure(e) => println(e)
        // java.lang.NumberFormatException: For input string: "asdasd"
    }
  }

  def main(args: Array[String]): Unit = {
    val num =
      for (head <- try_head_by_space_("");
           num <- try_to_int(head))
        yield num

    num match {
      case Success(res) => println(s"success fully get number $res")
      case Failure(e) => println(e)
        // java.lang.ArrayIndexOutOfBoundsException: 1
    }
    
    def main(args: Array[String]): Unit = {
    val num =
      for (head <- try_head_by_space_("aaa 233");
           num <- try_to_int(head))
        yield num

    num match {
      case Success(res) => println(s"success fully get number $res")
      case Failure(e) => println(e)
        // success fully get number 233
    }
```



### 利用类型系统做短路逻辑

利用类型系统做短路逻辑的方法,其实在上面的异常处理中已经有体现了(任何一步是Fail的话,之后的代码都不会再被执行了),其实它还能应对更复杂的场景.我们结合上一步的例子再增加一些代码:

```scala
  def try_head_by_space_(str: String): Try[String] = Try {
    str.split(" ")(1)
  }

  def try_to_int(str: String): Try[Int] = Try {
    str.toInt
  }

  def keep_even_number(num: Int): Option[Int] = Option(num).filter(_ % 2 == 0)

  def main(args: Array[String]): Unit = {
    val num =
      for (head <- try_head_by_space_("aaa 233").toEither;
           num <- try_to_int(head).toEither;
           even <- keep_even_number(num).toRight(new Exception(s"$num is not even")))
        yield even

    num match {
      case Right(res) => println(s"success fully get number $res")
      case Left(e) => println(e)
      // java.lang.Exception: 233 is not even
    }
  }
```

这个地方我们又增添了一个返回值为`Option[Int]`的方法,加入到了for语句中.为了让这个`for`语句拥有和`Try`,`Option`相兼容的结果类型,我们对`Try`和`Option`都进行了`toEither`操作(`Try`和`Option`都可以看作是`Either`的特殊情况).