## 1.ViewRoot和DecorView
- DecorView是Activity的顶级View,继承于FrameLayout.
- 一般情况下:DecorView内部持有1个垂直方向的LinearLayout.LinearLayout顶部是标题栏,下方是一个FrameLayout,FrameLayout的id值是android.R.id.content.
- 在Activity中设置setContentView,本质就是将布局文件通过LayoutInflator添加到DecorView中.
- 源码为证:
    ```java
    Activity:
    public void setContentView(@LayoutRes int layoutResID) {
        //Android中Window就是PhoneWindow,看PhoneWindow的setContentView
        getWindow().setContentView(layoutResID);
        initWindowDecorActionBar();
    }
    PhoneWindow:
    public void setContentView(int layoutResID) {
        if (mContentParent == null) {
            //在
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }
        ****
            //将布局文件生成的View填充到mContentParent
            //看一下mContentParent获取流程
            mLayoutInflater.inflate(layoutResID, mContentParent);
        ****
    }
    //mContentParent获取流程
    private void installDecor() {
        if (mDecor == null) {
            //生成DecorView
            mDecor = generateDecor(-1);
            ****
        }****
        if (mContentParent == null) {
            //根据DecorView生成mContentParent
            mContentParent = generateLayout(mDecor);
            ***
        }
    }
    /**
     * 返回在Activity中通过getWindow().requestFeature设置的标记
     * Return the feature bits that are being implemented by this Window.
     * This is the set of features that were given to requestFeature(), and are
     * being handled by only this Window itself, not by its containers.
     *
     * @return int The feature bits.
     */
    protected final int getLocalFeatures(){
        return mLocalFeatures;
    }
    protected ViewGroup generateLayout(DecorView decor) {
        ****
        //布局文件ID
        int layoutResource;
        //获取当前PhoneWindow通过requestFeature设置过的标记
        int features = getLocalFeatures();
        //含有不同标记,则对应不同的布局文件
        if ((features & (1 << FEATURE_SWIPE_TO_DISMISS)) != 0) {
            实例1
            layoutResource = R.layout.screen_swipe_dismiss;
            setCloseOnSwipeEnabled(true);
        } else if ((features & ((1 << FEATURE_LEFT_ICON) | (1 << FEATURE_RIGHT_ICON))) != 0) {
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleIconsDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                实例2
                layoutResource = R.layout.screen_title_icons;
            }
            // XXX Remove this once action bar supports these features.
            removeFeature(FEATURE_ACTION_BAR);
            // System.out.println("Title Icons!");
        } else if ((features & ((1 << FEATURE_PROGRESS) | (1 << FEATURE_INDETERMINATE_PROGRESS))) != 0
                && (features & (1 << FEATURE_ACTION_BAR)) == 0) {
            // Special case for a window with only a progress bar (and title).
            // XXX Need to have a no-title version of embedded windows.
            layoutResource = R.layout.screen_progress;
            // System.out.println("Progress!");
        } else if ((features & (1 << FEATURE_CUSTOM_TITLE)) != 0) {
            // Special case for a window with a custom title.
            // If the window is floating, we need a dialog layout
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogCustomTitleDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = R.layout.screen_custom_title;
            }
            // XXX Remove this once action bar supports these features.
            removeFeature(FEATURE_ACTION_BAR);
        } else if ((features & (1 << FEATURE_NO_TITLE)) == 0) {
            // If no other features and not embedded, only need a title.
            // If the window is floating, we need a dialog layout
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else if ((features & (1 << FEATURE_ACTION_BAR)) != 0) {
                layoutResource = a.getResourceId(
                        R.styleable.Window_windowActionBarFullscreenDecorLayout,
                        R.layout.screen_action_bar);
            } else {
                layoutResource = R.layout.screen_title;
            }
            // System.out.println("Title!");
        } else if ((features & (1 << FEATURE_ACTION_MODE_OVERLAY)) != 0) {
            layoutResource = R.layout.screen_simple_overlay_action_mode;
        } else {
            // Embedded, so no decoration is needed.
            实例3
            layoutResource = R.layout.screen_simple;
            // System.out.println("Simple!");
        }
        ****
        //关键代码
        //1:将layoutResource转换的Veiw填充到mDecor中
        mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);
        //2:contentParent就是mDecor中ID值为android.R.id.content的ViewGroup
        ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
        ****
        return contentParent;
    }
    public static final int ID_ANDROID_CONTENT = com.android.internal.R.id.content;
    @Override
    public final View getDecorView() {
        ****
        return mDecor;
    }
    
    //2:getDecorView()返回的就是在installDecor中生成的DecorView
    @Nullable
    public <T extends View> T findViewById(@IdRes int id) {
        return getDecorView().findViewById(id);
    }
    
    //1:将layoutResource转换的Veiw填充到mDecor中
    DecorView:
    public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks {
        void onResourcesLoaded(LayoutInflater inflater, int layoutResource){
            mStackId = getStackId();
            ****
            //mDecorCaptionView对应一个自由浮动重叠窗口,一般情况忽略即可
            mDecorCaptionView = createDecorCaptionView(inflater);
            //将布局文件转换为View实例
            final View root = inflater.inflate(layoutResource, null);
            if (mDecorCaptionView != null) {
                ****
            } else {
                //将layoutResource转换的Veiw填充到DecorView中
                addView(root, 0, new ViewGroup.LayoutParams(MATCH_PARENT,MATCH_PARENT));
            }
            ****
        }
    }
    
    //3:通过上面源码可知:
    在Activity中调用setContentView(int 布局文件),
    本质就是将布局文件生成的View实例V,
    填充到当前Activity对应的DecorView下id为android.R.id.content的ViewGroup中.
    
    最后一个问题:DecorView实例对应的布局文件具体结构:查看已经标注过的几个xml文件
    
    R.layout.screen_swipe_dismiss:
    //SwipeDismissLayout本身就继承于FrameLayout
    class SwipeDismissLayout extends FrameLayout {
        ***
    }
    <com.android.internal.widget.SwipeDismissLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@android:id/content"
        android:fitsSystemWindows="true"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
    
    R.layout.screen_title_icons:
    <LinearLayout 
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:fitsSystemWindows="true"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        状态栏
        <ViewStub android:id="@+id/action_mode_bar_stub"
                  android:inflatedId="@+id/action_mode_bar"
                  android:layout="@layout/action_mode_bar"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:theme="?attr/actionBarTheme"/>
        标题栏
        <RelativeLayout android:id="@android:id/title_container"
            style="?android:attr/windowTitleBackgroundStyle"
            android:layout_width="match_parent"
            android:layout_height="?android:attr/windowTitleSize">
            <!-- The title background has 9px left padding. -->
            <ImageView android:id="@android:id/left_icon"
                android:visibility="gone"
                android:layout_marginEnd="9dip"
                android:layout_width="16dip"
                android:layout_height="16dip"
                android:scaleType="fitCenter"
                android:layout_alignParentStart="true"
                android:layout_centerVertical="true" />
            <ProgressBar android:id="@+id/progress_circular"
                style="?android:attr/progressBarStyleSmallTitle"
                android:visibility="gone"
                android:max="10000"
                android:layout_centerVertical="true"
                android:layout_alignParentEnd="true"
                android:layout_marginStart="6dip"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />
            <!-- There are 6dip between this and the circular progress on the right, we
                 also make 6dip (with the -3dip margin_left) to the icon on the left or
                 the screen left edge if no icon. This also places our left edge 3dip to
                 the left of the title text left edge. -->
            <ProgressBar android:id="@+id/progress_horizontal"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginStart="-3dip"
                android:layout_toStartOf="@android:id/progress_circular"
                android:layout_toEndOf="@android:id/left_icon"
                android:layout_centerVertical="true"
                android:visibility="gone"
                android:max="10000" />
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="horizontal"
                android:layout_toStartOf="@id/progress_circular"
                android:layout_toEndOf="@android:id/left_icon"
                >
                <TextView android:id="@android:id/title"
                    style="?android:attr/windowTitleStyle"
                    android:layout_width="0dip"
                    android:layout_height="match_parent"
                    android:layout_weight="1"
                    android:background="@null"
                    android:fadingEdge="horizontal"
                    android:scrollHorizontally="true"
                    android:gravity="center_vertical"
                    android:layout_marginEnd="2dip"
                    />
                <!-- 2dip between the icon and the title text, if icon is present. -->
                <ImageView android:id="@android:id/right_icon"
                    android:visibility="gone"
                    android:layout_width="16dip"
                    android:layout_height="16dip"
                    android:layout_weight="0"
                    android:layout_gravity="center_vertical"
                    android:scaleType="fitCenter"
                    />
                </LinearLayout>
        </RelativeLayout>
        Activity中setContentView会填充进去的FrameLayout
        <FrameLayout android:id="@android:id/content"
            android:layout_width="match_parent"
            android:layout_height="0dip"
            android:layout_weight="1"
            android:foregroundGravity="fill_horizontal|top"
            android:foreground="?android:attr/windowContentOverlay" />
    </LinearLayout>
    
    R.layout.screen_simple:
    <LinearLayout 
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fitsSystemWindows="true"
        android:orientation="vertical">
        actionbar
        <ViewStub android:id="@+id/action_mode_bar_stub"
                  android:inflatedId="@+id/action_mode_bar"
                  android:layout="@layout/action_mode_bar"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:theme="?attr/actionBarTheme" />
        Activity中setContentView会填充进去的FrameLayout
        <FrameLayout
             android:id="@android:id/content"
             android:layout_width="match_parent"
             android:layout_height="match_parent"
             android:foregroundInsidePadding="false"
             android:foregroundGravity="fill_horizontal|top"
             android:foreground="?android:attr/windowContentOverlay" />
    </LinearLayout>
    ```
