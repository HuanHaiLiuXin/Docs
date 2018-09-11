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

## 6.Java对象的内存布局
#### 1.Java中创建对象的方式
1. Object.clone()及反序列化
    - 通过直接复制已有的数据,来初始化新建对象的实例字段
2. new语句和反射
    1. 通过调用构造器来初始化实例字段
    2. 当我们调用构造器,它会优先调用父类的构造器,直至Object类.这些构造器的调用者都是通过new指令新建而来的对象.
        - 通过new指令新建的对象,它的内存其实涵盖了所有父类中的实例字段.包括父类的私有实例字段,或者子类的实例字段隐藏了父类的同名实例字段.但是子类实例还是会为这些父类实例字段分配内存.<br>后面会通过Jol工具查看到.
#### 2.Java对象的内存布局:压缩指针+内存对齐+字段重排列
#### 3.压缩指针
1. 对象头<br>
    - JVM中,每个Java对象都有1个对象头.对象头由 标记字段+类型指针 构成.
        1. 标记字段
            1. 标记字段用于存储JVM关于该对象的的运行数据,如哈希码,GC信息及锁信息
            2. 在64位JVM中,标记字段占64位/8个字节(1个byte占8个二进制位)
        2. 类型指针
            1. 类型指针指向该对象的类
            2. 在64位JVM中,类型指针也占64位/8个字节
        3. 可见,默认情况下,每个对象的对象头就占据了16个字节
2. 压缩指针
> 为了减小内存占用,64位JVM引入了压缩指针的概念,将原本64位的Java对象指针压缩为32位.
1. 对象头的类型指针也被压缩为32位/4个字节.使得对象头的大小从16字节降为12字节.
2. 压缩类型除了应用于对象头类型指针,也应用于引用类型的字段及引用类型的数组.
3. 默认情况下,JVM的32位压缩指针可以寻址到32GB的地址空间,超过32GB就会关闭压缩指针.
#### 4.内存对齐
> 内存对齐:JVM堆中对象的起始地址需要对齐8字节的倍数.
1. JVM堆中对象的起始地址需要对齐8字节的倍数,如果1个对象用不到8N个字节,那么空白的那部分就被浪费了.浪费掉的空间成为对象间的填充
2. 内存对齐不仅存在于对象之间,也存在于对象中的字段之间
    1. JVM要求long,double,以及非压缩指针状态下的引用字段地址为8字节的倍数.
    2. 字段对其的1个原因,是让1个字段只存在于CPU的同1个缓存行中.如果字段没有内存对齐,该字段的读取需要替换两个缓存行,该字段的存储也会污染两个缓存行.跨行存储及跨行读取1个字段对程序效率不利.
