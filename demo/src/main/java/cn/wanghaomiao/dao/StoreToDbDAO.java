package cn.wanghaomiao.dao;

import cn.wanghaomiao.model.BlogContent;
import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.ReturnGeneratedKeys;
import net.paoding.rose.jade.annotation.SQL;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/30.
 */
@DAO
public interface StoreToDbDAO {
    @ReturnGeneratedKeys
    @SQL("insert into blog (title,content,update_time) values (:1.title,:1.content,now())")
    public int save(BlogContent blog);
}