- 源码一看就顺了,setContentView本质就是往DecorView中id为android.R.id.content的FrameLayout中进行填充

## 2.理解MeasureSpec
### 1.MeasureSpec
- MeasureSpec是一个32位的值,高2位代表SpecMode,低30位代表SpecSize
- SpecMode是测量模式,有3种情况
    - UNSPECIFIED:一般用不到
    - EXACTLY:父容器已经知道View所需的精确大小,对应LayoutParams中match_parent及具体数值
    - AT_MOST:父容器指定了一个可用大小,及SpecSize,View的大小不能超过SpecSize,对应LayoutParams中wrap_content
- 对于每个View,**宽和高各自有1个MeasureSpec**
### 2.MeasureSpec生成规则
- 对于顶级View(DecorView),宽/高的MeasureSpec是由窗口尺寸和自身LayoutParams共同确定
- 对于普通View,宽/高的MeasureSpec由父容器的MeasureSpec和自身LayoutParams共同确定
- 对于普通View,宽/高的MeasureSpec是在父元素的getChildMeasureSpec生成.
    - parentSpecMode:父元素MeasureSpec的SpecMode
    - parentSize:父元素目前可使用的大小
    - childSize:子元素LayoutParams的数值
<table align="center">
    <tr>
        <td></td>
        <td>EXACTLY</td>
        <td>AT_MOST</td>
        <td>parentSpecMode</td>
    </tr>
    <tr>
        <td>dp/px</td>
        <td>EXACTLY<br/>childSize</td>
        <td>EXACTLY<br/>childSize</td>
        <td></td>
    </tr>
    <tr>
        <td>match_parent</td>
        <td>EXACTLY<br/>parentSize</td>
        <td>AT_MOST<br/>parentSize</td>
        <td></td>
    </tr>
    <tr>
        <td>wrap_content</td>
        <td>AT_MOST<br/>parentSize</td>
        <td>AT_MOST<br/>parentSize</td>
        <td></td>
    </tr>
    <tr>
        <td>childLayoutParams</td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
