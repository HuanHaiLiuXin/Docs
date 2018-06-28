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
13. **mutate()**
    - 使1个Drawable实例mutable,1个mutable的Drawable不会和其他Drawable共享它的状态
    - 当我们要修改1个从资源文件加载的Drawable实例,mutate必须被调用.默认情况下,所有加载同1资源文件生成的Drawable实例共享1个状态.修改任意1个Drawable实例,所有其他实例都会发生同样的变化
14. **public static abstract class ConstantState**
    - ConstantState是1个抽象类,用于存储多个Drawable实例共享的常量及状态数据
    - 例如加载同1个资源文件生成的多个BitmapDrawable,共享1个ConstantState
    - getConstantState()返回当前Drawable关联的ConstantState
    - mutate()将为1个Drawable实例单独创建1个ConstantState进行关联,不与其他Drawable共享
    - ConstantState.newDrawable(@Nullable Resources res)
        - 通过ConstantState创建1个新的Drawable实例.res参数保证生成的Drawable实例在屏幕当前分辨率下有正确的缩放

### 2.Drawable绘制流程
Drawable最常见使用步骤:<br/>通过Resource获取Drawable实例<br/>将获取的Drawable实例设置为View的背景,或设置为ImageView的src
#### 2.1:通过Resource获取Drawable实例
源码追踪:
```java
Resources.java
public Drawable getDrawable(@DrawableRes int id) throws NotFoundException {
    final Drawable d = getDrawable(id, null);
    return d;
}
public Drawable getDrawable(@DrawableRes int id, @Nullable Theme theme) 
    throws NotFoundException {
    return getDrawableForDensity(id, 0, theme);
}
public Drawable getDrawableForDensity(@DrawableRes int id, int density, 
    @Nullable Theme theme) {
    ****
        final ResourcesImpl impl = mResourcesImpl;
        return impl.loadDrawable(this, value, id, density, theme);
    ****
}

ResourcesImpl.java
Drawable loadDrawable(@NonNull Resources wrapper, @NonNull TypedValue value, 
    int id,int density, @Nullable Resources.Theme theme) throws NotFoundException {
    ****
    //是否使用缓存
    final boolean useCache = density == 0 || value.density == mMetrics.densityDpi;
    //是否是ColorDrawable
    final boolean isColorDrawable;
    //Drawable缓存
    final DrawableCache caches;
    final long key;
    if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT&&
        value.type <=TypedValue.TYPE_LAST_COLOR_INT) {
        //属于ColorDrawable
        isColorDrawable = true;
        caches = mColorDrawableCache;
        key = value.data;
    } else {
        //不属于ColorDrawable
        isColorDrawable = false;
        caches = mDrawableCache;
        key = (((long) value.assetCookie) << 32) | value.data;
    }
    if (!mPreloading && useCache) {
        //1:如果要使用缓存,则将缓存的Drawable返回
        final Drawable cachedDrawable = caches.getInstance(key, wrapper, theme);
        if (cachedDrawable != null) {
            cachedDrawable.setChangingConfigurations(value.changingConfigurations);
            return cachedDrawable;
        }
    }
    //2:不使用缓存,则获取要返回的Drawable实例关联的ConstantState
    final Drawable.ConstantState cs;
    if (isColorDrawable) {
        cs = sPreloadedColorDrawables.get(key);
    } else {
        cs = sPreloadedDrawables[mConfiguration.getLayoutDirection()].get(key);
    }
    Drawable dr;
    if (cs != null) {
        //2.1:要返回的Drawable实例关联的ConstantState存在
        //则调用ConstantState.newDrawable(Resources)获取Drawable
        ****
        dr = cs.newDrawable(wrapper);
    } 
        //2.2:要返回的Drawable实例关联的ConstantState不存在
    else if (isColorDrawable) {
        //2.2.1:要返回的Drawable实例关联的ConstantState不存在,且属于ColorDrawabl
        //则创建ColorDrawable
        dr = new ColorDrawable(value.data);
    } else {
        //2.2.2:要返回的Drawable实例关联的ConstantState不存在,且不属于ColorDrawable
        //则调用loadDrawableForCookie获取Drawable
        dr = loadDrawableForCookie(wrapper, value, id, density, null);
    }
    return dr;
}
//Loads a drawable from XML or resources stream.
private Drawable loadDrawableForCookie(@NonNull Resources wrapper, 
    @NonNull TypedValue value,
    int id, int density, @Nullable Resources.Theme theme) {
    ****
    final String file = value.string.toString();
    final Drawable dr;
    try {
        if (file.endsWith(".xml")) {
            //2.2.2.1:Drawable.createFromXmlForDensity
            final XmlResourceParser rp = loadXmlResourceParser(
                        file, id, value.assetCookie, "drawable");
            dr = Drawable.createFromXmlForDensity(wrapper, rp, density, theme);
        } else {
            //2.2.2.2:Drawable.createFromResourceStream
            final InputStream is = mAssets.openNonAsset(
                        value.assetCookie, file, AssetManager.ACCESS_STREAMING);
            dr = Drawable.createFromResourceStream(wrapper, value, is, file, null);
        }
    } catch (Exception | StackOverflowError e) {
        ****
    }
    return dr;
}

Drawable.java
//2.2.2.1:Drawable.createFromXmlForDensity
public static Drawable createFromXmlForDensity(@NonNull Resources r,
        @NonNull XmlPullParser parser, int density, @Nullable Theme theme)
        throws XmlPullParserException, IOException {
    AttributeSet attrs = Xml.asAttributeSet(parser);
    ****
    Drawable drawable = createFromXmlInnerForDensity(r, parser, attrs,
        density, theme);
    return drawable;
}
static Drawable createFromXmlInnerForDensity(@NonNull Resources r,
        @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, 
        int density,
        @Nullable Theme theme) throws XmlPullParserException, IOException {
    
    //2.2.2.1.0:String name = parser.getName()获取xml标签名称,有3种情况
    //1:xml指定的是系统内置的Drawable,
    //name为"selector","animated-selector","shape"等
    //2:xml指定的是自定义Drawable,以自定义Drawable类名全称作为标签名称,
    //name为"com.jet.CustomDrawable"
    <com.jet.CustomDrawable 
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:color="#ffff0000" />
    //3:xml指定的是自定义Drawable,以"drawable"作为标签名称,
    //以自定义Drawable类名全称作为class
    //name为"drawable"
    <drawable
        xmlns:android="http://schemas.android.com/apk/res/android"
        class="com.jet.CustomDrawable"
        android:color="#ffff0000" />
    return r.getDrawableInflater().inflateFromXmlForDensity(
        parser.getName(), parser, attrs,density, theme);
}
DrawableInflater.java
Drawable inflateFromXmlForDensity(@NonNull String name, @NonNull XmlPullParser parser, 
    @NonNull AttributeSet attrs, int density, @Nullable Theme theme)
    throws XmlPullParserException, IOException {
    //当自定义Drawable在xml中使用,name最终值是自定义Drawable类名全称
    if (name.equals("drawable")) {
        //自定义Drawable定义在class属性中
        name = attrs.getAttributeValue(null, "class");
        if (name == null) {
            throw new InflateException("<drawable> tag must specify class attribute");
        }
    }
    Drawable drawable = inflateFromTag(name);
    if (drawable == null) {
        drawable = inflateFromClass(name);
    }
    drawable.setSrcDensityOverride(density);
    drawable.inflate(mRes, parser, attrs, theme);
    return drawable;
}
//2.2.2.1.1:根据xml标签名称,生成指定类型的Drawable实例
private Drawable inflateFromTag(@NonNull String name) {
    switch (name) {
        case "selector":
            return new StateListDrawable();
        case "animated-selector":
            return new AnimatedStateListDrawable();
        case "level-list":
            return new LevelListDrawable();
        case "layer-list":
            return new LayerDrawable();
        case "transition":
            return new TransitionDrawable();
        case "ripple":
            return new RippleDrawable();
        case "adaptive-icon":
            return new AdaptiveIconDrawable();
        case "color":
            return new ColorDrawable();
        case "shape":
            return new GradientDrawable();
        case "vector":
            return new VectorDrawable();
        case "animated-vector":
            return new AnimatedVectorDrawable();
        case "scale":
            return new ScaleDrawable();
        case "clip":
            return new ClipDrawable();
        case "rotate":
            return new RotateDrawable();
        case "animated-rotate":
            return new AnimatedRotateDrawable();
        case "animation-list":
            return new AnimationDrawable();
        case "inset":
            return new InsetDrawable();
        case "bitmap":
            return new BitmapDrawable();
        case "nine-patch":
            return new NinePatchDrawable();
        default:
            return null;
    }
}
//2.2.2.1.2:根据xml指定的Drawable继承类,获取其构造函数,
//得到Drawable继承类的新实例
private Drawable inflateFromClass(@NonNull String className) {
    ****
    Constructor<? extends Drawable> constructor;
    final Class<? extends Drawable> clazz = mClassLoader.loadClass(className)
        .asSubclass(Drawable.class);
    constructor = clazz.getConstructor();
    return constructor.newInstance();
    ****
}

//2.2.2.2:Drawable.createFromResourceStream
//Create a drawable from an inputstream, using the given resources and
//value to determine density information.
public static Drawable createFromResourceStream(Resources res, TypedValue value,
    InputStream is, String srcName, BitmapFactory.Options opts) {
    ****
    Bitmap  bm = BitmapFactory.decodeResourceStream(res,value,is,pad,opts);
    byte[] np = bm.getNinePatchChunk();
    return drawableFromBitmap(res, bm, np, pad, opticalInsets, srcName);
}
private static Drawable drawableFromBitmap(Resources res, Bitmap bm, byte[] np,
        Rect pad, Rect layoutBounds, String srcName) {
    //如果是.9.png,则返回1个NinePatchDrawable
    if (np != null) {
        return new NinePatchDrawable(res, bm, np, pad, layoutBounds, srcName);
    }
    //其他情况返回BitmapDrawable
    return new BitmapDrawable(res, bm);
}
```
总结:<br/>
- Resources.getDrawable会调用ResourcesImpl.loadDrawable
- ResourcesImpl.loadDrawable对将要返回的Drawable实例进行判断
    1. 如果要使用缓存,则将缓存的Drawable返回
    2. 不使用缓存,则获取要返回的Drawable实例关联的ConstantState
        - 2.1:要返回的Drawable实例关联的ConstantState存在:<br/>则调用ConstantState.newDrawable(Resources)获取Drawable并返回
        - 2.2:要返回的Drawable实例关联的ConstantState不存在:
            - 2.2.1:要返回的Drawable实例属于ColorDrawable,则创建ColorDrawable并返回
            - 2.2.2:要返回的Drawable实例不属于ColorDrawable,则调用loadDrawableForCookie获取Drawable并返回

