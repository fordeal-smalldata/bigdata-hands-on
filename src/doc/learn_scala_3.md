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



### 正则匹配

## 异常处理

## 调用Java代码

## 一些语法糖

## 试试摆脱break

## 尾递归

## 习题

牛顿迭代法求平方根

