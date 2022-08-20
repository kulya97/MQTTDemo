package com.kulya.socket;

/*
项目名称： Socket调试器
创建人：黄大神
类描述：
创建时间：2020/3/16 23:07
*/

public class MQTTentity {
    //以下请自己配置否则MainActivity中button_login的监听会有问题
    private String host;
    private String userName;
    private String passWord;
    private String timeTopic;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getTimeTopic() {
        return timeTopic;
    }

    public void setTimeTopic(String timeTopic) {
        this.timeTopic = timeTopic;
    }

    public MQTTentity(String timeTopic) {
        this.timeTopic = timeTopic;
    }

    public MQTTentity(String host, String userName, String passWord, String timeTopic) {
        this.host = host;
        this.userName = userName;
        this.passWord = passWord;
        this.timeTopic = timeTopic;
    }

    @Override
    public String toString() {
        return "MQTTentity{" +
                "host='" + host + '\'' +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", timeTopic='" + timeTopic + '\'' +
                '}';
    }
}
