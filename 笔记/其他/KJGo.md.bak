KJGo框架简介:
	1:关于作者
		谭成龙(Jet Tan)
		wall0920@163.com
		472868476@qq.com

	2:框架大致结构
		activity:各种Activity基类
		========================
		fragment:各种Fragment基类
		========================
		bitmap:图片加载工具类集合
		========================
		db:SQLite数据库工具类集合
		========================
		net:网络请求相关工具类集合
			1:lan:局域网中android设备间实现通讯的工具类集合:已经应用于实体门店
			2:http:常用网络请求工具类集合
		========================
		ui:UI相关工具类,开源及自定义控件 集合
			1:commonadapter:通用Adapter工具类,包括常用的 AbsListView 及 RecyclerView
			2:view:开源控件 及 自定义控件
				1:opensource 	开源控件
				2:custom		自定义控件
			3:
		========================
		utils:android开发常用的工具类集合
			1:opensource:工具类中涉及的开源库
				1.1:cloning:
						https://github.com/kostaskougios/cloning
						一个用于JAVA BEAN 深拷贝的开源库,JAVA BEAN类不必继承Cloneable接口.
						The cloning library is a small, open source (Apache licensed) Java library which deep-clones objects.
						The objects don't have to implement the Cloneable interface.
						cloning依赖于另一个开源jar:src/main/libs/objenesis-2.4.jar
		========================
		eventbus:android事件总线,完全拷贝Simple大神的博客
			http://blog.csdn.net/bboyfeiyu/article/details/44309093

	3:框架中部分功能使用方法:

	4:框架中涉及的部分知识点:
		4.1:面向对象六大原则
			https://github.com/simple-android-framework-exchange/android_design_patterns_analysis/blob/master/oop-principles/oop-principles.md
			单一职责原则:
				单一职责原则适用于类,接口,方法.一个方法尽可能只做一件事情;
				方法职责单一,让人一眼就知道方法是干嘛的;
				接口一定要做到单一职责;

				单一职责原则并不是说一个类只有一个函数，
				而是说这个类中的函数所做的工作必须要是高度相关的，也就是高内聚!!!
			里氏替换原则:
				面向对象语言的三大特点就是:继承、封装、多态。里氏替换原则就是依赖于继承和多态这两大原则。
				只要父类能出现的地方，子类就能出现，替换为子类也不会产生任何错误或异常。
					1：子类必须完全实现父类的方法
					2：子类可以有自己的个性
					3：子类覆盖或者实现父类方法，输入参数可以被向上放大	HashMap==>Map
					4：子类覆盖或者实现父类方法，输出结果可以被向下缩小	Map==>HashMap
				大部分框架都利用了里氏替换原则,输入参数定义一个父类或接口,根据情况灵活替换为不同的子类.
			依赖倒置原则:
				就是面向接口编程,或者面向抽象编程.这里的抽象指的是接口或者抽象类.
			接口隔离原则:
				1:接口尽量细化,同时接口中的方法数量尽量少;
				2:接口隔离原则 和 单一职责原则的角度不同,单一职责原则是按照业务逻辑进行区分,接口隔离原则根据接口中方法的数量进行区分,
					若两者产生冲突,则以 单一职责原则优先,首先保证业务逻辑.接口隔离原则的核心定义，不出现臃肿的接口（Fat  I n t erf ace），
					但是“小”是有限度的，首先就是不能违反单一职责原则.
				3:接口中尽量少公布public方法;
			迪米特原则:
				降低耦合性,增加稳定性;
					迪米特法则要求类“羞涩”一点，尽量不要对外公布太多的public方法和非静态的public变量，尽量内敛，多使用private、protected等访问权限.
			开闭原则:
				尽量做扩展,少做修改;
		4.2:Android常用设计模式
			<<设计模式之禅(第2版)>>
				CSDN可下载:http://download.csdn.net/detail/johnllon/8452639
			Android源码设计模式
				https://github.com/simple-android-framework-exchange/android_design_patterns_analysis
			---------------------
			4.2.1:策略模式
				定义一组算法,将每个算法都封装起来,算法之间可以自由切换.是if else if else的优化,在逻辑复杂的情况下代码更易读,耦合度更低,
				扩展方便,但随着策略数量增加,子类文件也会增加.
				策略枚举:
					把原有定义在抽象类中的抽象方法定义到枚举中,每个枚举项都会成为一个具体策略;
						示例代码:
						public enum Calculator{
							//加法运算
							ADD("+"){public int exec(int a,int b){return a+b;}},
							//减法运算
							SUB("-"){public int exec(int a,int b){return a-b;}};
							private String value;
							//枚举类的构造函数必须是私有
							private Calculator(String value){this.value = value;}
							public String getValue(){return this.value;}
							//移植到枚举类中的抽象方法
							public abstract int exec(int a,int b);
						}
					枚举策略的使用场景:
						优点是易读,代码量少,面向对象;缺点是 每个枚举项都是public static final,扩展性受到了一定影响(这句话没怎么理解);
						很适合与一些不经常发生变化的角色;
			4.2.2:模板方法模式
				定义一个操作中的算法框架,而将一些步骤延迟到子类中,使子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤.
				模板方法实现:
					1:父类(抽象类) public abstract类
						包含 基本方法 + 模板方法
							基本方法:protected abstract 方法,由子类具体实现;	用protected符合迪米特原则;
							模板方法:可以有1个或几个,完成对基本方法的调度,实现具体逻辑; public final方法;	用final是为了防止被子类复写改变了基本方法的调度逻辑;
					2:子类
				模板方法注意:
					1:父类提取公共部分代码,写成抽象基本方法,在模板方法中按照指定逻辑进行调度,子类则提供基本方法的具体实现;
						试用于多个子类有公有的方法,且调用逻辑基本相同;
					2:对模板方法模式进行扩展,需要用到"钩子方法".
						钩子方法是父类中的一个protect方法,但不是抽象方法,其返回值影响了 模板方法 中对基本方法的调用逻辑;
						子类可以 复写 钩子方法,从而对 父类中模板方法对基本方法 的调用逻辑;
			4.2.3:单例模式
				确保某一个类只有一个实例,而且自行实例化并向整个系统提供这个实例;
				单例模式通用代码:
					public class Singleton {
						private static final Singleton singleton = new Singleton();
						//限制产生多个对象
						private Singleton(){
						}
						//通过该方法获得实例对象
						public static Singleton getSingleton(){
						return singleton;
						}
						//类中其他方法，尽量是static
						public static void doSomething(){
						}
					}
				单例模式几种比较好的实现方式:保证线程安全
					1:静态内部类单例模式
						public class SingleTon{
							private SingleTon(){}
							public static SingleTon getInstance(){
								return SingleTonHolder.sInstance;
							}
							private static class SingleTonHolder{
								private static final SingleTon sInstance = new SingleTon();
							}
							/**
							*防止反序列化时候重新生成实例
							*/
							private Object readResolve() throws ObjectStreamException{
								return SingleTonHolder.sInstance;
							}
						}
						第一次加载SingleTon类时并不会初始化sInstance,只有第一次调用getInstance()方法时虚拟机加载
						SingleTonHolder并初始化sInstance,不仅能保证线程安全,也可以保证Singleton类的唯一性;
					2:枚举单例模式
						public enum SingleTon2{
							INSTANCE;
							public void doSomeThing(){}
							private Object readResolve() throws ObjectStreamException{
								return INSTANCE;
							}
						}
						默认枚举实例的创建是线程安全的,并且在任何情况下都是单例;
						枚举单例的优点就是简单,缺点是可读性不高;
					3:有上限的多例模式
						public class SingleTon3{
							//定义最多能产生的实例的数量
							private static int maxNumOfSingleTons = 3;
							//当前获取到的实例在集合中的索引值
							private static int index;
							//定义一个集合,用于容纳所有的实例,用Vector是因为其线程安全
							private static final Vector<SingleTon3> singleTon3s = new Vector<SingleTon3>();

							//静态代码块用于初始化集合
							static{
								for(int i=0;i<maxNumOfSingleTons;i++){
									singleTon3s.add();
								}
							}
							//私有化构造方法
							private SingleTon3(){}
							//公有方法
							public void doSomeThing(){}

							public static SingleTon3 getInstance(){
								index = new Random().nextInt(maxNumOfSingleTons);
								return singleTon3s.get(index);
							}

							/*
							*防止反序列化时候重新生成实例
							*/
							private Object readResolve() throws ObjectStreamException{
								return singleTon3s.get(index);
							}
						}
						如果不使用Vector,也可以使用Map,具体见:http://www.cnblogs.com/cloudwind/archive/2012/08/30/2664003.html

					4:一定记得在类中添加 readResolve() 方法,防止反序列化时候重新生成单例对象;
			4.2.4:Builder模式/建造者模式
				将一个复杂对象的构建与它的表示分离,使得同样的构建过程可以创建不同的表示.
				建造者模式包含4个角色:
					产品类		:实现了模板方法模式, 具体产品类的父类有模板方法和基本方法
					抽象建造者	:规范产品的组建,方法全是抽象方法
					具体建造者	:抽象建造者的子类,实现抽象建造者定义的所有方法,并且返回一个组建好的 具体产品类对象;
					导演类		:导演类起到封装作用,包含多个方法用于产生不同类型的 具体产品类;
				建造者模式最主要的功能是基本方法的调用顺序,顺序不同产生的对象也不同;
				代码示例:
					1.1:具体产品类的父类,一个抽象类
						public abstract class AbsModel{
							//这个参数定义了各个基本方法的执行顺序
							private ArrayList<String> sequence = new ArrayList<String>();
							//基本方法集合
							protected abstract void step1();
							protected abstract void step2();
							protected abstract void step3();
							//对参数进行设置
							public final void setSequence(ArrayList<String> sequence){
								this.sequence = sequence;
							}
							//模板方法
							public final void do(){
								for(int i=0;i<sequence.size();i++){
									String currAction = sequence.get(i);
									if(currAction.equalsIgnoreCase("step1")){
										this.step1();
									}else if(currAction.equalsIgnoreCase("step2")){
										this.step2();
									}else if(currAction.equalsIgnoreCase("step3")){
										this.step3();
									}
								}
							}
						}
					1.2:具体产品类子类:实现抽象父类中的基本方法
						public class Product1 extends AbsModel{
							protected void step1(){
								System.out.println("Product1:step1");
							}
							protected void step2(){
								System.out.println("Product1:step2");
							}
							protected void step3(){
								System.out.println("Product1:step3");
							}
						}
						public class Product2 extends AbsModel{
							protected void step1(){
								System.out.println("Product2:step1");
							}
							protected void step2(){
								System.out.println("Product2:step2");
							}
							protected void step3(){
								System.out.println("Product2:step3");
							}
						}
					2:抽象建造者:规范产品的组建,抽象父类
						public abstract class ProductBuilder{
							//设置产品参数
							public abstract void setSequence(ArrayList<String> sequence);
							//获取产品实例
							public abstract AbsModel getProduct();
						}
					3:具体建造者
						public class Product1Builder extends ProductBuilder{
							//私有变量就是将要产生的具体产品类实例
							private Product1 p = new Product1();
							public void setSequence(ArrayList<String> sequence){
								this.p.setSequence(sequence);
							}
							public AbsModel getProduct(){
								return this.p;
							}
						}
						public class Product2Builder extends ProductBuilder{
							//私有变量就是将要产生的具体产品类实例
							private Product2 p = new Product2();
							public void setSequence(ArrayList<String> sequence){
								this.p.setSequence(sequence);
							}
							public AbsModel getProduct(){
								return this.p;
							}
						}
					4:导演类:相当于一个工具类,用来调度各种具体建造者产生各种类型的具体产品实例
						public class Director{
							//影响产品流程顺序的参数
							private ArrayList<String> sequence = new ArrayList<String>();
							//具体建造者
							private Product1Builder product1Builder = new Product1Builder();
							private Product2Builder product2Builder = new Product2Builder();

							//根据需求自行扩展
							public Product1 gainProduct1A(){
								this.sequence.clear();
								this.sequence.add("step1");
								this.sequence.add("step2");
								this.sequence.add("step3");
								this.product1Builder.setSequence(this.sequence);
								return (Product1)this.product1Builder.getProduct();
							}
							public Product2 gainProduct2B(){
								this.sequence.clear();
								this.sequence.add("step1");
								this.sequence.add("step3");
								this.product2Builder.setSequence(this.sequence);
								return (Product2)this.product2Builder.getProduct();
							}
						}
			4.2.5:门面模式/外观模式
				外观模式(也成为门面模式)要求一个子系统的外部与其内部的通信必须通过一个统一的对象进行。它提供一个高层次的接口，使得子系统更易于使用。
					使用方便，使用外观模式客户端完全不需要知道子系统的实现过程；
					降低客户端与子系统的耦合；
					更好的划分访问层次；
			4.2.6:代理模式
			4.2.7:迭代器模式
			4.2.8:责任链模式
				一个请求沿着一条"链"传递,直到该链上的某个对象处理它为止;
				优点:
					实现了请求者和处理者之间的解耦合,增加了灵活性;
					请求者不知道最终是链中的哪一个对象处理,处理者也不知道请求的全貌;
				缺点:
					责任链采用从链头遍历到链尾的形式,在链很长的时候会影响性能;
				代码示例:
					public interface IWomen{
						void int getType();
						void String getRequest();
					}
					public class Women implements IWomen{
						private int type;
						private String request;
						public IWomen(int type,String request){
							this.type = type;
							this.request = request;
						}
						public void int getType(){
							return this.type;
						}
						public void String getRequest(){
							return this.request;
						}
					}
					public abstract class IWomenHandler{
						public static final int LEVEL_FATHER = 0;
						public static final int LEVEL_HUSBAND = 1;
						public static final int LEVEL_SON = 2;
						private int currLevel = 0;
						private IWomenHandler nextHandler;
						//抽象基本方法,在子类中具体实现
						protect abstract void response(IWomen women);
						public IWomenHandler(int level){
							this.currLevel = level;
						}
						//设置链中当前对象的下一个责任者
						public void setNextHandler(IWomenHandler nextHandler){
							this.nextHandler = nextHandler;
						}
						public void final handleMessage(IWomen women){
							if(women.getType==this.currLevel){
								this.response(women);
							}else{
								if(this.nextHandler!=null){
									this.nextHandler.response(women);
								}
							}
						}
					}
					public class Father extends IWomenHandler{
                        public Father() {
                            super(IWomenHandler.LEVEL_FATHER);
                        }
                        @Override
                        protected void response() {
                        }
                    }
                    public class Husband extends IWomenHandler{
						public Husband() {
							super(IWomenHandler.LEVEL_HUSBAND);
						}
						@Override
						protected void response() {
						}
					}
					public class Son extends IWomenHandler{
						public Son() {
							super(IWomenHandler.LEVEL_SON);
						}
						@Override
						protected void response() {
						}
					}
					private void testChainOfResponsibility(){
						List<IWomen> womens = new ArrayList<>();
						for(int i=0;i<10;i++){
							womens.add(new Women((int) (Math.random()*3),"老娘要逛街!!!"));
						}
						Father f = new Father();
						Husband h = new Husband();
						Son s = new Son();
						f.setNextHandler(h);
						h.setNextHandler(s);
						for(IWomen i:womens){
							f.handleMessage(i);
						}
					}
			4.2.9:命令模式
				定义:
					将一个请求封装成一个对象,将不同的请求参数化,对请求排队或者记录请求日志,以及支持可撤销的操作;
				构成角色:
					1:命令角色接口	:CommandInterface	声明具体命令实例需要执行的方法
					2:具体命令角色	:CommandImpl	命令角色接口的实现类,通常会持有 接收者角色实例,并调用接收者的功能来完成各个具体方法
					3:接收者角色		:Receiver	真正执行命令的角色
					4:调用者角色		:Invoker	负责调用 命令角色接口 实现客户角色的指定需求
					5:客户角色		:Client	创建调用者角色,调用 调用者中指定的方法 完成具体需求
				代码示例:
					public class PeopleBean{
						private int age = -1;
						private String name = "name";
						public PeopleBean(int age,String name){
							this.age = age;
							this.name = name;
						}
						public void update(int age,String name){
							this.age = age;
							this.name = name;
						}
						public void update(int age){
							this.age = age;
						}
						public void update(String name){
							this.name = name;
						}
						public void setAge(int age){
							this.age = age;
						}
						public void setName(String name){
							this.name = name;
						}
						public int getAge(){
							return this.age;
						}
						public String getName(){
							return this.name;
						}
						@Override
						public String toString() {
							return " 【年龄：" + age + "\t姓名：" + name + "】";
						}
					}
					/**
					*接收者
					*/
					public class ReceiverPeople{
						private PeopleBean people;
						//用于回滚
						private PeopleBean peopleCache = new PeopleBean();
						public ReceiverPeople(){
							this.people = new PeopleBean();
						}
						public ReceiverPeople(PeopleBean people){
							this.people = people;
						}
						//修改年龄
						public void updateAge(int age){
							this.people.setAge(age);
						}
						//修改姓名
						public void updateName(String name){
							this.people.setName(name);
						}
						//回滚年龄
						public void rollBackAge(){
							this.people.setAge(this.peopleCache.getAge());
						}
						//回滚姓名
						public void rollBackName(){
							this.people.setName(this.peopleCache.getName());
						}
					}
					/**
					*命令接口
					*/
					public interface Command{
						//执行动作
						public void execute();
						//回滚
						public void undo();
						//执行动作前可以修改命令的执行
						public void redo();
					}
					/**
					*具体命令1:修改年龄
					*/
					public class CommandImpl1 implements Command{
						private ReceiverPeople receiver1;
						public CommandImpl1(ReceiverPeople receiver1){
							this.receiver1 = receiver1;
						}
						@Override
						public void execute(){
							receiver1.updateAge();
						}
						@Override
						public void undo(){
							receiver1.rollBackAge();
						}
						@Override
						public void redo(){
						}
					}
					/**
					*具体命令2:修改姓名
					*/
					public class CommandImpl2 implements Command{
						private ReceiverPeople receiver2;
						public CommandImpl2(ReceiverPeople receiver2){
							this.receiver2 = receiver2;
						}
						@Override
						public void execute(){
							receiver2.updateName();
						}
						@Override
						public void undo(){
							receiver2.rollBackName();
						}
						@Override
						public void redo(){
						}
					}
					/**
					*调用者 Invoker
					*/
					public class Invoker{
						private Command command1;
						private Command command2;
						public void setCommand1(Command command1){
							this.command1 = command1;
						}
						public void setCommand2(Command command2){
							this.command2 = command2;
						}
						/**
						*执行命令
						*0:execute 1:undo
						*/
						public void invoke(int funcType){
							if(funcType == 0){
								this.command1.execute();
								this.command2.execute();
							}else if(funcType == 1){
								this.command1.undo();
								this.command2.undo();
							}
						}
					}
					/**
					*客户端
					*/
					public class Client{
						public void func1(){
							ReceiverPeople receiver1 = new ReceiverPeople();
							ReceiverPeople receiver2 = new ReceiverPeople();
							Command command1 = new CommandImpl1(receiver1);
							Command command2 = new CommandImpl2(receiver2);
							Invoker invoker = new Invoker();
							invoker.setCommand1(command1);
							invoker.setCommand2(command2);
							//execute
							invoker.invoke(0);
							//undo
							invoker.invoke(1);
						}
					}
			4.2.10:桥接模式/桥梁模式
				定义:
					将抽象部分与实现部分分离,使他们都可以独立变化;
				使用场景:
					一个类存在两个独立变化的维度,两个维度都需要进行扩展;
					设计要求实现话角色的任何改变不影响当前客户端,或者实现化角色的改变对客户端是完全透明的;
				Android中最明显的例子:
					AbsListView(抽象类) 和 ListAdapter(接口)
					AbsListView有不同的子类:ListView,GridView---
					ListAdapter有不同的实现类:ArrayAdapter,CursorAdapter---
						抽象部分扩展实现了不同的ViewGroup类型,数据展示形式;
						实现部分扩展实现了不同的ItemView;
				代码示例:
					//实现类需继承的接口
					public interface Product{
						public void produce();
						public void sell();
					}
					//具体实现类
					public class Book implements Product{
						@Override
						public void produce(){
							//具体实现
						}
						@Override
						public void sell(){
							//具体实现
						}
					}
					public class Car implements Product{
						@Override
						public void produce(){
							//具体实现
						}
						@Override
						public void sell(){
							//具体实现
						}
					}
					//抽象类
					public abstract class Corp{
						private Product product;
						public Corp(Product product){
							this.product = product;
						}
						public void makeMoney(){
							this.product.produce();
							this.product.sell();
						}
					}
					//抽象类的继承类
					public class CorpBook extends Corp{
						public CorpBook(Book book){
							super(book);
						}
						@Override
						public void makeMoney(){
							super.makeMoney();
						}
					}
					public class CorpCommand extends Corp{
						public CorpCommand(Product product){
							super(product);
						}
						@Override
						public void makeMoney(){
							super.makeMoney();
						}
					}
					//发现 抽象类 和 实现接口 都有独立的扩展,可以利用不同的公司生产不同的产品来赚钱;
					//Corp和Product基本不会改变,保证了稳定性.子类可以灵活扩展;
			4.2.11:原型模式
				定义:
					用原型实例指定创建对象的种类,并且通过拷贝这些原型创建新的对象;
				通用代码:
					public class CloneModel implements Cloneable{
						@Override
						public CloneModel clone(){
							CloneModel model = null;
							try{
								model = (CloneModel)super.clone();
							}catch(CloneNotSupportedException e){
							}
							return model;
						}
					}
					public class CloneTest{
						private void t(){
							CloneModel model = new CloneModel();
							for(int i=0;i<10000;i++){
								CloneModel c = model.clone();
								execute(c);
							}
						}
						private void execute(CloneModel c){
						}
					}
				优缺点:
					优点是性能优良,是内存中二进制流的拷贝,要比直接new一个对象性能好很多;
					缺点是构造函数不会执行,减少了约束;
				注意点:
					1:使用原型模式时候,对应类中的成员变量,如果不是基本类型和String,在重写clone方法时候,
						必须进行"深拷贝"处理;
					2:类中的成员变量不能使用final,因为final不能重新复制,导致拷贝后的实例和原始实例的成员变量,
						指向的是同一个内存地址;
					3:浅拷贝 和 深拷贝
						浅拷贝是仅仅拷贝了一个新对象,但是对象中的成员变量指向地址和原始对象相同;
						深拷贝不仅仅拷贝了一个新对象,新对象中的成员变量指向地址也是重新分配,和原始对象完全隔离,可以安全的进行操作;
					4:深拷贝示例
						public class CloneModel implements Cloneable{
							private ArrayList<String> a = new ArrayList<String>();
							@Override
							public CloneModel clone(){
								CloneModel c = null;
								try{
									//深拷贝,连同原始对象中的成员变量也一起进行拷贝,完全分隔新老对象,防止数据重叠
									c = (CloneModel)super.clone;
									c.a = (ArrayList<String>)this.a.clone();
								}catch(CloneNotSupportedException e){
								}
								return c;
							}
						}
			4.2.12:组合模式
				定义:
					组合模式也叫合成模式,有时也叫 "部分-整体模式",主要用来描述部分与整体之间的关系;
					将对象组合成树状结构以表示"部分-整体"的层次结构,使得用户对单个对象和组合对象的使用具有一致性;
				组合模式的几个角色:
					抽象构件:	总结叶子构件和树枝构件共性的抽象类;
					叶子构件:	最小对象,无下属;
					树枝构件:	组合树枝节点和叶子节点形成一个树状结构;
				组合模式的使用场景:
					树形结构/局部和整体关系,考虑使用组合模式;
				代码示例:
					/**
					*抽象类
					*数直接点 和 叶子节点 的共性;
					*/
					public abstract class Corp{
						//姓名
						private String name;
						//职位
						private String position;
						//工资
						private int salary;
						//上级
						private Corp parent;
						//下级
						private ArrayList<Corp> subs;
						public Corp(String name,String position,int salary){
							this.name = name;
							this.position = position;
							this.salary = salary;
						}
						//设置上级
						public void setParent(Corp parent){
							this.parent = parent;
						}
						//获取上级
						public Corp getParent(){
							return this.parent;
						}
						//设置,获取,增加,删除 下级
						protected abstract void setSub(ArrayList<Corp> subs);
						protected abstract ArrayList<Corp> getSub();
						protected abstract void addSub(Corp sub);
						protected abstract void deleteSub(Corp sub);
					}
					/**
					*树枝构件
					*/
					public class Branch extends Corp{
						public Branch(String name,String position,int salary){
							super(name,position,salary);
						}
						@Override
						public void setSub(ArrayList<Corp> subs){
							this.subs = subs;
						}
						@Override
						public ArrayList<Corp> getSub(){
							return this.subs;
						}
						@Override
						public void addSub(Corp sub){
							this.subs.add(sub);
						}
						@Override
						public void deleteSub(Corp sub){
							this.subs.delete(sub);
						}
					}
					/**
					*叶子构件
					*/
					public class Leaf extends Corp{
						public Branch(String name,String position,int salary){
							super(name,position,salary);
						}
						//因为叶子构件不具备下属,所以这些针对下属的方法都是空实现,且告诉调用者避免使用这些方法:@Deprecated
						@Override @Deprecated
						public void setSub(ArrayList<Corp> subs){
							//空实现
						}
						@Override @Deprecated
						public ArrayList<Corp> getSub(){
							return null;
						}
						@Override @Deprecated
						public void addSub(Corp sub){
							//空实现
						}
						@Override @Deprecated
						public void deleteSub(Corp sub){
							//空实现
						}
					}
			4.2.13:观察者模式(Observer Pattern)
				定义:
					观察者模式(Observer Pattern)也叫发布订阅模式,定义对象间一种一对多的依赖关系,使得每当一
					个对象改变状态,则所有依赖于它的对象都会得到通知并被自动更新.
				观察者模式中的角色:
					被观察者接口;
					观察者接口;
					具体的被观察者实现类;
					具体的观察者实现类;
				JAVA代码中已经实现了 被观察者实现类(Observable) 及 观察者接口(Observer).
				观察者模式的注意:
					1:一个被观察者,多个观察者,在执行 notifyObservers 时候,顺序执行容易卡壳,影响整体的执行效率,
						因而一般情况下 notifyObservers中要考虑使用异步.
					2:根据经验,一个观察者模式中,最多出现一个对象 既是观察者又是被观察者,消息最多被转发一次;
					3:被观察者要判断清楚是否需要向观察者发送消息,尽量不要让观察者收到消息后再决定是否消费;
				代码示例:
					/**
					*被观察者接口
					*/
					public Interface IHanFeiZi{
						public void haveBreakfast();
						public void haveFun();
					}
					/**
					*具体的被观察者实现类:继承Observable+实现被观察者接口
					*/
					public class HanFeiZi extends Observable implements IHanFeiZi{
						//是否需要通知观察者
						private boolean notifyObservers = true;
						@Override
						public void haveBreakfast(){
							if(notifyObservers){
								//设置被观察者状态已经发生改变
								super.setChanged();
								//通知所有观察者指定消息
								super.notifyObservers("韩非子在吃饭");
							}
						}
						@Override
						public void haveFun(){
							if(notifyObservers){
								super.setChanged();
								super.notifyObservers("韩非子在娱乐");
							}
						}
					}
					/**
					*具体的观察者实现类:继承Observer接口
					*/
					public Lisi implements Observer{
						@Override
						public void update(Observable observable, Object data) {
							//打印 被观察者 传递的消息
							System.out.println("李斯获得的消息:"+data.toString());
						}
					}
					public Liusi implements Observer{
						@Override
						public void update(Observable observable, Object data) {
							//打印 被观察者 传递的消息
							System.out.println("刘斯获得的消息:"+data.toString());
						}
					}
					/**
					*调用的客户端
					*/
					public class Client{
						public void func(){
							HanFeiZi hanFeiZi = new HanFeiZi();
							//继承的Observable接口中的方法,实现对观察者的管理
							hanFeiZi.addObserver(new LiSi());
							hanFeiZi.addObserver(new Liusi());
							//被观察者执行指定方法,会通知所有的观察者联动
							hanFeiZi.haveBreakfast();
							hanFeiZi.haveFun();
						}
					}
			4.2.14:备忘录模式
				定义:
					在不破坏封装性的前提下,补货一个对象内部的状态,并在该对象之外保存这个状态,这样以后就可以
					将对象恢复至保存前的状态;
				Android中使用场景:
					保存Activity中的简单成员变量的值的集合==>HashMap,在需要恢复初始状态时候,用保存过的HashMap
					给Activity中的成员变量赋值.
				注意:
					如果要把一个对象作为Map的键或值,此对象必须重写了equals()和hashCode()方法,否则会出现通过键值搜索失败的情况.
					例如: map.get(Object),map.contains(Object)等返回失败的结果;
					只是使用 {@link com.jet.kjgo.lib.utils.ReflectionUtils}则不需要对应的对象重写.
				代码示例:
					com.jet.kjgo.lib.utils.ReflectionUtils
						1:生成备忘录
							List<String> fieldNames = new ArrayList<String>();
							fieldNames.add("name");
							fieldNames.add("weight");
							fieldNames.add("children");
							ReflectionUtils.backupProp(ActivityRef.this,fieldNames);
						2:使用备忘录
							ReflectionUtils.restoreProp(ActivityRef.this);
			4.2.15:享元模式
			4.2.16:工厂方法模式
				定义:
					定义一个用于创建对象的接口,让子类决定实例化哪一个类.工厂方法使一个类的实例化延迟到其子类;
				代码示例:
					1:抽象产品类
						public abstract class AbsProduct{
							//抽象方法,由子类个性化实现
							public abstract void doCustom();
							//公共方法
							public void doCommon(){
								Toast.makeText(****).show();
							}
						}
					2:具体产品类
						public class Product1 extends AbsProduct{
							//实现父类的抽象方法
							public void doCustom(){
								Toast.makeText("Product1").show();
							}
						}
						public class Product2 extends AbsProduct{
							//实现父类的抽象方法
							public void doCustom(){
								Toast.makeText("Product2").show();
							}
						}
					3:抽象工厂类
						public abstract class AbsProductFactory{
							/**
							*使用泛型来指定工厂类创建的对象是 指定类的子类实例
							*参数可以是Class,也可以是String,Enum
							*/
							public abstract <T extends AbsProduct> T createProduct(Class<T> c);
						}
					4:具体工厂类
						public class ProductFactory extends AbsProductFactory{
							public <T extends AbsProduct> T createProduct(Class<T> c){
								Product product = null;
								try{
									//根据类名创建其实例
									product = (AbsProduct)Class.forName(c.getName()).newInstance();
								}catch(Exception e){
								}
								//向下转型
								return (T)product;
							}
						}
				工厂方法模式的扩展:
					1:简单工厂模式
						取消抽象工厂类,只有一个具体工厂类,无需继承.
					2:多个具体工厂类
						每个具体工厂类的功能单一,只用于创建指定的具体产品类实例.
							public abstract class AbsProductFactory{
								public abstract AbsProduct createProduct();
							}
							public class Product1Factory extends AbsProductFactory{
                            	public AbsProduct createProduct(){
                            		return new Product1();
                            	}
                            }
                            public class Product2Factory extends AbsProductFactory{
								public AbsProduct createProduct(){
									return new Product2();
								}
							}
			4.2.17:适配器模式
		4.3:Map线程安全几种实现方法
			http://www.cnblogs.com/cloudwind/archive/2012/08/30/2664003.html
			推荐使用 ReadWriteLock,因为synchronized性能很差,而Lock性能也比较差.而使用ConcurrentHashMap其实并不能完全保证线程安全,
			只是降低了线程不安全的风险而已.

			ReadWriteLock:当写操作时,其他线程无法读取或写入数据,当读操作时,其他线程无法写入数据,但可以读取数据;
			ReadWriteLock是一个接口,具体使用要用到其实现类:ReentrantReadWriteLock.
				具体用法:
				class ReadWriteLockTest{
					//读写锁
					ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
					//读锁
					private Lock read = readWriteLock.readLock();
					//写锁
					private Lock write = readWriteLock.writeLock();
					private int data;

					//读操作
					public int get() {
						int i = 0;
						//读锁加锁
						read.lock();
						i = data;
						//读锁解锁
						read.unlock();
						return i;
					}
					//写操作
					public void set(int data) {
						//写锁加锁
						write.lock();
						this.data = data;
						//写锁解锁
						write.unlock();
					}
				}
		4.4:Java中的枚举
			每个枚举项都是public static final的;
			枚举类要求:
				1:构造函数必须是private,枚举实例决不允许外部创建.
				2:枚举类可以实现接口,但是不能继承其他类.
				代码示例:
					public enum T{
						ONE(1,1),TWO(2,2),THREE(3,3);

						private int type;
						private int age;
						private T(int type, int age){
							this.type = type;
							this.age = age;
						}
					}
			枚举类使用场景:
				1:列举常量
					public enum Food{
						FOOD1,FOOD2,FOOD3;
					}
				2:使用接口组织枚举
					public interface People{
						public enum Young implements People{
							YOUNG1,YOUNG2,YOUNG3;
						}
						public enum Old implements People{
							OLD1,OLD2,OLD3;
						}
					}
				3:在Switch中使用
					注:在Switch中使用枚举,case中只能允许出现枚举项的名称,不能全称.如:case enumTest.BLUE: 会报错!
					enum enumTest{
						RED,GREEN,BLUE;
					}
					class enumTests{
						public void doSomething(){
							enumTest a = null;
							switch (a){
								case RED:
									break;
								case GREEN:
									break;
								//case enumTest.BLUE: 会报错
								case BLUE:
									break;
								default:
									break;
							}
						}
					}
		4.5:JAVA 注解
		4.6:JAVA 泛型
		4.7:JAVA 中几个关键字
		4.8:
