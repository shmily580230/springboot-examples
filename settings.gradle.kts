pluginManagement {
    repositories {
        // 阿里云公共仓库
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "springboot-examples"

include("springboot-otel")
include("springboot-nats")
