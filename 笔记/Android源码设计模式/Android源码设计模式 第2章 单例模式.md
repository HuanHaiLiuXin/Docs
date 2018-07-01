## 背景知识
### 1.synchronized 详解
- synchronized是Java中的关键字,利用锁机制来实现同步
- synchronized使用位置
    - 修饰代码块
        - 1:synchronized(this|Object){}     修饰对象获取的是对象锁
        - 2:synchronized(类.class){}        修饰类.class获取的是类锁
    - 修饰方法
        - 3:synchronized static method(){}      修饰静态方法获取的是类锁
        - 4:synchronized method(){}             修饰非静态方法获取的是对象锁
- 每个对象有1个对象锁,1个类下所有对象共用1个类锁
- 线程A获取了类锁,在A释放类锁前,其他线程无法获取到类锁
- 同1个类的多个对象,其多个对象锁彼此独立
    - 线程A获取了S1的对象锁,在A释放对象锁之前,其他线程无妨获取到S1的对象锁
    - 线程A获取了S1的对象锁,不影响其他线程获取S2,S3---的对象锁
- 对象锁和类锁彼此独立
    - 线程A获取了S1的对象锁,可以同时获取S1所属类的类锁
    - 线程A获取了S1的对象锁,线程B可以获取S1所属类的类锁
### 2.static 详解
#### static使用场景
static:静态的,可以用于 变量,方法,代码块,内部类,静态导包
1. 修饰变量:static修饰的变量为静态变量
    ```java
    public static int a = 1000;
    ```
2. 修饰方法:static修饰的方法为静态方法
    ```java
    public static void printS(){}
    ```
3. 修饰代码块:static修饰的代码块为静态代码块
    ```java
    static{
        System.out.println("静态代码块");
    }
    ```
4. 修饰内部类:static修饰的内部类为静态内部类
    ```java
    public class ParentClass{
        ****
        public static class ChildClass{
            public static ParentClass parent = new ParentClass();
        }
    }
    ```
5. 静态导包
    - 调用静态方法,访问静态变量,常用形式是:类名.func()或String s = 类名.s;
    - 静态导包后,可以就可以不写类名.
        - 静态导包的方法： import static 包名.类名.静态成员(静态变量或静态方法)
        - 静态导包后,可以直接调用静态方法或使用静态变量
        ```java
        package com.jet.designpattern.c2;
        
        public class Order{
            public static String s = "Order.s";
            public static void printS(){
                System.out.println(s);
            }
        }
        
        //静态导包
        import static com.jet.designpattern.c2.Order.printS;
        import static com.jet.designpattern.c2.Order.s;
        public class TestOrder {
            public static void main(String[] args) {
                //静态导包后,直接调用静态方法名 或 使用静态变量即可
                printS1();
                System.out.println(s1);
            }
        }
        ```
#### static特性
1. 静态变量也称类变量,当1个实例修改了此属性,所有实例的当前属性同步变化.静态变量的加载早于对象,随类的加载而加载,在内存中仅1份
2. 静态方法也称类方法,随类的加载而加载,内存中仅1份
3. 静态方法内部只能访问静态成员,不能访问非静态成员
4. 静态方法内部不能使用this及super
5. 静态代码块也随类的加载而加载,也仅被加载1次,
6. 存在多个静态代码块,则按照书写顺序依次执行,内部仅能访问静态成员
7. 只有在实例化时,非静态成员才开始加载,非静态代码块及非静态变量间无加载优先顺序,谁写在前面就先加载谁
#### static问题
1. 非静态内部类不能持有静态变量,为何?
    ```java
    public class Parent{
        public class Child{
            直接报错:Inner classes cannot have static declarations
            public static TestOrder testOrder = new TestOrder();
        }
    }
    ```
    非静态内部类Child本质上是外部类Parent的一个非静态变量,必须在外部类实例创建后才能加载;<br/>
    而JVM则要求testOrder(所有静态变量)必须在实例创建前就加载,前后矛盾;
2. 静态内部类可以持有静态变量,为何?
    - 静态内部类不持有外部类的引用
    - 静态内部类的加载不依赖外部类的实例化
    - 创建外部类实例,不会引起静态内部类的加载
    - 调用外部类静态或非静态方法,不涉及静态内部类,也不引起静态内部类的加载
    - 静态内部类只有被直接调用才加载类,并加载其持有的静态变量
3. static方法和static代码块的区别?
    - static方法被调用时候才执行
    - static方法块在类被加载的时候自动执行
