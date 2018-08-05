package com.lwz.controller.admin;


import com.lwz.constant.WebConst;
import com.lwz.controller.BaseController;
import com.lwz.dto.LogActions;
import com.lwz.exception.TipException;
import com.lwz.model.Bo.BackResponseBo;
import com.lwz.model.Bo.RestResponseBo;
import com.lwz.model.Vo.OptionVo;
import com.lwz.service.ILogService;
import com.lwz.service.IOptionService;
import com.lwz.service.ISiteService;
import com.lwz.utils.GsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置管理
 */
@Controller
@RequestMapping("/admin/setting")
public class SettingController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingController.class);

    @Resource
    private IOptionService optionService;

    @Resource
    private ILogService logService;

    @Resource
    private ISiteService siteService;

    /**
     * 跳转系统设置
     */
    @GetMapping(value = "")
    public String setting(HttpServletRequest request) {
        // 查询系统设置信息
        List<OptionVo> voList = optionService.getOptions();
        Map<String, String> options = new HashMap<>();
        voList.forEach((option) -> {
            options.put(option.getName(), option.getValue());
        });
        if (options.get("site_record") == null) {
            options.put("site_record", "");
        }
        // 页面传系统设置
        request.setAttribute("options", options);
        return "admin/setting";
    }

    /**
     * 保存系统设置
     */
    @PostMapping(value = "")
    @ResponseBody
    public RestResponseBo saveSetting(@RequestParam(required = false) String site_theme, HttpServletRequest request) {
        try {
            // request.getParameterMap()返回的是一个Map类型的值，
            // 该返回值记录着前端（如jsp页面）所提交请求中的请求参数和请求参数值的映射关系。
            // 这个返回值有个特别之处——只能读
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> querys = new HashMap<>();
            parameterMap.forEach((key, value) -> {
                querys.put(key, join(value));
            });
            // 保存一组配置
            optionService.saveOptions(querys);
            // 跟新系统配置
            WebConst.initConfig = querys;
            // 系统主题
            if (StringUtils.isNotBlank(site_theme)) {
                BaseController.THEME = "themes/" + site_theme;
            }
            // 添加系统日志
            logService.insertLog(LogActions.SYS_SETTING.getAction(), GsonUtils.toJsonString(querys), request.getRemoteAddr(), this.getUid(request));
            return RestResponseBo.ok();
        } catch (Exception e) {
            String msg = "保存设置失败";
            return RestResponseBo.fail(msg);
        }
    }


    /**
     * 系统备份
     *
     * @return
     */
    @PostMapping(value = "backup")
    @ResponseBody
    public RestResponseBo backup(@RequestParam String bk_type,
                                 @RequestParam String bk_path,
                                 HttpServletRequest request) {
//        if (StringUtils.isBlank(bk_type)) {
//            return RestResponseBo.fail("请确认信息输入完整");
//        }
//        try {
//            BackResponseBo backResponse = siteService.backup(bk_type, bk_path, "yyyyMMddHHmm");
//            logService.insertLog(LogActions.SYS_BACKUP.getAction(), null, request.getRemoteAddr(), this.getUid(request));
//            return RestResponseBo.ok(backResponse);
//        } catch (Exception e) {
//            String msg = "备份失败";
//            if (e instanceof TipException) {
//                msg = e.getMessage();
//            } else {
//                LOGGER.error(msg, e);
//            }
//            return RestResponseBo.fail(msg);
//        }
        return RestResponseBo.fail("还没有实现");
    }


    /**
     * 数组转字符串
     *
     * @param arr
     * @return
     */
    private String join(String[] arr) {
        StringBuilder ret = new StringBuilder();
        String[] var3 = arr;
        int var4 = arr.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String item = var3[var5];
            ret.append(',').append(item);
        }

        return ret.length() > 0 ? ret.substring(1) : ret.toString();
    }

}