#### 5.字段重排列
> 字段重排列:JVM重写分配字段的先后顺序,以达到内存对齐的目的
1. 使用了32位压缩指针的64位JVM,字段重排后第1个字段地址要对齐4字节的倍数;关闭压缩指针的64为JVM,字段重排列后第1个字段地址要对齐8字节的倍数;
2. 如果1个字段占据N字节,那么该字段的偏移量要对齐N字节的倍数.偏移量指该字段地址与对象起始地址的差值.
#### 6.Jol工具查看类中字段的内存分布
[code-tools包含1系列工具,Jol是工具之一](http://openjdk.java.net/projects/code-tools/)
<br>
[jol](http://openjdk.java.net/projects/code-tools/jol/)
<br>
**Jol工具使用方法:**<br>
1. 首先下载jol-cli-0.9-full.jar,已存在[github](https://github.com/HuanHaiLiuXin/Docs/blob/master/jar/jol-cli-0.9-full.jar)
2. 可以在Android Studio/Eclipse中使用,也可以直接在cmd命令行下使用
    1. AS下使用
        1. 将jol-cli-0.9-full.jar放在工程指定模块的libs文件夹下,然后点击右键"Add as Library"添加依赖即可.
        2. 在Java代码中使用jar包含的api查看指定类的内存布局
            ```java
            System.out.println(VM.current().details());
            System.out.println(ClassLayout.parseClass(B.class).toPrintable());
            ```
    2. cmd命令行下使用
        1. 将jol-cli-0.9-full.jar放在Java安装路径下的jre\lib\ext文件夹中.如:"C:\Program Files\Java\jdk1.7.0_80\jre\lib\ext"
        2. 在cmd命令行中直接查看Java内置类.
            ```
            java -cp jol-cli-0.9-full.jar org.openjdk.jol.Main internals java.lang.String
            ```
#### 7.Jol查看指定类的内存布局实例及字段重排列及缓存行
**1.代码实例**
```java
public class A {
	long l;
	int i;
	private int a,b;
}
public class B extends A{
	long l;
	int i;
}
public class JolTest {
    public static void main(String[] args) {
        System.out.println(VM.current().details());
        System.out.println(ClassLayout.parseClass(A.class).toPrintable());
        System.out.println(ClassLayout.parseClass(B.class).toPrintable());
    }
}

打印结果:
//运行于64位HotSpot虚拟机
# Running 64-bit HotSpot VM.
//使用32位压缩指针
# Using compressed oop with 3-bit shift.
# Using compressed klass with 3-bit shift.
//JVM堆中对象内存起始地址对齐8字节的倍数
# Objects are 8 bytes aligned.
# Field sizes by type: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]
# Array element sizes: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]

jt.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
    //对象头占据12个字节,启用压缩指针将对象头大小从16降至12字节
      0    12        (object header)                           N/A
    //发生了字段重排列,i放到了l前面.
    //因为i占用4个字节,偏移量是4字节的倍数,这样可以利用12-16剩下的4个字节空间.
    //如果l放在第1个,l是long,占用8个字节,只能是16-24.就会浪费12-16这4个字节空间.
     12     4    int A.i                                       N/A
     16     8   long A.l                                       N/A
    //a,b都是int,都占用4个字节,所以没必要重排.
     24     4    int A.a                                       N/A
     28     4    int A.b                                       N/A
Instance size: 32 bytes
Space losses: 0 bytes internal + 0 bytes external = 0 bytes total

jt.B object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0    12        (object header)                           N/A
    //B会为其父类A中的同名字段及私有字段分配内存空间     
     12     4    int A.i                                       N/A
     16     8   long A.l                                       N/A
     24     4    int A.a                                       N/A
     28     4    int A.b                                       N/A
    //这里l和i无需重排列,B.l可以完整使用8个字节的连续数据.没有浪费空间
     32     8   long B.l                                       N/A
     40     4    int B.i                                       N/A
     44     4        (loss due to the next object alignment)
Instance size: 48 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
**2.缓存行**<br>
1. CPU缓存
    1. CPU获取内存中的一条数据大概需要200多个CPU周期(CPU cycles), 而从CPU寄存器获取一条数据1个CPU周期就够了
    2. CPU设计者们就给CPU加上了缓存(CPU Cache). 如果你需要对同一批数据操作很多次,那么把数据放至离CPU更近的缓存, 会给程序带来很大的速度提升
    3. 现代CPU的缓存结构一般分三层,L1,L2和L3.
        1. 级别越小的缓存,越接近CPU,意味着速度越快且容量越少
        2. 当CPU运作时,它首先去L1寻找它所需要的数据,然后去L2,然后去L3.
            <br>如果三级缓存都没找到它需要的数据,则从内存里获取数据.
            <br>寻找的路径越长,耗时越长.
            <br>所以如果要非常频繁的获取某些数据,保证这些数据在L1缓存里,这样速度将非常快
    4. CPU到各级缓存及内存的大概速度:
        <table align="center">
            <tr>
                <td>从CPU到</td>
                <td>大约需要的CPU周期</td>
                <td>大约需要的时间(单位ns)</td>
            </tr>
            <tr>
                <td>CPU寄存器</td>
                <td>1 cycle</td>
                <td></td>
            </tr>
            <tr>
                <td>L1 Cache</td>
                <td>~3-4 cycles</td>
                <td>~0.5-1 ns</td>
            </tr>
            <tr>
                <td>L2 Cache</td>
                <td>~10-20 cycles</td>
                <td>~3-7 ns</td>
            </tr>
            <tr>
                <td>L3 Cache</td>
                <td>~40-45 cycles</td>
                <td>~15 ns</td>
            </tr>
            <tr>
                <td>跨槽传输</td>
                <td></td>
                <td></td>
            </tr>
            <tr>
                <td>内存</td>
                <td>~120-240 cycles</td>
                <td>~60-120ns</td>
            </tr>
        </table>
2. CPU缓存行
    1. CPU缓存是由CPU缓存行构成.每个缓存行是64字节.
    2. CPU存取缓存都是1行缓存行1行缓存行这样使用,即 缓存行是CPU存取缓存的最小单位.
    3. JVM要求long,double,以及非压缩指针状态下的引用字段地址为8字节的倍数,也是为了同1字段仅存在于1个CPU缓存行中.
        - 比如1个long类型字段占据8个字节,如果地址不是8字节的倍数,比如是60字节,那么加上占据的8个字节,就超过了64字节,需要占用2个缓存行进行存储,读取时候也要读取两个缓存行.这样对性能就很不利.
    4. 在Java编程中也要注意CPU缓存行.尽量避免跨缓存行读取数据,这样会增加耗时.实例.
        ```java
        public class CpuCacheTest {
            private static final int RUNS = 10;
            private static final int DIMENSION_1 = 1024 * 1024;
            private static final int DIMENSION_2 = 6;
            //longs中每1个一维数组中有6个long.
            //不启用32位压缩指针情况下,每个1维数组大小:16+8*6=64字节,正好占用1个完整CPU缓存行.
            //启用32位压缩指针,占用12+8*6=60字节.则2个一维数组间有4个字节的对象间的填充.
            private static long[][] longs;
        
            public static void main(String[] args) throws Exception {
                Thread.sleep(10000);
                longs = new long[DIMENSION_1][];
                for (int i = 0; i < DIMENSION_1; i++) {
                    longs[i] = new long[DIMENSION_2];
                    for (int j = 0; j < DIMENSION_2; j++) {
                        longs[i][j] = 0L;
                    }
                }
                System.out.println("starting....");
        
                final long start = System.nanoTime();
                long sum = 0L;
                for (int r = 0; r < RUNS; r++) {
                    //1:跨CPU缓存行读取数据,效率低下
                    for (int j = 0; j < DIMENSION_2; j++) {
                        //每个1维数组占据1个CPU缓存行,每次变更i,是跨缓存行读取数据
                        for (int i = 0; i < DIMENSION_1; i++) {
                            sum += longs[i][j];
                        }
                    }
                    //2:同1个CPU缓存行中读取数据,效率高
        //            for (int i = 0; i < DIMENSION_1; i++) {
        //                for (int j = 0; j < DIMENSION_2; j++) {
        //                    sum += longs[i][j];
        //                }
        //            }
                }
                System.out.println("duration = " + (System.nanoTime() - start));
            }
        }
        
        打印结果:
        1:跨CPU缓存行读取数据,效率低下
        starting....
        duration = 336318104
        2:同1个CPU缓存行中读取数据,效率高
        starting....
        duration = 90164258
        ```

## 7.JVM的垃圾回收
#### 1.什么是垃圾
> 对JVM而言,垃圾就是已经死亡的对象所占用的堆空间.

#### 2.如何辨别一个对象已经死亡:可达性分析算法
> 目前JVM采用的垃圾回收算法是可达性分析算法.

1. GC Roots:由堆外指向堆内的引用
2. 可达性分析算法的实质
    - 将一系列的GC Roots作为初始的存活对象集合,然后探索所有能被该集合引用到的对象,并将其加入到该集合中,这个过程称为标记.最终未被标记的对象就是死亡的,其占用的堆空间可以被回收

#### 3.JVM传统的垃圾回收流程:Stop-the-world
1. 安全点:找到1个稳定的执行状态,在这个状态下,JVM的堆栈不会发生变化.
2. Stop-the-world
    1. 当JVM收到Stop-the-world请求,会等待所有的非垃圾回收线程到达安全点,然后将所有线程暂停.
    2. 在到达安全点后,JVM的堆栈不会发生变化,再让垃圾回收线程利用可达性分析算法对JVM堆中的存活对象进行标记,未被标记的死亡对象占用的空间/垃圾就被回收.
    3. 垃圾回收结束后,被暂停的线程继续执行

#### 4.JVM垃圾回收的3种方式:清除,压缩,复制
1. 清除
    - 会造成内存碎片.因为JVM堆中对象必须是连续分布的,可能出现总内存空间足够,但实际无法为新建对象分配内存的情况.
2. 压缩
    - 将存活的对象聚集到内存区域的起始位置,留下一段连续的内存空间.避免内存碎片,但压缩算法性能较差.
3. 复制
    - 将内存区域2等分,用2个指针from和to来维护,并只用from指针指向的区域来分配内存.
    - 当发生垃圾回收时,将存活的对象复制到to,并交换from和to的指针.
    - 避免了内存碎片,但堆空间的使用效率低下.
4. 清除,压缩,复制图示
![](https://user-gold-cdn.xitu.io/2018/9/6/165ada3a67cf5591?w=1668&h=540&f=png&s=13687)
![](https://user-gold-cdn.xitu.io/2018/9/6/165ada37a2bfdd4e?w=1676&h=532&f=png&s=14177)
![](https://user-gold-cdn.xitu.io/2018/9/6/165ada3da32a23da?w=1670&h=578&f=png&s=19612)

#### 5.JVM的堆划分
![](https://user-gold-cdn.xitu.io/2018/9/10/165c20f53d3c0cf9?w=1526&h=514&f=png&s=21652)
JVM的堆被划分为新生代和老年代.其中新生代又包括Eden区及from和to两个大小相同的Survivor区.
<br>
1. 每次新建对象都会在Eden区划分一块空间进行存储.当Eden区的空间耗尽,就会触发1次MinorGC.
2. Minor GC就是收集新生代的垃圾并清除.存活下来的对象,则被送入Survivor区.
    1. Eden区和from区中的存活对象被复制到to中
    2. 然后交换from和to指针,保证to指向的Survivor区还是空的
    3. JVM会记录Survivor区中的对象一共被来回复制了几次,如果超过15次(不同虚拟机参数不同),则该存活对象晋升到老年代;另外如果单个Survior区已经被占用了50%(不同虚拟机参数不同),则较高复制次数的对象也会被晋升至老年代.
3. Minro GC避免了对整个堆进行垃圾回收.理想情况下,Eden区中的对象基本都死亡了,那么需要复制的数据极少性能很高.
4. JVM的分代垃圾回收基于1个前提:大部分对象只存活一小段时间,小部分对象却存活一大段时间.

## 8.HotSpot虚拟机的intrinsic
**对于Java API已经存在的功能,即使我们可以实现类似的功能,在class文件层面和Java源码很类似,实际执行效率也有天壤之别.**
1. Java API中部分方法会被@HotSpotIntrinsicCandidate注解所标注
2. 在HotSpot虚拟机中,被@HotSpotIntrinsicCandidate注解所标注的方法都是HotSpot intrinsic.<br>
对于HotSpot intrinsic方法,HotSpot虚拟机额外维护了一套高效的实现,这些高效实现依赖具体的CPU指令.
3. 所以对于API已有的功能不要自己尝试去实现,即使源码很类似,但在JVM层面完全无法使用HotSpot虚拟机对原生API的高效实现,性能相对是很低的.
