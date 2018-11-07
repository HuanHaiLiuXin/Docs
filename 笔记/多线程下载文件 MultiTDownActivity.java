package p2.com.p2;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MultiTDownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_tdown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }).start();
    }

    private String apkPath = "http://f3.market.xiaomi.com/download/AppStore/091b447c4153716a34d6207c4ea6dba35eb427a00/com.sdsmdg.harjot.MusicDNA.apk";
    private int netApkLength = 0;
    File d = null;
    File a = null;
    private RandomAccessFile apk = null;
    private int threadNum = 10;


    private void init(){
        try {
            //1:创建SD卡下的APK文件
            d = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "111Down");
            if(!d.exists()){
                d.mkdirs();
            }
            a = new File(d,"d.apk");
            if(!a.exists()){
                a.createNewFile();
            }
            apk = new RandomAccessFile(a,"rw");
            //2:获取网络apk的大小
            URL url = new URL(apkPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(
                    "Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                            + "application/x-shockwave-flash, application/xaml+xml, "
                            + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                            + "application/x-ms-application, application/vnd.ms-excel, "
                            + "application/vnd.ms-powerpoint, application/msword, */*");
//            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            netApkLength = conn.getContentLength();
            conn.disconnect();
            apk.setLength(netApkLength);
            apk.close();
        } catch (Exception e) {
            TextView tv1 = findViewById(R.id.sdkApkInfo);
            tv1.setText("异常:"+e.getLocalizedMessage());
        } finally {
        }
    }
    public void startDownOri(View view) {
//        http://f3.market.xiaomi.com/download/AppStore/091b447c4153716a34d6207c4ea6dba35eb427a00/com.sdsmdg.harjot.MusicDNA.apk
        //打印SD卡下apk是否已经创建,及apk的大小
        TextView tv1 = findViewById(R.id.sdkApkInfo);
//        if(d!=null && d.exists()){
//            tv1.setText("文件夹路径:"+d.getAbsolutePath());
//        }else{
//            tv1.setText("文件夹路径不存在");
//        }
//        if (a!=null){
//            tv1.setText("网上Apk大小:"+netApkLength+"\nSD卡中apk已存在;大小:"+a.getTotalSpace()+";路径:"+a.getAbsolutePath());
//        }
        final int perDownSize = netApkLength / threadNum + 1;
        for(int i =0; i<threadNum; i++){
            final int startPos = i * perDownSize;
            final int endPos = startPos + perDownSize > netApkLength ? netApkLength : startPos + perDownSize;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(apkPath);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(15 * 1000);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty(
                                "Accept",
                                "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                        conn.setRequestProperty("Accept-Language", "zh-CN");
                        conn.setRequestProperty("Referer", url.toString());
                        conn.setRequestProperty("Charset", "UTF-8");
                        conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// 设置获取实体数据的范围
//                        conn.setRequestProperty(
//                                "User-Agent",
//                                "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.connect();
                        if (conn.getResponseCode() == 206)
                        {
                            InputStream is = conn.getInputStream();
                            int len = 0;
                            byte[] buf = new byte[1024];
                            RandomAccessFile currentPart = new RandomAccessFile(a, "rw");
                            currentPart.seek(startPos);
                            while ((len = is.read(buf)) != -1)
                            {
                                currentPart.write(buf, 0, len);
                            }
                            currentPart.close();
                            is.close();
                            System.out.println(Thread.currentThread().getName()
                                    + "完成下载  ： " + startPos + " -- " + endPos);
                        }
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
