# 读写分离思想在JDK中的实现及缺点


## 常见的读写分离
通常在做持久层优化的时候，把对数据库的读操作`select` 和写操作`update,delete,insert`分开来，在不同的数据库上进行，当然这些库之间的数据是同步的。这样一来可以规避很多数据库锁的问题，例如mysql的innodb引擎，当你update一条数据时候，会锁住这行数据，这时候这条数据的查询就需要等待锁。如果读写分开的话，就不会在锁等待上耗时，提高了性能。

**缺点：**

* 占用了更多的空间，因为分开了多个库，每个库都冗余有相同数据。也就是软件中常见的**空间换时间**思路
* 会存在数据一致性问题，毕竟多个库之间数据同步会存在时间差，或者失败情况。

为了提高性能，进行了数据冗余，冗余又带来一致性问题，为了解决一致性又搞了分布式事务，加了事务又影响性能问题，即所谓的**CAP问题**。


-------
## JDK中的CopyOnWrite容器

在高并发多线程环境下，JDK中的容器类，例如`ArrayList,HashSet,HashMap`都不是线程安全的，如果加锁的话，性能上又大大折扣，这时候利用读写分离的思想，JDK为我们提供了一组`CopyOnWrit容器类`，方便应用在**读多写少**的业务场景。

其实现方式的就是读写分离常见的策略：写时复制。写数据的时候复制一份进行写操作，读还走原来的那份数据。

例如下面JDK中`CopyOnWriteArrayList`类的add方法源码：

```java

	//注意volatile声明
   private volatile transient Object[] array;
 
    public boolean add(E e) {
		//写时候加锁
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            //复制一份数据
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        } finally {
            lock.unlock();
        }
    }

    final Object[] getArray() {
        return array;
    }

	//由于array声明是volatile，所以修改饮用后，其他线程立即可见。
    final void setArray(Object[] a) {
        array = a;
    }
```

注意上面成员变量`array` 声明了volatile类型，同时add方法中加了乐观锁，避免多个add操作时候，复制很多array出来。

**缺点**

* 由于写操作都会复制一份数据出来，所以会占用跟多内存。
* 适用于读多写少场合，而且写尽可能的调用批量写接口addAll（），避免来回复制数组。
* 有理论上的一致性问题，但通常具体业务中很少会涉及到这里的一致性。
例如，同一个用户先添加后查询，添加不成功，用户是不会到类似‘已发布’的页面去查看发布结果的，其操作本身就有时间上顺序。
而不同用户的场景，例如，商家上架商品，用户查看店铺商品，由于信息本来就不对称，商家多上架几秒，少几秒根本不影响，用户也感知不到。 
