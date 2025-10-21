### 全局异步

注：idea需配置到VM options

打印行号严重影响性能

```
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```