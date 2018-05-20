# 1.Android��������
## 1.1 ��������µ���������
onCreate
onStart
onResume
onPause
onStop
onDestroy
1. onStart��onResumeʱActivity���ǿɼ���,��onStartʱActivity���ں�̨,��δ������ǰ̨,��onResumeʱActivity����ʾ��ǰ̨;
2. onPauseʱActivity��Ȼ�ɼ�,���Ѿ���������ǰ̨;onStopʱActivity�Ѳ��ɼ�;�����´򿪵�Activityʱ͸������,���Activity��ִ��onStop,��Ϊ��Ȼ�ǿɼ�״̬;
3. A(Activity).startActivity(B(Activity)),����A��onPauseִ����Ϻ�,��ִ��B��onCreate,����˳����:
```
A:onPause
B:onCreate
B:onStart
B:onResume
A:onStop
```
4. ���ϵ�Activity��onPauseִ����Ϻ�,��ִ����Activity��onCreate,onStart,onResume.������onPause�в�����ִ�к�ʱ����,�����Ӱ���µ�Activity��չʾ.
## 1.2 �쳣����µ���������
1. ϵͳ���øı䵼��Activity��ɱ�����ؽ�,����Ļ����ı�,Activity�������ڷ�������˳��:
```
��ɱ��:
onSaveInstanceState(������onPause֮ǰ,Ҳ������onPause֮��)
onStop
onDestroy
���ؽ�:
onCreate
onStart
ononRestoreInstanceState
```
- ��Activityһ��,ÿ��ViewҲ��onSavveInstanceState��onRestoreInstanceState����������;
- ϵͳֻ����Activity���쳣��ֹʱ�ŵ���onSaveInstanceState��onRestoreInstanceState
2. ϵͳ��Դ����,���µ����ȼ���Activity���ȱ�ɱ��.Activity�����ȼ��ɵ͵���:
- ǰ̨Activity,���ں��û�������Activity,���ȼ����;
- �ɼ�,����ǰ̨��Activity,����һ��Activity�е�����һ��Dialog,Activity�ɼ���λ�ں�̨,�޷����û�ֱ�ӽ���,���ȼ����;
- ��̨Activity,�����Ѿ�ִ����onStop,���ɼ�,���ȼ����;
- ��ɱ����ActivityҲ������ִ��:onSaveInstanceState,onRestoreInstanceState
3. ϵͳ���÷����ı䵼�µ�Activity��ɱ�����ؽ�,����ͨ������android:configChanges������.����configChanges�ĺ���:
- label:�豸�ı������÷����˸ı�,һ����ָ�л���ϵͳ����
- keyboardHidden:���̵Ŀɷ����Է����˸ı�,�����û������˼���
- orientation:��ת���ֻ���Ļ
- screenSize:��minSdkVersion��targetSdkVersion������1��>=13,screenSize��Ҫ��orientationһ������,���ܱ�֤��ת��Ļ����Activity���ؽ�
- ��������android:configChanges,Activity�������onSaveInstanceState��onRestoreInstanceState,�����onConfigurationChanged����.

# 2.Activity������ģʽ
## 2.1 Activity��LaunchMode
1. standard:Ĭ��ģʽ.ÿ�ζ������µ�Activityʵ��,��������Activity��������������Ǹ�Activity���ڵ�����ջ��.
> ������ʹ��ApplicationContext����һ��standardģʽ��Activity�ᱨ��,��Ϊ��Activity���͵�Context��û����ν������ջ,����취��Ϊ��������Activityָ��FLAG_ACTIVITY_NEW_TASK���,����������ʱ���ΪActivity����һ���µ�����ջ,ʵ����"singleTask"ģʽ����;
2. singleTop:ջ������ģʽ.����������Activity��singleTop,��λ����������Activity��������ջ��ջ��,���½�,����onNewIntent,�����½�Activityʵ��.
- ABCD-->D-->ABCD
- ABDC-->D-->ABDCD
3. singleTask:ջ�ڸ���ģʽ.**ֻҪActivity������1������ջ�д���,���������Activity���������´���ʵ��**,�������onNewIntent.��singleTaskĬ����clearTop��Ч��:ADBC-->D(D��singleTask)-->AD.������������:
- ���A�������������ջS��������,����ȴ���S,�ٴ���A,��Aѹ��S��;
- ���S����,Aδ����,�򴴽�A,��Aѹ��S��;
- ���S����,��A�Ѿ���S��,����S���� BCADE,��ὫA�л���ջ��,������onNewIntent,����A�����DE���,���BCA
4. singleInstance:��ʵ��ģʽ.��һ��singleTask�ļ�ǿ��.singleInstanceģʽ��Activityֻ�ܵ�������һ������ջ��.�������ε��ö���ִ����onNewIntent.
## 2.2 Activity��TaskAffinity
1. 1��Activity���������ջ������,�ɸ�Activity��TaskAffinity����.Ĭ�������,����Activity������ջ��������Ӧ�õİ���.
2. ���ǿ���Ϊ1��Activityָ��������TaskAffinity����,���벻�ܺͰ�����ͬ.
3. TaskAffinity��singleTask���ʹ�ò�������,���������Ч.��ʱ���ù���Activity��������TaskAffinityָ��������ջ��.
```
AΪstandard,B,CΪsingleTask��������android:taskAffinity="a.b.c.name",
����A,A����B,B����C,C����A,A����B.
��ʱ������BACK,����ǻص�����.

����:
1: A�����ڰ���ָ��������ջAppName��;
2: A����B,��ΪB,C��ָ����taskAffinity����singleTask,���Ի��ȴ�������ջS(a.b.c.name),�ٴ���B,��Bѹ��;
3: B����C,C����������ջS�Ѿ�����,����C,ѹ��S����
4: C����A,A��standard,�ٴ���1��A,ѹ��C���ڵ�����ջS����,��ʱS����:BCA
5: A����B,B��singleTask,B��S��,����B�л���ջ��,ִ����onNewIntent.singleTask����clearTopЧ��,B�����CA���Ƴ�,S�б��B

��ʱ������BACK

��һ��BACK,S�е�B��ջ,S�Ѳ�����,�ص���̨����ջ,��AppName��ʾ����.AppName�н���A;
�ڶ���BACK,AppName�е�AҲ��ջ,AppNameҲ��������,�ص�������.
```