</table>

- 上述表格可见,当View采用固定宽高,无论父元素MeasureSpec啥样,生成的MeasureSpec都是精确模式,且大小遵循其LayoutParams中大小
- 当View宽/高是match_parent,其MeasureSpec的测量模式继承父元素,EXACTLY时大小等于父元素;AT_MOST时候大小不大于父元素
- 当View宽/高是wrap_content,其MeasureSpec的测量模式总是AT_MOST,且大小不大于父元素
- 当View宽/高是match_parent或wrap_content时,其MeasureSpec的SpecSize相等,都是父元素目前可使用的大小

## 3.View的工作流程
### 1.measure过程
#### 1.View的measure过程
1. View在measure方法中调用onMeasure完成测量
2. onMeasure源码
    ```java
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
        getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
        getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        //结合getDefaultSize,onMeasure相当于
        //setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
    }
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        //由此可见,View的测量大小最终还是由其MeasureSpec中的SpecSize决定
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
    ```
3. onMeasure源码可见,View的测量大小最终还是由MeasureSpec中的SpecSize决定.
    - View的MeasureSpec生成规则可知,默认情况下,当View处于match_parent和wrap_content时,生成的MeasureSpec的SpecSize完全相同,都是父元素目前可使用的大小
    - 所以自定义View必须重写onMeasure,单独设置宽/高位wrap_content时View的大小
