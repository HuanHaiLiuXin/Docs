## 1.View基础知识
### 1.什么是View
View是Android中所有控件的基类.
### 2.View的位置参数
- View原始的左上角和右下角的x,y坐标值分别是mLeft,mTop,mRight,mBottom;这些值都是相对于View的父容器来说的,它是一种相对坐标.
- mLeft = v.getLeft()
- mTop = v.getTop()
- mRight = v.getRight()
- mBottom = v.getBottom()
- translationX和translationY是View的左上角相对于父容器的偏移量.x,y是View左上角当前实际坐标值
- x = mLeft + translationX
- y = mTop + translationY
### 3.MotionEvent和TouchSlop
#### 1.MotionEvent
- event.getX,event.getY:事件相对于当前View左上角的x,y坐标
- event.getRawX,event.getRawY:事件相对于屏幕左上角的x,y坐标
#### 2.TouchSlop
- TouchSlop是系统所能识别的被认为是滑动的最小距离
- 获取TouchSlop:ViewConfiguration.get(getContext()).getScaledTouchSlop()
### 4.VelocityTracker,GestureDetector,Scroller
#### 1.VelocityTracker

> VelocityTracker用于追踪手指在滑动过程中的速度,包括水平和竖直方向的速度;

VelocityTracker使用流程
```java
//获取Velocity实例
VelocityTracker vt = VelocityTracker.obtain();
//添加要追踪速度的MotionEvents实例
vt.addMovement(Movement ev);
//设置速度计算的时间跨度毫秒值(计算在units毫秒值内的移动距离,以px为单位)
vt.computeCurrentVelocity(int units)
//计算在units毫秒值内,x,y方向移动的距离,以px为单位
float xVelocity = vt.getXVelocity();
float yVelocity = vt.getYVelocity();
//VelocityTracker使用完毕,使用clear充值,并recycle回收内存
vt.clear();
vt.recycle();
```
#### 2.GestureDetector
*手势检测,一般情况下,需要监听双击行为才是用GestureDetector,实现其OnDoubleTapListener*
#### 3.Scroller
*用于实现View的弹性滑动,View的弹性滑动具体实现后面会详述*
## 2.View的滑动
### 实现View的滑动的三种方法:
- 通过View本身的scrollTo,scrollBy
- 通过动画给Veiw施加平移效果
- 通过改变View的LayoutParams使得View重新布局从而实现滑动
### 1.使用scrollTo,scrollBy实现View的滑动
> 使用scrollTo,scrollBy只能改变View中内容的位置,不能对View本身进行移动.
- scrollTo,scrollBy改变的是View内部的两个属性mScrollX和mScrollY,可以通过getScrollX,getScrollY获得;
- mScrollX代表View的左边缘和View中的内容左边缘在水平方向的距离;mScrollY代表View的上边缘和View中的内容上边缘在垂直方向的距离;
- mScrollX和mScrollY的单位是像素;
- 当View的上边缘在View中内容上边缘的下边时,mScrollY>0,反之<0
- 当Veiw的左边缘在View中内容左边缘的右边时,mScrollX>0,反之<0
### 2.使用动画实现View的滑动
- View动画是对View的影响做操作,无法真正对View的位置参数,宽高等
- 属性动画是对View中的属性,自定义属性进行变更,可以真正改变View的位置
### 3.改变一个View的LayoutParams来实现View的滑动.
```java
MarginLayoutParams prams = (MarginLayoutParams)bt.getLayoutParams();
//增加View的宽度100px
params.width += 100;
//增加View的左侧margin值100px
params.leftMargin += 100;
bt.setLayoutParams(params);
```
### 4.上述3中方式的场景
- scrollTo,scrollBy:适用于对View的内容的滑动
- 动画:适用于有要求动画效果但无用户交互的View(TextView)
- 改变布局参数:适用于有交互的View(Button)
## 3.弹性滑动
- 使用Scroller实现弹性滑动,Scroller滑动的是View的内容区域,View本身位置不变
    - 原理:
        1. 在smoothScroll中调用invalidate,触发View的draw方法
        2. draw方法会调用computeScroll
        3. 在computeScroll中,首先用Scroller实例计算的当前的mScrollX,mScrollY,调用scrollTo对内容区进行滑动;然后调用postInvalidate重绘
        4. postInvalidate会继续触发draw.然后2,3,4会不断重复,直至滑动完成
    - 示例:
        ```java
        public class ArtView extends View{
            //自定义View中持有Scroller实例
            private Scroller scroller;
    
            public ArtView(Context context) {
                this(context,null,0);
            }
            public ArtView(Context context, @Nullable AttributeSet attrs) {
                this(context, attrs,0);
            }
            public ArtView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                init(context);
            }
            private void init(Context context){
                //初始化Scroller实例
                scroller = new Scroller(context);
            }
    
            /**
             * 实现在duration毫秒内,滑动View的内容区域,实现mScrollX=targetX,mScrollY=targetY
             * @param targetX
             * @param targetY
             * @param duration
             */
            public void smoothScroll(int targetX,int targetY,int duration){
                int currScrollX = this.getScrollX();
                int currScrollY = this.getScrollY();
                int deltaX = targetX - currScrollX;
                int deltaY = targetY - currScrollY;
                scroller.startScroll(currScrollX,currScrollY,deltaX,deltaY,duration);
                invalidate();
            }
            @Override
            public void computeScroll() {
                super.computeScroll();
                if(scroller.computeScrollOffset()){
                    scrollTo(scroller.getCurrX(),scroller.getCurrY());
                    postInvalidate();
                }
            }
        }
        ```
