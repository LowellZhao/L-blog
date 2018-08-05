package com.lwz.controller.admin;


import com.github.pagehelper.PageInfo;
import com.lwz.constant.WebConst;
import com.lwz.controller.BaseController;
import com.lwz.dto.LogActions;
import com.lwz.dto.Types;
import com.lwz.model.Bo.RestResponseBo;
import com.lwz.model.Vo.AttachVo;
import com.lwz.model.Vo.UserVo;
import com.lwz.service.IAttachService;
import com.lwz.service.ILogService;
import com.lwz.utils.Commons;
import com.lwz.utils.TaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 附件管理
 * <p>
 */
@Controller
@RequestMapping("admin/attach")
public class AttachController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachController.class);

    public static final String CLASSPATH = TaleUtils.getUploadFilePath2();

    @Resource
    private IAttachService attachService;

    @Resource
    private ILogService logService;

    /**
     * 跳转附件页面
     *
     * @param request
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = "")
    public String index(HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "12") int limit) {
        // 查询附件信息
        PageInfo<AttachVo> attachPaginator = attachService.getAttachs(page, limit);
        // 前台保存附件信息
        request.setAttribute("attachs", attachPaginator);
        // 附件存放的URL
        request.setAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType(), Commons.site_url()));
        // 文件最大的大小
        request.setAttribute("max_file_size", WebConst.MAX_FILE_SIZE / 1024);
        return "admin/attach";
    }

    /**
     * 上传文件接口
     *
     * @param request
     * @return
     */
    @PostMapping(value = "upload")
    @ResponseBody
    public RestResponseBo upload(HttpServletRequest request,
                                 @RequestParam("file") MultipartFile[] multipartFiles) throws IOException {
        UserVo users = this.user(request);
        Integer uid = users.getUid();
        List<String> errorFiles = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                // 获取上传文件名称
                String fname = multipartFile.getOriginalFilename();
                // 判断上传文件大小
                if (multipartFile.getSize() <= WebConst.MAX_FILE_SIZE) {
                    String fkey = TaleUtils.getFileKey(fname);
                    String ftype = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
                    File file = new File(CLASSPATH + fkey);
                    try {
                        // 复制文件
                        FileCopyUtils.copy(multipartFile.getInputStream(), new FileOutputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 保存附件
                    attachService.save(fname, fkey, ftype, uid);
                } else {
                    // 添加没有保存的文件名
                    errorFiles.add(fname);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("文件上传失败");
            return RestResponseBo.fail();
        }
        return RestResponseBo.ok(errorFiles);
    }

    /**
     * 删除文件接口
     * @param id
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public RestResponseBo delete(@RequestParam Integer id, HttpServletRequest request) {
        try {
            AttachVo attach = attachService.selectById(id);
            if (null == attach) {
                return RestResponseBo.fail("不存在该附件");
            }
            attachService.deleteById(id);
            new File(CLASSPATH + attach.getFkey()).delete();
            logService.insertLog(LogActions.DEL_ARTICLE.getAction(), attach.getFkey(), request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e) {
            String msg = "附件删除失败";
            LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

}
