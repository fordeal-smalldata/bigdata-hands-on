# Scala入门(二)

## 函数

在编程中,为了能够做出更好的抽象,我们往往会把代码块整理成函数,这里介绍下在Scala中怎样定义一个函数

```scala
def add(a:Int,b:Int) = a + b
```

这是一个简化的定义,我们没有给出返回值的类型,编译器自动为我们做了这件事😄,现在我们试试在Ammonite中试一试这个函数

```scala
➜  ~ amm
Loading...
Welcome to the Ammonite Repl 1.6.8
(Scala 2.13.0 Java 1.8.0_181)
If you like Ammonite, please support our development at www.patreon.com/lihaoyi
renkai-renkai@ def add(a:Int,b:Int) = a + b
defined function add

renkai-renkai@ add(1,1)
res1: Int = 2
```

为了了解函数定义更多一点,我们再来看下`add`的完整写法

```scala
  def add(a: Int, b: Int): Int = {
    a + b
  }
```

和简化的写法相比,上面代码中的`: Int `定义了返回值的类型,花括号中包含了函数的所有类型,其中最后一行(这个例子里正好只有一行)表达式的值表示函数的返回值.在编写复杂函数的时候,建议先把返回值类型写明,这样实现逻辑的时候编译器就能帮你排错啦.

### 关键词`return`通常是是不需要的

很多语言都使用`return`关键词表示函数的返回值,出于照顾群众习惯的考虑,Scala也保留了这个关键词.但是,这通常是不必要的.比如像下面这样定义一个函数,功能和上面两个版本一样,但是编译器会给出一个`Warning: Return keyword is redundant`.

```scala
  def add(a: Int, b: Int): Int = {
    return a + b // Return keyword is redundant
  }
```

定义函数的时候,如果感觉确实离不开`return`,也不要气馁,用就用吧.但是同时记得考虑下是不是能通过充分利用表达式的特性把`return`去掉.下面是比较使用和不使用`return`关键词来实现函数分支操作的一个例子.

```scala tab=使用return
  def concat(a: String, b: String): String = {
    if (a == null || b == null)
      return null
    return a + b
  }
```

```scala tab=不使用return
  def concat(a: String, b: String): String = {
    if (a == null || b == null) null
    else a + b
  }
```

### 高阶函数

