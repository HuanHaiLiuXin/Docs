## 1.View����֪ʶ
### 1.ʲô��View
View��Android�����пؼ��Ļ���.
### 2.View��λ�ò���
- Viewԭʼ�����ϽǺ����½ǵ�x,y����ֵ�ֱ���mLeft,mTop,mRight,mBottom;��Щֵ���������View�ĸ�������˵��,����һ���������.
- mLeft = v.getLeft()
- mTop = v.getTop()
- mRight = v.getRight()
- mBottom = v.getBottom()
- translationX��translationY��View�����Ͻ�����ڸ�������ƫ����.x,y��View���Ͻǵ�ǰʵ������ֵ
- x = mLeft + translationX
- y = mTop + translationY
### 3.MotionEvent��TouchSlop
#### 1.MotionEvent
- event.getX,event.getY:�¼�����ڵ�ǰView���Ͻǵ�x,y����
- event.getRawX,event.getRawY:�¼��������Ļ���Ͻǵ�x,y����
#### 2.TouchSlop
- TouchSlop��ϵͳ����ʶ��ı���Ϊ�ǻ�������С����
- ��ȡTouchSlop:ViewConfiguration.get(getContext()).getScaledTouchSlop()
### 4.VelocityTracker,GestureDetector,Scroller
#### 1.VelocityTracker

> VelocityTracker����׷����ָ�ڻ��������е��ٶ�,����ˮƽ����ֱ������ٶ�;