<br/>2.2.2:loadDrawableForCookie
1. 当加载的资源是drawable文件夹下的xml,调用Drawable.createFromXmlForDensity
    - Drawable.createFromXmlForDensity调用到DrawableInflater.inflateFromXmlForDensity
    - 2.2.2.1:DrawableInflater.inflateFromXmlForDensity
        - 2.2.2.1.0:String name = parser.getName()获取xml标签名称,name有3种情况
            1. 系统内置Drawable类型,name为"shape","bitmap","selector"等
            2. 自定义Drawable类名全称作为xml标签名称,name为类名全称"com.jet.CustomDrawable"
            3. 自定义Drawable,以drawable作为xml标签名称,class指定自定义Drawable类名全称,则name为"drawable"
            4. 如果是第3种,inflateFromXmlForDensity会重新获取class属性值得到自定义Drawable类名全称
        - 2.2.2.1.1:首先调用inflateFromTag(@NonNull String name),根据xml标签名称,生成指定类型的Drawable实例
        - 2.2.2.1.2:inflateFromTag中系统标签和xml中标签对不上,返回null,则调用inflateFromClass,<br/>根据xml指定的Drawable继承类,获取其构造函数,得到Drawable继承类的新实例<br/>
        **inflateFromClass其实就是上文中提到的自定义Drawable用于xml中的情形**
