package cn.wanghaomiao.seimi.boot;

import cn.wanghaomiao.seimi.core.Seimi;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/12/30.
 */
public class Run {
    public static void main(String[] args) {
        Seimi s = new Seimi();
        if (ArrayUtils.isNotEmpty(args)) {
            if (args[0].matches("\\d+")) {
                int port = Integer.parseInt(args[0]);
                if (args.length>1){
                    s.startWithHttpd(port,ArrayUtils.subarray(args,1,args.length));
                }else {
                    s.startAllWithHttpd(port);
                }
            }else {
                s.start(args);
            }
        } else {
            s.startAll();
        }
    }
}