VelocityTrackerʹ������
```java
//��ȡVelocityʵ��
VelocityTracker vt = VelocityTracker.obtain();
//���Ҫ׷���ٶȵ�MotionEventsʵ��
vt.addMovement(Movement ev);
//�����ٶȼ����ʱ���Ⱥ���ֵ(������units����ֵ�ڵ��ƶ�����,��pxΪ��λ)
vt.computeCurrentVelocity(int units)
//������units����ֵ��,x,y�����ƶ��ľ���,��pxΪ��λ
float xVelocity = vt.getXVelocity();
float yVelocity = vt.getYVelocity();
//VelocityTrackerʹ�����,ʹ��clear��ֵ,��recycle�����ڴ�
vt.clear();
vt.recycle();
```
#### 2.GestureDetector
*���Ƽ��,һ�������,��Ҫ����˫����Ϊ������GestureDetector,ʵ����OnDoubleTapListener*
#### 3.Scroller
*����ʵ��View�ĵ��Ի���,View�ĵ��Ի�������ʵ�ֺ��������*
## 2.View�Ļ���
### ʵ��View�Ļ��������ַ���:
- ͨ��View�����scrollTo,scrollBy
- ͨ��������Veiwʩ��ƽ��Ч��
- ͨ���ı�View��LayoutParamsʹ��View���²��ִӶ�ʵ�ֻ���
### 1.ʹ��scrollTo,scrollByʵ��View�Ļ���
> ʹ��scrollTo,scrollByֻ�ܸı�View�����ݵ�λ��,���ܶ�View��������ƶ�.
- scrollTo,scrollBy�ı����View�ڲ�����������mScrollX��mScrollY,����ͨ��getScrollX,getScrollY���;
- mScrollX����View�����Ե��View�е��������Ե��ˮƽ����ľ���;mScrollY����View���ϱ�Ե��View�е������ϱ�Ե�ڴ�ֱ����ľ���;
- mScrollX��mScrollY�ĵ�λ������;
- ��View���ϱ�Ե��View�������ϱ�Ե���±�ʱ,mScrollY>0,��֮<0
- ��Veiw�����Ե��View���������Ե���ұ�ʱ,mScrollX>0,��֮<0
### 2.ʹ�ö���ʵ��View�Ļ���
- View�����Ƕ�View��Ӱ��������,�޷�������View��λ�ò���,��ߵ�
- ���Զ����Ƕ�View�е�����,�Զ������Խ��б��,���������ı�View��λ��
### 3.�ı�һ��View��LayoutParams��ʵ��View�Ļ���.
```java
MarginLayoutParams prams = (MarginLayoutParams)bt.getLayoutParams();
//����View�Ŀ��100px
params.width += 100;
//����View�����marginֵ100px
params.leftMargin += 100;
bt.setLayoutParams(params);
```
### 4.����3�з�ʽ�ĳ���
- scrollTo,scrollBy:�����ڶ�View�����ݵĻ���
- ����:��������Ҫ�󶯻�Ч�������û�������View(TextView)
- �ı䲼�ֲ���:�������н�����View(Button)
## 3.���Ի���
- ʹ��Scrollerʵ�ֵ��Ի���,Scroller��������View����������,View����λ�ò���
    - ԭ��:
        1. ��smoothScroll�е���invalidate,����View��draw����
        2. draw���������computeScroll
        3. ��computeScroll��,������Scrollerʵ������ĵ�ǰ��mScrollX,mScrollY,����scrollTo�����������л���;Ȼ�����postInvalidate�ػ�
        4. postInvalidate���������draw.Ȼ��2,3,4�᲻���ظ�,ֱ���������
    - ʾ��:
        ```java
        public class ArtView extends View{
            //�Զ���View�г���Scrollerʵ��
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
                //��ʼ��Scrollerʵ��
                scroller = new Scroller(context);
            }
    
            /**
             * ʵ����duration������,����View����������,ʵ��mScrollX=targetX,mScrollY=targetY
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
- ʹ�ö���ʵ�ֵ��Ի���
    - ����ʹ��ObjectAnimator����ValueAnimatorʵ��
    - ObjectAnimator��Ҫָ������,��ValueAnimator����ͨ��AnimatorUpdateListener��onAnimationUpdate�������������Խ�������
    - ObjectAnimatorʵ��:
        ```java
        Animator a1 = ObjectAnimator.ofInt(v,"scrollY",0,400).setDuration(400);
        Animator a2 = ObjectAnimator.ofInt(v,"scrollX",0,400).setDuration(400);
        a1.start();
        a2.start();
        ```
    - ValueAnimatorʵ��:
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

## 4.View���¼��ַ�����
> View���¼��ַ�����,���Ƕ�MotionEvent�ķַ��߼�.
### 1.����¼��Ĵ��ݹ���
- ViewGroup����¼����ݹ���α����
    ```java
    public boolean dispatchTouchEvent(MotionEvent ev){
        boolean consume = false;
        if(onInterceptTouchEvent(ev)){
            //�¼�������
            if(onTouchListener != null && onTouchListener.onTouch(this,ev)){
                //������ù�OnTouchListener,��ִ��OnTouchListener.onTouch,
                //onTouchΪtrue,ֱ�ӷ���
                return true;
            }else{
                //δ����OnTouchListener,����OnTouchListener.onTouch����false,
                //�����onTouchEvent����
                return onTouchEvent(ev);
            }
        }else{
            //�¼�δ������,���¼���������Ӧ����View
            consume = child.dispatchTouchEvent(ev);
        }
        return consume;
    }
    onTouchEvent�Ǽ̳е�View��,��View����.
    ```
- View����¼����ݹ���α����
    ```java
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(onTouchListener != null && onTouchListener.onTouch(this,ev)){
            //������ù�OnTouchListener,��ִ��OnTouchListener.onTouch,
            //onTouchΪtrue,ֱ�ӷ���
            return true;
        }else{
            //δ����OnTouchListener,����OnTouchListener.onTouch����false,
            //�����onTouchEvent����
            return onTouchEvent(ev);
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        //View��onTouchEvent,Ĭ�Ϸ���true,���ǲ��ɵ��
        boolean clickable = CLICKABLE || LONG_CLICKABLE;
        if(OnClickListener != null && clickable && �յ���ACTION_DOWN��ACTION_UP){
            //�ɵ��,�ҽ��չ�DOWN��UP,���ù���OnClickListener�Ż�ִ��
            OnClickListener.onClick(this);
        }
        return clickable;
    }
    ```
- ����¼����ݹ������
    1. һ���¼�������:�������¼���View
        - ���õ�OnTouchListener.onTouch����true
        - ��View��onTouchEvent����true
    2. ͬһ�¼�����:����ָ�Ӵ���Ļ��ʼ,���뿪��ĻΪֹ.down,move....up
    3. ���������,һ���¼�����ֻ�ܱ�1��View����������.
    4. ���View������ACTION_DOWN�¼�,��û������(onTouchEvent������false),��ô�¼������к����¼��������½������ĸ�Ԫ�ش���,�����ø�Ԫ�ص�onTouchEvent.
    5. ���View������ACTION_DOWN�¼�,�������¼���������,��ô�¼�����ʧ,��������ø�Ԫ�ص�onTouchEvent,��ǰView������ܵ������¼�,������Щ�¼�����Activity����.
    6. ViewGroupĬ�ϲ������¼�,onInterceptTouchEventĬ�Ϸ���false
    7. Viewû��onInterceptTouchEvent,dispatchTouchEvent�л�ȡ���¼�:
        - ��OnTouchListener��ִ��OnTouchListener.onTouch,onTouchΪtrue��ֱ�ӷ���true;
        - û��OnTouchListener,����OnTouchListener.onTouchΪfalse,��ִ��onTouchEvent
    8. onTouchEventĬ�Ϸ���true,���ǲ��ɵ��
        - CLICKABLE��LONG_CLICKABLE����false,�ű�ʾ���ɵ��,����false;
        - �ɵ��,��View���չ�DOWN��UP,��ִ��OnClickListener��onClick;
    9. setOnClickListener���Զ���View��CLICKABLE��Ϊtrue;setOnLongClickListener����Զ���View��LONG_CLICKABLE��Ϊtrue.
    10. ��View����ͨ��requestDisallowInterceptTouchEvent������Ԥ��Ԫ�ص��¼��ַ�,����ACTION_DOWN����(��������,�ȼ�ס����)

### 2.����¼���Դ�����
- requestDisallowInterceptTouchEvent�����Ч���漰����2��������dispatchTouchEvent,resetTouchState�����η���
    - requestDisallowInterceptTouchEvent
        ```java
        ViewGroup.java
        
        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (disallowIntercept == ((mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0)) {
                //1.1:��mGroupFlags�Ѿ���ӹ�FLAG_DISALLOW_INTERCEPT����mGroupFlags & FLAG_DISALLOW_INTERCEPT��=0
                //1.2:�Ѿ���ӹ�FLAG_DISALLOW_INTERCEPT���ٴ����(disallowInterceptΪtrue),ֱ��return
                return;
            }
            if (disallowIntercept) {
                //2.1.disallowInterceptΪtrue,ΪmGroupFlags���FLAG_DISALLOW_INTERCEPT
                mGroupFlags |= FLAG_DISALLOW_INTERCEPT;
            } else {
                //2.2:disallowInterceptΪfalse,ΪmGroupFlags�Ƴ�FLAG_DISALLOW_INTERCEPT
                mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
            }
            if (mParent != null) {
                //3:�ݹ� ��ӻ��Ƴ� ��ǰViewGroup�丸View��FLAG_DISALLOW_INTERCEPT��־λ
                mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
        ```
    - resetTouchState
        ```java
        private void resetTouchState() {
            clearTouchTargets();
            resetCancelNextUpFlag(this);
            //�ؼ�������,��resetTouchState��,��ΪmGroupFlags�Ƴ�FLAG_DISALLOW_INTERCEPT
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
                    //1:ACTION_DOWNʱ��ص���resetTouchState:resetTouchState���Ƴ�mGroupFlags�е�FLAG_DISALLOW_INTERCEPT
                    resetTouchState();
                }
                final boolean intercepted;
                if (actionMasked == MotionEvent.ACTION_DOWN|| mFirstTouchTarget != null) {
                    //ACTION_DOWM �� ���ڴ���ǰ�¼�����Ԫ��
                    
                    //2.1:ACTION_DOWM�����,mGroupFlags�е�FLAG_DISALLOW_INTERCEPT��resetTouchState()���Ƴ���
                    //(mGroupFlags & FLAG_DISALLOW_INTERCEPT) = 0,����ACTION_DOWM�����disallowInterceptһ��Ϊfalse;
                    //2.2:����ACTION_DOWM����£�
                        //2.2.1:������ù�requestDisallowInterceptTouchEvent(true),�� mGroupFlags & FLAG_DISALLOW_INTERCEPT!=0,disallowInterceptΪtrue;
                        //2.2.2:��δ���ù�requestDisallowInterceptTouchEvent��requestDisallowInterceptTouchEvent(false),�� mGroupFlags & FLAG_DISALLOW_INTERCEPT=0��disallowInterceptΪfalse
                        
                    final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
                    if (!disallowIntercept) {
                        //ACTION_DOWM�����,disallowInterceptΪfalse,һ����ִ��onInterceptTouchEvent�ж��Ƿ������¼�
                        //��ACTION_DOWN�����,��δ���û�requestDisallowInterceptTouchEvent(false),disallowInterceptΪfalse,Ҳ��ִ��onInterceptTouchEvent
                        intercepted = onInterceptTouchEvent(ev);
                        ev.setAction(action); // restore action in case it was changed
                    } else {
                        //��ACTION_DOWN �� ���ڴ���ǰ�¼�����Ԫ�� �� ִ�й�requestDisallowInterceptTouchEvent(true),��������
                        intercepted = false;
                    }
                } else {
                    //��ACTION_DOWN �� �����ڴ���ǰ�¼�����Ԫ�أ�һ������
                    intercepted = true;
                }
            }
            ****
        }
        ```
    - �ܽ�����Դ���֪��
        - ��ACTION_DOWN����£������Ƿ���ù�requestDisallowInterceptTouchEvent(true)��һ����ִ��onInterceptTouchEvent�ж��Ƿ������¼�
        - ��ACTION_DOWN����£�
            - �����ڴ���ǰ�¼�����Ԫ�أ�һ������ 
            - ���ڴ���ǰ�¼�����Ԫ�أ���ִ�й�requestDisallowInterceptTouchEvent(true)����ViewGroup�������¼���������Ԫ�ش���
            - ���ڴ���ǰ�¼�����Ԫ�أ���δ���û�requestDisallowInterceptTouchEvent(false),��ִ��onInterceptTouchEvent�ж��Ƿ������¼�
- mFirstTouchTarget
    - ��diapatchTouchEvent���õ���mFirstTouchTarget(����ǰ�¼�����Ԫ��),�丳ֵҲ����dispatchTouchEvent��,��dispatchTouchEvent�е���addTouchTargetΪmFirstTouchTarget��ֵ
    - [dispatchTouchEvent���](https://www.jianshu.com/p/238d1b753e64)
    - Դ��������н�����dispatchTouchEvent
        ```java
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            ****
            boolean handled = false;
            if (onFilterTouchEventForSecurity(ev)) {
                //�н�����dispatchTouchEvent����
                ****
                final boolean intercepted;
                if (actionMasked == MotionEvent.ACTION_DOWN|| mFirstTouchTarget != null) {
                    ****
                } else {
                    //��ACTION_DOWN �� �����ڴ���ǰ�¼�����Ԫ�أ�һ������
                    intercepted = true;
                }
                
                //��������mFirstTouchTarget��ֵ���̣�
                final boolean canceled = resetCancelNextUpFlag(this) || actionMasked == MotionEvent.ACTION_CANCEL;
                if (!canceled && !intercepted) {
                    //�¼�����ȡ���¼���Ҳû��������ô��Ҫ�ж�
                    if (actionMasked == MotionEvent.ACTION_DOWN  
                        || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)  
                        || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                        ****
                        final int childrenCount = mChildrenCount;
                        if (newTouchTarget == null && childrenCount != 0) {
                            //����µĴ�������Ϊnull����������������𣩲��ҵ�ǰViewGroup����Ԫ��
                            final float x = ev.getX(actionIndex);  
                            final float y = ev.getY(actionIndex);  
                            //���������Ĺ����������ҵ����Խ�������¼�����Ԫ��  
                            final View[] children = mChildren;
                            //�Ƿ�ʹ���Զ����˳������ӿؼ�  
                            final boolean customOrder = isChildrenDrawingOrderEnabled();
                            for (int i = childrenCount - 1; i >= 0; i--) {
                                //����������Զ����˳������ӿؼ�����ô���Ƶ�View��˳���mChildren��˳���ǲ�һ����  
                                //����Ҫ����getChildDrawingOrderȡ�������Ļ��Ƶ�View  
                                //�Զ���Ļ��ƣ����ܵ�һ���ử�����������͵��ĸ����ڶ���������һ����������������ݺ�Children�ǲ�һ����
                                final int childIndex = customOrder ?  getChildDrawingOrder(childrenCount, i) : i;  
                                final View child = children[childIndex];  
                                //���child�����Խ�������������¼���continue
                                //canViewReceivePointerEvents:View�Ƿ�ɼ�������ִ�ж���
                                //isTransformedTouchPointInView:����¼������Ƿ���View�ķ�Χ��
                                if (!canViewReceivePointerEvents(child) || !isTransformedTouchPointInView(x, y, child, null)) {  
                                    continue;  
                                }
                                //��ȡ�µĴ������������ǰ����View��֮ǰ�Ĵ���Ŀ����б��оͷ���touchTarget  
                                //��View����֮ǰ�Ĵ���Ŀ���б���ô�ͷ���null
                                newTouchTarget = getTouchTarget(child);
                                ****
                                //dispatchTransformedTouchEventʵ���Ͼ��ǵ�����Ԫ�ص�dispathTouchEvent
                                if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                    //dispatchTransformedTouchEvent����true,˵���¼��Ѿ�����Ԫ�����ĵ���
                                    //���Ǿ�Ӧ�ö�mFirstTouchTarget��ֵ
                                    
                                    // Child wants to receive touch within its bounds.  
                                    mLastTouchDownTime = ev.getDownTime();  
                                    mLastTouchDownIndex = childIndex;  
                                    mLastTouchDownX = ev.getX();  
                                    mLastTouchDownY = ev.getY();  
                                    //�ؼ��㣺addTouchTarget(child, idBitsToAssign)����mFirstTouchTarget��ֵ
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
        //addTouchTarget������˶�mFirstTouchTarget�ĸ�ֵ
        private TouchTarget addTouchTarget(View child,int pointerIdBits){
            TouchTarget target = TouchTarget.obtain(child,pointerIdBits);
            target.next = mFirstTouchTarget;
            mFirstTouchTarget = target;
            return target;
        }
        //View�ɼ�������ִ�ж���,�򷵻�true
        private static boolean canViewReceivePointerEvents(@NonNull View child) {
            return (child.mViewFlags & VISIBILITY_MASK) == VISIBLE || child.getAnimation() != null;
        }
        //����¼���������Ƿ���View�ķ�Χ��
        protected boolean isTransformedTouchPointInView(float x, float y, View child,PointF outLocalPoint) {
            final float[] point = getTempPoint();
            point[0] = x;
            point[1] = y;
            transformPointToViewLocal(point, child);
            //����View��pointInView���������ж�������Ƿ���View��
            final boolean isInView = child.pointInView(point[0], point[1]);
            if (isInView && outLocalPoint != null) {
                outLocalPoint.set(point[0], point[1]);
            }
            return isInView;
        }
        ```
    - mFirstTouchTarget��ֵ�����ܽ�
        1. �¼�����ȡ���¼�����û�б����ؾͼ����ж�
        2. ����ViewGroup��������Ԫ�أ���ȡ�ܽ��յ���¼�����Ԫ��.��Ԫ�����ܽ��յ��¼���Ҫ����2�㣺
            1. ��Ԫ�ؿɼ�,����Ԫ������ִ�ж���
            2. ����¼��������λ����Ԫ�ط�Χ��
        3. �ܽ����¼�����Ԫ�ػ�ȡ������ͨ��dispatchTransformedTouchEvent������Ԫ�ص�dispatchTouchEvent.
            - dispatchTouchEvent����true,˵��MotionEvent�Ѿ�����Ԫ�����ĵ��������addTouchTarget��mFirstTouchTarget��ֵ
    
## 5.View�Ļ�����ͻ
### 1.������ͻ����
- �ⲿ����������ڲ���������һ��
- �ⲿ����������ڲ���������һ��
- ���ؼ����������������Ƕ��
### 2.������ͻ�Ľ����ʽ
1. �ⲿ���ط�
    > �ⲿ���ط���ָ����¼����Ⱦ�����������onInterceptTouchEvent,�����������Ҫ���¼��ͷ���true,����Ҫ�ͷ���false.ʵ�ַ�ʽ�ȽϷ��ϵ���¼��ķַ�����.��Ҫ��д��������onInterceptTouchEvent
    - �ⲿ���ط�α����
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
                    if(��ǰViewGroup��Ҫ����){
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
    - ACTION_DOWN���뷵��false,���������ACTION_MOVE,ACTION_UP���޷����ݸ���Ԫ��;
    - ACTION_UP���뷵��false,������Ԫ�صĵ���¼����޷�����
    - ACTION_MOVE���Ը�����������Ƿ񴫵ݸ���Ԫ��.
2. �ڲ����ط�
    > �ڲ����ط���ָ�������������κ��¼�,�����¼������ݸ���Ԫ��.�����Ԫ����Ҫ���¼������ĵ�,���򽻸����������д���.���ַ�ʽʵ�ֽϸ���,��Ҫ��д��Ԫ�ص�dispatchTouchEvent,����Ҫ���requestDisallowInterceptTouchEvent����������������. һ�㶼Ĭ�ϲ����ⲿ���ط�����.