4. 自定义View重写onMeasure解决wrap_content和match_parent大小一致问题,原生控件都会重写onMeasure
    ```
    //宽/高为wrap_content时对应的默认尺寸
    private int default_wrapcontent_width = 100;
    private int default_wrapcontent_height = 100;
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(default_wrapcontent_width,default_wrapcontent_height);
        }else if(widthSpecMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(default_wrapcontent_width,heightSpecSize);
        }else if(heightSpecMode == MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSpecSize,default_wrapcontent_height);
        }
    }
    ```
#### 2.ViewGroup的measure过程
> ViewGroup提供了一个measureChildren方法,在其中遍历子元素,生成每个子元素的MeasureSpec,调用子元素的measure方法完成子元素的测量

### 2.layout过程
1. layout确定View自身位置
2. onLayout确定所有子元素的位置
3. 源码为证
    ```java
    View
    
    /**
     * Called from layout when this view should
     * assign a size and position to each of its children.
     *
     * @param changed This is a new size or position for this view
     * @param left Left position, relative to parent
     * @param top Top position, relative to parent
     * @param right Right position, relative to parent
     * @param bottom Bottom position, relative to parent
     */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }
    /**
     * Assign a size and position to a view and all of its
     * descendants
     *
     * <p>Derived classes should not override this method.
     * Derived classes with children should override
     * onLayout. In that method, they should
     * call layout on each of their children.</p>
     *
     * @param l Left position, relative to parent
     * @param t Top position, relative to parent
     * @param r Right position, relative to parent
     * @param b Bottom position, relative to parent
     */
    @SuppressWarnings({"unchecked"})
    public void layout(int l, int t, int r, int b) {
        ****
        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);
        if (changed || ***) {
            onLayout(changed, l, t, r, b);
            ***
        }
        ****
    }
    /**
     * Assign a size and position to this view.
     *
     * This is called from layout.
     *
     * @param left Left position, relative to parent
     * @param top Top position, relative to parent
     * @param right Right position, relative to parent
     * @param bottom Bottom position, relative to parent
     * @return true if the new size and position are different than the
     *         previous ones
     * {@hide}
     */
    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;
        //首先校验左上角和右下角坐标是否有变化
        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            changed = true;
            // Remember our drawn bit
            int drawn = mPrivateFlags & PFLAG_DRAWN;
            //计算老的宽高
            int oldWidth = mRight - mLeft;
            int oldHeight = mBottom - mTop;
            //计算新宽高
            int newWidth = right - left;
            int newHeight = bottom - top;
            //确定尺寸是否发生变化
            boolean sizeChanged = (newWidth != oldWidth) || (newHeight != oldHeight);
            // Invalidate our old position
            invalidate(sizeChanged);
            //更新左上角,右下角的原始坐标:mLeft,mTop,mRight,mBottom
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
            mRenderNode.setLeftTopRightBottom(mLeft, mTop, mRight, mBottom);
            mPrivateFlags |= PFLAG_HAS_BOUNDS;
            if (sizeChanged) {
                sizeChange(newWidth, newHeight, oldWidth, oldHeight);
            }
            if ((mViewFlags & VISIBILITY_MASK) == VISIBLE || mGhostView != null) {
                // If we are visible, force the DRAWN bit to on so that
                // this invalidate will go through (at least to our parent).
                // This is because someone may have invalidated this view
                // before this call to setFrame came in, thereby clearing
                // the DRAWN bit.
                mPrivateFlags |= PFLAG_DRAWN;
                invalidate(sizeChanged);
                // parent display list may need to be recreated based on a change in the bounds
                // of any child
                invalidateParentCaches();
            }
            // Reset drawn bit to original value (invalidate turns it off)
            mPrivateFlags |= drawn;
            mBackgroundSizeChanged = true;
            mDefaultFocusHighlightSizeChanged = true;
            if (mForegroundInfo != null) {
                mForegroundInfo.mBoundsChanged = true;
            }
            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        return changed;
    }
    ```
