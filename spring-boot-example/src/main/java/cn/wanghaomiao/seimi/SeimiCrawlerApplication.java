package cn.wanghaomiao.seimi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:applicationContext.xml"})
public class SeimiCrawlerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SeimiCrawlerApplication.class, args);
	}
}
