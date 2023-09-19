package com.chen.rocketmqdemo.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.PropertyValueConst;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * rocket 基础配置属性
 */
@Data
@ConfigurationProperties(prefix = "rocketmq")
public class RocketProperties {

  private Map<String, RocketConfig> configs = new HashMap<>();

  public Map<String, RocketConfig> getConfigs() {
    return configs;
  }

  @Data
  public static class RocketConfig {

    private String accessKey = "accessKey";

    private String secretKey = "secretKey";

    private String nameSrvAddr = "http://localhost:80";

    private String topic = "topic";

    private String groupSuffix = "GID_";

    private boolean enable = true;

    private Long delay = 5000L;

    public Properties rocketProperties() {
      Properties properties = new Properties();
      properties.put(PropertyKeyConst.AccessKey, accessKey);
      properties.put(PropertyKeyConst.SecretKey, secretKey);
      properties.put(PropertyKeyConst.NAMESRV_ADDR, nameSrvAddr);
      // 集群消费模式
      properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
      return properties;

    }
  }

  public static class RocketKey {
    //erp系统
    public static final String ERP_KEY = "erp";
    //文件系统
    public static final String FILE_KEY = "file";
  }
}
