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
- x = left + translationX
- y = top + translationY
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
```
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
```
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
