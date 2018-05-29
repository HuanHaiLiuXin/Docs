# android网络框架源码解析及对比

## android常用网络框架对比
### Volley:
**特点**
- 基于HttpUrlConnection
- 封装了UIL图片加载框架,支持图片加载
- 缓存
- Activity和生命周期的联动,Activity结束时取消在此Activity中调用了所有网络请求

**应用场景**
- 适合传输数据量小,网络请求频繁的场景
- 不能进行大数据量的网络操作,比如下载及上传文件,原因如下:
    - Volley的Request和Response都是把数据放到byte[]中,如果设计文件的上传及下载,byte[]就会变得很大,严重的消耗内存.比如下载一个大文件,不可能把整个文件一次性全部放到byte[]中再写到本地文件.
    - 源码为证:
        ```
        Request:
        com.android.volley.Request
            //Reqest中的数据,最后是被转换为byte[]数组
            //Returns the raw POST or PUT body to be sent.
            public byte[] getBody() throws AuthFailureError {
                Map<String, String> params = getParams();
                if (params != null && params.size() > 0) {
                    return encodeParameters(params, getParamsEncoding());
                }
                return null;
            }
            //Request实例解析当前请求得到的网络相应数据
            protected abstract Response<T> parseNetworkResponse(NetworkResponse response);
        
        Response:
        com.android.volley.NetworkResponse
            //网络响应中的原始数据,以byte[]形式存在
            //Raw data from this response.
            public final byte[] data;
        
        Request的具体继承类,都是在parseNetworkResponse方法中,对NetworkResponse的data(byte[])进行解析:
        StringRequest.parseNetworkResponse:
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        ImageRequest.parseNetworkResponse:
            byte[] data = response.data;
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        ```
### OkHttp:
**特点**
- 基于NIO和Okio,请求处理速度更快.
    ``IO:阻塞式;NIO:非阻塞式;Okio:Square基于IO和NIO做的更高效处理数据流的库``
    ```
    IO和NIO的区别:
    1.IO是面向流(Stream)的,而NIO是面向缓冲区(Buffer)的.
        1.1:面向流意味着IO读取流中1个或多个字节,他们没有被缓存在任何地方,此外它不能前后移动流中的数据;
        1.2:面向缓冲区意味着先将数据读取到一个稍后处理的缓冲区,需要读取时可以在缓冲区中前后移动;
    2:IO是阻塞的,NIO是非阻塞的
        2.1:IO的各种流是阻塞的,意味着一个线程执行read()或write()时,该线程被阻塞,直到数据被读取或完全写入,期间不能做任何别的事情;
        2.2:NIO,一个线程从1个通道读取数据,或者向1个通道写入数据,如果通道中暂时没有数据可以读取,或者写入数据没有完成,线程不会阻塞,可以去做别的事情.直到通道中出现了可以读取的数据或者可以继续写入数据,再继续之前的工作.NIO情况下,一个线程可以处理多个通道的读取和写入,更充分的利用线程资源;
    3:IO和NIO的适用场景
        3.1:IO适合于链接数量不大,但是每个链接需要发送/接收的数据量很大,需要长时间连续处理;
        3.2:NIO更适合于同时存在海量链接,但是每个链接单次发送/接收的数据量较小的情形.比如聊天服务器.海量链接但是单个链接单次数据较小
    ```
