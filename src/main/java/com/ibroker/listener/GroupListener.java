package com.ibroker.listener;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.anno.depend.Beans;
import com.forte.qqrobot.beans.messages.msgget.GroupMemberIncrease;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import java.util.List;

@Beans
public class GroupListener {

    Logger logger= LogManager.getLogger(this.getClass());

    //只处理哪些qq群
    private static List<String> qqgroups= Lists.newArrayList();
    static {
        Resource resource = new ClassPathResource("application.properties");
        try{
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            String numbers = props.getProperty("qqgroup.numbers");
            qqgroups = Arrays.asList(numbers.split(","));
        }catch (Exception ex){

        }


    }


    /**
     * 私信消息
     */
    @Listen(MsgGetTypes.privateMsg)
    public void privateMsg(PrivateMsg msg, MsgSender sender){
        System.out.println("收到消息：" + msg);
    }

    /**
     * 接收群消息
     * @param msg
     * @param sender
     */
    @Listen(MsgGetTypes.groupMsg)
    public void groupMsg(GroupMsg msg, MsgSender sender){
        String groupNumber=msg.getGroup();
        if (!qqgroups.contains(groupNumber)){
            logger.log(Level.ERROR,String.format("QQ群%s不在配置列表中，请重新设置!",
                    msg.getGroup()));
            return;
        }
        logger.log(Level.INFO,String.format("QQ群[%s]-%s(%s)在%s说:%s",
                msg.getGroup(),msg.getNickname(),msg.getQQ(),msg.getTimeToLocalDateTime().toString(),msg.getMsg()));
        //System.out.println("收到消息：" + msg);
    }


//    /**
//     * 群消息
//     *  @Filter(at=true) 只要at我的信息
//     * @param msg
//     * @param sender
//     */
//    @Listen(MsgGetTypes.groupMsg)
//    @Filter(at=true)
//    public void groupMsg(GroupMsg msg, MsgSender sender){
//        String groupNumber=msg.getGroup();
//        if (!processGroupNumber.contains(groupNumber)){
//            return;
//        }
//        //sender.SENDER.sendGroupMsg(groupNumber,weather);
//
//    }

//    @Listen(MsgGetTypes.groupMemberIncrease)
//    public void groupMemAdd(GroupMemberIncrease groupMemberIncrease, MsgSender sender){
////        String groupNumber=groupMemberIncrease.getGroup();
////        if (!processGroupNumber.contains(groupNumber)){
////            return;
////        }
////        String beOperatedQQ = groupMemberIncrease.getBeOperatedQQ();
////        sender.SENDER.sendGroupMsg(groupNumber,"欢迎"+beOperatedQQ+"加入本群");
//    }
}
