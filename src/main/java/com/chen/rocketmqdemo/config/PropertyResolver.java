package com.chen.rocketmqdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: zhuCan
 * @date: 2020/4/19 19:41
 * @description: properties 配置文件属性注入到自定义注解里面 使用 spring expression language 读取
 * 格式: ${propertyKey}
 */
public class PropertyResolver {

  @Autowired
  private Environment environment;

  /**
   * 解析读取配置文件
   * 并支持多个配置属性读取格式
   * ${propertyKey}xxx${propertyKey}
   *
   * @param el
   * @return
   */
  public String springElResolver(String el) {

    String regex = "\\$\\{([^}]*)\\}";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(el);
    while (matcher.find()) {
      String expression = matcher.group();
      String propertyKey = expression.substring(2, expression.length() - 1);
      String value = environment.getProperty(propertyKey);
      if (StringUtils.hasText(value)) {
        el = el.replaceFirst(expression, value);
      } else {
        throw new RuntimeException("property " + propertyKey + "done not have default value in the .yml");
      }
    }
    return el;
  }

  /**
   * 使用spring自带的配置属性识别
   *
   * @param el el表达式
   * @return 返回配置文件属性
   */
  public String resolvePlaceHolders(String el){
    return environment.resolvePlaceholders(el);
  }
}
