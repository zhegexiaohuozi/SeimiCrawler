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
package cn.wanghaomiao.seimi.struct;


import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2015/5/31.
 */
public class CommonObject implements Serializable {
    private static final long serialVersionUID = -3239260503197729552L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
