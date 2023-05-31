# Voice Room Service

## 项目结构

### common 对环信im服务的封装

#### im im主要是对环信服务端api的封装集成
##### ImApi中封装了环信服务端api的调用方法 [ImApi.java](voiceRoom%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvoiceroom%2Fcommon%2Fim%2FImApi.java)

### mic 语聊房间服务

#### controller 控制器
##### 用户控制器 [UserController.java](voiceRoom%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvoiceroom%2Fmic%2Fcontroller%2FUserController.java)
##### 语聊房间控制器 [VoiceRoomController.java](voiceRoom%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvoiceroom%2Fmic%2Fcontroller%2FVoiceRoomController.java)
##### 语聊房间礼物控制器 [VoiceRoomGiftController.java](voiceRoom%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvoiceroom%2Fmic%2Fcontroller%2FVoiceRoomGiftController.java)
##### 语聊房间麦位控制器 [VoiceRoomMicController.java](voiceRoom%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvoiceroom%2Fmic%2Fcontroller%2FVoiceRoomMicController.java)
##### 语聊房房间用户控制器 [VoiceRoomUserController.java](voiceRoom%2Fsrc%2Fmain%2Fjava%2Fcom%2Fvoiceroom%2Fmic%2Fcontroller%2FVoiceRoomUserController.java)

## 项目配置

请参考`resources/application.properties`，另外开发环境和线上部署环境的差异化配置请参考`resources/application-dev.properties`和`resources/application-prod.properties`

## 编译与运行方法

1. 创建MySQL数据库，数据库名可自定义，配置到连接中即可
2. 使用doc/ddl.sql，初始化数据表
3. 使用IDEA打开本工程
4. 按项目配置需求配置服务
   * 配置环信IM（可以通过环信控制台申请获得）
     * im.easemob.appkey 为环信appkey
     * im.easemob.clientId 为环信Restful Api 客户key
     * im.easemob.clientSecret 为环信Restful API客户secret
     * im.easemob.baseUri 为环信Restful API请求地址（可以不填，会默认获取dns）
     * im.easemob.httpConnectionPoolSize 为 请求环信 Restful API 线程池大小
     * 配置声网RTC
       * agora.service.appId 为声网项目app ID（尽可能与客户端APP ID一致）
       * agora.service.appCert 为声网项目token 
       * agora.service.expireInSeconds
     * 配置房间密码是否加密
       * is.encrypt.password 默认为false，true为加密
     * 其他配置
       * 其他服务配置都有默认参数，如果你对Spring配置熟悉，可以按自己服务需求进行调整
5. 在IDEA中，选择当前要编译的环境（dev/prod)
   * 配置MySQL连接信息
     * spring.datasource.url为MySQL数据库连接地址
     * spring.datasource.username为MySQL数据库连接用户名
     * spring.datasource.password为MySQL数据库连接密码
   * 配置Redis连接信息
     * spring.redis.host为redis连接地址
     * spring.redis.port为redis连接端口
     * spring.redis.password为redis连接密码，如果为空则留空即可
   * 其他配置
     * 其他服务配置都有默认参数，如果你对Spring配置熟悉，可以按自己服务需求进行调整
7. 在命令行中运行`mvn clean package -P dev`或`mvn clean packege -P prod` 在./voiceRoom/target目录下生成 `voiceRoom-0.0.1-SNAPSHOT.jar`
8. 将目标jar上传至指定服务器，执行`nohup java -jar voiceRoom-0.0.1-SNAPSHOT.jar &` ，服务即可运行

# Gateway

## 项目配置

请参考`resources/application.yml`

## 编译与运行方法

1. 使用IDEA打开本工程
2. 按项目配置需求配置服务
3. 在命令行中运行`mvn package`生成 `gateway-0.0.1-SNAPSNOT.jar`
4. 将目标jar上传至指定服务器，执行`nohup java -jar 生成gateway-0.0.1-SNAPSNOT.jar &`，服务即可运行
