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
4. 