- 无缝支持GZIP来减少数据流量
    - GZIP是网站压缩加速的一种技术,开启后可以加快客户端的打开速度.原理是响应数据先经过服务器压缩,客户端快速解压呈现内容,减少客户端接收的数据量
    - android客户端在Request头加入"Accept-Encoding","gzip",告知服务器客户端接受gzip的数据;服务器支持的情况下，返回gzip后的response body，同时加入以下header:
        ```
        Content-Encoding: gzip：表明body是gzip过的数据
        Content-Length:117：表示body gzip压缩后的数据大小，便于客户端使用。
        或
        Transfer-Encoding: chunked：分块传输编码
        ```
    - OkHttp3是支持Gzip解压缩的:它支持我们在发起请求的时候自动加入header,Accept-Encoding:gzip,而我们的服务器返回的时候也需要header中有Content-Encoding:gzip
        ```
        开发者没有在Header中添加Accept-Encoding时,自动添加Accept-Encoding: gzip
        自动添加的request，response支持自动解压
        手动添加不负责解压缩
        自动解压时移除Content-Length，所以上层Java代码想要contentLength时为-1
        自动解压时移除 Content-Encoding
        自动解压时的分块编码传输不受影响
        
        okhttp3.internal.http.BridgeInterceptor
        public final class BridgeInterceptor implements Interceptor {
            @Override 
            public Response intercept(Chain chain) throws IOException {
                ****
                //如果header中没有Accept-Encoding,默认自动添加,且标记变量transparentGzip为true
                boolean transparentGzip = false;
                if (userRequest.header("Accept-Encoding") == null) {
                  transparentGzip = true;
                  requestBuilder.header("Accept-Encoding", "gzip");
                }
                ****
                Response.Builder responseBuilder = networkResponse.newBuilder().request(userRequest);
                //符合条件时执行gzip自动解压:
                    //header中手动添加ccept-Encoding不负责gzip解压
                    //自动添加ccept-Encoding才负责gzip解压
                if (transparentGzip&& "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))&&HttpHeaders.hasBody(networkResponse)) {
                    //gzip自动解压的前提条件
                    //1:transparentGzip = true,即用户没有主动在request头中加入"Accept-Encoding", "gzip"
                    //2:header中标明了Content-Encoding为gzip
                    //3:networkResponse中有body
                    GzipSource responseBody = new GzipSource(networkResponse.body().source());
                    Headers strippedHeaders = networkResponse.headers().newBuilder()
                        //自动解压时移除Content-Encoding
                        .removeAll("Content-Encoding")
                        //自动解压时移除Content-Length，所以上层Java代码想要contentLength时为-1
                        .removeAll("Content-Length")
                        .build();
                    responseBuilder.headers(strippedHeaders);
                    responseBuilder.body(new RealResponseBody(strippedHeaders,Okio.buffer(responseBody)));
                }
                return responseBuilder.build();
            }
        }
        ```
    - 使用OkHttp3,我们在向服务器提交大量数据,希望对post的数据进行gzip压缩的实现方法:首先实现自定义拦截器,然后在构建OkhttpClient的时候，添加拦截器
        ```
        实现自定义拦截器(官方实现):
        static class GzipRequestInterceptor implements Interceptor {
            @Override 
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                if (originalRequest.body() == null ||             originalRequest.header("Content-Encoding") != null) {
                    return chain.proceed(originalRequest);
                }
                Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
                return chain.proceed(compressedRequest);
            }
            private RequestBody gzip(final RequestBody body) {
                return new RequestBody() {
                    @Override public MediaType contentType() {
                        return body.contentType();
                    }
                    @Override public long contentLength() {
                        //因为无法预知在经过gzip压缩后的长度,设置为-1
                        return -1;
                    }
                    @Override 
                    public void writeTo(BufferedSink sink) throws IOException {
                        //通过GzipSink,将原始的BufferedSink进行gzip压缩
                        BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                        //将经过gzip压缩的内容写入RequestBody
                        body.writeTo(gzipSink);
                        gzipSink.close();
                    }
                };
            }
        }
        
        构建OkhttpClient的时候,添加拦截器:
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new GzipRequestInterceptor())//开启Gzip压缩
            ...
            .build();
        ```
**应用场景**
- 重量级网络交互场景:网络请求频繁,传输数据量大
### Retrofit:
**特点**
- 基于OkHttp
- 通过注解配置请求
- 性能最好,处理最快
- 解析数据需要使用统一的Converter
- 易与其他框架RxJava联合使用

**应用场景**
- 任何场景下都优先使用,特别是项目中有使用RxJava或者后台API遵循Restful风格

## Retrofit的使用
### Retrofit涉及到的注解
1. 网络请求方法注解:
    ```
    @GET,@POST,@PUT,@OPTIONS,@PATCH,@DELETE,@HEAD,@HTTP
    @HTTP用于替换其余方法注解,通过method,path,hasBody进行设置:
    public @interface HTTP {
        //网络请求的方法（区分大小写）
        String method();
        //网络请求地址路径
        String path() default "";
        //是否有请求体
        boolean hasBody() default false;
    }
    实例:
    @HTTP(method="GET",path="blog/{id}",hasBody=false)
    Call<ResponseBody> getCall(@Path("id") int id);
    
    网络请求的完整Url=创建Retrofit实例时通过.baseUrl()+方法注解(path),
    通常使用:baseUrl目录形式+path相对路径 的方法组成完整Url:
        Url = "http:host:port/a/b/appath"
        baseUrl = "http:host:port/a/b/"
        path = appath
    ```
