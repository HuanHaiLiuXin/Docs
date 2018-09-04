## 1.Java代码是怎么运行的
#### 1.Java代码运行于Java虚拟机中
#### 2.Java代码为何要运行在虚拟机中
1. 之所以Java代码要运行于虚拟机中,是为了实现其可移植性/跨平台.只要Java代码被编译为字节码(.class文件),就可以在不同平台的Java虚拟机上运行;"一次编写,到处运行".
2. Java虚拟机还提供了1个代码托管的环境,替我们实现自动内存管理和垃圾回收等功能.
#### 3.Java代码执行大致流程
1. 首先通过编译期将Java源文件编译为Java虚拟机可以识别的字节码(.class文件).
2. Java虚拟机加载class文件,加载后的Java类会被存放于方法区.实际运行时,Java虚拟机会执行方法区中的代码.
    1. 从硬件角度看,Java字节码无法直接执行,JVM需要将字节码翻译成机器码
    2. 标准JDK的HotSpot虚拟机采用了2种方式将字节码翻译成机器码
        1. 解释执行:即逐条将字节码翻译成机器码并执行
        2. 即时编译/JIT:即将1个方法中所有的字节码翻译成机器码后再执行
            1. 即时编译的时间开销
                - 解释执行流程:输入的代码 -> [ 解释器 解释执行 ] -> 执行结果
                - 即时编译流程:输入的代码 -> [ 编译器 编译 ] -> 编译后的代码 -> [ 执行编译后的代码 ] -> 执行结果
                - 说JIT比解释快,说的是“执行编译后的代码”比“解释器解释执行”要快,并不是1次即时编译流程比1次解释执行流程更快.对“只执行一次”的代码而言,解释执行其实总是比JIT编译执行要快
                - 即时编译后的代码会保存在内存中,只有对频繁执行的代码,JIT编译才能保证有正面的收益
            2. 即时编译的空间开销
                - 对一般的Java方法,即时编译后代码的大小相对于字节码的大小,膨胀比达到10x是很正常
                - 只有对执行频繁的代码才值得编译,如果把所有代码都编译则会显著增加代码所占空间,导致“代码爆炸”
        3. 解释执行的优势在于节约内存,无需等待编译;即时编译的优势在于频繁调用的代码字节码翻译成机器码后再执行,实际运行速度更快,但内存开销更大.
        4. 为了提高运行效率,HotSpot采用的是一种混合执行的策略:它会解释执行字节码,然后会将其中反复执行的热点代码,以方法为单位进行即时编译,翻译成机器码后直接运行在底层硬件之上.
