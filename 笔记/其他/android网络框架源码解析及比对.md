# android������Դ��������Ա�

## android���������ܶԱ�
### Volley:
**�ص�**
- ����HttpUrlConnection
- ��װ��UILͼƬ���ؿ��,֧��ͼƬ����
- ����
- Activity���������ڵ�����,Activity����ʱȡ���ڴ�Activity�е�����������������

**Ӧ�ó���**
- �ʺϴ���������С,��������Ƶ���ĳ���
- ���ܽ��д����������������,�������ؼ��ϴ��ļ�,ԭ������:
    - Volley��Request��Response���ǰ����ݷŵ�byte[]��,�������ļ����ϴ�������,byte[]�ͻ��úܴ�,���ص������ڴ�.��������һ�����ļ�,�����ܰ������ļ�һ����ȫ���ŵ�byte[]����д�������ļ�.
    - Դ��Ϊ֤:
        ```
        Request:
        com.android.volley.Request
            //Reqest�е�����,����Ǳ�ת��Ϊbyte[]����
            //Returns the raw POST or PUT body to be sent.
            public byte[] getBody() throws AuthFailureError {
                Map<String, String> params = getParams();
                if (params != null && params.size() > 0) {
                    return encodeParameters(params, getParamsEncoding());
                }
                return null;
            }
            //Requestʵ��������ǰ����õ���������Ӧ����
            protected abstract Response<T> parseNetworkResponse(NetworkResponse response);
        
        Response:
        com.android.volley.NetworkResponse
            //������Ӧ�е�ԭʼ����,��byte[]��ʽ����
            //Raw data from this response.
            public final byte[] data;
        
        Request�ľ���̳���,������parseNetworkResponse������,��NetworkResponse��data(byte[])���н���:
        StringRequest.parseNetworkResponse:
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        ImageRequest.parseNetworkResponse:
            byte[] data = response.data;
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        ```
### OkHttp:
**�ص�**
- ����NIO��Okio,�������ٶȸ���.
    ``IO:����ʽ;NIO:������ʽ;Okio:Square����IO��NIO���ĸ���Ч�����������Ŀ�``
    ```
    IO��NIO������:
    1.IO��������(Stream)��,��NIO�����򻺳���(Buffer)��.
        1.1:��������ζ��IO��ȡ����1�������ֽ�,����û�б��������κεط�,����������ǰ���ƶ����е�����;
        1.2:���򻺳�����ζ���Ƚ����ݶ�ȡ��һ���Ժ���Ļ�����,��Ҫ��ȡʱ�����ڻ�������ǰ���ƶ�;
    2:IO��������,NIO�Ƿ�������
        2.1:IO�ĸ�������������,��ζ��һ���߳�ִ��read()��write()ʱ,���̱߳�����,ֱ�����ݱ���ȡ����ȫд��,�ڼ䲻�����κα������;
        2.2:NIO,һ���̴߳�1��ͨ����ȡ����,������1��ͨ��д������,���ͨ������ʱû�����ݿ��Զ�ȡ,����д������û�����,�̲߳�������,����ȥ���������.ֱ��ͨ���г����˿��Զ�ȡ�����ݻ��߿��Լ���д������,�ټ���֮ǰ�Ĺ���.NIO�����,һ���߳̿��Դ�����ͨ���Ķ�ȡ��д��,����ֵ������߳���Դ;
    3:IO��NIO�����ó���
        3.1:IO�ʺ���������������,����ÿ��������Ҫ����/���յ��������ܴ�,��Ҫ��ʱ����������;
        3.2:NIO���ʺ���ͬʱ���ں�������,����ÿ�����ӵ��η���/���յ���������С������.�������������.�������ӵ��ǵ������ӵ������ݽ�С
    ```
