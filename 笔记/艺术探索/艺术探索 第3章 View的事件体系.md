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
- x = left + translationX
- y = top + translationY
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
```
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
```
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