2. 当加载的资源是图片,调用Drawable.createFromResourceStream
    - 如果是.9.png,则返回1个NinePatchDrawable
    - 其他类型图片返回BitmapDrawable

#### 2.2:将获取的Drawable实例设置为View的背景
View.setBackground(Drawable background)
<br/>
源码追踪:
```java
View.java

public void setBackground(Drawable background) {
    setBackgroundDrawable(background);
}
public void setBackgroundDrawable(Drawable background) {
    ****
    //若当前View原始背景和参数相同,直接return
    if (background == mBackground) {
        return;
    }
    //是否需要重新布局
    boolean requestLayout = false;
    mBackgroundResource = 0;
    if (mBackground != null) {
        //取消原始背景的动画
        //移除原始背景的动画监听接口
    }
    if (background != null) {
        //对background设置一系列属性
        //确定是否需要重新布局
        requestLayout = true/false;
        //设置mBackground为background
        mBackground = background;
    }else{
        mBackground = null;
        requestLayout = true;
    }
    if (requestLayout) {
        //重新布局
        requestLayout();
    }
    mBackgroundSizeChanged = true;
    //重绘View实例
    invalidate(true);
    //重建View实例的外部轮廓
    invalidateOutline();
}
```
可见,setBackground替换了View实例的背景Drawable,然后执行了requestLayout(重新布局),invalidate(重绘),invalidateOutline(重建View外部轮廓).<br/>
**invalidate会触发View的draw方法**
```java
public void draw(Canvas canvas) {
    ****
    // Step 1, draw the background, if needed
    drawBackground(canvas);
    ****
}
private void drawBackground(Canvas canvas) {
    //此处的background,就是之前setBackground传入的参数
    final Drawable background = mBackground;
    ****
    background.draw(canvas);
    ****
}
```
可见,View实例的背景Drawable实例最终还是调用自身的Drawable.draw(@NonNull Canvas canvas)方法绘制到屏幕上
```java
Drawable.java
//draw是1个抽象方法
public abstract void draw(@NonNull Canvas canvas);

//对于普通的图片,getResources().getDrawable获取到的是BitmapDrawable,
//看一下BitmapDrawable的draw方法
public class BitmapDrawable extends Drawable {
    ****
    //最终调用了Canvas.drawBitmap方法，
    //将Drawable实例中的bitmap绘制到View实例关联的画布上
    canvas.drawBitmap(bitmap, null, mDstRect, paint);
    ****
}
```
总结:<br/>
将获取的Drawable实例设置为View的背景依次执行
1. 替换View的原始背景Drawable,触发View的重新布局,重绘,重建外部轮廓
2. 重绘执行draw方法,调用drawBackground
3. drawBackground调用了Drawable.draw
4. Drawable.draw是个抽象方法,以之前获取的BitmapDrawable为例,最终调用了Canvas.drawBitmap方法，将Drawable实例中的bitmap绘制到View的画布上