- �޷�֧��GZIP��������������
    - GZIP����վѹ�����ٵ�һ�ּ���,��������Լӿ�ͻ��˵Ĵ��ٶ�.ԭ������Ӧ�����Ⱦ���������ѹ��,�ͻ��˿��ٽ�ѹ��������,���ٿͻ��˽��յ�������
    - android�ͻ�����Requestͷ����"Accept-Encoding","gzip",��֪�������ͻ��˽���gzip������;������֧�ֵ�����£�����gzip���response body��ͬʱ��������header:
        ```
        Content-Encoding: gzip������body��gzip��������
        Content-Length:117����ʾbody gzipѹ��������ݴ�С�����ڿͻ���ʹ�á�
        ��
        Transfer-Encoding: chunked���ֿ鴫�����
        ```
    - OkHttp3��֧��Gzip��ѹ����:��֧�������ڷ��������ʱ���Զ�����header,Accept-Encoding:gzip,�����ǵķ��������ص�ʱ��Ҳ��Ҫheader����Content-Encoding:gzip
        ```
        ������û����Header�����Accept-Encodingʱ,�Զ����Accept-Encoding: gzip
        �Զ���ӵ�request��response֧���Զ���ѹ
        �ֶ���Ӳ������ѹ��
        �Զ���ѹʱ�Ƴ�Content-Length�������ϲ�Java������ҪcontentLengthʱΪ-1
        �Զ���ѹʱ�Ƴ� Content-Encoding
        �Զ���ѹʱ�ķֿ���봫�䲻��Ӱ��
        
        okhttp3.internal.http.BridgeInterceptor
        public final class BridgeInterceptor implements Interceptor {
            @Override 
            public Response intercept(Chain chain) throws IOException {
                ****
                //���header��û��Accept-Encoding,Ĭ���Զ����,�ұ�Ǳ���transparentGzipΪtrue
                boolean transparentGzip = false;
                if (userRequest.header("Accept-Encoding") == null) {
                  transparentGzip = true;
                  requestBuilder.header("Accept-Encoding", "gzip");
                }
                ****
                Response.Builder responseBuilder = networkResponse.newBuilder().request(userRequest);
                //��������ʱִ��gzip�Զ���ѹ:
                    //header���ֶ����ccept-Encoding������gzip��ѹ
                    //�Զ����ccept-Encoding�Ÿ���gzip��ѹ
                if (transparentGzip&& "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))&&HttpHeaders.hasBody(networkResponse)) {
                    //gzip�Զ���ѹ��ǰ������
                    //1:transparentGzip = true,���û�û��������requestͷ�м���"Accept-Encoding", "gzip"
                    //2:header�б�����Content-EncodingΪgzip
                    //3:networkResponse����body
                    GzipSource responseBody = new GzipSource(networkResponse.body().source());
                    Headers strippedHeaders = networkResponse.headers().newBuilder()
                        //�Զ���ѹʱ�Ƴ�Content-Encoding
                        .removeAll("Content-Encoding")
                        //�Զ���ѹʱ�Ƴ�Content-Length�������ϲ�Java������ҪcontentLengthʱΪ-1
                        .removeAll("Content-Length")
                        .build();
                    responseBuilder.headers(strippedHeaders);
                    responseBuilder.body(new RealResponseBody(strippedHeaders,Okio.buffer(responseBody)));
                }
                return responseBuilder.build();
            }
        }
        ```
    - ʹ��OkHttp3,��������������ύ��������,ϣ����post�����ݽ���gzipѹ����ʵ�ַ���:����ʵ���Զ���������,Ȼ���ڹ���OkhttpClient��ʱ�����������
        ```
        ʵ���Զ���������(�ٷ�ʵ��):
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
                        //��Ϊ�޷�Ԥ֪�ھ���gzipѹ����ĳ���,����Ϊ-1
                        return -1;
                    }
                    @Override 
                    public void writeTo(BufferedSink sink) throws IOException {
                        //ͨ��GzipSink,��ԭʼ��BufferedSink����gzipѹ��
                        BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                        //������gzipѹ��������д��RequestBody
                        body.writeTo(gzipSink);
                        gzipSink.close();
                    }
                };
            }
        }
        
        ����OkhttpClient��ʱ��,���������:
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new GzipRequestInterceptor())//����Gzipѹ��
            ...
            .build();
        ```
**Ӧ�ó���**
- ���������罻������:��������Ƶ��,������������
### Retrofit:
**�ص�**
- ����OkHttp
- ͨ��ע����������
- �������,�������
- ����������Ҫʹ��ͳһ��Converter
- �����������RxJava����ʹ��

**Ӧ�ó���**
- �κγ����¶�����ʹ��,�ر�����Ŀ����ʹ��RxJava���ߺ�̨API��ѭRestful���

## Retrofit��ʹ��
### Retrofit�漰����ע��
1. �������󷽷�ע��:
    ```
    @GET,@POST,@PUT,@OPTIONS,@PATCH,@DELETE,@HEAD,@HTTP
    @HTTP�����滻���෽��ע��,ͨ��method,path,hasBody��������:
    public @interface HTTP {
        //��������ķ��������ִ�Сд��
        String method();
        //���������ַ·��
        String path() default "";
        //�Ƿ���������
        boolean hasBody() default false;
    }
    ʵ��:
    @HTTP(method="GET",path="blog/{id}",hasBody=false)
    Call<ResponseBody> getCall(@Path("id") int id);
    
    �������������Url=����Retrofitʵ��ʱͨ��.baseUrl()+����ע��(path),
    ͨ��ʹ��:baseUrlĿ¼��ʽ+path���·�� �ķ����������Url:
        Url = "http:host:port/a/b/appath"
        baseUrl = "http:host:port/a/b/"
        path = appath
    ```
