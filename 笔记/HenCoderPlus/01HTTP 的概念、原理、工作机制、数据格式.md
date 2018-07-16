### HTTP 的概念、原理、工作机制、数据格式

#### 幂等
- 幂等(即反复调用多次时会得到相同的结果)
- HTTP方法的幂等性是指一次和多次请求某一个资源应该具有同样的副作用
- GET,PUT,DELETE是幂等操作,POST不是
  - GET:HTTP GET方法用于获取资源，不应有副作用，所以是幂等的。<br>比如：GET http://www.bank.com/account/123456，不会改变资源的状态，不论调用一次还是N次都没有副作用。<br/>请注意，这里强调的是一次和N次具有相同的副作用，而不是每次GET的结果相同。GET http://www.news.com/latest-news这个HTTP请求可能会每次得到不同的结果，但它本身并没有产生任何副作用，因而是满足幂等性的
  - PUT:修改资源<br/>PUT http://www.forum/articles/4231的语义是创建或更新ID为4231的帖子。对同一URI进行多次PUT的副作用和一次PUT是相同的；因此，PUT方法具有幂等性。
  - DELETE:DELETE方法用于删除资源，有副作用，但它应该满足幂等性。<br/>比如：DELETE http://www.forum.com/article/4231，调用一次和N次对系统产生的副作用是相同的，即删掉id为4231的帖子；因此，调用者可以多次调用或刷新页面而不必担心引起错误
  - POST:POST所对应的URI并非创建的资源本身，而是资源的接收者。<br/>比如：POST http://www.forum.com/articles的语义是在http://www.forum.com/articles下创建一篇帖子，HTTP响应中应包含帖子的创建状态以及帖子的URI。<br/>两次相同的POST请求会在服务器端创建两份资源，它们具有不同的URI；所以，POST方法不具备幂等性。
  - POST和PUT:HTTP协议规定，POST方法修改资源状态时，URL指示的是该资源的父级资源，待修改资源的ID信息在请求体中携带。而PUT方法修改资源状态时，URL直接指示待修改资源。因此，同样是创建资源，重复提交POST请求可能产生两个不同的资源，而重复提交PUT请求只会对其URL中指定的资源起作用，也就是只会创建一个资源。

#### 状态码
1. 206:Partial Content,该状态码表示客户端进行了范围请求，而服务器成功执行了这部分的GET请求。响应报文中包含由 Content-Range 指定范围的实体内容

#### Transfer-Encoding: chunked
````
HTTP/1.1 200 OK
Date: Tue, 03 Jul 2012 04:40:56 GMT
Cache-Control: public, max-age=604800
Content-Type: text/javascript; charset=utf-8
Expires: Tue, 10 Jul 2012 04:40:56 GMT
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Encoding: gzip
Transfer-Encoding: chunked
Connection: keep-alive
cf0 ←16进制(10进制为3312)
...3312字节分块数据...
392 ←16进制(10进制为914)
...914字节分块数据...
0
````
以上用例中，正如在首部字段 Transfer-Encoding 中指定的那样，有效使用分块传输编码，且分别被分成 3312 字节和 914 字节大小的分块数据

####  Cache-Control
> Cache-Control可以操作缓存的工作机制,Cache-Control: private, max-age=0, no-cache,多个指令通过逗号分隔可同时设置
- Cache-Control: public 表示响应数据可以 被缓存服务器缓存 并 提供给所有用户
- Cache-Control: private 响应数据被缓存服务器缓存后,只能提供给特定的用户
- Cache-Control: no-cache
  - 客户端发送的请求中如果包含 no-cache 指令，则表示客户端将不会接收缓存过的响应。于是，“中间”的缓存服务器必须把客户端请求转发给源服务器
  - 服务器返回的响应中包含 no-cache 指令，那么缓存服务器不能对资源进行缓存
- Cache-Control: no-store 表示请求或响应中包含机密信息,则请求和响应都不会进行缓存
- Cache-Control: no-transform 规定无论是在请求还是响应中，缓存都不能改变实体主体的媒体类型.这样做可防止缓存或代理压缩图片等类似操作
- Cache-Control: max-age=604800（单位：秒）
  - 客户端发送的请求中包含 max-age 指令时,如果判定缓存资源的缓存时间数值比指定时间的数值更小且未过期,那么客户端就接收缓存的资源
  - 服务器返回的响应中包含 max-age 指令时,代表资源保存为缓存的最长时间,超过时间视为过期
- Cache-Control: max-stale=3600（单位：秒） 
  - max-stale表示即使缓存资源过期,客户端也接收
  - max-stale未指定参数值,客户端一律接收缓存
  - max-stale指定了参数值,只要已缓存的时间低于参数值,即使已经过期也接收缓存

![public与private的区别](https://user-gold-cdn.xitu.io/2018/7/11/16488ad52ff7b3ad?w=631&h=574&f=png&s=141172)
