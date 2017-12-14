## 获取MobIM


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

// 在MobSDK的扩展中注册UMSSDK的相关信息
MobSDK {
    appKey "d580ad56b***"
    appSecret "7fcae59a62342e7e2759e9e397c82***"

    MobIM {}
    
}
```