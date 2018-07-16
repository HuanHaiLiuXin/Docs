## HTTPS 连接建立过程

#### 1. 客户端向服务器发送"Client Hello",包含以下内容:
- 客户端可以接受的 所有的SSL/TLS的版本
- 客户端可以接受的 所有的 非对称加密算法+对称加密算法+hash算法组合.每个组合称为1个Cipher Suite(密钥算法套件).
- 客户端随机数
- Server name:告知服务器要下发到哪个子服务器
#### 2. 服务器收到客户端发来的"Client Hello"后,选出要使用的SSL/TLS版本+Cipher Suite,再加上服务器随机数,再一起发给客户端:"Server Hello"
- 保存客户端随机数
- 从客户端提供的所有 SSL/TLS版本 中选出要使用的版本
- 从客户端提供的所有 Cipher Suite 中选出要使用的 Cipher Suite
- 然后将选好的 SSL/TLS版本+Cipher Suite,再加上服务器随机数,一起发给客户端:"Server Hello"
- **选好的 SSL/TLS版本+Cipher Suite+客户端随机数+服务器随机数,服务器和客户端一人一份**
#### 3. 服务器向客户端发送 服务器证书
服务器证书结构:
