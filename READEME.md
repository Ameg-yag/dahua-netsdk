##使用方法
### 直接运行
...
### 导入项目运行
* 导入jna.jar
* 导入大华netsdk插件
* 初始化对象
``` java
SdkService service = new SdkService().builder()
   .set("ip", "192.168.0.100")
   .set("port", 37777)
   .set("name", "admin")
   .set("password", "admin")
   .set("savePic", false) // 智能交通是否保存图片
   .set("dllPath", "./src/main/resources/libs/win32/") // 插件路径
   .set("logPath", "./logs")   
   .build();
   
service.start()
```
* 启动车辆检测
``` java
TrafficEvent event = new TrafficEvent();
event.startEvent();
Thread r = new Thread() {
    @Override
    public void run() {
    while (true) {
        Queue<CaptureBean> queue = event.getQueue();
        if (queue.size() > 0) {
            System.out.println(queue.poll());
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
};
r.start();
```