2. ���ע��
    ```
    @FormUrlEncoded,@Multipart,@Streaming
    
    @FormUrlEncoded:��ʾ����form-encoded������
        ÿ����ֵ����Ҫ��@Filed��ע�����,���Ķ����ṩֵ
    @Multipart:��ʾ����form-encoded������(���������ļ��ϴ��ĳ���)
        1:ÿ����ֵ����Ҫ��@Part��ע�����,���Ķ����ṩֵ.
        2:@Part����֧��3����������:RequestBody,okhttp3.MultipartBody.Part,��������
    @Streaming:��ʾ����������������ʽ����,�����ڷ������ݽϴ�ĳ���.���û��ʹ��Streaming,Ĭ�ϰ�����ȫ�������ڴ�,֮���ȡ����Ҳ�Ǵ��ڴ��л�ȡ.
    
    ʵ��:
    public interface GetRequest_Interface {
        /**
         *������һ������ʽ������Content-Type:application/x-www-form-urlencoded��
         * Field("username")��ʾ�������String name��name��ȡֵ��Ϊusername ��ֵ
         */
        @POST("/form")
        @FormUrlEncoded
        Call<ResponseBody> testFormUrlEncoded1(@Field("username") String name, @Field("age") int age);
         
        @POST("/form")
        @Multipart
        Call<ResponseBody> testFileUpload1(@Part("name") RequestBody name, @Part("age") RequestBody age, @Part MultipartBody.Part file);
    }
    ����ʹ��:
    GetRequest_Interface service = retrofit.create(GetRequest_Interface.class);
    // @FormUrlEncoded 
    Call<ResponseBody> call1 = service.testFormUrlEncoded1("Carson", 24);
    
    // @Multipart
    // 1:Part���εĲ�������:RequestBody
    MediaType textType = MediaType.parse("text/plain");
    RequestBody name = RequestBody.create(textType, "Carson");
    RequestBody age = RequestBody.create(textType, "24");
    // 2:Part���εĲ�������:MultipartBody.Part
        //2.1:�ļ�·��
    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ File.separator + "icon.jpg";
        //2.2:�ļ�
    File file = new File(path);
        //2.3:�ļ���������MediaType
    MediaType type = MediaType.parse("image/*");
        //2.4:ͨ��MediaType��File����RequestBody
    RequestBody body = RequestBody.create(type,file);
        //2.5:ͨ��MultipartBody.Part.createFormData(String name, @Nullable String filename, RequestBody body)
        // ����ָ���ļ�������MultipartBody.Partʵ��
    MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", "icon.jpg", body);
    Call<ResponseBody> call3 = service.testFileUpload1(name, age, filePart);
    ```
    - MultipartBody.Part.createFormData(String name, @Nullable String filename, RequestBody body):
        - name�����������е�����
        - fileName���ļ�����,���ڷ���˽���
        - body�����ļ�������RequestBodyʵ��
    - ÿ��RequestBody��Ҫָ��MediaType,������MediaType.parse(X)��X:
        ```
        text/plain�����ı���
        application/x-www-form-urlencoded��ʹ��HTTP��POST�����ύ�ı���
        multipart/form-data��ͬ�ϣ�����Ҫ���ڱ��ύʱ�����ļ��ϴ��ĳ���
        image/gif��GIFͼ��
        image/jpeg��JPEGͼ�񣩡�PHP��Ϊ��image/pjpeg��
        image/png��PNGͼ�񣩡�PHP��Ϊ��image/x-png��
        video/mpeg��MPEG������
        application/octet-stream������Ķ��������ݣ�
        application/pdf��PDF�ĵ���
        application/msword��Microsoft Word�ļ���
        ```
    - ������ļ���չ����X֮��Ķ�Ӧ��ϵ:[MIME �ο��ֲ�](http://www.w3school.com.cn/media/media_mimeref.asp)
    - �������һ���ļ�/File,��֪�����Ӧ��MediaType type=MediaType.parse(X)��X��ʲô,ͨ�����´�����Ի�ȡXֵ:
        ```
        ͨ���ļ�����·��,����ȡ���Ӧ��X
        
        import java.net.FileNameMap;    
        import java.net.URLConnection;    
        public class FileUtils {    
          public static String getMimeType(String fileUrl) throws java.io.IOException    
            {    
                FileNameMap fileNameMap = URLConnection.getFileNameMap();    
                String type = fileNameMap.getContentTypeFor(fileUrl);
                if (contentType == null) {
                    //* exe,���еĿ�ִ�г���
                    contentType = "application/octet-stream"; 
                }
                return type;    
            }
        }
        1:�ļ�������·��
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
3. �����������ע��
    - @Headers:������ӹ̶�������ͷ,ע���ڷ�����
    ```
    ʵ��:
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
    - @Header:������Ӳ��̶�������ͷ,ע���ڷ���������
    ```
    ʵ��:
    @GET("user")
    Call<User> getUser(@Header("Authorization") String authorization)
    ```
    - @HeaderMap:�����������ͷ����,ע���ڷ���������
    ```
    ʵ��:
    Map<string,string> headers = new HashMap()<>;
    headers.put("Accept","text/plain");
    headers.put("Accept-Charset", "utf-8");
    
    @GET("/search")
    void list(@HeaderMap Map<string, string=""> headers);
    ```
    - @Body:�� Post��ʽ ���� �Զ����������� ��������
        - @Bodyע�����,����ͬʱʹ��@FormUrlEncoded��@Multipart,����ᱨ��:
            ```
            @Body parameters cannot be used with form or multi-part encoding
            ```
        - @Body����ʲô��ʽ�ϴ��Ĳ���:���ϴ���@Body����ʵ���Json�ַ���,�����ڲ���Ҫһ��GsonCoverter����ʵ��ת����json�ַ���,��ҪRetrofit������addConverterFactory(GsonConverterFactory.create()).����ᱨ��:
            ```
            Unable to create @Body converter for ***
            ```
        - @Bodyʹ����ȷ����
            ```
            //1:�������Gson
            Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();
            //2:Retrofitʵ������addConverterFactory(GsonConverterFactory.create())
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:4567/")
                //���Խ����Զ����Gson����ȻҲ���Բ���
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
            //3:@Bodyע��Ĳ���,���ڷ���ȥ��@FormUrlEncoded��@Multipart
            public interface BlogService {
                @POST("blog")
                Call<Result<Blog>> createBlog(@Body Blog blog);
            }
            //4:����
            BlogService service = retrofit.create(BlogService.class);
            Blog blog = new Blog();
            blog.content = "�½���Blog";
            blog.title = "����";
            blog.author = "�ֵ�kidou";
            Call<Result<Blog>> call = service.createBlog(blog);
            ```
    - @Field,@FieldMap:���� Post����ʱ�ύ����ı��ֶ�,��Ҫ��@FormUrlEncoded���ʹ��
    ```
    ʵ��:
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
    Call<ResponseBody> call2 = service.testFormUrlEncoded2(map);��
    ```
    - @Part,@PartMap:���� Post���� ʱ�ύ����ı��ֶ�,��Ҫ��@Multipart���ʹ��.�������ļ��ϴ�����.
        - @Partע��Ĳ�������:RequestBody,okhttp3.MultipartBody.Part,��������
        - @PartMapע��һ��Map<String,RequestBody>
    ```
    ʵ��:
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
    
    // ����ʹ��
    MediaType textType = MediaType.parse("text/plain");
    RequestBody name = RequestBody.create(textType, "Carson");
    RequestBody age = RequestBody.create(textType, "24");
    RequestBody file = RequestBody.create(MediaType.parse("multipart/form-data"), Fileһ���ļ�);
    // @Part
    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "test.txt", file);
    Call<ResponseBody> call3 = service.testFileUpload1(name, age, filePart);
    // @PartMap
    // ʵ�ֺ�����ͬ����Ч��
    Map<String, RequestBody> fileUpload2Args = new HashMap<>();
    fileUpload2Args.put("name", name);
    fileUpload2Args.put("age", age);
    //���ﲢ���ᱻ�����ļ�����Ϊû���ļ���(������Content-Disposition����ͷ��)��������� filePart ��
    //fileUpload2Args.put("file", file);
    Call<ResponseBody> call4 = service.testFileUpload2(fileUpload2Args, filePart); 
    ```
    - @Query,@QueryMap:���� @GET �����Ĳ�ѯ����(Query = Url �� ��?�� ����� key-value)
        - @Query��URL�ʺź���Ĳ����� 
        - @QueryMap���൱�ڶ��@Query
        - @Query��@QueryMapע��Ĳ�ѯ������key��valueĬ�϶��Ὺ��URL����,ʹ������encoded=true���ر�URL����.
            - @Query(value="group",encoded=true)
            - @QueryMap(encoded=true)
        - @Queryע��Ĳ���,����ֵ����Ϊ��,Ϊ�ոò����ᱻ����
        - @QueryMapע���Map,�����ֵ������Ϊ��,�����׳�IllegalArgumentException�쳣
    ```
    ʵ��:
    @Query:
    @GET("/list")
    Call<responsebody> list(@Query("category") String category);
    //����һ������
    @GET("/list")
    Call<responsebody> list(@Query("category") String... categories);
    //������URL����
    @GET("/search")
    Call<responsebody> llist(@Query(value="foo", encoded=true) String foo);
    @Query����:
    X.list("1")     URL:/list?category=1
    X.list(null)    URL:/list
    X.list("a","b") URL:/list?category=a&category=b
    @Query(value="foo", encoded=true)��,���ɵ�URL�Ͳ�����encoded=true����޲��,ֻ�ǹر���key��value��URL����
    
    @QueryMap:
    @GET("/search")
    Call<responsebody> list(@QueryMap Map<string, string> filters);
    @GET("/search")
    Call<responsebody> list(@QueryMap(encoded=true) Map<string,string> filters);
    @QueryMap����:
    X.list(ImmutableMap.of("group", "coworker", "age", "42"))
        URL:/search?roup=coworker&age=42
    X.list(ImmutableMap.of("group", "coworker"))
        URL:/search?roup=coworker
    ```
    - @Path:URL��"?"ǰ�沿��,Pathע�������滻url·���еĲ���
    ```
    ʵ��:
    @GET("users/{user}/repos")
    Call<ResponseBody>  getBlog��@Path("user") String user ��;
    X.getBlog("bb")     URL:users/bb/repos
    ```
    - @Url:�����ڷ�������,ֱ����������Ľӿڵ�ַ
        - ��@GET,@POST��ע������û��url��ַʱ,�����ڷ�����ʹ��@Url������ַ�Ե�1����������ʽ����
        - @Urlע��ĵ�ַ,��Ҫ��/��ͷ
        - @Url֧�ֵ������� okhttp3.HttpUrl, String, java.net.URI, android.net.Uri
        - @Pathע����@Urlע�ⲻ��ͬʱʹ��,��������쳣
            ```
            Pathע�������滻url·���еĲ���,���Ҫ����ʹ��pathע��ʱ,
            �����Ѿ���������·��,��Ȼû���滻·����ָ���Ĳ�����,
            ��Urlע�����ڲ�����ָ��������·����,���ʱ��ָ������·���Ѿ�����,
            pathע���Ҳ�������·��,�������������·���еĲ�����
            ```
    ```
    public interface BlogService {
        /**
         * ��GET��POST...HTTP�ȷ�����û������Urlʱ�������ʹ�� {@link Url}�ṩ
         * ����Query��QueryMap���������String����Map�ĵڶ������Ͳ�������String��ʱ
         * �ᱻĬ�ϻ����toStringת����String����
         * Url֧�ֵ������� okhttp3.HttpUrl, String, java.net.URI, android.net.Uri
         * {@link retrofit2.http.QueryMap} �÷���{@link retrofit2.http.FieldMap} �÷�һ��������˵��
         */
        @GET //����URLע��ʱ�������URL��ʡ����
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
### Retrofitʹ������
- ����1�����Retrofit�������
    - Retrofit֧�ֶ������ݽ�����ʽ,ʹ��ʱ��Ҫ��build.gradle�������
        ```
        build.gradle�������:
        compile 'com.squareup.retrofit2:retrofit:2.0.2'
        
        Gson	    com.squareup.retrofit2:converter-gson:2.0.2
        Jackson	    com.squareup.retrofit2:converter-jackson:2.0.2
        Simple XML  com.squareup.retrofit2:converter-simplexml:2.0.2
        Protobuf    com.squareup.retrofit2:converter-protobuf:2.0.2
        Moshi	    com.squareup.retrofit2:converter-moshi:2.0.2
        Wire	    com.squareup.retrofit2:converter-wire:2.0.2
        Scalars	    com.squareup.retrofit2:converter-scalars:2.0.2
        ```
    - Retrofit֧�ֶ�������������������ʽ��guava��Java8��rxjava
        ```
        build.gradle�������:
        guava	    com.squareup.retrofit2:adapter-guava:2.0.2
        Java8	    com.squareup.retrofit2:adapter-java8:2.0.2
        rxjava	    com.squareup.retrofit2:adapter-rxjava:2.0.2
        ```
- ����2������ ���շ������������� ����
- ����3������ ���������������� �Ľӿ�
- ����4������ Retrofit ʵ��
    ```
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://fanyi.youdao.com/") // �������������Url��ַ
        .addConverterFactory(GsonConverterFactory.create())//�������ݽ�����
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//֧��RxJavaƽ̨
        .build();
    ```
- ����5������ ��������ӿ�ʵ�� �� ���������������
    ```
    // ���� ��������ӿ� ��ʵ��
    GetRequest_Interface request = retrofit.create(GetRequest_Interface.class);
    //�� �������� ���з�װ
    Call<Reception> call = request.getCall();
    ```
- ����6��������������(�첽 / ͬ��)
    ```
    //������������(�첽)
    call.enqueue(new Callback<Translation>() {
        //����ɹ�ʱ�ص�
        @Override
        public void onResponse(Call<Translation> call,Response<Translation> response) {
            // �Է������ݽ��д���
            response.body().show();
        }
        //����ʧ��ʱ��Ļص�
        @Override
        public void onFailure(Call<Translation> call, Throwable throwable) {
            System.out.println("����ʧ��");
        }
    });
    // ������������ͬ����
    Response<Reception> response = call.execute();
    // �Է������ݽ��д���
    response.body().show();
    ```
## RetrofitԴ�����
### Retrofit�漰�������ģʽ
1. ģ��ģʽ
    - ����:����һ���������㷨���,��һЩ�����ӳٵ�������,ʹ���಻�ı��㷨�Ľṹ�������¶�����㷨��ĳЩ�ض�����
    - ʹ�ó���:��������й��з����������๫�з����ĵ����߼�������ͬ
    - ģ��ģʽ����2����ɫ:
      - ����:������ public abstract  class AbsParent
        - �����а���:�������� + ģ�巽�� + ���ӷ���
        - ��������:������ȡ��������:protected abstract ����,���������ʵ��
        - ģ�巽��:������1���򼸸�,��ɶԻ��������ĵ���,ʵ�־����߼�:public final����,��ֹ���ิд
        - ���ӷ���:protected����,ע�ⲻ�ǳ��󷽷�.�ڸ����ģ�巽���е���,��ģ�巽����ִ�н���Լ��
      - ����:�����ʵ���� public class Child1 extends AbsParent
        - �����а���:���������ľ���ʵ�� + ���ӷ�������д
    - ����ʵ��
      ```
       //����
		public abstract class AbsParent{
		  //���ӷ���
		  protected boolean executeStep1(){
		    return false;
		  }
		  //��������
		  protected abstract void step1();
		  protected abstract void step2();
		  protected abstract void step3();
		  //ģ�巽��
		  public final void execute(){
		    if(this.executeStep1()){
		      this.step1();
		    }
		    this.step2();
		    this.step3();
		  }
		}
        //����
        public class Child1 extends AbsParent{
          //���๳�ӷ�������ֵ
          private boolean executeFlag = true;
          //�����п��ԶԹ��ӷ�������ֵ��������,�Ӷ��Ը����ģ�巽������Լ��
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
2. Builderģʽ/������ģʽ
    - ����:��һ�����Ӷ���Ĺ��������ı�ʾ����,ʹ��ͬ���Ĺ������̿��Դ�����ͬ�ı�ʾ
    - ����:�û�����Ҫ֪�����Ӷ���Ľ������ϸ��,ֻ��Ҫָ������ľ�������,���ɴ������Ӷ���;���彨���߸����û�ָ���Ķ����������A,����ָ��˳�򴴽�A��ʵ��;
    - ������ģʽ����4����ɫ:
      - ��Ʒ��
        - ��Ʒ��ʵ����ģ��ģʽ.�����Ʒ�����������������ģ�巽��,�����Ʒ����ʵ���˻�������.
      - ��������
        - public abstract class:�淶�˲�Ʒ���齨,ȫ�ǳ��󷽷�,�Ǿ��彨���ߵĸ���
      - ���彨����
        - ʵ�ֳ����������з���,������һ������õľ����Ʒ����ʵ��
      - ������
        - ���ж�����彨����,�����������,����������������Ʒ��ʵ��;
    - ����ʵ��
		```
		�����Ʒ����:��������������ģ�巽��
		public abstract class AbsProduct{
		  //������������˸�����������ִ��˳��
		  private ArrayList<String> sequence = new ArrayList<String>();
		  //��������
		  protected abstract void step1();
		  protected abstract void step2();
		  //���ò���
		  public final void setSequence(ArrayList<String> sequence){
		    this.sequence = sequence;
		  }
		  //ģ�巽��
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
		�����Ʒ����:ʵ���˻�������
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
		��������:�淶��Ʒ���齨
		public abstract class ProductBuilder{
		  //���ò�Ʒ����
		  public abstract void setSequence(ArrayList<String> sequence);
		  //��ȡ��Ʒʵ��
		  public abstract AbsProduct getProduct();
		}
		���彨����:ʵ�ֳ����������з���,������һ������õľ����Ʒ����ʵ��
		public class Product1Builder extends ProductBuilder{
		  //˽�б������ǽ�Ҫ�����ľ����Ʒ��ʵ��
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
		  //˽�б������ǽ�Ҫ�����ľ����Ʒ��ʵ��
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
		������:���ж�����彨����,�����������,����������������Ʒ��ʵ��
		public class Director{
		  //Ӱ���Ʒ����˳��Ĳ���
		  private ArrayList<String> sequence = new ArrayList<String>();
		  //���彨����
		  private Product1Builder builder1 = new Product1Builder();
		  private Product2Builder builder2 = new Product2Builder();
		  //���������������չ
		  //1:������ͬ���͵ľ����Ʒ
		    //(gainProduct1A,gainProduct1B) �� gainProduct2 ����������ͬ���͵ľ����Ʒ
		  //2:��ͬ���͵Ĳ�Ʒ,���Ʒ���̵�������˳��Ҳ��������仯:
		    //gainProduct1A��gainProduct1B ����ͬ���Ʒ������������˳��仯
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
3. ���ģʽ/����ģʽ
    - ����:�ڸ���ϵͳS�Ϳͻ���C֮���ټ�һ��"�Ӵ�Ա"R,��R��ʵ�ֶ�S���ӹ��ܵķ��ʷ�װ;Cֱ�Ӻ�R��������.
    - ����:������S�ĸ�����;R��S�и��ӹ��ܽ����˷�װ,C����R��װ���ķ���,�ɱ����ˮƽ����
    - ����:ȥҽԺ������Ҫ �Һ�,����,�ɷ�,ȡҩ,�û��߻��߼������úܸ��ӣ�������ṩ�Ӵ���Ա��ֻ�ýӴ���Ա�������ͺܷ���
    - ���ģʽ����:�ӿ�+ʵ����+�����(R)
    - ����ʵ��
		```
		//����1���ӿ�,����ҽԺÿ������
		public interface Step{
		  void execute();
		}
		//����ʵ����,����ͬ���͵ľ�������
		public class GuaHao implements Step{
		  @Override
		  public void execute(){
		    System.out.println("�������ڹҺ�");
		  }
		}
		public class WenZhen implements Step{
		  @Override
		  public void execute(){
		    System.out.println("������������");
		  }
		}
		public class JiaoFei implements Step{
		  @Override
		  public void execute(){
		    System.out.println("�������ڽɷ�");
		  }
		}
		public class QuYao implements Step{
		  @Override
		  public void execute(){
		    System.out.println("��������ȡҩ");
		  }
		}
		//���������"�Ӵ�ԱR"
		public class Reception{
		  //"�Ӵ�Ա"����S�и��ӹ��ܵ�����
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
		  //����һ�����ͻ��˵��õķ���,����ʵ��һ������
		  public void executeAll(){
		    this.guahao.execute();
		    this.wenzhen.execute();
		    this.jiaofei.execute();
		    this.quyao.execute();
		  }
		}
		//�ͻ���ֱ�ӵ���Reception
		Reception r = new Reception();
		r.executeAll();
		```
3. ����ģʽ
    - ����:ͨ�����ʴ�����ķ�ʽ����ӷ���Ŀ����
    - �ŵ�:����Ŀ����ʵ��ϸ��;���ı�Ŀ���������,��ָ������ǰ��ִ����չ,�������У�����������
    - ����:
      - ��̬����:�������ڳ�������ǰ�Ѿ�����
      - ��̬����:�������ڳ�������ǰ�����ڡ�����ʱ�ɳ���̬���ɵĴ���ʽ��Ϊ��̬����
    - ��̬�������:
      - Ŀ����ʹ����๲ͬʵ�ֵĽӿ�
      - Ŀ����,������(�������г���Ŀ����ʵ��)
    - ��̬����ʵ��:
		```
		//����һ���ӿ�
		public interface MyOpt{
		  void opt();
		}
		//����Ŀ����
		public class TargetOpt implements MyOpt{
		  @Override
		  public void opt(){
		    System.out.println("TargetOpt:opt");
		  }
		}
		//����������
		public class ProxyOpt implements MyOpt{
		  //�������г��� Ŀ����ʵ��
		  private TargetOpt target;
		  public ProxyOpt(){
		    this.target = new TargetOpt();
		  }
		  @Override
		  public void opt(){
		    this.target.opt();
		  }
		}
		//�ͻ��˺ʹ�����ֱ�ӽ��н���:
		ProxyOpt proxy= new ProxyOpt();
		proxy.opt();
		```
    - ��̬����
    	- ��̬�������Pִ�з�������˳��:
	      - P.func==>InvocationHandler.invoke==>Ŀ����ʵ��.func
	    - ��̬����ʵ����Ҫ3��:
	      - 1 ����Ŀ����ӿ� �� Ŀ����
	      - 2 ʵ��InvocationHandler�ӿ�
	        - ���ô�������ÿ������ʵ�����ն��ǵ�����InvocationHandler��invoke����
	      - 3 ͨ��Proxy���½����������:Proxy.newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
	    - ��̬����ʵ��:
			```
			//�����ӿ�
			public interface Step{
			  void execute();
			}
			//����Ŀ����
			public class MyStep implements Step{
			  @Override
			  public void execute(){
			    System.out.println("MyStep:execute");
			  }
			}
			//ʵ��InvocationHandler�ӿ�
			public StepHandler implements InvocationHandler{
			  //target:Ŀ����ʵ��
			  private Object target;
			  public StepHandler(){}
			  public StepHandler(Object obj){
			    this.target = obj;
			  }
			  //proxy:ͨ�� Proxy.newProxyInstance() ���ɵĴ������
			  //method:��ʾproxy�����õķ���
			  //args:��ʾproxy�����õķ����Ĳ���
			  @Override
			  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			    Object obj = method.invoke(this.target, args);
			    return obj;
			  }
			}

			//ͨ��Proxy���½��������,ֱ�ӵ��ô������ķ���
			//1:����InvocationHandler��ʵ����ʵ��,��Ŀ����ʵ����Ϊ�����������
			StepHandler h = new StepHandler(new MyStep());
			//2:�����������
			Proxy: Object newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
			    loader:Ŀ����̳еĽӿ��������������
			    interfaces:Ŀ����̳еĽӿڵ�Class
			    h:InvocationHandler��ʵ����ʵ��
			Step step = (Step)(Proxy.newProxyInstance(Step.class.getClassLoader(),new Class[]{Step.class},h));
			//3:ֱ�ӵ��ô������ķ���
			step.execute();    ==> "MyStep:execute"

	      step.execute()ʵ���ǵ��������ɵĴ������P�е�execute����
	      ==>
	      ��P�е�execute����,�ǵ����˸ոմ�����h.invoke����
	      ==>
	      h.invoke,�������Ŀ����MyStepʵ���е�execute����
			```
	    - **Proxy.newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)**Դ�����
```
�������ؼ�����

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
    //��ȡĿ����̳еĽӿڵ�Class����
    final Class<?>[] intfs = interfaces.clone();
    //1:ͨ���ӿڵ�Class����,��ȡ�������Class
    Class<?> cl = getProxyClass0(loader, intfs);
    ****
  }
  //1:����������Ѿ������򷵻ظ���;��������ͨ��ProxyClassFactory����������:��1.1
  private static Class<?> getProxyClass0(ClassLoader loader,
                                           Class<?>... interfaces) {
        // If the proxy class defined by the given loader implementing
        // the given interfaces exists, this will simply return the cached copy;
        // otherwise, it will create the proxy class via the ProxyClassFactory
    return proxyClassCache.get(loader, interfaces);
  }
  //1.1:�˴�ֱ�ӿ�ProxyClassFactory��apply��������
  proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
  private static final class ProxyClassFactory implements BiFunction<ClassLoader, Class<?>[], Class<?>>{
    //����Ҫ���ɵĴ���������ǰ׺
    // prefix for all proxy class names
    private static final String proxyClassNamePrefix = "$Proxy";
    //Ϊ�����ɵĴ����಻������ȡ�����ƺ�׺:�������ῴ��
    // next number to use for generation of unique proxy class names
    private static final AtomicLong nextUniqueNumber = new AtomicLong();
    //���ɴ������Class
    @Override
    public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {
      //�ӿ���������Map
      Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
      //����������Ҫʵ�ֵ�ÿ���ӿ�,ע�������Interface,��������쳣
      for (Class<?> intf : interfaces) {
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                //���newProxyInstance�д����ClassLoader�����ǽӿ�������ClassLoader,�����쳣
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(intf + " is not visible from class loader");
                }
               //���newProxyInstance�д����Class����,���������Interface,���쳣
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(
                        interfaceClass.getName() + " is not an interface");
                }
                //ͨ��Set,��ֹͬһ���ӿ��ظ�����
                if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException(
                        "repeated interface: " + interfaceClass.getName());
                }
      }
      String proxyPkg = null;     //Ҫ���ɵĴ���������package
      int accessFlags = Modifier.PUBLIC | Modifier.FINAL;
      //����ÿ���ӿ�,��ȡ����������package
      for (Class<?> intf : interfaces) {
                int flags = intf.getModifiers();
                if (!Modifier.isPublic(flags)) {
                    accessFlags = Modifier.FINAL;
                    //��ȡ�ӿ�����
                    //��:�ӿ�IStep,�ӿ�����packageΪa.b.c,��(new IStep()).getClass().getNameΪ:  "a.b.c.IStep"
                    String name = intf.getName();
                    int n = name.lastIndexOf('.');
                    //��ȡ�ӿ�����package:��ӦIStep��Ϊ"a.b.c."
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        //proxyPkg��������ֵ
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        //ע��:���proxyPkg�Ѿ�����,˵������Ľӿ�Class���鲻ֹһ��Clas��
                        //Ϊ����������еĽӿڰ�����ͬ,�ͻ����쳣
                        //����  
                          //1:��֤����Class������Interface������ͬ
                          //2:ֻ��1��Interface��Class��������,����Ҫ������ĳ�����?
                        throw new IllegalArgumentException(
                            "non-public interfaces from different packages");
                    }
                }
      }
      ****
      //�����õ���AtomicLong,����ƴ�Ӵ����������
      long num = nextUniqueNumber.getAndIncrement();
      //�Խӿ�"a.b.c.IStep"Ϊ��,�����������Ϊ:"a.b.c.$Proxy0"
      String proxyName = proxyPkg + proxyClassNamePrefix + num;
      //1.2:���ɴ�����class,��byte[]��ʽ����
      byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);
      try {
        //1.3:native�������ɴ������Class,������
        return defineClass0(loader, proxyName,proxyClassFile, 0, proxyClassFile.length);
      } catch (ClassFormatError e) {
        ****
      }
    }
  }
  //1.3:defineClass0��һ��native����
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