2. 标记注解
    ```
    @FormUrlEncoded,@Multipart,@Streaming
    
    @FormUrlEncoded:表示发送form-encoded的数据
        每个键值对需要用@Filed来注解键名,随后的对象提供值
    @Multipart:表示发送form-encoded的数据(适用于有文件上传的场景)
        1:每个键值对需要用@Part来注解键名,随后的对象提供值.
        2:@Part后面支持3种数据类型:RequestBody,okhttp3.MultipartBody.Part,任意类型
    @Streaming:表示返回数据以流的形式返回,适用于返回数据较大的场景.如果没有使用Streaming,默认把数据全部载入内存,之后读取数据也是从内存中获取.
    
    实例:
    public interface GetRequest_Interface {
        /**
         *表明是一个表单格式的请求（Content-Type:application/x-www-form-urlencoded）
         * Field("username")表示将后面的String name中name的取值作为username 的值
         */
        @POST("/form")
        @FormUrlEncoded
        Call<ResponseBody> testFormUrlEncoded1(@Field("username") String name, @Field("age") int age);
         
        @POST("/form")
        @Multipart
        Call<ResponseBody> testFileUpload1(@Part("name") RequestBody name, @Part("age") RequestBody age, @Part MultipartBody.Part file);
    }
    具体使用:
    GetRequest_Interface service = retrofit.create(GetRequest_Interface.class);
    // @FormUrlEncoded 
    Call<ResponseBody> call1 = service.testFormUrlEncoded1("Carson", 24);
    
    // @Multipart
    // 1:Part修饰的参数类型:RequestBody
    MediaType textType = MediaType.parse("text/plain");
    RequestBody name = RequestBody.create(textType, "Carson");
    RequestBody age = RequestBody.create(textType, "24");
    // 2:Part修饰的参数类型:MultipartBody.Part
        //2.1:文件路径
    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ File.separator + "icon.jpg";
        //2.2:文件
    File file = new File(path);
        //2.3:文件所关联的MediaType
    MediaType type = MediaType.parse("image/*");
        //2.4:通过MediaType和File创建RequestBody
    RequestBody body = RequestBody.create(type,file);
        //2.5:通过MultipartBody.Part.createFormData(String name, @Nullable String filename, RequestBody body)
        // 创建指定文件关联的MultipartBody.Part实例
    MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", "icon.jpg", body);
    Call<ResponseBody> call3 = service.testFileUpload1(name, age, filePart);
    ```
    - MultipartBody.Part.createFormData(String name, @Nullable String filename, RequestBody body):
        - name是网络请求中的名称
        - fileName是文件名称,用于服务端解析
        - body就是文件关联的RequestBody实例
    - 每个RequestBody都要指定MediaType,常见的MediaType.parse(X)中X:
        ```
        text/plain（纯文本）
        application/x-www-form-urlencoded（使用HTTP的POST方法提交的表单）
        multipart/form-data（同上，但主要用于表单提交时伴随文件上传的场合
        image/gif（GIF图像）
        image/jpeg（JPEG图像）【PHP中为：image/pjpeg】
        image/png（PNG图像）【PHP中为：image/x-png】
        video/mpeg（MPEG动画）
        application/octet-stream（任意的二进制数据）
        application/pdf（PDF文档）
        application/msword（Microsoft Word文件）
        ```
    - 详情的文件扩展名和X之间的对应关系:[MIME 参考手册](http://www.w3school.com.cn/media/media_mimeref.asp)
    - 如果任意一个文件/File,不知道其对应的MediaType type=MediaType.parse(X)中X填什么,通过以下代码可以获取X值:
        ```
        通过文件完整路径,来获取其对应的X
        
        import java.net.FileNameMap;    
        import java.net.URLConnection;    
        public class FileUtils {    
          public static String getMimeType(String fileUrl) throws java.io.IOException    
            {    
                FileNameMap fileNameMap = URLConnection.getFileNameMap();    
                String type = fileNameMap.getContentTypeFor(fileUrl);
                if (contentType == null) {
                    //* exe,所有的可执行程序
                    contentType = "application/octet-stream"; 
                }
                return type;    
            }
        }
        1:文件的完整路径
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyDirectoty" + File.separator + "test.png";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyDirectoty" + File.separator + "2.doc";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyDirectoty" + File.separator + "2.csv";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyDirectoty" + File.separator + "LiveUpdate.exe";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyDirectoty" + File.separator + "1.txt";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyDirectoty" + File.separator + "demo.jpg";
        String mimeType = FileUtils.getMimeType(filePath);
        //image/png  
        //application/msword  
        //application/vnd.ms-excel  
        //application/x-msdownload  
        //text/plain  
        //image/jpeg 
        ```
3. 网络请求参数注解
    - @Headers:用于添加固定的请求头,注解在方法上
    ```
    实例:
    @Headers({
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    })
    @GET("users/{username}")
    Call<User> getUser(@Path("username") String username);
    @Headers("Cache-Control: max-age=640000")
    @GET("widget/list")
    Call<List<Widget>> widgetList();
    ```
    - @Header:用于添加不固定的请求头,注解在方法参数上
    ```
    实例:
    @GET("user")
    Call<User> getUser(@Header("Authorization") String authorization)
    ```
    - @HeaderMap:用于添加请求头集合,注解在方法参数上
    ```
    实例:
    Map<string,string> headers = new HashMap()<>;
    headers.put("Accept","text/plain");
    headers.put("Accept-Charset", "utf-8");
    
    @GET("/search")
    void list(@HeaderMap Map<string, string=""> headers);
    ```
    - @Body:以 Post方式 传递 自定义数据类型 给服务器
        - @Body注解参数,则不能同时使用@FormUrlEncoded、@Multipart,否则会报错:
            ```
            @Body parameters cannot be used with form or multi-part encoding
            ```
        - @Body是以什么形式上传的参数:是上传的@Body参数实体的Json字符串,所以内部需要一个GsonCoverter来将实体转换成json字符串,需要Retrofit里配置addConverterFactory(GsonConverterFactory.create()).否则会报错:
            ```
            Unable to create @Body converter for ***
            ```
        - @Body使用正确姿势
            ```
            //1:配置你的Gson
            Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();
            //2:Retrofit实例设置addConverterFactory(GsonConverterFactory.create())
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:4567/")
                //可以接收自定义的Gson，当然也可以不传
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
            //3:@Body注解的参数,所在方法去掉@FormUrlEncoded、@Multipart
            public interface BlogService {
                @POST("blog")
                Call<Result<Blog>> createBlog(@Body Blog blog);
            }
            //4:调用
            BlogService service = retrofit.create(BlogService.class);
            Blog blog = new Blog();
            blog.content = "新建的Blog";
            blog.title = "测试";
            blog.author = "怪盗kidou";
            Call<Result<Blog>> call = service.createBlog(blog);
            ```
    - @Field,@FieldMap:发送 Post请求时提交请求的表单字段,需要和@FormUrlEncoded配合使用
    ```
    实例:
    public interface GetRequest_Interface {
        @POST("/form")
        @FormUrlEncoded
        Call<ResponseBody> testFormUrlEncoded1(@Field("username") String name, @Field("age") int age);
        
        @POST("/form")
        @FormUrlEncoded
        Call<ResponseBody> testFormUrlEncoded2(@FieldMap Map<String, Object> map);
    }
    // @Field
    Call<ResponseBody> call1 = service.testFormUrlEncoded1("Carson", 24);
    // @FieldMap
    Map<String, Object> map = new HashMap<>();
    map.put("username", "Carson");
    map.put("age", 24);
    Call<ResponseBody> call2 = service.testFormUrlEncoded2(map);。
    ```
    - @Part,@PartMap:发送 Post请求 时提交请求的表单字段,需要和@Multipart配合使用.适用于文件上传场景.
        - @Part注解的参数类型:RequestBody,okhttp3.MultipartBody.Part,任意类型
        - @PartMap注解一个Map<String,RequestBody>
    ```
    实例:
    public interface GetRequest_Interface {
        @POST("/form")
        @Multipart
        Call<ResponseBody> testFileUpload1(@Part("name") RequestBody name, @Part("age") RequestBody age, @Part MultipartBody.Part file);
        @POST("/form")
        @Multipart
        Call<ResponseBody> testFileUpload2(@PartMap Map<String, RequestBody> args, @Part MultipartBody.Part file);
        @POST("/form")
        @Multipart
        Call<ResponseBody> testFileUpload3(@PartMap Map<String, RequestBody> args);
    }
    
    // 具体使用
    MediaType textType = MediaType.parse("text/plain");
    RequestBody name = RequestBody.create(textType, "Carson");
    RequestBody age = RequestBody.create(textType, "24");
    RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data"), File一个文件);
    // @Part
    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "test.txt", file);
    Call<ResponseBody> call3 = service.testFileUpload1(name, age, filePart);
    // @PartMap
    // 实现和上面同样的效果
    Map<String, RequestBody> fileUpload2Args = new HashMap<>();
    fileUpload2Args.put("name", name);
    fileUpload2Args.put("age", age);
    //这里并不会被当成文件，因为没有文件名(包含在Content-Disposition请求头中)，但上面的 filePart 有
    //fileUpload2Args.put("file", file);
    Call<ResponseBody> call4 = service.testFileUpload2(fileUpload2Args, filePart); 
    ```
    - @Query,@QueryMap:用于 @GET 方法的查询参数(Query = Url 中 ‘?’ 后面的 key-value)
        - @Query：URL问号后面的参数； 
        - @QueryMap：相当于多个@Query
        - @Query和@QueryMap注解的查询参数的key和value默认都会开启URL编码,使用如下encoded=true来关闭URL编码.
            - @Query(value="group",encoded=true)
            - @QueryMap(encoded=true)
        - @Query注解的参数,参数值可以为空,为空该参数会被忽略
        - @QueryMap注解的Map,其键和值都不能为空,否则抛出IllegalArgumentException异常
    ```
    实例:
    @Query:
    @GET("/list")
    Call<responsebody> list(@Query("category") String category);
    //传入一个数组
    @GET("/list")
    Call<responsebody> list(@Query("category") String... categories);
    //不进行URL编码
    @GET("/search")
    Call<responsebody> llist(@Query(value="foo", encoded=true) String foo);
    @Query调用:
    X.list("1")     URL:/list?category=1
    X.list(null)    URL:/list
    X.list("a","b") URL:/list?category=a&category=b
    @Query(value="foo", encoded=true)下,生成的URL和不设置encoded=true情况无差别,只是关闭了key和value的URL编码
    
    @QueryMap:
    @GET("/search")
    Call<responsebody> list(@QueryMap Map<string, string> filters);
    @GET("/search")
    Call<responsebody> list(@QueryMap(encoded=true) Map<string,string> filters);
    @QueryMap调用:
    X.list(ImmutableMap.of("group", "coworker", "age", "42"))
        URL:/search?roup=coworker&age=42
    X.list(ImmutableMap.of("group", "coworker"))
        URL:/search?roup=coworker
    ```
    - @Path:URL中"?"前面部分,Path注解用于替换url路径中的参数
    ```
    实例:
    @GET("users/{user}/repos")
    Call<ResponseBody>  getBlog（@Path("user") String user ）;
    X.getBlog("bb")     URL:users/bb/repos
    ```
    - @Url:作用于方法参数,直接设置请求的接口地址
        - 当@GET,@POST等注解里面没有url地址时,必须在方法中使用@Url，将地址以第1个参数的形式传入
        - @Url注解的地址,不要以/开头
        - @Url支持的类型有 okhttp3.HttpUrl, String, java.net.URI, android.net.Uri
        - @Path注解与@Url注解不能同时使用,否则会抛异常
            ```
            Path注解用于替换url路径中的参数,这就要求在使用path注解时,
            必须已经存在请求路径,不然没法替换路径中指定的参数啊,
            而Url注解是在参数中指定的请求路径的,这个时候指定请求路径已经晚了,
            path注解找不到请求路径,更别提更换请求路径中的参数了
            ```
    ```
    public interface BlogService {
        /**
         * 当GET、POST...HTTP等方法中没有设置Url时，则必须使用 {@link Url}提供
         * 对于Query和QueryMap，如果不是String（或Map的第二个泛型参数不是String）时
         * 会被默认会调用toString转换成String类型
         * Url支持的类型有 okhttp3.HttpUrl, String, java.net.URI, android.net.Uri
         * {@link retrofit2.http.QueryMap} 用法和{@link retrofit2.http.FieldMap} 用法一样，不再说明
         */
        @GET //当有URL注解时，这里的URL就省略了
        Call<ResponseBody> testUrlAndQuery(@Url String url,@Query("showAll") boolean showAll);
    }
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://localhost:4567/")
        .build();
    BlogService service = retrofit.create(BlogService.class);
    Call<ResponseBody> call1 = service.testUrlAndQuery("headers",false);
    //http://localhost:4567/headers?showAll=false
    ```
![](https://user-gold-cdn.xitu.io/2018/5/27/163a1f24584c5f47?w=1158&h=667&f=png&s=36735)
### Retrofit使用流程
- 步骤1：添加Retrofit库的依赖
    - Retrofit支持多种数据解析方式,使用时需要在build.gradle添加依赖
        ```
        build.gradle添加依赖:
        compile 'com.squareup.retrofit2:retrofit:2.0.2'
        
        Gson	    com.squareup.retrofit2:converter-gson:2.0.2
        Jackson	    com.squareup.retrofit2:converter-jackson:2.0.2
        Simple XML  com.squareup.retrofit2:converter-simplexml:2.0.2
        Protobuf    com.squareup.retrofit2:converter-protobuf:2.0.2
        Moshi	    com.squareup.retrofit2:converter-moshi:2.0.2
        Wire	    com.squareup.retrofit2:converter-wire:2.0.2
        Scalars	    com.squareup.retrofit2:converter-scalars:2.0.2
        ```
    - Retrofit支持多种网络请求适配器方式：guava、Java8和rxjava
        ```
        build.gradle添加依赖:
        guava	    com.squareup.retrofit2:adapter-guava:2.0.2
        Java8	    com.squareup.retrofit2:adapter-java8:2.0.2
        rxjava	    com.squareup.retrofit2:adapter-rxjava:2.0.2
        ```
- 步骤2：创建 接收服务器返回数据 的类
- 步骤3：创建 用于描述网络请求 的接口
- 步骤4：创建 Retrofit 实例
    ```
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://fanyi.youdao.com/") // 设置网络请求的Url地址
        .addConverterFactory(GsonConverterFactory.create())//设置数据解析器
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//支持RxJava平台
        .build();
    ```
- 步骤5：创建 网络请求接口实例 并 配置网络请求参数
    ```
    // 创建 网络请求接口 的实例
    GetRequest_Interface request = retrofit.create(GetRequest_Interface.class);
    //对 发送请求 进行封装
    Call<Reception> call = request.getCall();
    ```
- 步骤6：发送网络请求(异步 / 同步)
    ```
    //发送网络请求(异步)
    call.enqueue(new Callback<Translation>() {
        //请求成功时回调
        @Override
        public void onResponse(Call<Translation> call,Response<Translation> response) {
            // 对返回数据进行处理
            response.body().show();
        }
        //请求失败时候的回调
        @Override
        public void onFailure(Call<Translation> call, Throwable throwable) {
            System.out.println("连接失败");
        }
    });
    // 发送网络请求（同步）
    Response<Reception> response = call.execute();
    // 对返回数据进行处理
    response.body().show();
    ```
## Retrofit源码分析
### Retrofit涉及到的设计模式
1. 模板模式
    - 定义:定义一个操作的算法框架,将一些步骤延迟到子类中,使子类不改变算法的结构即可重新定义该算法的某些特定步骤
    - 使用场景:多个子类有公有方法，且子类公有方法的调度逻辑基本相同
    - 模板模式包含2个角色:
      - 父类:抽象类 public abstract  class AbsParent
        - 父类中包含:基本方法 + 模板方法 + 钩子方法
        - 基本方法:父类提取公共代码:protected abstract 方法,由子类具体实现
        - 模板方法:可以有1个或几个,完成对基本方法的调度,实现具体逻辑:public final方法,防止子类复写
        - 钩子方法:protected方法,注意不是抽象方法.在父类的模板方法中调用,对模板方法的执行进行约束
      - 子类:父类的实现类 public class Child1 extends AbsParent
        - 子类中包含:基本方法的具体实现 + 钩子方法的重写
    - 代码实例
      ```
       //父类
		public abstract class AbsParent{
		  //钩子方法
		  protected boolean executeStep1(){
		    return false;
		  }
		  //基本方法
		  protected abstract void step1();
		  protected abstract void step2();
		  protected abstract void step3();
		  //模板方法
		  public final void execute(){
		    if(this.executeStep1()){
		      this.step1();
		    }
		    this.step2();
		    this.step3();
		  }
		}
        //子类
        public class Child1 extends AbsParent{
          //子类钩子方法返回值
          private boolean executeFlag = true;
          //子类中可以对钩子方法返回值进行设置,从而对父类的模板方法进行约束
          public void setExecuteFlag(boolean flag){
            this.executeFlag = flag;
          }
          @Override
          protected boolean executeStep1(){
            return this.executeFlag;
          }
          @Override
          protected void step1(){
            System.out.println("Child1:step1")
          }
          @Override
          protected void step2(){
            System.out.println("Child1:step2")
          }
          @Override
          protected void step3(){
            System.out.println("Child1:step3")
          }
        }
      ```
2. Builder模式/建造者模式
    - 定义:将一个复杂对象的构建和它的表示分离,使得同样的构建过程可以创建不同的表示
    - 作用:用户不需要知道复杂对象的建造过程细节,只需要指定对象的具体类型,即可创建复杂对象;具体建造者根据用户指定的对象具体类型A,按照指定顺序创建A的实例;
    - 建造者模式包含4个角色:
      - 产品类
        - 产品类实现了模板模式.抽象产品父类包含基本方法和模板方法,具体产品子类实现了基本方法.
      - 抽象建造者
        - public abstract class:规范了产品的组建,全是抽象方法,是具体建造者的父类
      - 具体建造者
        - 实现抽象建造者所有方法,并返回一个建造好的具体产品子类实例
      - 导演类
        - 持有多个具体建造者,包含多个方法,用来生产多个具体产品类实例;
    - 代码实例
		```
		抽象产品父类:包含基本方法和模板方法
		public abstract class AbsProduct{
		  //这个参数定义了各基本方法的执行顺序
		  private ArrayList<String> sequence = new ArrayList<String>();
		  //基本方法
		  protected abstract void step1();
		  protected abstract void step2();
		  //设置参数
		  public final void setSequence(ArrayList<String> sequence){
		    this.sequence = sequence;
		  }
		  //模板方法
		  public final void do(){
		    for(String item:sequence){
		      if(item.equalsIgnoreCase("step1")){
		        this.step1();
		      }else if(item.equalsIgnoreCase("step2")){
		        this.step2();
		      }
		    }
		  }
		}
		具体产品子类:实现了基本方法
		public class Product1 extends AbsProduct{
		  @Override
		  protected void step1(){
		    System.out.println("Product1:step1");
		  }
		  @Override
		  protected void step2(){
		    System.out.println("Product1:step2");
		  }
		}
		public class Product2 extends AbsProduct{
		  @Override
		  protected void step1(){
		    System.out.println("Product2:step1");
		  }
		  @Override
		  protected void step2(){
		    System.out.println("Product2:step2");
		  }
		}
		抽象建造者:规范产品的组建
		public abstract class ProductBuilder{
		  //设置产品参数
		  public abstract void setSequence(ArrayList<String> sequence);
		  //获取产品实例
		  public abstract AbsProduct getProduct();
		}
		具体建造者:实现抽象建造者所有方法,并返回一个建造好的具体产品子类实例
		public class Product1Builder extends ProductBuilder{
		  //私有变量就是将要产生的具体产品类实例
		  private Product1 p = new Product1();
		  @Override
		  public void setSequence(ArrayList<String> sequence){
		    this.p.setSequence(sequence);
		  }
		  @Override
		  public AbsProduct getProduct(){
		    return this.p;
		  }
		}
		public class Product2Builder extends ProductBuilder{
		  //私有变量就是将要产生的具体产品类实例
		  private Product2 p = new Product2();
		  @Override
		  public void setSequence(ArrayList<String> sequence){
		    this.p.setSequence(sequence);
		  }
		  @Override
		  public AbsProduct getProduct(){
		    return this.p;
		  }
		}
		导演类:持有多个具体建造者,包含多个方法,用来生产多个具体产品类实例
		public class Director{
		  //影响产品流程顺序的参数
		  private ArrayList<String> sequence = new ArrayList<String>();
		  //具体建造者
		  private Product1Builder builder1 = new Product1Builder();
		  private Product2Builder builder2 = new Product2Builder();
		  //根据需求可自行扩展
		  //1:生产不同类型的具体产品
		    //(gainProduct1A,gainProduct1B) 和 gainProduct2 就是生产不同类型的具体产品
		  //2:相同类型的产品,其产品流程的数量及顺序也可以任意变化:
		    //gainProduct1A和gainProduct1B 就是同类产品的流程数量及顺序变化
		  public Product1 gainProduct1A(){
		    this.sequence.clear();
		    this.sequence.add("step1");
		    this.sequence.add("step2");
		    this.builder1.setSequence(this.sequence);
		    return (Product1)this.builder1.getProduct();
		  }
		  public Product1 gainProduct1B(){
		    this.sequence.clear();
		    this.sequence.add("step2");
		    this.builder1.setSequence(this.sequence);
		    return (Product1)this.builder1.getProduct();
		  }
		  public Product2 gainProduct2(){
		    this.sequence.clear();
		    this.sequence.add("step1");
		    this.sequence.add("step2");
		    this.builder2.setSequence(this.sequence);
		    return (Product2)this.builder2.getProduct();
		  }
		}
		```
3. 外观模式/门面模式
    - 定义:在复杂系统S和客户端C之间再加一层"接待员"R,在R中实现对S复杂功能的访问封装;C直接和R交互即可.
    - 作用:隐藏了S的复杂性;R对S中复杂功能进行了封装,C调用R封装过的方法,可避免低水平错误
    - 场景:去医院看病，要 挂号,问诊,缴费,取药,让患者或患者家属觉得很复杂，如果有提供接待人员，只让接待人员来处理，就很方便
    - 外观模式包含:接口+实现类+外观类(R)
    - 代码实例
		```
		//创建1个接口,代表医院每个流程
		public interface Step{
		  void execute();
		}
		//创建实现类,代表不同类型的具体流程
		public class GuaHao implements Step{
		  @Override
		  public void execute(){
		    System.out.println("老子正在挂号");
		  }
		}
		public class WenZhen implements Step{
		  @Override
		  public void execute(){
		    System.out.println("老子正在问诊");
		  }
		}
		public class JiaoFei implements Step{
		  @Override
		  public void execute(){
		    System.out.println("老子正在缴费");
		  }
		}
		public class QuYao implements Step{
		  @Override
		  public void execute(){
		    System.out.println("老子正在取药");
		  }
		}
		//创建外观类"接待员R"
		public class Reception{
		  //"接待员"持有S中复杂功能的引用
		  private Step guahao;
		  private Step wenzhen;
		  private Step jiaofei;
		  private Step quyao;
		  public Reception(){
		    this.guahao = new GuaHao();
		    this.wenzhen = new WenZhen();
		    this.jiaofei = new JiaoFei();
		    this.quyao = new QuYao();
		  }
		  //定义一个供客户端调用的方法,完整实现一串流程
		  public void executeAll(){
		    this.guahao.execute();
		    this.wenzhen.execute();
		    this.jiaofei.execute();
		    this.quyao.execute();
		  }
		}
		//客户端直接调用Reception
		Reception r = new Reception();
		r.executeAll();
		```
3. 代理模式
    - 定义:通过访问代理类的方式来间接访问目标类
    - 优点:隐藏目标类实现细节;不改变目标类情况下,对指定操作前后执行扩展,比如进行校验和其他操作
    - 分类:
      - 静态代理:代理类在程序运行前已经存在
      - 动态代理:代理类在程序运行前不存在、运行时由程序动态生成的代理方式称为动态代理
    - 静态代理包含:
      - 目标类和代理类共同实现的接口
      - 目标类,代理类(代理类中持有目标类实例)
    - 静态代理实例:
		```
		//创建一个接口
		public interface MyOpt{
		  void opt();
		}
		//创建目标类
		public class TargetOpt implements MyOpt{
		  @Override
		  public void opt(){
		    System.out.println("TargetOpt:opt");
		  }
		}
		//创建代理类
		public class ProxyOpt implements MyOpt{
		  //代理类中持有 目标类实例
		  private TargetOpt target;
		  public ProxyOpt(){
		    this.target = new TargetOpt();
		  }
		  @Override
		  public void opt(){
		    this.target.opt();
		  }
		}
		//客户端和代理类直接进行交互:
		ProxyOpt proxy= new ProxyOpt();
		proxy.opt();
		```
    - 动态代理
    	- 动态代理对象P执行方法调用顺序:
	      - P.func==>InvocationHandler.invoke==>目标类实例.func
	    - 动态代理实现需要3步:
	      - 1 创建目标类接口 及 目标类
	      - 2 实现InvocationHandler接口
	        - 调用代理对象的每个函数实际最终都是调用了InvocationHandler的invoke函数
	      - 3 通过Proxy类新建代理类对象:Proxy.newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
	    - 动态代理实例:
			```
			//创建接口
			public interface Step{
			  void execute();
			}
			//创建目标类
			public class MyStep implements Step{
			  @Override
			  public void execute(){
			    System.out.println("MyStep:execute");
			  }
			}
			//实现InvocationHandler接口
			public StepHandler implements InvocationHandler{
			  //target:目标类实例
			  private Object target;
			  public StepHandler(){}
			  public StepHandler(Object obj){
			    this.target = obj;
			  }
			  //proxy:通过 Proxy.newProxyInstance() 生成的代理对象
			  //method:表示proxy被调用的方法
			  //args:表示proxy被调用的方法的参数
			  @Override
			  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			    Object obj = method.invoke(this.target, args);
			    return obj;
			  }
			}

			//通过Proxy类新建代理对象,直接调用代理对象的方法
			//1:创建InvocationHandler的实现类实例,将目标类实例作为构造参数传入
			StepHandler h = new StepHandler(new MyStep());
			//2:创建代理对象
			Proxy: Object newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
			    loader:目标类继承的接口所属的类加载器
			    interfaces:目标类继承的接口的Class
			    h:InvocationHandler的实现类实例
			Step step = (Step)(Proxy.newProxyInstance(Step.class.getClassLoader(),new Class[]{Step.class},h));
			//3:直接调用代理对象的方法
			step.execute();    ==> "MyStep:execute"

	      step.execute()实质是调用了生成的代理对象P中的execute方法
	      ==>
	      而P中的execute方法,是调用了刚刚创建的h.invoke方法
	      ==>
	      h.invoke,则调用了目标类MyStep实例中的execute方法
			```
	    - **Proxy.newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)**源码分析
```
仅贴出关键代码

Proxy:
package java.lang.reflect
public class Proxy implements java.io.Serializable {
  private static final WeakCache<ClassLoader, Class<?>[], Class<?>>
        proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
  private static final Class<?>[] constructorParams = { InvocationHandler.class };
  protected InvocationHandler h;
  protected Proxy(InvocationHandler h) {
    Objects.requireNonNull(h);
    this.h = h;
  }
  @CallerSensitive
  public static Object newProxyInstance(
    ClassLoader loader,
    Class<?>[] interfaces,
    InvocationHandler h                             )
        throws IllegalArgumentException
  {
    //获取目标类继承的接口的Class副本
    final Class<?>[] intfs = interfaces.clone();
    //1:通过接口的Class副本,获取代理类的Class
    Class<?> cl = getProxyClass0(loader, intfs);
    ****
  }
  //1:如果代理类已经存在则返回副本;不存在则通过ProxyClassFactory创建并返回:见1.1
  private static Class<?> getProxyClass0(ClassLoader loader,
                                           Class<?>... interfaces) {
        // If the proxy class defined by the given loader implementing
        // the given interfaces exists, this will simply return the cached copy;
        // otherwise, it will create the proxy class via the ProxyClassFactory
    return proxyClassCache.get(loader, interfaces);
  }
  //1.1:此处直接看ProxyClassFactory的apply方法即可
  proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
  private static final class ProxyClassFactory implements BiFunction<ClassLoader, Class<?>[], Class<?>>{
    //所有要生成的代理类名称前缀
    // prefix for all proxy class names
    private static final String proxyClassNamePrefix = "$Proxy";
    //为了生成的代理类不重名采取的名称后缀:后面代码会看到
    // next number to use for generation of unique proxy class names
    private static final AtomicLong nextUniqueNumber = new AtomicLong();
    //生成代理类的Class
    @Override
    public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {
      //接口数组生成Map
      Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
      //遍历代理类要实现的每个接口,注意必须是Interface,否则会抛异常
      for (Class<?> intf : interfaces) {
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                //如果newProxyInstance中传入的ClassLoader并不是接口所属的ClassLoader,会抛异常
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(intf + " is not visible from class loader");
                }
               //如果newProxyInstance中传入的Class数组,数组项不属于Interface,抛异常
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(
                        interfaceClass.getName() + " is not an interface");
                }
                //通过Set,防止同一个接口重复处理
                if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException(
                        "repeated interface: " + interfaceClass.getName());
                }
      }
      String proxyPkg = null;     //要生成的代理类所在package
      int accessFlags = Modifier.PUBLIC | Modifier.FINAL;
      //遍历每个接口,获取代理类所在package
      for (Class<?> intf : interfaces) {
                int flags = intf.getModifiers();
                if (!Modifier.isPublic(flags)) {
                    accessFlags = Modifier.FINAL;
                    //获取接口名称
                    //如:接口IStep,接口所在package为a.b.c,则(new IStep()).getClass().getName为:  "a.b.c.IStep"
                    String name = intf.getName();
                    int n = name.lastIndexOf('.');
                    //获取接口所在package:对应IStep则为"a.b.c."
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        //proxyPkg不存在则赋值
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        //注意:如果proxyPkg已经存在,说明传入的接口Class数组不止一个Clas项
                        //为了如果数组中的接口包名不同,就会抛异常
                        //所以  
                          //1:保证所有Class关联的Interface包名相同
                          //2:只传1个Interface的Class不就行了,有需要传多个的场景吗?
                        throw new IllegalArgumentException(
                            "non-public interfaces from different packages");
                    }
                }
      }
      ****
      //这里用到了AtomicLong,用来拼接代理类的名称
      long num = nextUniqueNumber.getAndIncrement();
      //以接口"a.b.c.IStep"为例,其代理类名称为:"a.b.c.$Proxy0"
      String proxyName = proxyPkg + proxyClassNamePrefix + num;
      //1.2:生成代理类class,以byte[]形式返回
      byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);
      try {
        //1.3:native方法生成代理类的Class,并返回
        return defineClass0(loader, proxyName,proxyClassFile, 0, proxyClassFile.length);
      } catch (ClassFormatError e) {
        ****
      }
    }
  }
  //1.3:defineClass0是一个native方法
  private static native Class<?> defineClass0(ClassLoader loader,String name,byte[] b, int off, int len);
}

//1.2:ProxyGenerator.generateProxyClass
ProxyGenerator

package sun.misc
public class ProxyGenerator {
  private String className;
  private Class<?>[] interfaces;
  private int accessFlags;

  private ProxyGenerator(String var1, Class<?>[] var2, int var3) {
        this.className = var1;
        this.interfaces = var2;
        this.accessFlags = var3;
  }
  public static byte[] generateProxyClass(final String var0, Class<?>[] var1, int var2) {
    ProxyGenerator var3 = new ProxyGenerator(var0, var1, var2);
    final byte[] var4 = var3.generateClassFile();
    ***
    return var4;
  }
}
```