package commands;

import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/3.
 */
@Usage("test cmd")
public class TestCmd extends BaseCommand {
    @Command
    public String test(@Usage("执行内部方法，Service") @Option(names={"s","service"}) String service,
                       @Option(names={"m","method"}) String method, InvocationContext context){
        BeanFactory defaultListableBeanFactory = (BeanFactory) context.getAttributes().get("spring.beanfactory");
        Object serviceIns = defaultListableBeanFactory.getBean(service);
        if (serviceIns!=null){
            Method m = ReflectionUtils.findMethod(serviceIns.getClass(),method);
            if (m == null){
                return "not found method:"+method+" in service:"+service;
            }
            context.getWriter().write("-- res:"+ReflectionUtils.invokeMethod(m,serviceIns));
        }else {
            return "not found "+service;
        }
        return "over__";
    }
}
