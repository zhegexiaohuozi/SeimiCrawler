package cn.wanghaomiao.seimi.utils;

import cn.wanghaomiao.seimi.core.SeimiDownloader;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.struct.CrawlerModel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 汪浩淼  github.com/zhegexiaohuozi et.tw@163.com
 * @since 2023/4/6.
 */
public class ClazzUtils {
    private static final ConcurrentHashMap<Class<?>, SeimiDownloader> instanceCache = new ConcurrentHashMap<>();
    private static final ThreadLocal<CrawlerModel> localCache = new ThreadLocal<>();

    public static SeimiDownloader getInstance(Class<? extends SeimiDownloader> clazz){
        SeimiDownloader downloader = instanceCache.get(clazz);
        if (downloader == null){
            try {
                downloader = clazz.newInstance();
                instanceCache.put(clazz, downloader);
            } catch (Exception e) {
                throw new SeimiProcessExcepiton(e);
            }
        }
        return downloader;
    }

    public static CrawlerModel currentCModel(){
        return localCache.get();
    }

    public static void setCurrentCModel(CrawlerModel model){
        localCache.set(model);
    }
}
