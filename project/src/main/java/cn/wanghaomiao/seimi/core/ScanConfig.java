package cn.wanghaomiao.seimi.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/4/9.
 */
@Configuration
@ImportResource("classpath*:**/seimi*.xml")
public class ScanConfig {
}
