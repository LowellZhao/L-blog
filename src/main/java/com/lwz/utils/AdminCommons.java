package com.lwz.utils;


import com.lwz.model.Vo.MetaVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 后台公共函数
 */
@Component
public final class AdminCommons {

    /**
     * 判断category和cat的交集  article_edit.html 88
     *
     * @param cats
     * @return
     */
    public static boolean exist_cat(MetaVo category, String cats) {
        String[] arr = StringUtils.split(cats, ",");
        if (null != arr && arr.length > 0) {
            for (String c : arr) {
                if (c.trim().equals(category.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final String[] COLORS = {"default", "primary", "success", "info", "warning", "danger", "inverse", "purple", "pink"};

    /**
     * 随机颜色，html页面使用
     * @return
     */
    public static String rand_color() {
        int r = Tools.rand(0, COLORS.length - 1);
        return COLORS[r];
    }

}
