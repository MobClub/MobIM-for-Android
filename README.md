# MobIM集成文档

## 配置gradle

### 1、将下面的脚本添加到您的根模块build.gradle中：
``` gradle
buildscript {
    // 添加MobSDK的maven地址
    repositories {
        maven {
            url "http://mvn.mob.com/android"
        }
    }

    dependencies {
        // 注册MobSDK
        classpath 'com.mob.sdk:MobSDK:+'
    }
}
```

### 2、在使用MobIM模块的build.gradle中，添加MobSDK插件和扩展，如：
``` gradle
// 添加插件
apply plugin: 'com.mob.sdk'

// 在MobSDK的扩展中注册MobIM的相关信息
MobSDK {
    appKey "d580ad56b***"
    appSecret "7fcae59a62342e7e2759e9e397c82***"

    MobIM {}
}
```

## 添加代码

### 1、初始化MobIM

如果您没有在`AndroidManifest`中设置`appliaction`的类名，MobSDK会将这个设置为`com.mob.MobApplication`，但如果您设置了，请在您自己的Application类中调用：
```java
MobSDK.init(this);
```
以初始化MobSDK。

#### 2、调用API

在应用启动后，用户自己的用户登录系统登录成功后调用如下api，设置用户到MobIM即可。
	
登录成功后，设置用户信息（IM会使用此用户进行通讯）
```java
MobSDK.setUser("用户ID", "用户昵称","用户头像地址", null);
```
在需要监听消息回调的地方，调用如下api，设置消息监听
```java
MobIM.addMessageReceiver(MobIMMessageReceiver mobMsgRever);
```
在IM程序时，调用移除监听消息接口：
```java
MobIM.removeMessageReceiver(MobIMMessageReceiver mobMsgRever);
```	
注：MobIMMessageReceiver是处理收到消息的回调接口，根据获取的消息进行处理，接口实现如下：
```java
public interface MobIMMessageReceiver {
    void onMessageReceived(List<IMMessage> messageList);
}
```
获取会话列表
```java
MobIM.getChatManager().getAllLocalConversations(MobIMCallback<List<IMConversation>> conversations)
```
发送消息
```java
MobIM.getChatManager().sendMessage(msg, new MobIMCallback<Void>() {
    public void onSuccess(Void result)  {
        // TODO 处理消息发送成功的结果
    }
    public void onError(int code, String message)  {
        // TODO 根据错误码（code）处理错误返回
    }
});
```
备注：
MobIM并不自带用户系统（没有登录与注册操作，需要用户有自己的），用户成功登陆您自有用户系统后，开发者可对接登录MobIM中相对应的用户。

## MOBIMCallback
MobIMCallback是MobIM所有异步操作的结果回调，包含下面2个方法：

 方法名称|参数列表|使用说明
-------|-------|-------
onSuccess|T t 具体操作的结果数据|成功回调
onError|int code, String message 具体操作的异常类型<br>关于错误码的项目描述，请参考 [MobIM错误码][1]|错误回调
## 混淆设置

MobIM已经做了混淆处理，再次混淆会导致不可预期的错误，请在您的混淆脚本中添加如下的配置，跳过对MobIM的混淆操作：
```
-keep class com.mob.**{*;}
```
## 注意事项
  1. MobSDK默认为MobIM提供最新版本的集成，如果您想锁定某个版本，可以在`MobIM`下设置“version "某个版本"”来固定使用这个版本
  2. 如果使用MobSDK的模块会被其它模块依赖，请确保依赖它的模块也引入MobSDK插件，或在此模块的gradle中添加：
``` gradle
repositories {
    maven {
        url "http://mvn.mob.com/android"
    }
}
```


  [1]: http://wiki.mob.com/mobim%E9%94%99%E8%AF%AF%E7%A0%81-android/