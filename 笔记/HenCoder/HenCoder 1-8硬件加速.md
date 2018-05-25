## 硬件加速

 - 硬件加速定义
 
```对于Android而言,硬件加速是指在View绘制过程中,将Canvas.drawXX的工作交给GPU而不是CPU进行.```

 - 开启硬件加速,为什么可以加速
    - 本来CPU的工作,分摊一部分给GPU,自然可以提升效率;
    - 相对于CPU,GPU的设计特性,决定其执行Canvas.drawXX的效率高于CPU(记住就好);
    - CPU和GPU绘制机制的不同,导致View内容改变时GPU的效率极大高于CPU;
        - 未开启硬件加速,当界面中的某个View A由于内容发生改变而调用invalidate()方法时,A的父View直至顶层View,以及所有和A相交的兄弟View,都会调用invalidate()来重绘.这个工作量非常大;
        - 开启硬件情况下,只有发生改变的A会调用invalidate(),A的父View及兄弟View无重复动作,工作量很小;
 - 硬件加速的限制:Canvas的有些方法在硬件加速开启后会失效或无法正常工作,具体的 API 限制和 API 版本的关系如下图：
![](https://user-gold-cdn.xitu.io/2018/5/9/16345132ebca8b17?w=783&h=936&f=jpeg&s=187608)
 - 所有的原生控件,都没有用到API版本不兼容的绘制操作,我们自定义View时候需要对照这张图,防止出现不兼容;
 - 开启硬件加速
 ```view.setLayerType(LAYER_TYPE_HARDWARE, null);```
 - 关闭硬件加速:view.setLayerType(LAYER_TYPE_SOFTWARE, null);
 ```view.setLayerType(LAYER_TYPE_SOFTWARE, null);```
 - View.setLayerType
    - Specifies the type of layer backing this view,设置view的Layer的类型;
    - 可选类型有 LAYER_TYPE_NONE,LAYER_TYPE_SOFTWARE,LAYER_TYPE_HARDWARE;
    - Layer又称离屏缓冲,Layer的作用是单独启用一块地方来绘制View,无论是否开启硬件加速,当调用了setLayerType,View的最终的绘制结果会被缓存下来.
    - **在绘制内容没有发生变化的情况下(没有主动或间接调用当前View实例的invalidate())**,setLayerType可以大大提高当前View的重绘效率,直接使用Layer缓存过的绘制结果即可;
    - setLayerType只有在对translationX,translationY,rotation,alpha等无需调用invalidate()的属性做属性动画的时候,才能提高重绘效率;
1:不适用于基于自定义属性,在onDraw里绘制的动画;
2:也不适用于会触发当前View实例invalidate()方法的属性,例如setScrollX,**setScrollX最终还是会调用invalidated**
```
/**
 * Set the horizontal scrolled position of your view. This will cause a call to
 * {@link #onScrollChanged(int, int, int, int)} and the view will be
 * invalidated.//看到了吧,setScrollX最终还是会调用invalidated
 * @param value the x position to scroll to
 */
public void setScrollX(int value) {
    scrollTo(value, mScrollY);
}
```
**正确使用setLayerType提升属性动画性能的两种方式**
```
1:
view.setLayerType(LAYER_TYPE_HARDWARE, null);  
ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 180);

animator.addListener(new AnimatorListenerAdapter() {  
    @Override
    public void onAnimationEnd(Animator animation) {
        view.setLayerType(LAYER_TYPE_NONE, null);
    }
});
animator.start(); 

2:
view.animate().rotationY(90).withLayer(); 
// withLayer() 可以自动完成上面这段代码的复杂操作
```