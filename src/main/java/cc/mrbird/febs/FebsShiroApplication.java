package cc.mrbird.febs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author MrBird
 */
@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling//定时任务
@MapperScan("cc.mrbird.febs.*.mapper")
public class FebsShiroApplication {


    //@FebsShiroApplication(exclude= {DataSourceAutoConfiguration.class});
    public static void main(String[] args) {
        new SpringApplicationBuilder(FebsShiroApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }


}
