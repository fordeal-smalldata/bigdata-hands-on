# Scala 语言入门
Scala是目前在大数据处理技术中广泛应用的语言,现在主流的数据基础设施Spark,Flink,Kafka都使用了大量Scala代码用来做实现或者接口.当然,这些框架同时都会有Java接口,像Spark甚至还有Python和R语言的接口.不会Scala也能正常使用这些设施.但是学习Scala可以让开发者加深对这些设施的理解,更好地利用他们输出成果,甚至说可以帮助软件开发者整体上成为一个更好的程序员,而不只是在数据处理领域.

不过本系列讲义会更关注于数据处理,不会教会您用Scala做到一切事情(可能会列一些参考资料,那些资料能教您做这些),这份讲义假定您已经有一定的Java开发经验,在此基础上进行教学.

## 环境部署
开始学习前我们先安装下必要环境
### Ammonite
[Ammonite](http://ammonite.io/#Ammonite)是一个Scala的REPL环境,可以帮助您快速试验一些简单的Scala代码,安装方式在[这里](http://ammonite.io/#Ammonite-REPL)

在终端敲击`amm`打开Ammonite,键入`1+1`回车,见到`res0: Int = 2`字样就说明已经安装成功了
```scala
➜  ~ amm
Loading...
Welcome to the Ammonite Repl 1.6.8
(Scala 2.13.0 Java 1.8.0_181)
If you like Ammonite, please support our development at www.patreon.com/lihaoyi
renkai-renkai@ 1+1
res0: Int = 2
```
### Jetbrains IDEA
推荐使用[IDEA](https://www.jetbrains.com/idea/download/)加上它官方的用来编辑Scala代码.当然,其实用任何文本编辑器或者IDE都是可以的,但是在强大的IDE帮助下整个过程能够更加得心应手.商业版的功能更强大,不过就教学目的而言,社区版已经足够了.

### 习题集
这份讲义对应的习题集放在[这里](https://github.com/Renkai/bigdata-hands-on-quiz).如果使用IDEA打开这个项目的话,记得导入时候选择`Import project from external model -> Gradle`获得完整功能