#### 2.3:将获取的Drawable实例设置为ImageView的src
本质上还是执行了Drawable.draw,将src生成的Drawable实例绘制到ImageView实例所在的画布<br/>
[详见简书](https://www.jianshu.com/p/c56b762210f2)

### 3.Drawable中比较重要的方法
#### 3.1.Drawable着色
1. Drawable着色的通用代码
```java
//1:通过图片资源文件生成Drawable实例
Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher).mutate();
//2:先调用DrawableCompat的wrap方法
drawable = DrawableCompat.wrap(drawable);
//3:再调用DrawableCompat的setTint方法，为Drawable实例进行着色
DrawableCompat.setTint(drawable, Color.RED);
```
2. Drawable.mutate()
    - mutate()将为1个Drawable实例单独创建1个ConstantState进行关联,不与其他Drawable共享
![](https://user-gold-cdn.xitu.io/2018/6/28/164454c61b9a9826?w=307&h=400&f=png&s=43896)
    - 未执行mutate()的Drawable,和其他从同1资源生成的Drawable共享1个ConstantState
![](https://user-gold-cdn.xitu.io/2018/6/28/164454f50371bf71?w=307&h=400&f=png&s=40118)
3. DrawableCompat.wrap(@NonNull Drawable drawable)
    - SDK>=23:DrawableCompat.wrap直接返回原始的Drawable实例
    - 其余情况下,DrawableCompat.wrap返回了Drawable的子类DrawableWrapperGingerbread的一个新实例,且在updateTint方法中移除了该新实例关联过的ColorFilter,设置了该新实例的绘制范围和原始Drawable实例相同
4. DrawableCompat.setTint(@NonNull Drawable drawable, @ColorInt int tint)
    - 在SDK>=21:DrawableCompat.setTint执行的是Drawable原生的setTint方法
    - 其余情况下,DrawableCompat.setTint本质上还是执行了Drawable中的setColorFilter方法
#### 3.2:Drawable.getOutline引出ViewOutlineProvider,其用法如下
**经试验,在SDK>=21下生效,且仅能生成 椭圆,圆角矩形,矩形 三种轮廓,尝试使用path生成任意多边形失败**
```java
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Out0 extends ViewOutlineProvider{
    @Override
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),40);
    }
}
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Out1 extends ViewOutlineProvider{
    @Override
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(100,100,view.getWidth()-100,view.getHeight()-100,40);
    }
}
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Out2 extends ViewOutlineProvider{
    @Override
    public void getOutline(View view, Outline outline) {
        Path path = new Path();
        path.setFillType(Path.FillType.WINDING);
        path.moveTo(0,0);
        path.rLineTo(50,20);
        path.rLineTo(50,30);
        path.rLineTo(50,40);
        path.rLineTo(50,50);
        path.close();
        Log.e("Jet","path.isConvex():"+path.isConvex());
        outline.setConvexPath(path);
    }
}
int outLineIndex = 0;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public void toggleOutline(View view) {
    v.setClipToOutline(false);
    if(outLineIndex == 0){
        v.setOutlineProvider(new Out0());
    }
    else if(outLineIndex == 1){
        v.setOutlineProvider(new Out1());
    }
    else if(outLineIndex == 2){
        v.setOutlineProvider(new Out2());
    }
    v.setClipToOutline(true);
    outLineIndex = (++outLineIndex) % 3;
}
```

### 4.Drawable子类用法
[详见简书](https://www.jianshu.com/p/39f09ea26430)