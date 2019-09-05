# Scala入门(三)

## case class和模式匹配

咱们的讲义开头已经提到了,这个系列讲义是教您怎么用Scala处理数据的.那么我们就在这里着重介绍Scala提供的一个能为您处理数据提供一个很大便利性的特性,叫做`case class`.

`case class`在默认的`class`基础上提供了若干便利特性(Scala默认的`class`基本上就是Java的`class`),我们先看一下一些默认`class`代码的运行结果

```scala
  class Point(val x: Int, val y: Int)

  val point1 = new Point(1, 1)
  val point2 = new Point(1, 1)
  println(point1)
  //输出 Point@1e643faf
  println(point1 == point2)
  //输出 false

  val point1x = point1.x
  val point1y = point1.y
  println("point1x = " + point1x) //point1x = 1
  println("point1y = " + point1y) //point1y = 1
```

上述代码中我们可以注意到两个点,一个是`println(point1)`输出了`Point@1e643faf`,这是默认调用了Java里的`Object.toString`的结果,一个Scala类就是一个Java类,所以Scala类也都是Java里Object类的子类.默认的`Object.toString`在我们实际工作中一般起不到什么帮助,所以我们一般会重新为我们的类定义一个`toString`来覆盖`Object.toString`来方便调试监控什么的.

另外,`println(point1 == point2)`输出了`false`,因为`==`调用了`equals`方法,按照默认的`Object.equals`实现,这个方法是比较两个对象的内存地址,如果地址不一样就返回`false`.实际绝大部分数据处理场景下,几乎没有人关心两个对象地址是否一样,所以默认的`equals`方法也没什么用处.

每次定义一个新类,都要定义大量的重载,是一件挺麻烦的事情,所以针对群众的需要,`case class`出现了,我们来看下`case class`的表现.

```scala
  case class Point(x: Int, y: Int)

  val point1 = Point(1, 1)
  val point2 = Point(1, 1)
  val point3 = point2.copy(y = 10)
  println(point3)
  //输出 Point(1,10)
  println(point1 == point2)
  //输出 true

  val Point(point1x, point1y) = point1
  println("point1x = " + point1x)
  // point1x = 1
  println("point1y = " + point1y)
  // point1y = 1
```

定义`case class`的时候,您不用在`x`和`y`面前增加`val`(因为`case class` 默认都用`val`定义成员),并且会自动给您生成一个实用的`toString`和一个实用的`equals`方法,除此之外,还提供了便利地用旧`case class`局部重新赋值,生成新`case class`的方法(见`point3`),还提供了模式匹配功能,见(`point1x`和`point1y`),模式匹配功能我们接着细说.

### `case class` 匹配

