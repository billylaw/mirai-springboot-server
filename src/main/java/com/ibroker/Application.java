package com.ibroker;

import com.forte.qqrobot.SimpleRobotApplication;
import com.simbot.component.mirai.MiraiApplication;

@SimpleRobotApplication(resources = "conf.properties")
public class Application {

    public static void main(String[] args) {
        // 获取Mirai组件的启动器类
        MiraiApplication application = new MiraiApplication();
        // 启动
        application.run(Application.class, args);
    }

}
