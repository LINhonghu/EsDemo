package com.honghu.configs;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.rmi.transport.Transport;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by 鸿鹄 on 2018/4/26.
 */
@Configuration
public class myconfig {
    @Bean
    public TransportClient client() throws UnknownHostException {
        //创建ES地址端口配置
        InetSocketTransportAddress node=new InetSocketTransportAddress(
                InetAddress.getByName("localhost"),9300
        );
        //配置集群名
        Settings settings=Settings.builder()
                .put("cluster.name","honghu")
                .build();
        TransportClient client=new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);
        return  client;
    }
}
