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



### 正则匹配

## 异常处理

## 调用Java代码

## 一些语法糖

## 试试摆脱break

## 尾递归

## 习题

牛顿迭代法求平方根