`case class`和模式匹配一起工作能大幅提高代码的表达能力,叙述模式匹配的作用的时候,为了能让场景更贴近实际,我们来引入一个第三方库[json4s](https://github.com/json4s/json4s).我们的习题项目里已经加入了json4s依赖,直接用就可以了,使用方法是在代码中加入以下内容

```scala
    import org.json4s._
    import org.json4s.jackson.JsonMethods._
    implicit val formats = DefaultFormats
```

在Ammonite中,可以这样加入json4s依赖

```scala
➜  bigdata-hands-on git:(master) ✗ amm
Loading...
Welcome to the Ammonite Repl 1.6.8
(Scala 2.13.0 Java 1.8.0_181)
If you like Ammonite, please support our development at www.patreon.com/lihaoyi
renkai-bigdata-hands-on@     import $ivy.`org.json4s::json4s-jackson:3.6.7`
import $ivy.$

renkai-bigdata-hands-on@     import org.json4s._
import org.json4s._

renkai-bigdata-hands-on@     import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.JsonMethods._

renkai-bigdata-hands-on@     implicit val formats = DefaultFormats
formats: DefaultFormats.type = org.json4s.DefaultFormats$@cbf1997
```

json4s给出了以下`case class`定义,这些定义可以表达一个完整的json结构

```scala
//json4s已经定义好了,不用自己定义,导入就行啦
sealed abstract class JValue
case object JNothing extends JValue // 'zero' for JValue
case object JNull extends JValue
case class JString(s: String) extends JValue
case class JDouble(num: Double) extends JValue
case class JDecimal(num: BigDecimal) extends JValue
case class JInt(num: BigInt) extends JValue
case class JLong(num: Long) extends JValue
case class JBool(value: Boolean) extends JValue
case class JObject(obj: List[JField]) extends JValue
case class JArray(arr: List[JValue]) extends JValue

type JField = (String, JValue)
```

假设,现在一项工作目标来了,您需要处理一些别人产生的json,取出其中的一个字段的整型值,然而这些json有些小毛病,有的缺少这个字段,有的这个字段为`null`,有的这个字段是字符串形式(虽然字符串里都是整数),某些编程语言(比如说PHP)在业务复杂之后很容易产生这样格式混乱的数据,我们需要把各种情况都应对了,拿着干净的数据进行进一步的处理.

一开始,您比较傻比较天真,以为数据格式都是好的,写出了一版这样的代码,只考虑了确实有相应字段,并且格式也正确的情况

```scala
  val messyData = Seq(
    "{\"should_int\": 1024}",
    "{\"should_int\": \"1025\"}",
    "{\"should_int\": null}",
    "{}"
  )
  
  import org.json4s._
  import org.json4s.jackson.JsonMethods._
  implicit val formats = DefaultFormats

  val cleanData = messyData.map {
    jsStr =>
      val jsObj = parse(jsStr)
      (jsObj \ "should_int") match {
        case JInt(num) => num
      }
  }
```

而当我们尝试编译这段代码的时候,就会收到这样的警告

```scala
match may not be exhaustive.
It would fail on the following inputs: JArray(_), JBool(_), JDecimal(_), JDouble(_), JLong(_), JNothing, JNull, JObject(_), JSet(_), JString(_)
      (jsObj \ "should_int") match {
             ^
one warning found
```

告诉您不要太傻太天真,您在代码里没有考虑的异常情况可能还有很多.让我们仔细考虑下这个warning,思考自己有什么做的不足的地方,发现原来有好多情况没考虑,于是加上了针对所有情况的应对方案(毕竟数据往往是别人生成的,什么不靠谱的事情都可能发生)

```scala
  val cleanData = messyData.map {
    jsStr =>
      val jsObj = parse(jsStr)
      (jsObj \ "should_int") match {
        case JInt(num) => num.toInt
        case JArray(x) => 0
        case JBool(x) => 0
        case JDecimal(x) => x.toInt
        case JDouble(x) => x.toInt
        case JLong(x) => x.toInt
        case JNothing => 0
        case JNull => 0
        case JObject(x) => 0
        case JSet(x) => 0
        case JString(x) => x.toInt
      }
  }
  println(cleanData)
  //输出 List(1024, 1025, 0, 0)
```

现在所有情况都能应对了,不过代码稍微冗余了些,您和生产数据的小伙伴沟通了一下,发现除了`JInt`,`JDouble`,`JString`,`JNull`,`JNothing`这些情况是它无心之失产生的,并且数据还有的救之外,其他数据类型都是不应该产生,并且发现了马上要上报异常的,代码就可以改成这样.

```scala
  val cleanData = messyData.map {
    jsStr =>
      val jsObj = parse(jsStr)
      (jsObj \ "should_int") match {
        case JInt(num) => num.toInt
        case JString(x) if x.forall(_.isDigit) => x.toInt
        case JDouble(x) => x.toInt
        case JNull => 0
        case JNothing => 0
        case x => throw new Exception("不应该运行到这里" + x)
      }
  }
  println(cleanData)
  //输出 List(1024, 1025, 0, 0)
```

我们来看下这个场景下`case class`加模式匹配为您做了什么.

首先它通过编译器提示帮助您找出了考虑疏漏的地方,然后针对匹配到的内容,您可以同时便捷的取出`case class`中需要的数据.最后,您可以通过一个通配的情况统一处理您不并关心的其它各种可能性.

### 变长参数匹配

模式匹配除了匹配`case class`和基本数据类型外,还能匹配标准库里的一些数据结构,例如`Seq`,我们可以利用模式匹配便利地为`Seq`中我们需要的内容命名

```scala
  val seq = Seq(1, 2, 3, 4, 5)
  val Seq(a, b, c, d, e) = seq
  println(a,b,c,d,e) //输出 (1,2,3,4,5)
```

当然,这种情况需要两边元素个数完全符合才行,如果元素有多或者又少就会出现编译错误

```scala
  val seq = Seq(1, 2, 3, 4, 5)
  val Seq(a, b, c, d) = seq
  // Exception in thread "main" scala.MatchError: List(1, 2, 3, 4, 5) (of class scala.collection.immutable.$colon$colon)
  println(a,b,c,d)
```

如果我们只是想获得头部的几个元素,而忽略后面的元素怎么办?这个时候可以用一个`_*`表示接收后面的元素

```scala
  val seq = Seq(1, 2, 3, 4, 5, 6)
  val Seq(a, b, c, d, _*) = seq
  println(a, b, c, d) // (1,2,3,4)
```

如果后面的元素我们还是想要,而不是简单地丢弃,该怎么办呢,我们可以写一个`x @ _*`,表示用`_*`接收后面的元素,`x @`表示将他们命名为`x`

```scala
  val seq = Seq(1, 2, 3, 4, 5, 6)
  val Seq(a, b, c, d, x @ _*) = seq
  println(a, b, c, d) // (1,2,3,4)
  println(x) // List(5, 6)
```

### 正则匹配

正则表达式是从文本中提取数据常用的工具,关于正则表达式的学习,网上有很多优秀的教程和文档大家可以自行查阅.

这里推荐一份教程http://regextutorials.com/](http://regextutorials.com/)

和一份文档[https://docs.python.org/3/library/re.html](https://docs.python.org/3/library/re.html) (没错,就是Python语言的官方文档!你要问我Python有哪点好,我想来想去也就这个正则表达式文档确实不错)

好了,现在假设您已经掌握了基本的正则表达式,想利用Scala模式匹配的特性便利地进行工作,一项工作内容是提取出一个CSS文件中的所有键值对,然后代码就可以这么写

```scala
  val keyValPattern = "([0-9a-zA-Z- ]+): ([0-9a-zA-Z-#()/. ]+)".r

  val input: String =
    """background-color: #A03300;
      |background-image: url(img/header100.png);
      |background-position: top center;
      |background-repeat: repeat-x;
      |background-size: 2160px 108px;
      |margin: 0;
      |height: 108px;
      |width: 100%;""".stripMargin

  val kvMap: Map[String, String] =
    keyValPattern.findAllIn(input) // 获取所有符合正则表达式的字符串
      .map {
        case keyValPattern(x, y) => (x, y) //通过模式匹配获取键值对
      }
      .toMap //把Seq[(String,String)]转换为Map[String,String]

  println("CSS中的内容有: ")
  kvMap.foreach {
    case (k, v) => //用模式匹配获取
      println(s"'$k' '$v'")
  }
//CSS中的内容有: 
//'background-image' 'url(img/header100.png)'
//'margin' '0'
//'background-repeat' 'repeat-x'
//'height' '108px'
//'background-color' '#A03300'
//'background-size' '2160px 108px'
//'width' '100'
//'background-position' 'top center'
```

更多具体用例可以看Scala的[官方文档](https://www.scala-lang.org/api/2.12.1/scala/util/matching/Regex.html)



## 异常处理

Scala的异常处理和Java基本一样,用`try {} catch {}`块处理就行了,区别只是Scala用模式匹配来捕获具体的错误类型,需要注意的是,`Exception`的子类基本上不会是`case class`,而是普通`class`,所以模式匹配在这里只能做到识别不同的`Exception`类,无法自动提取出`Exception`的`message`和`cause`成员.

```scala
  class NotPredictedException(message: String = "", cause: Throwable = null) extends Exception(message, cause)

  val emptyArr = Array.empty[Int]
  val div0 = () => 3 / 0
  val outofBound = () => emptyArr(0)

  val justThrow = () => {
    throw new NotPredictedException("一个没有预先知道的错误类型")
  }
  val allErrorFunctions = Seq(div0, outofBound, justThrow)

  allErrorFunctions.foreach {
    func =>
      try {
        func()
      } catch {
        case e: ArithmeticException => println("算数错误:" + e)
        case e: ArrayIndexOutOfBoundsException => println("数组越界: " + e)
        case e: Exception => println("未知错误: " + e)
      }
  }
  //算数错误:java.lang.ArithmeticException: / by zero
  //数组越界: java.lang.ArrayIndexOutOfBoundsException: 0
  //未知错误: Playground$NotPredictedException: 一个没有预先知道的错误类型
```



## 调用Java代码

在主流工作环境中,Scala代码和Java代码基本是共生的.当然有少数勇者会用[Scala Native](http://www.scala-native.org)把Scala编译成机器码,还有一些勇者会用[Scala.js](https://www.scala-js.org/)把Scala编译成JavaScript,我们暂时不考虑这种情况,只考虑在JVM上工作的情况.

Scala调用Java代码几乎不需要什么额外的成本,如果需要使用现成的第三方Jar包,像在Java项目里一样增加依赖就行了,调用自己写的Java源码,也直接`import`就行,除了不需要在行尾写一个分号,和在Java里调用Java代码几乎没有区别.

习题集里包含了一些调用样例 [Java部分](https://github.com/fordeal-smalldata/bigdata-hands-on-quiz/blob/master/src/main/java/demo/AJavaClass.java) [Scala部分](https://github.com/fordeal-smalldata/bigdata-hands-on-quiz/blob/master/src/main/scala/demo/ScalaCallJava.scala)

需要注意的是,Java和Scala的基本数据类型是通用的,但是标准数据结构并不是,如果调用的Java方法参数或者返回值中包含`List`,`Set`,`Map`等类型,需要`import scala.collection.JavaConverters._`配合`asJava`,`asScala`方法来进行转换,具体例子如下

```java tab=Java部分
package demo;


import java.util.Arrays;
import java.util.List;

public class AJavaClass {
    public void aJavaMethod() {
        System.out.println("这是一个Java方法");
    }

    public List<Integer> getJavaList() {
        return Arrays.asList(1, 0, 2, 4);
    }

    public void printList(List<Integer> list) {
        System.out.println("数组中包含元素:");
        list.forEach(System.out::println);
    }
}

```

```scala tab=Scala部分
package demo

object ScalaCallJava {
  def main(args: Array[String]): Unit = {
    val jClass = new AJavaClass
    jClass.aJavaMethod()
    //输出 这是一个Java方法

    import scala.collection.JavaConverters._
    val list = jClass.getJavaList.asScala
    println("转换成Scala的标准库: " + list) // 输出 转换成Scala的标准库: Buffer(1, 0, 2, 4)
    jClass.printList(list.asJava)
    //输出 数组中包含元素:
    //1
    //0
    //2
    //4
  }
}
```



## 一些语法糖

Scala是一门语法糖很多的语言,甚至有人把Scala的这种特性称为"语法齁".善用语法糖能提升代码表达力,但是滥用语法糖会造成代码可读性下降,所以在工作中最好节制使用.这一节主要向大家介绍别人用语法糖写代码的话,我们怎么还原出它的非糖形式(Desugar).

### 空格和括号

Scala社区下主流的一个单元测试框架是[ScalaTest](http://www.scalatest.org/),我们可以看到首页上有这样一篇代码样例

```scala
import collection.mutable.Stack
import org.scalatest._

class ExampleSpec extends FlatSpec with Matchers {

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    } 
  }
}
```

其中`stack.pop() should be (2)`其实用了空格语法糖,去糖化的表达形式是`stack.pop().should.be(2)`,Scala对象中简单的单参数或者无参数方法都可以用`' '`代替`'.'`(上述代码例子中,语法糖是提升了代码表达能力的,这个应该没人会反对吧😄).

习惯别的编程语言的玩家有可能忽略一点,那就是容易被认为是'基本操作'的`+ - * /`等,其实也是语法糖,不管是符号,还是文字,在Scala的世界里都是平等的.

```scala
➜  bigdata-hands-on git:(master) ✗ amm
Loading...
Welcome to the Ammonite Repl 1.6.8
(Scala 2.13.0 Java 1.8.0_181)
If you like Ammonite, please support our development at www.patreon.com/lihaoyi
renkai-bigdata-hands-on@   val shouldBe2 = 1 + 1
shouldBe2: Int = 2

renkai-bigdata-hands-on@   val shouldBe2Too = 1.+(1)
shouldBe2Too: Int = 2
```

### 下划线

下划线是Scala代码中的常客,它在不同的上下文中出现代表着不同的含义.

* 含义一:我全都要

我全都要的含义出现在`import`场景下,Java中的`import java.time.*`操作等价于Scala的`import java.time._`原因[这里](https://softwareengineering.stackexchange.com/questions/194686/why-does-scala-use-the-operator-for-package-import-instead-of-as-in-java)有介绍

* 含义二:我知道你有,但我不在乎

这种场景常见于模式匹配中,您在获得一个`case class`后可能只对其中的某些成员感兴趣,另一些成员当时就丢弃了,但是为了模式匹配要用`_`做占位符,Scala官方网站的[教程](https://docs.scala-lang.org/tour/pattern-matching.html)中就能找到例子,贴于此处

```scala
abstract class Notification

case class Email(sender: String, title: String, body: String) extends Notification

case class SMS(caller: String, message: String) extends Notification

case class VoiceRecording(contactName: String, link: String) extends Notification

def showNotification(notification: Notification): String = {
  notification match {
    case Email(sender, title, _) => // <- 这里不关心body,所以用_做了占位符
      s"You got an email from $sender with title: $title"
    case SMS(number, message) =>
      s"You got an SMS from $number! Message: $message"
    case VoiceRecording(name, link) =>
      s"you received a Voice Recording from $name! Click the link to hear it: $link"
  }
}

val someSms = SMS("12345", "Are you there?")
val someVoiceRecording = VoiceRecording("Tom", "voicerecording.org/id/123")

println(showNotification(someSms))  // prints You got an SMS from 12345! Message: Are you there?

println(showNotification(someVoiceRecording))  // you received a Voice Recording from Tom! Click the link to hear it: voicerecording.org/id/123
```



* 含义三:给一个默认值

用`_`赋值给某种类型的变量,相应变量会被赋值为该类型的默认值

```scala
var aInt: Int = _ // aInt: Int = 0
var aDouble: Double = _ // aDouble: Double = 0.0
var aString: String = _ // aString: String = null
```

* 含义四:跑龙套

在工作中,我们可能经常需要调用一些高阶函数给数据做简单的处理,但是我们懒得为那些高阶函数起变量名(起变量名是编程中最大的困难😓),这个时候`_`能为您稍许解决一些困扰

```scala
  val strInts = Seq("1", "2", "3")
  // strInts: Seq[String] = List("1", "2", "3")
  val ints1 = strInts.map(_.toInt)
  // ints1: Seq[Int] = List(1, 2, 3)
  val ints2 = strInts.map(x => x.toInt)
  //和上面的形式是等价的
  // ints2: Seq[Int] = List(1, 2, 3)
```

两个下划线对应简单的双参数函数场景(用到的机会不多,但是看到的容易懵)

```scala
  val oneTwoThree = Seq(1, 2, 3)
	//oneTwoThree: Seq[Int] = List(1, 2, 3)
  val sum1 = oneTwoThree.reduce(_ + _)
	//sum1: Int = 6
  val sum2 = oneTwoThree.reduce((x, y) => x + y)
	//sum2: Int = 6 和上方 _ + _ 等价
```

### for 循环

Scala里不光操作符是语法糖,for循环也都是语法糖来的,for循环代码会根据实际情况被编译成`foreach`,`map`和`flatMap`,下面我们给出一些例子.更详细些的介绍可以看[官方文档](https://docs.scala-lang.org/tutorials/FAQ/yield.html).

```scala
  val oneTwoThree = Seq(1, 2, 3)
  for (num <- oneTwoThree) {
    println(num)
  }
  /*输出
  1
  2
  3
   */
  
  oneTwoThree.foreach {
    num => println(num)
  }
  /*和上方代码等价,输出
  1
  2
  3
 */
```

```scala
  val xPoints = Seq(1, 2, 3)
  val yPoints = Seq(1, 2, 3)
  val cartesianProd1 =
    for (x <- xPoints; y <- yPoints) yield (x, y)
  println(cartesianProd1)
  // List((1,1), (1,2), (1,3), (2,1), (2,2), (2,3), (3,1), (3,2), (3,3))
  val cartesianProd2 =
    xPoints.flatMap {
      x =>
        yPoints.map {
          y => (x, y)
        }
    }
  println(cartesianProd2)
  //和上方的for ... 等价
  //List((1,1), (1,2), (1,3), (2,1), (2,2), (2,3), (3,1), (3,2), (3,3))
```



## 试试摆脱break

`break`关键词是很多编程语言内置的特性,它在流程控制中几乎是一个必要的关键词,可以说没有`break`就没办法工作…但是当我们的视角从工作流变成数据的时候,`break`就显得并没有需要了,Scala甚至没有默认提供`break`,而是您需要导入才能使用.

我们来考虑一下这样一个场景,您是一名企业主,您的事业蒸蒸日上,不断地有人来投奔您要加入您的企业.这个时候为了方便记录员工信息,您就需要为员工都赋予一个工号.根据历史经验,企业主多多少少都是迷信的人,您希望员工工号都必须至少有一个6或8,并且不能有4,那么前100名员工的工号会是多少呢?让我们试试用工作流控制的方式来获得这些工号.

```scala
  import scala.util.control.Breaks._

  var employeeIds = Vector.empty[Int]

  breakable {
    for (id <- 0 to Int.MaxValue) {
      val idStr = id.toString
      if ((idStr.contains('6') || idStr.contains('8')) && !idStr.contains('4')) employeeIds :+= id
      if (employeeIds.size >= 100) break
    }
  }
  
  println(employeeIds)
  // Vector(6, 8, 16, 18, 26, 28, 36, 38, 56, 58, 60, 61 ...
```

这段代码工作良好,结果正确.不过从数据处理的角度看,多了一些不必要的噪音(我只是想要数据啊,为什么要关心你的控制流呢),我们来看看只关心数据流的情况下代码是什么样的

```scala
  val employeeIds =
    Iterator.iterate(0)(_ + 1).filter {
      id =>
        val idStr = id.toString
        (idStr.contains('6') || idStr.contains('8')) && !idStr.contains('4')
    }.take(100).toVector
  println(employeeIds)
	// Vector(6, 8, 16, 18, 26, 28, 36, 38, 56, 58, 60, 61 ...
```

我们用`Iterator.iterate`生成了所有整数列表,注意,`Iterator.iterate`只是为您定义了获得所有整数的方法,并没有真的把所有整数都产生出来放在内存里(如果是那样的话内存就爆了😨),真的把所有符合要求的数据存下来的代码是`toVector`,其他条件都是为这个`toVector`服务的.您不需要用代码声明"我要100个,到了之后程序就停下",您只要说"我要100个",停下的事情它自己会做.

## 递归和尾递归

递归能够帮助我们简洁的表达很多概念.假设您是一名企业主,您的企业包括您自己有100名员工,您没有上一名企业主那样迷信,所以您企业里的员工ID是0~99(0号就是您自己).现在春节刚过去,员工都斗志满满地要来公司上班啦,为了激励员工,您决定让每个员工都获得不少于一百元的开门[利是](https://baike.baidu.com/item/%E5%88%A9%E6%98%AF/6676492).可是作为一名创业阶段的企业主,您囊中羞涩,您的预算总共只有99元.

于是您想了个主意,定了这样一个利是规则,既能满足您最初的初心,又不会超预算.您的方案是这样的,从编号最大的员工开始,先发n元利是给比自己工号大1的人(如果没有这个人,就发给老板),然后工号比自己小1的人发n+1元给自己,这样一直循环到工号0为止.刚才说了,希望大家都能拿到百元以上利是,可是预算总量有限,那老板自己委屈点,n就设置为99吧.

我们先看看相应的Java代码实现

```java
    static int maxEmployeeId = 99;

    public static void sendRedPack(int currentEmployeeId, int price) {
        int recipientId = (currentEmployeeId + 1) % maxEmployeeId;
        System.out.println("员工 " + currentEmployeeId + "发利是给员工" + recipientId + " " + price + " 元");
        if (currentEmployeeId > 0) sendRedPack(currentEmployeeId - 1, price + 1);
    }

    public static void main(String[] args) {
        sendRedPack(99,99);
    }
        //员工 99发利是给员工0 99 元
        //员工 98发利是给员工99 100 元
        //员工 97发利是给员工98 101 元
        //员工 96发利是给员工97 102 元
        //...
        //员工 3发利是给员工4 195 元
        //员工 2发利是给员工3 196 元
        //员工 1发利是给员工2 197 元
        //员工 0发利是给员工1 198 元
```

太好了,预算没有超,大家都拿到了满意的利是,优秀的递归技巧帮助您成为了一名优秀的企业家😄

经过了多年的苦心经营,您的企业人数增加到了10000人,而您仍然保持着每年派利是激励员工的习惯,现在您希望每名员工都拿到万元以上利是了,您的预算也大幅提升到9999元.真是令人难以置信,世界上怎么会有您这么慷慨的企业主呢.您拿出了多年前那份代码,稍作修改,决定计算派利是的规则,结果遭遇了小小的问题.

```java
    static int maxEmployeeId = 9999;

    public static void sendRedPack(int currentEmployeeId, int price) {
        int recipientId = (currentEmployeeId + 1) % (maxEmployeeId + 1);
        System.out.println("员工 " + currentEmployeeId + "发利是给员工" + recipientId + " " + price + " 元");
        if (currentEmployeeId > 0) sendRedPack(currentEmployeeId - 1, price + 1);
    }

    public static void main(String[] args) {
        sendRedPack(maxEmployeeId, maxEmployeeId);
    }
    //员工 9999发利是给员工0 9999 元
    //员工 9998发利是给员工9999 10000 元
    //员工 9997发利是给员工9998 10001 元
    //员工 9996发利是给员工9997 10002 元
    //员工 9995发利是给员工9996 10003 元
    // .....
    //Exception in thread "main" java.lang.StackOverflowError
    //	at demo.AJavaClass.sendRedPack(AJavaClass.java:26)
```

您居然遭遇了Stack Overflow!

这其实也不难理解,如果您学习过本科数据结构课,您应该知道递归的一个常规实现是利用栈存储信息,递归结束后栈的内容会清空,递归过程中栈的深度可能会很深,如果过深的话就会Stack Overflow.如果您之前没有好好学习过这块内容,可以试试搜索引擎查阅'递归原理',或者看看[这篇文章](https://www.bilibili.com/read/cv840653/).

难道已经没救了吗?优雅的递归模型无法满足您作为万人规模企业主的需求了?实事并不是这样的,还有得救.

这里要提到一个概念叫做[尾递归](https://zh.wikipedia.org/wiki/%E5%B0%BE%E8%B0%83%E7%94%A8),关于什么是尾递归可以看 [群众讨论1](https://www.zhihu.com/question/20761771),[群众讨论2](https://stackoverflow.com/questions/33923/what-is-tail-recursion).

反正我们现在的这个递归是符合尾递归的概念的,不过针对尾递归的情况,Java编译器并没有为我们做优化,也就是说在Java环境下,尾递归和普通递归并没有什么区别.但是Scala是能对尾递归做优化的,我们来看看相应的Scala版本.

```scala
  val maxEmployeeId = 9999

  def sendRedPack(currentEmployeeId: Int, price: Int): Unit = {
    val recipientId = (currentEmployeeId + 1) % (maxEmployeeId + 1)
    println(s"员工 $currentEmployeeId 发利是给员工 $recipientId $price 元")
    if (currentEmployeeId > 0) sendRedPack(currentEmployeeId - 1, price + 1)
  }

  sendRedPack(maxEmployeeId, maxEmployeeId)
  //员工 9999 发利是给员工 0 9999 元
  //员工 9998 发利是给员工 9999 10000 元
  //员工 9997 发利是给员工 9998 10001 元
  //员工 9996 发利是给员工 9997 10002 元
  //...
  //员工 4 发利是给员工 5 19994 元
  //员工 3 发利是给员工 4 19995 元
  //员工 2 发利是给员工 3 19996 元
  //员工 1 发利是给员工 2 19997 元
  //员工 0 发利是给员工 1 19998 元
```

问题迎刃而解了,您的企业就算到了一百万人也不怕了!

小提示:

在IDEA中,如果您的递归符合尾递归需求,您定义的递归函数左侧会出现一个带箭头的小圆圈.如果您的递归只是普通递归,您定义的函数左侧会出现一个蚊香形状.

![image-20190905123058619](images/recursive_example.png)

## 习题

1. 牛顿迭代法求平方根,牛顿迭代法的介绍见[这里](https://www.matrix67.com/blog/archives/361),或者你去别的什么地方找也行.

   按照[材料准备](site:/index.md#习题集)里的提示把习题下载到本地,编辑`src/main/scala/quiz/Quiz3.scala`,更改文件中的`???`为您的答案.在项目根目录下执行`./gradlew run --args='quiz.Quiz3'`,获得以下输出说明习题完成.

   ```scala
   牛顿迭代法求平方根,精度为 1.0E-5
   1 的平方根是 1.0
   2 的平方根是 1.4142156862745097
   3 的平方根是 1.7320508100147274
   4 的平方根是 2.0000000929222947
   5 的平方根是 2.2360688956433634
   6 的平方根是 2.4494897427875517
   7 的平方根是 2.6457513111113693
   8 的平方根是 2.8284271250498643
   9 的平方根是 3.000000001396984
   ```

   