# 大数据场景下利用DeltaLake on 对象存储代替 HDFS 的调研

## 对象存储为什么值得

1. 整个可用区的用户共享一个存储资源池,资源利用率可以达到非常高的程度,集群规模增长也很稳定,云服务上可以很精确地规划扩容来控制成本.相比之下,用户自己扩容各自的HDFS集群就要保留更多余量,为不使用的部分付出更多租金.
2. 在云上建HDFS集群成本高于自有机房.以AWS为例,AWS提供的大容量硬盘是EBS([https://aws.amazon.com/cn/ebs/?ebs-whats-new.sort-by=item.additionalFields.postDateTime&ebs-whats-new.sort-order=desc](https://aws.amazon.com/cn/ebs/?ebs-whats-new.sort-by=item.additionalFields.postDateTime&ebs-whats-new.sort-order=desc)),EBS的可用性非常高,远超自建机房的普通硬盘,但是损坏了依然会导致数据丢失,所以组HDFS还是需要保持多副本,同时没有因为EBS的可用性高获得更多益处,但要支付相应的成本.
3. 对象存储可以利用专用硬件,我透过咨询搞对象存储的朋友得知,对象存储使用的是高密度硬盘(可能是指瓦楞式硬盘 [https://zhuanlan.zhihu.com/p/57800929](https://zhuanlan.zhihu.com/p/57800929)),专门硬件可以大幅降低单位容量/带宽的成本,但是改写/移动/追加数据的代价较大,会在一定程度上影响对象存储服务上层的API设计.
4. 人力投入方面,HDFS需要有掌握相应技能的人值班来解决业务迭代变更中不定时发生的OOM,Crash,不当使用之类的问题.在业务导向的公司,除非业务规模非常大,值得自己维持团队维护,否则这块工作内容更适合交给专门的技术服务公司来处理.相比之下对象存储按需付费,高可用的特性对于中小团队来说是非常省心的选择.

在我们的使用场景下,相同的数据量,对象存储相对HDFS的数据存储成本大约是1:10,对象存储的性能综合而言低于HDFS,但是满足大部分场景的使用要求.

## 对象存储的代价

对象存储在大数据场景使用上相对于HDFS的一些缺点应当是由于设计取向(超大规模的集群,所有用户共享一个池子,高可用作为第一优先级)和特定硬件(高密度硬盘,廉价的空间和带宽作为第一优先级)决定的,可以认为这些缺点会长期存在. 注: 我只实际用过S3,下面说的缺点也是针对S3

1. 最终一致性: S3只实现了最终一致性([https://docs.aws.amazon.com/zh_tw/AmazonS3/latest/dev/Introduction.html](https://docs.aws.amazon.com/zh_tw/AmazonS3/latest/dev/Introduction.html)),个人认为这是为了在超大集群规模(整个AWS可用区)下实现高可用([https://aws.amazon.com/cn/s3/storage-classes/?nc1=h_ls](https://aws.amazon.com/cn/s3/storage-classes/?nc1=h_ls))和可接受的性能做出的取舍.带来的问题是写入后不能期望直接得到最新版本的数据,需要使用者自行等待和校验最新数据是否到位.
2. 元信息获取性能差,S3的目录list操作几百毫秒只能返回一千个对象,在对象数量多的情况下性能会难以令人接受.这应该也是为了在大集群规模下实现高可用做出的取舍.
3. Rename代价大,S3上对文件进行重命名的时候会真的将数据本身进行位移,从用户的视角看就是较大的时间消耗和额外的计费.这也会导致一些在HDFS上流行的处理逻辑不再适用,需要重新开发(比如处理过程中的文件用 `_` 开头,处理完结了再将文件重命名)
4. 没有跨对象的事务: 这个问题其实HDFS也有,但是在原因3的影响下,一旦出现任务失败,从失败中恢复的代价S3是高于HDFS的.

## 解决方案

在巨大的成本差异的诱惑下和对象存储一些显而易见的缺陷的驱动下,一些公司发展出了专门的处理对象存储上的大数据解决方案,包括

- Deltalake(Databricks开源 [https://delta.io/](https://delta.io/))
- Apache Hudi(Uber开源 [https://hudi.apache.org/](https://hudi.apache.org/))
- Apache Iceberg(Netflix开源 [https://iceberg.apache.org/](https://iceberg.apache.org/))

三者的设计思路非常相似,其中Deltalake发布了论文来公开他们的思路,论文可以在 [https://databricks.com/wp-content/uploads/2020/08/p975-armbrust.pdf](https://databricks.com/wp-content/uploads/2020/08/p975-armbrust.pdf) 找到,解决对象存储相应问题的核心思路有以下这些:

1. 针对最终一致性问题,DeltaLake生成的新文件名和旧文件名之间是严格紧凑地递增的,也就是说,如果目录中还存在这新文件,DeltaLake是知道新文件的文件名到底是什么的,S3针对新文件写入的情况,会让客户端等待而不是返回一个空结果.另外DeltaLake也不会改变已有文件的内容.通过这两点DeltaLake保障客户端不在最终一致性模型下读到错误的数据.
2. 针对元信息获取性能查的情况,DeltaLake在生成数据的过程中就将数据元信息整理好放在单独的文件中,这样在需要的时候一次get操作就能获得必要的元信息
3. 针对rename代价大的问题,DeltaLake利用元信息告知客户端哪些文件是已经就绪的可以使用的,不rename
4. DeltaLake实现了跨对象事务,具体可见论文

## 使用DeltaLake一些额外的好处和代价

DeltaLake 除了针对性地解决对象存储的问题之外,还带来了一些额外的好处:

1. DeltaLake的文件结构可以认为是基于不可变数据集做的可持久化数据结构,可以在较低的存储代价下实现历史版本的存储(类似git),论文中称为time travel
2. DeltaLake的数据结构同时也对Change Data Capture (CDC)更加友好,比如类似在MySQL数据同步到Hive这样的场景下,以往我们会在旧的快照和新的binlog merge之后重写整张hive表,利用DetlaLake可以将新的binlog作为一个patch打到旧的表上产生一个新版本,整体消耗降低,在同样的计算资源下我们可以选择加快同步频率来提升同步的实时性
3. 批流一体: 在论文中我们可以看到DeltaLake实现了对简单的流数据场景的适配,不过仅限于使用Spark Structured Streaming,在对数据实时性不高的情况下,能够使用流处理的语义编写计算逻辑,实时性要求较高时不适用,但也足够处理很多场景

完成这些需要的代价是:

1. 客户端需要一些额外的计算来获取数据,因为需要先获取一次元数据得到数据的分布情况,然后再解析获取的对象得到有效的按列分布的数据.
2. 保证元信息的可靠性变得更重要,元信息丢失的话,几乎无法通过扫描目录恢复全部或者局部的数据.
3. 生态需要建设,读取DeltaLake的客户端(Presto,Hive,Spark,Flink)等需要各自单独开发

## 总结

云环境下利用对象存储处理大数据的未来很香,值得投入积极的建设工作来实现😃

## 参考资料

- Databricks关于S3的介绍博客 [https://databricks.com/blog/2017/05/31/top-5-reasons-for-choosing-s3-over-hdfs.html](https://databricks.com/blog/2017/05/31/top-5-reasons-for-choosing-s3-over-hdfs.html)
- DeltaLake主页 [https://delta.io/](https://delta.io/)
- Apache Hudi主页 [https://hudi.apache.org/](https://hudi.apache.org/)
- Apache Iceberg主页[https://iceberg.apache.org/](https://iceberg.apache.org/)
- DeltaLake 论文 [https://databricks.com/wp-content/uploads/2020/08/p975-armbrust.pdf](https://databricks.com/wp-content/uploads/2020/08/p975-armbrust.pdf)
- DeltaLake实现CDC相关的介绍 [https://databricks.com/session_na20/simplify-cdc-pipeline-with-spark-streaming-sql-and-delta-lake](https://databricks.com/session_na20/simplify-cdc-pipeline-with-spark-streaming-sql-and-delta-lake)
- 网上发现的不错的公有云对象存储比较 [https://xuanwo.io/2020/03/03/object-storage/](https://xuanwo.io/2020/03/03/object-storage/)

有问题欢迎在 [https://github.com/fordeal-smalldata/bigdata-hands-on/issues](https://github.com/fordeal-smalldata/bigdata-hands-on/issues) 讨论