4. static有关的加载顺序:[实例化一个对象(类加载)的执行顺序详解](https://blog.csdn.net/u013182381/article/details/74574278)
    ```java
    public class Test {
        public Test() {
            System.out.println("test constructor");
        }
        {
            System.out.println("test {}1");
        }
        Person person = new Person("Test");
        static{
            System.out.println("test static");
        }
        {
            System.out.println("test {}2");
        }
        public static void main(String[] args) {
            new MyClass();
        }
    }
     
    class Person{
        {
            System.out.println("person {}1");
        }
        static{
            System.out.println("person static");
        }
        {
            System.out.println("person {}2");
        }
        public Person(String str) {
            System.out.println("person "+str);
        }
    }
     
    class MyClass extends Test {
        public MyClass() {
            System.out.println("myclass constructor");
        }
        {
            System.out.println("myclass {}1");
        }
        Person person = new Person("MyClass");
        static{
            System.out.println("myclass static");
        }
        {
            System.out.println("myclass {}2");
        }
    }
    
    打印结果:
    test static
    myclass static
    test {}1
    person static
    person {}1
    person {}2
    person Test
    test {}2
    test constructor
    myclass {}1
    person {}1
    person {}2
    person MyClass
    myclass {}2
    myclass constructor
    ```
    一个类实例化顺序是:<br/>
    非静态成员只有在实例化时候才开始加载,非静态变量和非静态代码块加载顺序无优先级别,谁写在前面谁先加载.
    
    1.父类多个static变量按顺序加载<br/>
    2.父类多个static代码块按顺序加载<br/>
    3.当前类多个static变量按顺序加载<br/>
    4.当前类多个static代码块按顺序加载<br/>
    5.父类非静态成员按顺序加载<br/>
    6.父类构造函数<br/>
    7.当前类非静态成员按顺序加载<br/>
    8.当前类构造函数
5. 静态常量和类加载的关系
    1. 静态常量:static final 修饰的数据只占据一段不能改变的存储空间,被称为静态常量
    2. 静态常量分为 编译时常量 和 运行时常量
        - 编译时常量:在编译期就可以确定值,并不依赖于类.调用不引起类的加载
        - 运行时常量:在运行时才可以确定值,依赖于类的加载
    3. 编译时常量条件:[Java Compile-time Constant 编译时常量](https://www.jianshu.com/p/f24e49efec19)
    ```
    1. 原始类型字面量，或者String字面量
    2. 能转型为原始类型字面量，或String字面量的常量
    3. 一元运算符（+,-,~,!，但不包含++, --) 和1，2组成的表达式 
    4. 多元运算符（*，/和%）和1，2组成的表达式
    5. 附加运算符( additive operators) （+ 或 -）与之前几条组成的表达式
    6. 位移运算符（<<，>>, >>>）和之前几条组成的表达式
    7. 关系运算符（<,<=,>,>= ，不包括 instanceof）与之前几条组成的表达式
    8. 关系运算符（==，!=）与之前几条组成的表达式
    9. 位运算符（&, ^, |）与之前几条组成的表达式
    10. 条件与和条件或运算符（&&, ||) 与之前几条组成的表达式
    11. 三元运算符 (？：）和之前几条组成的表达式
    12. 带括号的表达式，括号内也是常量表达式
    13. 引用常量变量的简单变量 [§6.5.6.1](https://docs.oracle.com/javase/specs/jls/se7/html/jls-6.html#jls-6.5.6.1)
    14. 类中的常量变量引用，使用类的全限定名或类名进行引用（String.class)
    ```
    4. 示例
    ```java
    public class Test {
            编译时常量
        //	public static final int a = 10;
            编译时常量
        //	public static final String c = "test";
        	运行时常量:不属于原始类型及String
        //	public static final Object a = new Object();
            编译时常量:String的一元运算组成的表达式
        //	public static final String c = "test" + ":test";
        	编译时常量:原始类型的一元运算组成的表达式
        //	public static final int a = 10 + 20;
        //	public static final String c = "test" + ":test";
        	运行时常量:调用了Random类及nextInt函数
        //	public static final int a = new Random().nextInt(10000);
            运行时常量:调用了length函数
        //	public static final int c = "test".length();
    
    	//编译时常量:a的指在编译时就是确定的
    	public static final int a = 10;
    	//运行时常量:c的值在运行时才可以确定
    	public static final int c = "test".length();
    	static{
    		System.out.println("Class Test Was Loaded !");
    	}
    }
    public class CompilConstant {
    	public static void main(String[] args) {
    	        //a是编译时常量,不引起类的加载,不执行static代码块
    		System.out.println(Test.a);
    		//c是运行时常量,运行时常量的调用依赖于类的加载,因而会先执行static代码块
    		System.out.println(Test.c);
    	}
    }
    打印结果:
    10
    Class Test Was Loaded !
    4
    ```
6. 静态变量的声明和赋值是分开的,变量名的声明先加载,赋值是按照代码顺序执行
    ```java
    public class StaticOrderTest {
    	static String s = null;
    	static{
    		System.out.println("StaticOrderTest:static");
    		s = "test";
    	}
    	public static void main(String[] args) {
    		System.out.println(s);
    	}
    }
    打印结果:
    StaticOrderTest:static
    test
    
    调整静态代码块的位置:
    public class StaticOrderTest {
    	static{
    		System.out.println("StaticOrderTest:static");
    		s = "test";
    	}
    	static String s = null;
    	public static void main(String[] args) {
    		System.out.println(s);
    	}
    }
    打印结果:
    StaticOrderTest:static
    null
    ```
## 单例模式
### 单例模式定义
确保某1个类只有1个实例,而且自行实例化并向整个系统提供这个实例
### 单例模式使用场景