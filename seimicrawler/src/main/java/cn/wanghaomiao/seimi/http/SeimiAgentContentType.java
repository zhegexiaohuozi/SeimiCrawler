/*
   Copyright 2015 Wang Haomiao<seimimaster@gmail.com>

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
package cn.wanghaomiao.seimi.http;

/**
 * 用于指定SeimiAgent处理请求返回的内容的数据格式
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2016/6/26.
 */
public enum SeimiAgentContentType {
    /**
     * 向SeimiAgent请求返回内容为HTML
     */
    HTML(1,"html"),
    /**
     * 向SeimiAgent请求返回内容为图片，实际图片格式为png
     */
    IMG(2,"img"),
    /**
     * 向SeimiAgent请求返回内容为PDF
     */
    PDF(3,"pdf");
    private int val;
    private String seimiAgentType;
    SeimiAgentContentType(int val,String typeStr){
        this.val = val;
        this.seimiAgentType = typeStr;
    }
    public int val(){
        return this.val;
    }

    public String typeVal(){
        return seimiAgentType;
    }
}