在Scala里,函数可以作为另一个函数的参数使用,在下方的例子中,`callFunc`被称为[高阶函数](https://zh.wikipedia.org/wiki/%E9%AB%98%E9%98%B6%E5%87%BD%E6%95%B0).

```scala
  def square(x: Int): Int = x * x
  def cube(x: Int): Int = x * x * x
  def callFunc(x: Int, func: Int => Int) = func(x)
```

我们在Ammonite中操作试试

```scala
➜  ~ amm
Loading...
Welcome to the Ammonite Repl 1.6.8
(Scala 2.13.0 Java 1.8.0_181)
If you like Ammonite, please support our development at www.patreon.com/lihaoyi
renkai-renkai@   def square(x: Int): Int = x * x
defined function square

renkai-renkai@   def cube(x: Int): Int = x * x * x
defined function cube

renkai-renkai@   def callFunc(x: Int, func: Int => Int) = func(x)
defined function callFunc

renkai-renkai@   callFunc(3,square)
res3: Int = 9

renkai-renkai@   callFunc(3,cube)
res4: Int = 27
```

高阶函数在Scala的标准库中广泛使用,为我们编程带来了很多便利,下一节我们学习Scala内置数据结构的时候就能看到.

## 数据结构

### `Array`

Scala的`Array`和Java中的`Array`底层实现是一样的,但是增加了很多便利的接口可以操作或者生成数据

```scala
  val arr = Array(1, 2, 3) // arr: Array[Int] = Array(1, 2, 3)
  arr(0) // res1: Int = 1
  arr(0) = 3
  arr(0) // res3: Int = 3
  arr.mkString(",") // res4: String = "3,2,3"
```

上方代码展示的是一些最简单的操作,更多操作可以查看[官网文档](https://www.scala-lang.org/api/2.13.0/scala/Array.html)进行学习

### `Vector`

`Vector`是Scala实现的一个高性能数据结构,能在接近常数复杂度的时间内实现序列的拼接,随机获取等.需要注意的是,这个数据结构是不可变([Immutable](https://docs.scala-lang.org/overviews/collections-2.13/overview.html))的,一个`Vector`生成后就不能改变它的值.关于不可变数据结构的进一步了解可以参考[这篇](https://hackernoon.com/how-immutable-data-structures-e-g-immutable-js-are-optimized-using-structural-sharing-e4424a866d56)和[这篇](https://zhuanlan.zhihu.com/p/27133830)文章,很多同学可能不习惯使用不可变数据结构进行编程,不要担心,当您习惯用表达式进行编程时也会同时习惯不可变数据结构.

```scala
  val vec1 = Vector(1, 2, 3) ++ Vector(4, 5, 6)
  //vec1: Vector[Int] = Vector(1, 2, 3, 4, 5, 6)
  val vec2 = vec1 :+ 7
  //vec2: Vector[Int] = Vector(1, 2, 3, 4, 5, 6, 7)
  vec2(0)
  //res4: Int = 1
  val vec3 = 0 +: vec2
  //vec3: Vector[Int] = Vector(0, 1, 2, 3, 4, 5, 6, 7)
  vec3(0)
  //res3: Int = 0
```

更多操作见[官方文档](https://www.scala-lang.org/api/2.13.0/scala/collection/immutable/Vector.html)

### `Seq`

`Seq`是Scala实现的一个序列类型接口,`Array`和`Vector`都实现了`Seq`接口,在函数定义中把参数或者返回值类型设置为`Seq`,然后用`Array`或者`Vector`实现,方便以后有重构需求的时候不用变更函数签名.

```scala
  val arrSeq: Seq[Int] = Array(1, 2, 3)
  val vecSeq: Seq[Int] = Vector(1, 2, 3)
  val listSeq: Seq[Int] = List(1, 2, 3)
  //编译通过
```

### `Set`

`Set`应该大家都很熟了,是保证内部每个元素都唯一的集合,下方展示一些常用操作

```scala
  val set1 = Set(1, 2, 3)
  // set1: Set[Int] = Set(1, 2, 3)
  val set2 = set1 + 1
  // set2: Set[Int] = Set(1, 2, 3)
  val set3 = set2 + 4
  // set3: Set[Int] = Set(1, 2, 3, 4)
  val set4 = Set(3, 4, 5)
  // set4: Set[Int] = Set(3, 4, 5)
  val set5 = set1.intersect(set4)
  // set5: Set[Int] = Set(3)
  val set6 = set1.union(set4)
  // set6: Set[Int] = HashSet(5, 1, 2, 3, 4)
```

### `Map`

`Map`是由键值对组成的集合,它保证内部每个键都是唯一的,下方展示一些常用的操作

```scala
  val map1 = Map(1 -> 2, 3 -> 4, 5 -> 6)
  // map1: Map[Int, Int] = Map(1 -> 2, 3 -> 4, 5 -> 6)
  val map2 = map1 + (1 -> 3)
  // map2: Map[Int, Int] = Map(1 -> 3, 3 -> 4, 5 -> 6)
  val map3 = map2 - 1
  // map3: Map[Int, Int] = Map(3 -> 4, 5 -> 6)
  map3(3)
  // res17: Int = 4
  map3.get(3)
  // res18: Option[Int] = Some(4)
  map3.get(8)
  // res19: Option[Int] = None
	map3.getOrElse(8,666)
	// res20: Int = 666
```

这里需要注意的是,类似`map3(3)`这样的操作在没有相应值的时候会抛异常,而不是返回一个`null`,所以一般不推荐使用,推荐使用的方法是`map3.get(3)`,`get`方法不会直接返回一个`Int`,而是返回一个`Option[Int]`,这里的设计思想我们在下一节介绍

### `Tuple`

当您需要定义一个有多个返回值的函数时,`Tuple`能帮您大幅简化代码量,比如说下方的例子里`currentYearMonthDay`定义了一个返回值为`Tuple3[Int,Month,Int]`的函数.如果您有需要的话,可以无痛定义返回值在`Tuple2`到`Tuple22`间的各种函数,详细介绍可以看[官网文档](https://docs.scala-lang.org/tour/tuples.html).需要注意的是,`Tuple`中的每个元素类型都是独立的,建议在定义函数一开始就设计好每个元素的类型以充分使用编译器的排错能力

```scala
  import java.time._
  def currentYearMonthDay(): (Int, Month, Int) = {
    val now = LocalDateTime.now()
    (now.getYear, now.getMonth, now.getDayOfMonth)
  }

  val yearMonthDay = currentYearMonthDay()
  // yearMonthDay: (Int, Month, Int) = (2019, AUGUST, 20)
  yearMonthDay._1
  // res3: Int = 2019
  yearMonthDay._2
  // res4: Month = AUGUST
  yearMonthDay._3
  // res5: Int = 20
  val (year, month, day) = yearMonthDay
  //year: Int = 2019
  //month: Month = AUGUST
  //day: Int = 20
```

### 通用操作

Scala标准库中有许多通用的操作,`Array`,`Vector`,`Set`,`Map`等等都能用,下面以Vector为例展示这些操作.

```scala
  val vec = Vector(1, 2, 3, 4, 5)
  // vec: Vector[Int] = Vector(1, 2, 3, 4, 5)
  val vec2 = Vector("a", "b", "c", "d", "e", "f", "g")
  // vec2: Vector[String] = Vector("a", "b", "c", "d", "e", "f", "g")
  vec.take(3)
  // res2: Vector[Int] = Vector(1, 2, 3)
  vec.drop(3)
  // res3: Vector[Int] = Vector(4, 5)
  vec.filter(x => x % 2 == 0)
  // res4: Vector[Int] = Vector(2, 4)
  vec.map(x => x.toString)
  // res5: Vector[String] = Vector("1", "2", "3", "4", "5")
  vec.reverse
  // res6: Vector[Int] = Vector(5, 4, 3, 2, 1)
  val grouped = vec.groupBy(x => x % 2 == 0)
  // grouped: Map[Boolean, Vector[Int]] = HashMap(false -> Vector(1, 3, 5), true -> Vector(2, 4))
  grouped(true)
  // res8: Vector[Int] = Vector(2, 4)
  grouped(false)
  // res9: Vector[Int] = Vector(1, 3, 5)
```

除了上述通用的操作外,标准库还提供了便利的方法让这些数据结构相互转换,通常这种操作叫`toXXX`,勇敢地使用ide的自动提示就能发现他们.

```scala
  val tuples = vec.zip(vec2)
  // tuples: Vector[(Int, String)] = Vector((1, "a"), (2, "b"), (3, "c"), (4, "d"), (5, "e"))

  val tuplesMap = tuples.toMap
  // tuplesMap: Map[Int, String] = HashMap(5 -> "e", 1 -> "a", 2 -> "b", 3 -> "c", 4 -> "d")

  val tuplesSeq = tuplesMap.toSeq
  // tuplesSeq: Seq[(Int, String)] = List((5, "e"), (1,  "a"), (2, "b"), (3, "c"), (4, "d"))

	val arrToSet =   Array(1,2,3,4,4).toSet
	// arrToSet: Set[Int] = Set(1, 2, 3, 4)
```

 对数据做聚合也是数据处理中经常用到的技巧,比较有代表性的就是`reduce`和`fold`,详细一些的介绍可以看[这篇](https://www.geeksforgeeks.org/scala-reduce-fold-or-scan/)文章或者上一些[主流视频平台搜索](https://www.youtube.com/results?search_query=scala++reduce+fold)获得

```scala
  val reduced = vec.reduce {
    (x: Int, y: Int) => x.max(y)
  }
  //reduced: Int = 5

  val folded = vec.foldLeft("values of vector:") {
    (str, num) => str + " " + num
  }
  // folded: String = "values of vector: 1 2 3 4 5"
```

Spark也好,Flink也好,它们的API几乎就是个大号的Scala标准库,也就是在标准库的基础上帮您做了一些分布式,高并发,异步处理,背压之类的工作.对于用户而言,用起来是差不多的,所以掌握好标准库对您之后操作那些设施很有帮助,建议多多练习.

## Billion Dollar Mistake

如果您不知道什么是Billion Dollar Mistake,详细的来龙去脉可以看看这篇[InfoQ文章](https://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare/).[Tony Hoare](https://zh.wikipedia.org/wiki/%E6%9D%B1%E5%B0%BC%C2%B7%E9%9C%8D%E7%88%BE)在1965年将空指针功能加入了他所设计的ALGOL W编程语言中,"只是因为这样比较好实现".这一行为引起了C/C++/Java等等等等语言的效仿.Tony Hoare认为他所设计的空指针功能从那时开始到现在,至少为人民群众带来了十亿美金的损失(主要是运行时的进程崩溃带来的),进行了忏悔.

忏悔都忏悔了,损失已经造成了,那我们要怎样带着历史包袱前进,来应对空指针可能带来的损失呢?Scala给我们提供了一个工具叫`Option`.

举个例子,在Java中您想获得一个字符串的长度,可以这么定义一个函数

```java
    public static int lenOfString(String str) {
        return str.length();
    } // 版本1
```

一些比较年轻,比较简单,有时还很幼稚的小朋友可能觉得这个定义没什么问题,可一到实际试用中就会出事

比如当一个空值传入的时候

```java
        String a = null;
        System.out.println("length of a: " + lenOfString(a));
        //Exception in thread "main" java.lang.NullPointerException
        //at NullSample.lenOfString(NullSample.java:5)
        //at NullSample.main(NullSample.java:15)
```

为了避免这种情况,一些长者会这么写

```java
    public static int lenOfNullableString(String str) {
        if (str == null) return 0;
        return str.length();
    }
```

这样确实可以避免null异常发生.可是它有一个问题,那就是它不是强制的,依据每个人的工作环境不同,没做空值检查的代码可能会获得Warning,也可能不会,(就算会有Warning,很多人也会被过多Warning伤害从而无视Warning).当项目中有稍许基础需要做空检查的时候,大部分人都能做好,可是当对空检查的需求到达几十次,几百次的时候,人就多多少少会有遗漏,那些遗漏可能带来大量的运行时损失.

Scala因为继承了JVM的设计,也是允许`null`值出现的,但是您在Scala原创的代码中几乎总是有很好的方法避免使用`null`值,需要处理`null`值的情况主要出现在调用Java代码的情况下.

如果我们用`Option(...)`包住一个`null`值,我们就会自动得到一个`None`,如果我们用`Option(...)`包住一个非`null`值,我们就会得到一个`Some(...)`

```scala
  def mockSomeJCodeMayNull(giveMeNull: Boolean) = {
    if (giveMeNull) null else "not null"
  }

  val nullToOpt = Option(mockSomeJCodeMayNull(true))
  // nullToOpt: Option[String] = None
  val nonNullToOpt = Option(mockSomeJCodeMayNull(false))
  // nonNullToOpt: Option[String] = Some("not null")
```

↑↑↑↑↑上方就是把可能为`null`的值变成`Option`的玩法啦,既然已经知道怎么把可能为`null`的值转成`Option`了,下面就展示些代码看我们怎么可以和`Option`玩耍吧.

`Option`很常见的一种应用场景是处理`Map`中可能不包含相应键的情况

```scala
  val squares = Map(1 -> 1, 2 -> 4, 3 -> 9, 4 -> 16)
  val squares2 = squares(2)
  // squares2: Int = 4 直接取出相应值,有可能抛出NoSuchElementException,不建议使用
  val squares5 = squares(5)
  // java.util.NoSuchElementException: key not found: 5
  val squares2Opt = squares.get(2)
  // squares5Opt: Option[Int] = None 使用get方法,没有相应键的时候不是抛异常,而是返回一个None
  val squares5Opt = squares.get(5)
  val squares5WithDefault = squares.getOrElse(5, 25)
  // squares5WithDefault: Int = 25 如果没有相应键,就采用我们填入的默认值,这里是25
  // 下方可以看getOrElse具体的实现方法 ↓↓↓
  //  def getOrElse[V1 >: V](key: K, default: => V1): V1 = get(key) match {
  //    case Some(v) => v
  //    case None => default
  //  }
```

在一般集合中,我们也能用`Option`来帮忙应对它们可能没有某些符合要求的值和可能为空的情况

```scala
  val vec = Vector(1, 2, 3, 4, 5)
  val findAOdd = vec.find(x => x % 2 != 0)
  //findAOdd: Option[Int] = Some(1)
  val findGt8 = vec.find(x => x > 8)
  //findGt8: Option[Int] = None
  val emptyVec = Vector.empty[Int]
  val headOfVec = vec.headOption
  //headOfVec: Option[Int] = Some(1)
  val headOfEmptyVec = emptyVec.headOption
  //headOfEmptyVec: Option[Int] = None
```

为什么`Option`可以帮助我们避免运行时空指针错误呢?秘诀就是它把错误都提前到了编译时,因为`Option[Int]`不是`Int`,`Option[String]`也不是`String`,您把`Option`中的内容取出来前,无法正确地用`Int`或`String`或别的什么类型做出任何进一步操作,否则编译器就会报错.下面展示一下有哪些方法可以合适地操作`Option`.

* 给出一个默认值

  ```scala
    val noneOrElse = None.getOrElse(2)
    // noneOrElse: Int = 2
    val someOrElse = Some(1).getOrElse(2)
    // someOrElse: Int = 1
  ```

* 把`Option`也当做一个集合,集合通用的各种操作都能在`Option`中使用

  ```scala
    val square10Opt = Some(10).map(x => x * x)
    //square10Opt: Option[Int] = Some(100)
    val intNone: Option[Int] = None
    val squareNone = intNone.map(x => x * x)
    //squareNone: Option[Int] = None
    val someForeach = Some(10).foreach {
      x => println(s"这个Option里包含了整数 $x")
    }
    //输出: 这个Option里包含了整数 10
    val nonForeach = intNone.foreach {
      x => println(s"这个Option里包含了整数 $x")
    }
    //什么也没输出
  
  //可以和别的类型的集合组合
    val vec1 = Vector(1,2,3) ++ Option(4)
    //vec1: Vector[Int] = Vector(1, 2, 3, 4)
    val vec2 = Vector(1,2,3) ++ None
    //vec2: Vector[Int] = Vector(1, 2, 3)
  ```
  

一点小提示: 刚接触`Option`的同学可能出现一个非常依赖`getOrElse`的情况,这样就可以从`Option`的危险区回落到自己习惯的基本类型安全区了.需要这样做并不丢人,但要多思考一下自己是不是没有充分利用Scala的表达式能力,以及`Option`作为集合的各种特性.

温馨提示: 对各种API的功能有怀疑的时候,多查[官方文档](https://www.scala-lang.org/api/2.13.0/index.html).

## 习题

按照[材料准备](site:/index.md#习题集)里的提示把习题下载到本地,编辑`src/main/scala/quiz/Quiz2.scala`,更改文件中的`???`为您的答案.在项目根目录下执行`./gradlew run --args='quiz.Quiz2'`,获得以下输出说明习题完成.

* 喜欢挑战的同学可以考虑不看参考代码,直接编写逻辑生成下列内容.
  
```scala
100以内的素数有:
[2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97]

   2019年1季度日历
         1月
日 一 二 三 四 五 六
      01 02 03 04 05
06 07 08 09 10 11 12
13 14 15 16 17 18 19
20 21 22 23 24 25 26
27 28 29 30 31

         2月
日 一 二 三 四 五 六
               01 02
03 04 05 06 07 08 09
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28

         3月
日 一 二 三 四 五 六
               01 02
03 04 05 06 07 08 09
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28 29 30
31
```

参考答案在`src/main/scala/answer/Answer2.scala`中

* 发现一套不错的线上习题集,建议尝试: [https://www.scala-exercises.org/std_lib](https://www.scala-exercises.org/std_lib)