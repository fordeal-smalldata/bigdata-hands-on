# Scala入门(一)

## 值和变量

Scala中有两个关键词,`val`(value)和`var`(variable),分别用来定义一个值和变量,一个值大体上可以等价于Java中带final修饰符的变量.下方展示了Scala中定义值和变量的方法以及在Java中大致等价的代码.

```scala tab=Scala
val a = 3
var b = "string"
```

```java tab=Java
final int a = 3;
String b = "string";
```

## 类型

![image](images/tumblr_lylhmlhD061qd12yo.png)

上面一幅图介绍了现在各个主流编程语言的静态类型/动态类型,强类型/弱类型属性,关于这幅图片的原始文章可以看[这里](https://dustyprogrammer-blog.tumblr.com/post/16746798643/should-your-start-up-go-static-or-dynamic),网上还有许多讨论,比如[这里](https://segmentfault.com/a/1190000012372372)和[这里](https://www.zhihu.com/question/19918532),本文不再赘述.我主要想介绍的是,Scala其实是静态强类型语言,很多缺少了解的人粗略看一些源码,见到一个`var ...`就认为是JavaScript那样的动态弱类型语言,这个理解是错误的. 

为什么一个静态强类型语言会容易被人误认为是弱类型语言呢?这其中的关键在于[类型自动推导](https://zh.wikipedia.org/wiki/%E7%B1%BB%E5%9E%8B%E6%8E%A8%E8%AE%BA).有更多兴趣的人可以看看[知乎相关文章](https://www.zhihu.com/search?type=content&q=%E7%B1%BB%E5%9E%8B%E6%8E%A8%E5%AF%BC).如果您关心C++和Java的话,应该知道[C++11](https://en.cppreference.com/w/cpp/language/auto)和[Java11](https://openjdk.java.net/projects/amber/LVTIFAQ.html)都已经有了类型自动推导的特性,遗憾的是,由于实际工作环境中的历史遗留问题,很多C++和Java用户不知道什么时候才能真的用上这些特性.

在Scala里情况就好多了, Scala从最早的版本开始就支持类型自动推导,您可以打开Ammonite试试键入`val a=3`,和`val b:Int = 3`还有`val c: String = 3`看看结果.

```scala
➜  ~ amm
Loading...
Welcome to the Ammonite Repl 1.6.8
(Scala 2.13.0 Java 1.8.0_181)
If you like Ammonite, please support our development at www.patreon.com/lihaoyi
renkai-renkai@ val a = 3
a: Int = 3

renkai-renkai@ val b: Int = 3
b: Int = 3

renkai-renkai@ val c: String = 3
cmd2.sc:1: type mismatch;
 found   : Int(3)
 required: String
val c: String = 3
                ^
Compilation Failed
```

您可以看到,定义一个`val a = 3`,编译器会自动推导出`a`的类型是`Int`,如果您希望在代码里指定类型好在阅读源码的时候获得一些安全感,Scala当然也是支持的`val b: Int = 3`的效果和`val a = 3`效果一样,区别在于编译器不再为您做自动推导而是做一个检查类型是否匹配的工作,如果类型匹配没有问题,则相安无事.如果类型匹配出问题了,编译器会在编译期就抛出错误,从而避免更大的损失在运行期发生. 