- 使用动画实现弹性滑动
    - 可以使用ObjectAnimator或者ValueAnimator实现
    - ObjectAnimator需要指定属性,而ValueAnimator可以通过AnimatorUpdateListener的onAnimationUpdate方法对任意属性进行设置
    - ObjectAnimator实例:
        ```java
        Animator a1 = ObjectAnimator.ofInt(v,"scrollY",0,400).setDuration(400);
        Animator a2 = ObjectAnimator.ofInt(v,"scrollX",0,400).setDuration(400);
        a1.start();
        a2.start();
        ```
    - ValueAnimator实例:
        ```java
        ValueAnimator animator = ValueAnimator.ofInt(0,100).setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currValue = (int) animation.getAnimatedValue();
                v.scrollTo(currValue,currValue);
            }
        });
        animator.start();
        ```

## 4.View的事件分发机制
> View的事件分发机制,就是对MotionEvent的分发逻辑.
### 1.点击事件的传递规则
- ViewGroup点击事件传递规则伪代码
    ```java
    public boolean dispatchTouchEvent(MotionEvent ev){
        boolean consume = false;
        if(onInterceptTouchEvent(ev)){
            //事件被拦截
            if(onTouchListener != null && onTouchListener.onTouch(this,ev)){
                //如果设置过OnTouchListener,先执行OnTouchListener.onTouch,
                //onTouch为true,直接返回
                return true;
            }else{
                //未设置OnTouchListener,或者OnTouchListener.onTouch返回false,
                //则调用onTouchEvent返回
                return onTouchEvent(ev);
            }
        }else{
            //事件未被拦截,则将事件传递至对应的子View
            consume = child.dispatchTouchEvent(ev);
        }
        return consume;
    }
    onTouchEvent是继承的View的,看View即可.
    ```
- View点击事件传递规则伪代码
    ```java
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(onTouchListener != null && onTouchListener.onTouch(this,ev)){
            //如果设置过OnTouchListener,先执行OnTouchListener.onTouch,
            //onTouch为true,直接返回
            return true;
        }else{
            //未设置OnTouchListener,或者OnTouchListener.onTouch返回false,
            //则调用onTouchEvent返回
            return onTouchEvent(ev);
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        //View的onTouchEvent,默认返回true,除非不可点击
        boolean clickable = CLICKABLE || LONG_CLICKABLE;
        if(OnClickListener != null && clickable && 收到了ACTION_DOWN和ACTION_UP){
            //可点击,且接收过DOWN和UP,设置过的OnClickListener才会执行
            OnClickListener.onClick(this);
        }
        return clickable;
    }
    ```
