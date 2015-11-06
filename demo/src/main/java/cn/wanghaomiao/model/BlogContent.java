package cn.wanghaomiao.model;

import cn.wanghaomiao.seimi.annotation.Xpath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Xpath语法可以参考 http://jsoupxpath.wanghaomiao.cn/
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/10/27.
 */
public class BlogContent {
    @Xpath("//h1[@class='postTitle']/a/text()|//a[@id='cb_post_title_url']/text()")
    private String title;
    //也可以这么写 @Xpath("//div[@id='cnblogs_post_body']//text()")
    @Xpath("//div[@id='cnblogs_post_body']/allText()")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        if (StringUtils.isNotBlank(content)&&content.length()>100){
            //方便查看截断下
            this.content = StringUtils.substring(content,0,100)+"...";
        }
        return ToStringBuilder.reflectionToString(this);
    }
}
