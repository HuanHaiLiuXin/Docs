> 《Android开发艺术探索》对于Drawable的讲解相对简单.笔记内容主要来自之前自己写的文章.

### 1.Drawable源码分析
**概述:**<br/>
1. Drawable是1个用于处理各种可绘制资源的抽象类,使用Drawable最常见的情况就是将获取到的资源绘制到屏幕上
2. Drawable无法接收事件及与用户交互
3. 所有SDK版本都支持自定义Drawable,从SDK24开始,可以在xml中直接使用自定义Drawable
    - 自定义Drawable要至少重写draw方法
    - SDK>=24,自定义Drawable在xml中使用的2种方式
        - 直接引用自定义Drawable类名全称,自定义Drawable类必须为公共顶层类
        ```xml
        <com.jet.CustomDrawable 
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:color="#ffff0000" />
        ```
        - 使用drawable作为XML的元素名称,指定该自定义Drawable类的全称.该自定义Drawable类可以是 公共顶层类或者公共静态内部类
        ```xml
        <drawable
            xmlns:android="http://schemas.android.com/apk/res/android"
            class="com.jet.CustomDrawable"
            android:color="#ffff0000" />
        ```
**源码:**<br/>
1. draw(@NonNull Canvas canvas)
    - 将当前Drawable实例绘制到canvas上
    - 在setBounds设置过的范围内绘制,setAlpha和setColorFilter影响绘制效果
2. setBounds(int left, int top, int right, int bottom)
    - 为当前Drawable实例设置绘制范围
    - 在draw执行时,会调用getBounds来获取绘制范围
3. copyBounds(@NonNull Rect bounds)
    - 将当前Drawable绘制范围赋值到参数Rect中
4. public final Rect copyBounds()
    - 获取当前Drawable的绘制范围
    - 获取的Rect不可修改
    - 想修改获取到的Rect,调用copyBounds(@NonNull Rect bounds)即可
5. setDither(boolean dither)
    - 是否开启抖动.设置true可以让高质量的图片在低质量的屏幕上保持较好的显示效果
6. setFilterBitmap(boolean filter)
    - 是否开启滤波过滤.设置true可以提升Bitmap在缩放及旋转时的绘制效果
7. **setTint(@ColorInt int tintColor)<br/>setTintList(@Nullable ColorStateList tint)<br/>setColorFilter(@ColorInt int color, @NonNull PorterDuff.Mode mode)<br/>和Drawable着色有关,后续会介绍**
8. setState(@NonNull final int[] stateSet)
    - 为当前Drawable设置1个状态值集合
    - 当现有状态和stateSet不同,触发onStateChange(stateSet)
9. jumpToCurrentState
    - 如果当前Drawable正在执行状态过度动画,则跳过动画,立即跳转到当前状态
10. Drawable getCurrent()
    - 返回当前Drawable实例正在使用的Drawable实例,对于一般单个Drawable，返回值就是自身，对于像StateListDrawable这样的复合Drawable实例，则返回其持有的一个子Drawable实例
11. setLevel(@IntRange(from=0,to=10000) int level)
    - 为当前Drawable实例设置图像级别，从0到10000
    - 当现在值和level不同,触发onLevelChange(level)
    - 如LevelListDrawable和ScaleDrawable会在onLevelChange中变更Drawable的绘制
12. **getOutline:获取当前Drawable实例的绘制区域轮廓,后续会介绍**

### 2.Drawable中比较重要的几个方法
### 3.Drawable子类用法
### 4.自定义Drawable

> 别忘了有 ViewOutlineProvider .尝试一下能不能实现任意形状的轮廓;

> Outline.java<br/>MODE_CONVEX_PATH <br/> public Path mPath;<br/>public void setConvexPath(@NonNull Path convexPath) {

> 《Android开发艺术探索》对于Drawable的讲解相对简单.笔记内容主要来自之前自己写的文章.