- 点击事件传递规则结论
    1. 一个事件被消耗:拦截了事件的View
        - 设置的OnTouchListener.onTouch返回true
        - 或View的onTouchEvent返回true
    2. 同一事件序列:从手指接触屏幕开始,到离开屏幕为止.down,move....up
    3. 正常情况下,一个事件序列只能被1个View拦截且消耗.
    4. 如果View拦截了ACTION_DOWN事件,但没有消耗(onTouchEvent返回了false),那么事件序列中后续事件都会重新交给它的父元素处理,即调用父元素的onTouchEvent.
    5. 如果View仅消耗ACTION_DOWN事件,但后续事件都不消耗,那么事件会消失,并不会调用父元素的onTouchEvent,当前View会持续受到后续事件,最终这些事件交友Activity处理.
    6. ViewGroup默认不拦截事件,onInterceptTouchEvent默认返回false
    7. View没有onInterceptTouchEvent,dispatchTouchEvent中获取到事件:
        - 有OnTouchListener则执行OnTouchListener.onTouch,onTouch为true则直接返回true;
        - 没有OnTouchListener,或者OnTouchListener.onTouch为false,则执行onTouchEvent
    8. onTouchEvent默认返回true,除非不可点击
        - CLICKABLE和LONG_CLICKABLE都是false,才表示不可点击,返回false;
        - 可点击,且View接收过DOWN和UP,才执行OnClickListener的onClick;
    9. setOnClickListener会自动将View的CLICKABLE设为true;setOnLongClickListener则会自动将View的LONG_CLICKABLE设为true.
    10. 子View可以通过requestDisallowInterceptTouchEvent方法干预父元素的事件分发,但是ACTION_DOWN除外(后面会详解,先记住这条)

### 2.点击事件的源码解析
- requestDisallowInterceptTouchEvent如何生效，涉及另外2个方法：dispatchTouchEvent,resetTouchState，依次分析
    - requestDisallowInterceptTouchEvent
        ```java
        ViewGroup.java
        
        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept == ((mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0)) {
                //1.1:若mGroupFlags已经添加过FLAG_DISALLOW_INTERCEPT，则：mGroupFlags & FLAG_DISALLOW_INTERCEPT！=0
                //1.2:已经添加过FLAG_DISALLOW_INTERCEPT，再次添加(disallowIntercept为true),直接return
                return;
            }
            if (disallowIntercept) {
                //2.1.disallowIntercept为true,为mGroupFlags添加FLAG_DISALLOW_INTERCEPT
                mGroupFlags |= FLAG_DISALLOW_INTERCEPT;
            } else {
                //2.2:disallowIntercept为false,为mGroupFlags移除FLAG_DISALLOW_INTERCEPT
                mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
            }
            if (mParent != null) {
                //3:递归 添加或移除 当前ViewGroup其父View的FLAG_DISALLOW_INTERCEPT标志位
                mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
        ```
    - resetTouchState
        ```java
        private void resetTouchState() {
            clearTouchTargets();
            resetCancelNextUpFlag(this);
            //关键的来了,在resetTouchState中,会为mGroupFlags移除FLAG_DISALLOW_INTERCEPT
            mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
            mNestedScrollAxes = SCROLL_AXIS_NONE;
        }
        ```
    - dispatchTouchEvent
        ```java
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            ****
            boolean handled = false;
            if (onFilterTouchEventForSecurity(ev)) {
                final int action = ev.getAction();
                final int actionMasked = action & MotionEvent.ACTION_MASK;
                if (actionMasked == MotionEvent.ACTION_DOWN) {
                    cancelAndClearTouchTargets(ev);
                    //1:ACTION_DOWN时候回调用resetTouchState:resetTouchState会移除mGroupFlags中的FLAG_DISALLOW_INTERCEPT
                    resetTouchState();
                }
                final boolean intercepted;
                if (actionMasked == MotionEvent.ACTION_DOWN|| mFirstTouchTarget != null) {
                    //ACTION_DOWM 或 存在处理当前事件的子元素
                    
                    //2.1:ACTION_DOWM情况下,mGroupFlags中的FLAG_DISALLOW_INTERCEPT在resetTouchState()被移除，
                    //(mGroupFlags & FLAG_DISALLOW_INTERCEPT) = 0,所以ACTION_DOWM情况下disallowIntercept一定为false;
                    //2.2:不是ACTION_DOWM情况下：
                        //2.2.1:如果调用过requestDisallowInterceptTouchEvent(true),则 mGroupFlags & FLAG_DISALLOW_INTERCEPT!=0,disallowIntercept为true;
                        //2.2.2:若未调用过requestDisallowInterceptTouchEvent或requestDisallowInterceptTouchEvent(false),则 mGroupFlags & FLAG_DISALLOW_INTERCEPT=0，disallowIntercept为false
                        
                    final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
                    if (!disallowIntercept) {
                        //ACTION_DOWM情况下,disallowIntercept为false,一定会执行onInterceptTouchEvent判断是否拦截事件
                        //非ACTION_DOWN情况下,若未调用或requestDisallowInterceptTouchEvent(false),disallowIntercept为false,也会执行onInterceptTouchEvent
                        intercepted = onInterceptTouchEvent(ev);
                        ev.setAction(action); // restore action in case it was changed
                    } else {
                        //非ACTION_DOWN 且 存在处理当前事件的子元素 且 执行过requestDisallowInterceptTouchEvent(true),则不再拦截
                        intercepted = false;
                    }
                } else {
                    //非ACTION_DOWN 且 不存在处理当前事件的子元素，一律拦截
                    intercepted = true;
                }
            }
            ****
        }
        ```
    - 总结以上源码可知：
        - 在ACTION_DOWN情况下，不论是否调用过requestDisallowInterceptTouchEvent(true)，一定会执行onInterceptTouchEvent判断是否拦截事件
        - 非ACTION_DOWN情况下：
            - 不存在处理当前事件的子元素，一律拦截 
            - 存在处理当前事件的子元素，且执行过requestDisallowInterceptTouchEvent(true)，则ViewGroup不拦截事件，交由子元素处理
            - 存在处理当前事件的子元素，若未调用或requestDisallowInterceptTouchEvent(false),会执行onInterceptTouchEvent判断是否拦截事件
