# 1.Android生命周期
## 1.1 典型情况下的生命周期
onCreate
onStart
onResume
onPause
onStop
onDestroy
1. onStart和onResume时Activity都是可见的,但onStart时Activity还在后台,并未出现在前台,在onResume时Activity才显示到前台;
2. onPause时Activity依然可见,但已经不运行在前台;onStop时Activity已不可见;比如新打开的Activity时透明主题,则旧Activity不执行onStop,因为依然是可见状态;
3. A(Activity).startActivity(B(Activity)),会在A的onPause执行完毕后,才执行B的onCreate,具体顺序是:
```
A:onPause
B:onCreate
B:onStart
B:onResume
A:onStop
```
4. 在老的Activity的onPause执行完毕后,才执行新Activity的onCreate,onStart,onResume.所以在onPause中不可以执行耗时操作,否则会影响新的Activity的展示.
## 1.2 异常情况下的生命周期
1. 系统配置改变导致Activity被杀死并重建,如屏幕方向改变,Activity生命周期方法调用顺序:
```
被杀死:
onSaveInstanceState(可能在onPause之前,也可能在onPause之后)
onStop
onDestroy
被重建:
onCreate
onStart
ononRestoreInstanceState
```
- 和Activity一样,每个View也有onSavveInstanceState和onRestoreInstanceState这两个方法;
- 系统只有在Activity被异常终止时才调用onSaveInstanceState和onRestoreInstanceState
2. 系统资源不足,导致低优先级的Activity优先被杀死.Activity的优先级由低到高:
- 前台Activity,正在和用户交互的Activity,优先级最高;
- 可见,但非前台的Activity,比如一个Activity中弹出了一个Dialog,Activity可见但位于后台,无法和用户直接交互,优先级其次;
- 后台Activity,比如已经执行了onStop,不可见,优先级最低;
- 被杀死的Activity也会依次执行:onSaveInstanceState,onRestoreInstanceState
3. 系统配置发生改变导致的Activity被杀死和重建,可以通过设置android:configChanges来避免.常用configChanges的含义:
- label:设备的本地设置发生了改变,一般是指切换了系统语言
- keyboardHidden:键盘的可访问性发生了改变,比如用户调出了键盘
- orientation:旋转了手机屏幕
- screenSize:当minSdkVersion和targetSdkVersion至少有1个>=13,screenSize需要和orientation一起设置,才能保证旋转屏幕导致Activity被重建
- 当设置了android:configChanges,Activity不会调用onSaveInstanceState和onRestoreInstanceState,会调用onConfigurationChanged方法.

# 2.Activity的启动模式
## 2.1 Activity的LaunchMode
1. standard:默认模式.每次都创建新的Activity实例,被创建的Activity会进入启动它的那个Activity所在的任务栈中.
> 当我们使用ApplicationContext启动一个standard模式的Activity会报错,因为非Activity类型的Context并没有所谓的任务栈,解决办法是为待启动的Activity指定FLAG_ACTIVITY_NEW_TASK标记,这样启动的时候会为Activity创建一个新的任务栈,实际以"singleTask"模式启动;
2. singleTop:栈顶复用模式.当被启动的Activity是singleTop,且位于启动它的Activity所属任务栈的栈顶,则不新建,调用onNewIntent,否则新建Activity实例.
- ABCD-->D-->ABCD
- ABDC-->D-->ABDCD
3. singleTask:栈内复用模式.**只要Activity在任意1个任务栈中存在,多次启动此Activity都不会重新创建实例**,会调用其onNewIntent.且singleTask默认有clearTop的效果:ADBC-->D(D是singleTask)-->AD.具体流程如下:
- 如果A和其所需的任务栈S都不存在,则会先创建S,再创建A,将A压入S中;
- 如果S存在,A未创建,则创建A,将A压入S中;
- 如果S存在,且A已经在S中,比如S中是 BCADE,则会将A切换到栈顶,调用其onNewIntent,并将A上面的DE清除,变成BCA
4. singleInstance:单实例模式.是一种singleTask的加强版.singleInstance模式的Activity只能单独存在一个任务栈中.创建后多次调用都会执行其onNewIntent.
## 2.2 Activity的TaskAffinity
1. 1个Activity所需的任务栈的名称,由该Activity的TaskAffinity决定.默认情况下,所有Activity的任务栈的名称是应用的包名.
2. 我们可以为1个Activity指定单独的TaskAffinity属性,必须不能和包名相同.
3. TaskAffinity和singleTask配对使用才有意义,其他情况无效.此时设置过的Activity会运行在TaskAffinity指定的任务栈中.
```
A为standard,B,C为singleTask且设置了android:taskAffinity="a.b.c.name",
启动A,A启动B,B启动C,C启动A,A启动B.
此时按两次BACK,结果是回到桌面.

解析:
1: A运行于包名指定的任务栈AppName中;
2: A启动B,因为B,C都指定了taskAffinity且是singleTask,所以会先创建任务栈S(a.b.c.name),再创建B,将B压入;
3: B启动C,C所属的任务栈S已经存在,创建C,压入S顶部
4: C启动A,A是standard,再创建1个A,压入C所在的任务栈S顶部,此时S中是:BCA
5: A启动B,B是singleTask,B在S中,并将B切换到栈顶,执行其onNewIntent.singleTask具有clearTop效果,B上面的CA被移除,S中变成B

此时按两次BACK

第一次BACK,S中的B出栈,S已不存在,回到后台任务栈,将AppName显示出来.AppName中仅有A;
第二次BACK,AppName中的A也出栈,AppName也不存在了,回到了桌面.
```