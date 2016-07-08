/*
   Copyright 2015 Wang Haomiao<et.tw@163.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
                    s.startAllWorkersWithHttpd(port);
                }
            }else {
                s.start(args);
            }
        } else {
            s.startWorkers();
        }
    }
}
