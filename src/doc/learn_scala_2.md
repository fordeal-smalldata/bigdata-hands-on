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



## Billion Dollar Mistake