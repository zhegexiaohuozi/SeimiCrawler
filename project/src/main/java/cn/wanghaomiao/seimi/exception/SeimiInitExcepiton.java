package cn.wanghaomiao.seimi.exception;
/*
   Copyright 2016 汪浩淼(Haomiao Wang)

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


/**
 * 启动初始化异常
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/26.
 */
public class SeimiInitExcepiton extends RuntimeException {
    public SeimiInitExcepiton(String msg){
        super(msg);
    }
}