![](https://user-gold-cdn.xitu.io/2018/8/27/1657add6d3729800?w=1918&h=1076&f=png&s=121552)
#### 4.JVM中的 运行时内存区域 结构
![](https://user-gold-cdn.xitu.io/2018/8/27/1657a64e8cabf87f?w=1916&h=1074&f=png&s=122919)
**方法区和堆是所有线程共享;PC寄存器,Java方法栈,本地方法栈是每个线程私有.**<br>
1. 方法区
    1. 方法区Method Area是各个线程共享的内存区域,必须保证线程安全
        > 如两个类要同时加载1个尚未被加载的类C,1个类会请求ClassLoader去加载类C,另1个类只能等待C加载完成而不会重复加载
    2. 方法区存储已被加载的类的信息,常量,静态变量,即时编译器编译后的代码等.
2. 堆
    1. 堆是JVM管理的内存中最大的一块,被所有线程共享,在JVM启动时创建,非线程安全.
    2. 堆存在的目的就是为了存放对象实例.
    3. 堆是垃圾收集器管理的主要区域.从内存回收的角度看,现在的垃圾收集器都基于分代回收算法,Java堆可以进一步细分.
3. PC寄存器/程序计数器
    1. PC寄存器是一块较小的内存空间,用于记录当前线程正在执行的虚拟机字节码的地址;
    2. Java虚拟机的多线程是通过线程轮流切换分配处理器执行时间来实现,在任何1个确定时刻,一个CPU/多核CPU中的1个核只能执行1条线程的指令.
    3. 当有多个线程交叉执行时,被中断的线程当前执行到哪个字节码内存地址必然要保存下来,以便用于被中断的线程恢复执行时继续执行.每个线程都有一个独立的PC寄存器,多线程之间互不影响.
4. Java方法栈
    1. Java方法栈是线程私有的,所以不需要关心数据一致性,也不存在同步锁问题.
    2. 每当创建1个线程,JVM就会为其创建对应的Java方法栈.
    3. Java方法栈中包含多个栈帧.
        1. 每个栈帧关联了1个Java方法,每运行1个方法,就创建1个栈帧,栈帧包含了关联方法的信息.
        2. 每当1个方法执行完毕,该栈帧就会弹出栈帧的元素作为方法的返回值,并清理栈帧.Java方法栈的栈顶就是当前正在执行的方法,PC寄存器记录的也是这个地址.
        3. Java方法栈的栈顶的栈帧A调用新方法,会创建新的对应栈帧B压入栈顶,B执行完毕后,B被移除,B的返回值作为A的1个操作数,继续执行A
    4. JVM规范规定了Java方法栈中的2中异常
        1. 如果线程请求的栈深度大于虚拟机所允许的深度,将抛出StackOverflowError异常
        2. 如果虚拟机可以动态扩展，如果扩展时无法申请到足够的内存，就会抛出OutOfMemoryError异常
5. 本地方法栈/Native Method Stack
    1. 本地方法栈Java方法栈所发挥的作用是非常相似的,其区别不过是Java方法栈为虚拟机执行Java方法服务,而本地方法栈则是为虚拟机使用到的Native方法服务
    2. 本地方法栈也会抛出StackOverflowError和OutOfMemoryError异常

## 2.Java的基本类型
#### 1.JVM的boolean类型
1. 在JVM规范中,boolean类型被映射为int类型.true被映射为1,false被映射为0.
2. 实例:
    ```java
    public class T3 {
    	boolean tag = true;
    	public static void main(String[] args) {
    		T3 t = new T3();
    		t.t();
    	}
    	private void t(){
    	        //1:tag不是0吧?
    		if(tag){
    			System.out.println("tag不是0");
    		}
    		//2:tag真的是1吗?
    		if(true == tag){
    			System.out.println("tag真的是1");
    		}
    	}
    }
    
    打印结果:
    tag不是0
    tag真的是1
    ```
    在经过编译后,上面的代码对于JVM的实际逻辑是:<br>
    1. tag不是0吧?如果tag对应的是2,3等其他非0整数,也一样会打印
    2. tag真的是1吗?因为true在JVM中对应的就是1,只要tag对应的整数不是1,就无法和true(1)相等.
#### 2.Java的基本类型
Java的8种基本类型,在JVM内存中,8种基本类型的默认值都是0
#### 3.Float.NaN很特殊:Float.NaN不和任何float值相等,包括自身,除了“!=”始终返回true,所有其他比较结果都会返false
```java
float f = Float.intBitsToFloat(0x7fc00000);
System.out.println("f:"+f);
System.out.println("NaN>=1.0F:"+(f>=1.0F));
System.out.println("NaN<=1.0F:"+(f<=1.0F));
float NaN = Float.NaN;
System.out.println("NaN==Float.NaN:"+(NaN==Float.NaN));
System.out.println("NaN!=Float.NaN:"+(NaN!=Float.NaN));

打印结果:
f:NaN
NaN>=1.0F:false
NaN<=1.0F:false
NaN==Float.NaN:false
NaN!=Float.NaN:true
```
## 3.JVM类加载过程
#### 1.Java语言的类型
1. 8种基本类型
    ```java
    public class PrimitiveTypeTest {
        public static void main(String[] args) {
            // byte
            System.out.println("基本类型：byte 二进制位数：" + Byte.SIZE);
            System.out.println("包装类：java.lang.Byte");
            System.out.println("最小值：Byte.MIN_VALUE=" + Byte.MIN_VALUE);
            System.out.println("最大值：Byte.MAX_VALUE=" + Byte.MAX_VALUE);
            System.out.println();

            // short
            System.out.println("基本类型：short 二进制位数：" + Short.SIZE);
            System.out.println("包装类：java.lang.Short");
            System.out.println("最小值：Short.MIN_VALUE=" + Short.MIN_VALUE);
            System.out.println("最大值：Short.MAX_VALUE=" + Short.MAX_VALUE);
            System.out.println();

            // int
            System.out.println("基本类型：int 二进制位数：" + Integer.SIZE);
            System.out.println("包装类：java.lang.Integer");
            System.out.println("最小值：Integer.MIN_VALUE=" + Integer.MIN_VALUE);
            System.out.println("最大值：Integer.MAX_VALUE=" + Integer.MAX_VALUE);
            System.out.println();

            // long
            System.out.println("基本类型：long 二进制位数：" + Long.SIZE);
            System.out.println("包装类：java.lang.Long");
            System.out.println("最小值：Long.MIN_VALUE=" + Long.MIN_VALUE);
            System.out.println("最大值：Long.MAX_VALUE=" + Long.MAX_VALUE);
            System.out.println();

            // float
            System.out.println("基本类型：float 二进制位数：" + Float.SIZE);
            System.out.println("包装类：java.lang.Float");
            System.out.println("最小值：Float.MIN_VALUE=" + Float.MIN_VALUE);
            System.out.println("最大值：Float.MAX_VALUE=" + Float.MAX_VALUE);
            System.out.println();

            // double
            System.out.println("基本类型：double 二进制位数：" + Double.SIZE);
            System.out.println("包装类：java.lang.Double");
            System.out.println("最小值：Double.MIN_VALUE=" + Double.MIN_VALUE);
            System.out.println("最大值：Double.MAX_VALUE=" + Double.MAX_VALUE);
            System.out.println();

            // char
            System.out.println("基本类型：char 二进制位数：" + Character.SIZE);
            System.out.println("包装类：java.lang.Character");
            // 以数值形式而不是字符形式将Character.MIN_VALUE输出到控制台
            System.out.println("最小值：Character.MIN_VALUE="
                    + (int) Character.MIN_VALUE);
            // 以数值形式而不是字符形式将Character.MAX_VALUE输出到控制台
            System.out.println("最大值：Character.MAX_VALUE="
                    + (int) Character.MAX_VALUE);
        }
    }

    打印结果:
    基本类型：byte 二进制位数：8
    包装类：java.lang.Byte
    最小值：Byte.MIN_VALUE=-128
    最大值：Byte.MAX_VALUE=127

    基本类型：short 二进制位数：16
    包装类：java.lang.Short
    最小值：Short.MIN_VALUE=-32768
    最大值：Short.MAX_VALUE=32767

    基本类型：int 二进制位数：32
    包装类：java.lang.Integer
    最小值：Integer.MIN_VALUE=-2147483648
    最大值：Integer.MAX_VALUE=2147483647

    基本类型：long 二进制位数：64
    包装类：java.lang.Long
    最小值：Long.MIN_VALUE=-9223372036854775808
    最大值：Long.MAX_VALUE=9223372036854775807

    基本类型：float 二进制位数：32
    包装类：java.lang.Float
    最小值：Float.MIN_VALUE=1.4E-45
    最大值：Float.MAX_VALUE=3.4028235E38

    基本类型：double 二进制位数：64
    包装类：java.lang.Double
    最小值：Double.MIN_VALUE=4.9E-324
    最大值：Double.MAX_VALUE=1.7976931348623157E308

    基本类型：char 二进制位数：16
    包装类：java.lang.Character
    最小值：Character.MIN_VALUE=0
    最大值：Character.MAX_VALUE=65535
    ```
2. 引用类型
    1. 类
    2. 接口
    3. 数组类
    4. 泛型参数
3. JVM中存在几种:3种
    1. 8种基本类型是由JVM预先定义好的
    2. 泛型参数会在编译过程中被擦除
    3. JVM中只存在 类,接口,数组类
        1. 数组类由JVM直接生成
        2. 类,接口则有对应的字节流
        3. 字节流最常见的形式就是.class文件
#### 2.从.class文件到内存中的类,按先后顺序需要经过 加载-->链接-->初始化 3大步骤
1. 加载:就是查找字节流,并且根据字节流创建类的过程
    1. 上面可知,基本类型JVM预先定义好了,泛型参数被擦除,数组类由JVM直接生成,因而记载涉及的只是: 类 及 接口.
    2. JVM需要借助类加载器完成查找字节流的过程
    3. 最重要的3个类加载器
        1. 启动类加载器:boat class loader
            1. 是所有类加载器的祖师爷;
            2. 除了启动类加载器,其他类加载器都是java.lang.classLoader的子类,都需要由另外的类加载器,比如boot class loader将其加载到JVM中,才能执行类的加载;
            2. 在Java 9之前,启动类加载器用于加载最基础,最重要的类
            3. 从Java 9开始引入模块系统,启动类加载器只加载少数几个关键模块
        2. 扩展类加载器:extension class loader
            1. 扩展类加载器的父类加载器是 启动类加载器.
            2. 在Java 9之前,扩展类加载器用于加载相对重要,但又通用的类
            3. 从Java 9开始引入模块系统,扩展类加载器改名为 平台类加载器:platform class loader,除了少数几个关键模块由启动类加载器加载,其他模块都由平台类加载器加载
        3. 应用类加载器:application class loader
            1. application class loader的父类加载器是 extension class loader
            2. 负责加载应用程序路径下的类,默认情况下,应用程序中包含的类就由应用类加载器加载
    4. 除了加载功能,类加载器还提供了命名空间的作用.
        - 在JVM中,类的唯一性是由类加载器实例及类的全名共同确定.即使是同一串字节流,由2个类加载器加载,JVM也将其当做两个不同的类.
    5. 类加载器加载类有1个规则:双亲委派模型
        1. 如果一个类加载器收到类加载的请求,它首先不会自己去尝试加载这个类,而是把这个请求委派给父类加载器完成.每个类加载器都是如此,只有当父加载器在自己的搜索范围内找不到指定的类时,子加载器才会尝试自己去加载.
        2. 双亲委派模型具体流程
            1. 当application class loader收到1个类C的加载请求,它首先不会自己尝试加载C,而是委派给父类加载器 extension class loader去加载C
            2. 当extension class loader收到C的加载请求,它首先也不会自己尝试加载,而是委派给父类加载器 boot class loader去加载C
            3. boot class loader没有父类加载器,直接尝试加载C.
                - 如果C加载成功,加载过程就完成
                - 如果C加载失败,则丢回给子类 extension class loader 加载
            4. extension class loader尝试加载C.
                - 如果C加载成功,完成
                - 如果C加载失败,则丢回给子类 application class loader 加载
            5. application class loader尝试加载C.
                - 如果C加载成功,完成
                - 如果C加载失败,则使用自定义加载器加载
            6. 自定义类加载器尝试加载C
                - 如果C加载成功,完成
                - 如果C加载失败,则抛出ClassNotFoundException异常
        3. 双亲委派模型的意义:确保了类加载器加载类的优先级,防止恶意伪造的系统类被加载,保证了安全.
2. 链接:是指将创建成功的类合并至JVM中,使之能够执行的过程.分为 验证,准备,解析 三个阶段.
    1. 验证
        - 验证的目的是确保被加载的类满足JVM的约束条件,一般Java编译器生成的类文件必然满足约束条件.
    2. 准备
        - 准备阶段的目的:
            1. 为被加载的类的静态字段分配内存:仅仅是分配内存,静态字段具体初始化/赋值则是在初始化阶段进行
            2. 构造与该类相关联的方法表,方法表用于实现JVM的动态绑定
    3. 解析
        1. 在class文件被加载至JVM之前,这个类C无法知道其他类O及O中的方法和字段对应的地址,甚至不知道自己的方法,字段的地址;每当需要引用这些还不知道地址的成员,Java编译器会生成1个符号引用.
        2. 解析阶段的作用,正是将这些符号引用解析为实际引用.
            1. 比如符号引用指向了1个未被加载的类S,或执行S的字段或方法,那么解析阶段将触发S的加载.
            2. 仅仅触发符号引用指向的未被加载的类的加载,未必会触发其 链接 及 初始化.
3. 初始化:类加载的最后一步即为 初始化
    1. Java类中的8种基本类型或字符串,如果是static final的,这样的成员被Java编译器标记为常量值;
    2. Java类中的其他静态成员及静态代码块中的代码,会被Java编译器置于同1个方法中,方法名为<clinit>
    3. 初始化,就是为标记为常量值的字段赋值,并执行<clinit>方法.JVM会通过加锁来保证<clinit>方法仅被执行一次.
    4. 只有初始化完成后,类才正式成为可执行状态.
    
## 4.JVM是如何执行方法调用的
#### 1.Java及JVM是如何识别目标方法的
1. Java中方法存在重载及重写的概念
    1. 重载:方法名相同而参数类型不同的方法之间的关系
    2. 重写:父类和子类间,方法名和参数类型都相同的方法间的关系:Override
2. JVM识别目标方法,除了方法名和参数类型,还包括返回类型
    1. 在同1个类中,如果出现多个方法的方法名及参数类型及返回类型相同,在链接过程的验证阶段会报错.
    2. 重载的方法在Java编译器的编辑阶段已经完成了识别,因而可以认为JVM中不存在重载.
    3. JVM通过 静态绑定 和 动态绑定 来识别目标方法
        1. Java字节码中与调用相关的指令共5种:
            1. invokestatic:用于调用静态方法
            2. invokespecial:
                1. 调用私有实例方法,构造函数
                2. 使用super调用父类的实例方法和构造函数
                3. 调用所实现的接口的默认方法.
            3. invokevirtual:用于调用非私有实例方法
            4. invokeinterface:用于调用接口方法
            5. invokedynamic:用于调用动态方法
        2. 静态绑定
            1. 在链接/解析阶段便能直接识别目标方法的情况称为静态绑定.
            2. 对于invokestatic和invokespecial而言,JVM能够直接识别具体的目标方法.
                1. 静态方法是属于类的,因而可以确定
                2. super调用父类的实例方法及构造函数,即使其父类继续super,递归也可以找到对应层级父类的具体方法.
        3. 动态绑定
            1. 需要在运行过程中根据调用者的动态类型来识别目标方法的情况称为动态绑定
            2. 对于invokevirtual和invokeinterface而言,JVM需要在执行过程中根据调用者的动态类型,确定具体的目标方法.
                1. invokevirtual对应非私有实例方法.子类重写,所以调用者到底是当前类实例还是哪一个子类实例,决定着具体目标方法.
                2. invokeinterface对应接口方法.接口方法可以被多个实现类实现.具体目标方法也需要由调用者属于哪个实现类决定.
        4. 动态绑定,静态绑定 与 在JVM类加载过程的链接/解析阶段 的关系
            - 在class文件中,Java编译器会用符号引用指代目标方法.而链接/解析阶段作用就是将符号引用解析为实际引用.
                1. 对于静态绑定的方法调用而言,实际引用是目标方法的指针
                2. 对于需要动态绑定的方法调用,实际引用是1个方法表的索引.
                    - 方法表后面会详述
#### 2.JVM中的虚方法调用的具体实现
1. JVM中的虚方法调用
    - Java里所有非私有实例方法调用都会被编译成invokevirtual指令,而接口方法调用都会被编译成invokeinterface指令.这两种指令,均属于JVM中的虚方法调用.
2. 虚方法调用的目标方法,通过动态绑定确定
    1. JVM的动态绑定是通过方法表实现
        - JVM根据调用者的动态类型,在其对应的方法表中,根据索引值获取到目标方法
    2. 如果虚方法调用指向1个标记为final的方法,JVM则静态绑定该final方法
    3. 相对于静态绑定的非虚方法调用,虚方法调用更加耗时.
3. 方法表
    1. 方法表分类:
        1. invokevirtual:使用虚方法表(virtual method table,vtable)
        2. invokeinterface:使用接口方法表(interface method table,itable)
    2. 方法表本质:
        - 方法表本质上是1个数组,每个数组元素指向1个当前类及其祖先类中非私有实例方法.
    3. 方法表特征:
        1. 子类方法表中包含父类方法表中的所有方法
        2. 子类方法在方法表中的索引值,与它所重写的父类方法的索引值相同.
    4. JVM类加载的 链接/解析 阶段:符号引用解析成实际引用
        1. 对于静态绑定的方法调用,实际引用指向具体的目标方法
        2. 对于动态绑定的方法调用,实际引用是方法表的索引
    5. 方法表实例
        ```java
        //父类
        public class People{
            public void eat(){}
            public void drink(){}
        }
        //2个子类:白人,黑人
        public class White extends People{
            @Overrride
            public void eat(){}
            @Overrride
            public void drink(){}
        }
        public class Black extends People{
            @Overrride
            public void eat(){}
            @Overrride
            public void drink(){}
        }
        //调用
        public meetOnePeople(People p){
            p.eat();
            p.drink();
        }
        meetOnePeople(new People());
        meetOnePeople(new White());
        //meetOnePeople(new Black())原理和meetOnePeople(new White())类似
        meetOnePeople(new Black());
        ```
        1. 父类People的方法表包含2个元素:0-eat,1-drink;
        2. 子类White和Black方法表中包含People方法表中所有方法,且方法索引值和重写的父类一致,都是:0-eat,1-drink;
        3. meetOnePeople(new People())
            - JVM获取到调用者实际类型是People,获取到People对应的方法表,eat索引值是0,drink索引值是1,根据索引值获取到目标方法并执行;
        4. meetOnePeople(new White())
            - JVM获取到调用者实际类型是White,获取到White对应的方法表,eat索引值是0,drink索引值是1,根据索引值获取到目标方法并执行;
4. JVM中JIT编译器(Just-In-Time Compiler/即时编译器)对动态绑定的优化
    1. 方法内联:method inlining
    2. 内联缓存:inlining cache
5. 内联缓存的定义及规则
    - 内联缓存就是缓存虚方法调用中调用者的动态类型及对应方法;之后执行过程中,如果匹配到已缓存的动态类型,则直接调用已缓存的目标方法,如果匹配失败,则退化至动态绑定方式.
6. 内联缓存的分类
    1. 单态内联缓存
        - 只缓存了1种动态类型及其对应的目标方法.如果和调用者类型匹配,则直接调用对应的目标方法
    2. 多态内联缓存
        - 缓存有限种类的动态类型及对应目标方法.需要逐个和调用者进行比较,命中则调用对应的目标方法
    3. 超多态内联缓存:就是原始动态绑定
7. JVM中的JIT编译器默认的内联缓存策略:<br>
    - 为节省内存,默认采取单内联缓存,保存调用者的动态类型及对应的目标方法;
    - 当碰到新的调用者,如果类型匹配,则直接调用缓存的目标方法,不匹配则劣化至超多态内联缓存,在今后的执行过程中直接使用方法表进行动态绑定.

## 5.JVM如何处理异常
#### 1.异常的分类:所有异常直接或间接继承于Throwable
![](https://user-gold-cdn.xitu.io/2018/8/31/1658edb0e26e30a1?w=798&h=545&f=png&s=30000)
#### 2.异常处理的2大要素是 抛出异常 和 捕获异常.这两大要素共同实现程序流的非正常转移.
1. 抛出异常的方式分为显式和隐式
    1. 显式抛异常
        - 显式抛异常的主体是应用程序,在应用程序中使用throw关键字手动抛出异常实例.
    2. 隐式抛异常
        - 隐式抛异常的主体是JVM,在JVM执行过程中,碰到无法继续执行的异常状态,自动抛出异常.
2. 捕获异常涉及 try,catch,finally 3个代码块
    1. try代码块:用来标记需要进行异常监控的代码
    2. catch代码块:用来捕获在try代码块中触发的1种或多种指定类型的异常.
        1. try后面可以跟多个catch代码块
        2. 每个catch代码块可以用来捕获1种或多种指定类型异常
        3. JVM会从上到下,从左到右匹配异常.前面的异常类型不能包含后面的,否则报错.
            ```java
            public void t4(){
        		try {
        		} 
        		//每个catch代码块可以捕获1种或多种类型异常
        		catch (NullPointerException | IOException e) {
        		} 
        		catch (IndexOutOfBoundsException e){
        		}
        		//这里会报错:
        		Unreachable catch block for ArrayIndexOutOfBoundsException. 
        		It is already handled by the catch block for IndexOutOfBoundsException
        		catch (ArrayIndexOutOfBoundsException e){
        		}
        		finally{}
        	}
            ```
    3. finally代码块:跟在try和catch之后,用来声明1段必定会运行的代码.finally的设计初衷是为了避免跳过某些关键的清理代码,如关闭已打开的系统资源.
#### 3.异常实例的构造十分昂贵/耗费性能
因为在构造异常实例时,JVM需要生成该异常的栈轨迹(stack trace).JVM会逐一访问当前线程的Java方法栈的栈帧,并记录各种调试信息:
- 栈帧指向的方法名字
- 栈帧指向的方法的所在的类名,文件名
- 在代码的第几行触发该异常
- 代码实例:
    ```java
    public class ExceptionCreate {
    	public static void main(String[] args) {
    		ExceptionCreate c = new ExceptionCreate();
    		c.t();
    	}
    	public void t(){
    		t1();
    	}
    	public void t1(){
    		t2();
    	}
    	public void t2(){
    		t3();
    	}
    	int[] a = new int[]{};
    	public void t3(){
    		int o = a[3];
    	}
    }
    
    Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 3
    	at github.com.HuanHaiLiuXin.ExceptionCreate.t3(ExceptionCreate.java:19)
    	at github.com.HuanHaiLiuXin.ExceptionCreate.t2(ExceptionCreate.java:15)
    	at github.com.HuanHaiLiuXin.ExceptionCreate.t1(ExceptionCreate.java:12)
    	at github.com.HuanHaiLiuXin.ExceptionCreate.t(ExceptionCreate.java:9)
    	at github.com.HuanHaiLiuXin.ExceptionCreate.main(ExceptionCreate.java:6)
    ```
#### 4.JVM如何捕获异常
1. 首先对比1下源码和编译后的class文件
    1. 源码:
        ```java
        public class TException {
        	public static void main(String[] args) {
        		try {
        			TException t = new TException();
        			t.t1();
        		} catch (Exception e) {
        			System.out.println("捕获到异常");
        		}
        	}
        	public void t1(){
        		try {
        			t3();
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
        	public void t2(){
        		try {
        			int a = 0;
        		} catch (NullPointerException | IndexOutOfBoundsException e) {
        			e.printStackTrace();
        		} finally{
        			System.out.println("t2 finally");
        		}
        	}
        	public void t3(){
        		throw new RuntimeException("抛出运行时异常");
        	}
        }
        ```
    2. class文件
![](https://user-gold-cdn.xitu.io/2018/8/31/1658edbea1c7835b?w=945&h=2115&f=png&s=50700)
2. 在class文件中,每个含有catch方法块或finally方法块的方法都包含1个异常表/Exception table.
    1. 每个异常表包含多个条目,每个条目包含from,to,target,type
        ```
        Exception table:
       from    to  target type
           0     2     5   Class java/lang/NullPointerException
           0     2     5   Class java/lang/IndexOutOfBoundsException
           0     2     9   any
           5     6     9   any
           9    10     9   any
        ```
        1. from:代码监控范围从索引为from的字节码开始
        2. to:代码监控范围从索引为to的字节码结束,不包括to
        3. target:这个异常处理器从索引为target的字节码开始
        4. type:该异常处理器所捕获的异常类型
    2. 每个try+catch块生成异常表中的1个条目;
    3. 每个finally块生成异常表中的3个条目;见图:![](https://user-gold-cdn.xitu.io/2018/8/31/1658ef58dd0c94be?w=988&h=325&f=png&s=24615)
3. JVM捕获异常过程
    - 程序触发异常时,JVM会在异常发生的方法对应的异常表中从上到下进行遍历.
        - 当触发异常的字节码索引值在异常表某个条目的from和to范围内,JVM会判断抛出的异常类型和该条目的type是不是匹配.
            - 如果匹配,JVM会将控制流转移至该条目target对应的字节码
        - 如果当前异常表所有条目都不匹配,则会弹出当前方法对应的Java栈帧.并在当前方法的调用方法中重复上面过程.
            - 最坏情况下,JVM需要遍历当前线程的Java方法栈上所有栈帧对应的方法的异常表.
#### 5.原本的异常被忽略问题
1. 1个finally代码块会编译为异常表中3个条目<br>
第3个条目用于捕获try触发但catch未捕获的异常并抛出,或捕获catch代码块触发的异常并抛出.<br>
如果catch代码块捕获了try代码块触发的异常A,且catch代码块触发了异常B,那finally代码块捕获并重新抛出的异常是:B!!!!<br>
如何解决原本的异常被忽略的问题?
2. Java 7引入了try-with-resources语法糖.可以解决1中的问题.
    1. Java 7引入了Supressed异常来解决1中的问题,允许开发人员将1个异常付于另1个异常之上.因此抛出的异常可以附带多个异常信息.
    2. try-with-resources在字节码层面自动使用Supressed异常.try-with-resources除了自动使用Supressed异常,更重要的是精简了资源打开关闭的用法.<br>
    try-with-resources示例:![](https://user-gold-cdn.xitu.io/2018/8/31/1658f1e4d2cd1eb7?w=704&h=944&f=png&s=25395)