- mFirstTouchTarget
    - 在diapatchTouchEvent中用到的mFirstTouchTarget(处理当前事件的子元素),其赋值也是在dispatchTouchEvent中,在dispatchTouchEvent中调用addTouchTarget为mFirstTouchTarget赋值
    - [dispatchTouchEvent详解](https://www.jianshu.com/p/238d1b753e64)
    - 源码分析：承接上面dispatchTouchEvent
        ```java
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            ****
            boolean handled = false;
            if (onFilterTouchEventForSecurity(ev)) {
                //承接上面dispatchTouchEvent部分
                ****
                final boolean intercepted;
                if (actionMasked == MotionEvent.ACTION_DOWN|| mFirstTouchTarget != null) {
                    ****
                } else {
                    //非ACTION_DOWN 且 不存在处理当前事件的子元素，一律拦截
                    intercepted = true;
                }
                
                //继续分析mFirstTouchTarget赋值过程：
                final boolean canceled = resetCancelNextUpFlag(this) || actionMasked == MotionEvent.ACTION_CANCEL;
                if (!canceled && !intercepted) {
                    //事件不是取消事件，也没有拦截那么就要判断
                    if (actionMasked == MotionEvent.ACTION_DOWN  
                        || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)  
                        || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                        ****
                        final int childrenCount = mChildrenCount;
                        if (newTouchTarget == null && childrenCount != 0) {
                            //如果新的触摸对象为null（这个不是铁定的吗）并且当前ViewGroup有子元素
                            final float x = ev.getX(actionIndex);  
                            final float y = ev.getY(actionIndex);  
                            //下面所做的工作，就是找到可以接收这个事件的子元素  
                            final View[] children = mChildren;
                            //是否使用自定义的顺序来添加控件  
                            final boolean customOrder = isChildrenDrawingOrderEnabled();
                            for (int i = childrenCount - 1; i >= 0; i--) {
                                //如果是用了自定义的顺序来添加控件，那么绘制的View的顺序和mChildren的顺序是不一样的  
                                //所以要根据getChildDrawingOrder取出真正的绘制的View  
                                //自定义的绘制，可能第一个会画到第三个，和第四个，第二个画到第一个，这样里面的内容和Children是不一样的
                                final int childIndex = customOrder ?  getChildDrawingOrder(childrenCount, i) : i;  
                                final View child = children[childIndex];  
                                //如果child不可以接收这个触摸的事件，continue
                                //canViewReceivePointerEvents:View是否可见或正在执行动画
                                //isTransformedTouchPointInView:点击事件坐标是否在View的范围内
                                if (!canViewReceivePointerEvents(child) || !isTransformedTouchPointInView(x, y, child, null)) {  
                                    continue;  
                                }
                                //获取新的触摸对象，如果当前的子View在之前的触摸目标的列表当中就返回touchTarget  
                                //子View不在之前的触摸目标列表那么就返回null
                                newTouchTarget = getTouchTarget(child);
                                ****
                                //dispatchTransformedTouchEvent实际上就是调用子元素的dispathTouchEvent
                                if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                    //dispatchTransformedTouchEvent返回true,说明事件已经被子元素消耗掉，
                                    //这是就应该对mFirstTouchTarget赋值
                                    
                                    // Child wants to receive touch within its bounds.  
                                    mLastTouchDownTime = ev.getDownTime();  
                                    mLastTouchDownIndex = childIndex;  
                                    mLastTouchDownX = ev.getX();  
                                    mLastTouchDownY = ev.getY();  
                                    //关键点：addTouchTarget(child, idBitsToAssign)，对mFirstTouchTarget赋值
                                    newTouchTarget = addTouchTarget(child, idBitsToAssign);  
                                    alreadyDispatchedToNewTouchTarget = true;  
                                    break;
                                }
                            }
                        }
                        ****
                    }
                }
                ****
            }
            ****
            return handled;
        }
        //addTouchTarget中完成了对mFirstTouchTarget的赋值
        private TouchTarget addTouchTarget(View child,int pointerIdBits){
            TouchTarget target = TouchTarget.obtain(child,pointerIdBits);
            target.next = mFirstTouchTarget;
            mFirstTouchTarget = target;
            return target;
        }
        //View可见或正在执行动画,则返回true
        private static boolean canViewReceivePointerEvents(@NonNull View child) {
            return (child.mViewFlags & VISIBILITY_MASK) == VISIBLE || child.getAnimation() != null;
        }
        //点击事件的坐标点是否在View的范围内
        protected boolean isTransformedTouchPointInView(float x, float y, View child,PointF outLocalPoint) {
            final float[] point = getTempPoint();
            point[0] = x;
            point[1] = y;
            transformPointToViewLocal(point, child);
            //调用View的pointInView方法进行判断坐标点是否在View内
            final boolean isInView = child.pointInView(point[0], point[1]);
            if (isInView && outLocalPoint != null) {
                outLocalPoint.set(point[0], point[1]);
            }
            return isInView;
        }
        ```
    - mFirstTouchTarget赋值流程总结
        1. 事件不是取消事件，还没有被拦截就继续判断
        2. 遍历ViewGroup的所有子元素，获取能接收点击事件的子元素.子元素是能接收到事件需要满足2点：
            1. 子元素可见,或子元素正在执行动画
            2. 点击事件的坐标点位于子元素范围内
        3. 能接收事件的子元素获取到，则通过dispatchTransformedTouchEvent调用子元素的dispatchTouchEvent.
            - dispatchTouchEvent返回true,说明MotionEvent已经被子元素消耗掉，会调用addTouchTarget对mFirstTouchTarget赋值
    
## 5.View的滑动冲突
### 1.滑动冲突分类
- 外部滑动方向和内部滑动方向不一致
- 外部滑动方向和内部滑动方向一致
- 多层控件，上面两种情况的嵌套
### 2.滑动冲突的解决方式
1. 外部拦截法
    > 外部拦截法是指点击事件都先经过父容器的onInterceptTouchEvent,如果父容器需要此事件就返回true,不需要就返回false.实现方式比较符合点击事件的分发机制.需要重写父容器的onInterceptTouchEvent
    - 外部拦截法伪代码
        ```java
        public boolean onInterceptTouchEvent(MotionEvent event){
            boolean intercepted = false;
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    intercepted = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(当前ViewGroup需要拦截){
                        intercepted = true;
                    }else{
                        intercepted = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    intercepted = false;
                    break;
                default:
                    break;
            }
            return intercepted;
        }
        ```
    - ACTION_DOWN必须返回false,否则后续的ACTION_MOVE,ACTION_UP就无法传递给子元素;
    - ACTION_UP必须返回false,否则子元素的点击事件就无法触发
    - ACTION_MOVE可以根据情况决定是否传递给子元素.
2. 内部拦截法
    > 内部拦截法是指父容器不拦截任何事件,所有事件都传递给子元素.如果子元素需要此事件则消耗掉,否则交给父容器进行处理.这种方式实现较复杂,需要重写子元素的dispatchTouchEvent,并需要配合requestDisallowInterceptTouchEvent方法才能正常工作. 一般都默认采用外部拦截法处理.