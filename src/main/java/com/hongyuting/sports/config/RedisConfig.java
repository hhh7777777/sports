package com.hongyuting.sports.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * 配置RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 使用FastJSON序列化
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // value序列化方式采用FastJSON
        template.setValueSerializer(fastJsonRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // hash的value序列化方式采用FastJSON
        template.setHashValueSerializer(fastJsonRedisSerializer);
        // 设置默认序列化方式
        template.setDefaultSerializer(fastJsonRedisSerializer);
        // 启用事务支持
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }
}