4. 源码可知:
    - layout调用setFrame更新自身左上角及右下角的坐标,然后调用onLayout方法
    - onLayout方法会遍历子View并调用子View的layout方法完成子元素的布局

### 3.draw过程
1. draw
    - 伪代码
    ```
    public void draw(Canvas canvas) {
        drawBackground(canvas);     //private方法不可重写
        onDraw(canvas);             //可重写
        dispatchDraw(canvas);       //可重写
        onDrawForeground(canvas);   //可重写
    }
    ```
    - 绘制顺序依次:
        - 绘制背景      private方法不可重写
        - 绘制自身内容  可重写
        - 绘制子元素    可重写
        - 绘制装饰      可重写
2. onDraw中绘制自身内容
    - View中onDraw是个空实现,所以直接extends View,onDraw中的super.onDraw删掉无妨
    ```
    /**
     * Implement this to do your drawing.
     */
    protected void onDraw(Canvas canvas) {
    }
    ```
    - 自定义View继承已存在的空间,绘制代码写在super.onDraw(canvas)上面,自己绘制的内容会被控件的原内容盖住;写在super.onDraw(canvas)下面,自己绘制的内容会盖住控件原始内容
3. dispatchDraw中绘制子元素
4. onDrawForeground依次绘制:滑动边缘渐变+滚动条+前景
5. setWillNotDraw(boolean willNotDraw)
    - 如果我们自定义控件继承ViewGroup,并且自身不具备绘制功能,可以调用setWillNotDraw(true),系统会进行优化
    - 如果明确知道一个ViewGroup需要通过onDraw绘制内容,必须在onDraw中显式调用setWillNotDraw(false)
    ```
    /**
     * If this view doesn't do any drawing on its own, set this flag to
     * allow further optimizations. By default, this flag is not set on
     * View, but could be set on some View subclasses such as ViewGroup.
     * Typically, if you override {@link #onDraw(android.graphics.Canvas)}
     * you should clear this flag.
     *
     * @param willNotDraw whether or not this View draw on its own
     */
    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
    }
    ```
6. 自定义View,有时候一段绘制代码写在不同的绘制方法中效果是一样的.但有一个例外：如果绘制代码既可以写在 onDraw() 里，也可以写在其他绘制方法里，那么优先写在 onDraw() ，因为 Android 有相关的优化，可以在不需要重绘的时候自动跳过  onDraw() 的重复执行，以提升开发效率。享受这种优化的只有 onDraw() 一个方法
7. 绘制代码的位置 与 绘制内容出现的位置 之间的关系,源自HenCoder
![](https://user-gold-cdn.xitu.io/2018/6/13/163f922afd1d5dcf?w=943&h=504&f=jpeg&s=86548)

