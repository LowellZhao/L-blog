package com.lwz.controller.admin;

import com.github.pagehelper.PageInfo;
import com.lwz.constant.WebConst;
import com.lwz.controller.BaseController;
import com.lwz.dto.LogActions;
import com.lwz.dto.Types;
import com.lwz.model.Bo.RestResponseBo;
import com.lwz.model.Vo.ContentVo;
import com.lwz.model.Vo.ContentVoExample;
import com.lwz.model.Vo.UserVo;
import com.lwz.service.IContentService;
import com.lwz.service.ILogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 后台，页面管理
 */
@Controller()
@RequestMapping("admin/page")
public class PageController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageController.class);

    @Resource
    private IContentService contentsService;

    @Resource
    private ILogService logService;

    /**
     * 跳转到页面管理页面
     * @param request
     * @return
     */
    @GetMapping(value = "")
    public String index(HttpServletRequest request) {
        ContentVoExample contentVoExample = new ContentVoExample();
        contentVoExample.setOrderByClause("created desc");
        // 添加页面查询条件
        contentVoExample.createCriteria().andTypeEqualTo(Types.PAGE.getType());
        // 分页查询页面
        PageInfo<ContentVo> contentsPaginator = contentsService.getArticlesWithpage(contentVoExample, 1, WebConst.MAX_POSTS);
        // 页面赋值
        request.setAttribute("articles", contentsPaginator);
        return "admin/page_list";
    }

    /**
     * 跳转到新增页面
     * @param request
     * @return
     */
    @GetMapping(value = "new")
    public String newPage(HttpServletRequest request) {
        return "admin/page_edit";
    }

    /**
     * 根据id跳转到修改页面
     * @param cid
     * @param request
     * @return
     */
    @GetMapping(value = "/{cid}")
    public String editPage(@PathVariable String cid, HttpServletRequest request) {
        // 查询id页面信息
        ContentVo contents = contentsService.getContents(cid);
        // 页面信息传值
        request.setAttribute("contents", contents);
        return "admin/page_edit";
    }

    /**
     * 保存页面信息
     * @param title         标题
     * @param content       内容
     * @param status        状态
     * @param slug
     * @param allowComment
     * @param allowPing
     */
    @PostMapping(value = "publish")
    @ResponseBody
    public RestResponseBo publishPage(@RequestParam String title, @RequestParam String content,
                                      @RequestParam String status, @RequestParam String slug,
                                      @RequestParam(required = false) Integer allowComment,
                                      @RequestParam(required = false) Integer allowPing,
                                      HttpServletRequest request) {
        // 获取当前用户信息
        UserVo users = this.user(request);
        ContentVo contents = new ContentVo();
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        // 设置类型为page
        contents.setType(Types.PAGE.getType());
//        if (null != allowComment) {
//            contents.setAllowComment(allowComment == 1);
//        }
//        if (null != allowPing) {
//            contents.setAllowPing(allowPing == 1);
//        }
        contents.setAllowComment(true);
        contents.setAllowPing(true);
        contents.setAuthorId(users.getUid());
        // 保存页面
        String result = contentsService.publish(contents);
        if (!WebConst.SUCCESS_RESULT.equals(result)) {
            return RestResponseBo.fail(result);
        }
        return RestResponseBo.ok();
    }

    /**
     * 更新页面
     * @param cid
     * @param title
     * @param content
     * @param status
     * @param slug
     * @param allowComment
     * @param allowPing
     */
    @PostMapping(value = "modify")
    @ResponseBody
    public RestResponseBo modifyArticle(@RequestParam Integer cid, @RequestParam String title,
                                        @RequestParam String content,
                                        @RequestParam String status, @RequestParam String slug,
                                        @RequestParam(required = false) Integer allowComment,
                                        @RequestParam(required = false) Integer allowPing,
                                        HttpServletRequest request) {
        UserVo users = this.user(request);
        ContentVo contents = new ContentVo();
        contents.setCid(cid);
        contents.setTitle(title);
        contents.setContent(content);
        contents.setStatus(status);
        contents.setSlug(slug);
        contents.setType(Types.PAGE.getType());
        if (null != allowComment) {
            contents.setAllowComment(allowComment == 1);
        }
        if (null != allowPing) {
            contents.setAllowPing(allowPing == 1);
        }
        contents.setAuthorId(users.getUid());
        // 更新页面
        String result = contentsService.updateArticle(contents);
        if (!WebConst.SUCCESS_RESULT.equals(result)) {
            return RestResponseBo.fail(result);
        }
        return RestResponseBo.ok();
    }

    /**
     * 更具id删除页面
     * @param cid
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public RestResponseBo delete(@RequestParam int cid, HttpServletRequest request) {
        // 删除文章
        String result = contentsService.deleteByCid(cid);
        // 增加日志
        logService.insertLog(LogActions.DEL_ARTICLE.getAction(), cid + "", request.getRemoteAddr(), this.getUid(request));
        if (!WebConst.SUCCESS_RESULT.equals(result)) {
            return RestResponseBo.fail(result);
        }
        return RestResponseBo.ok();
    }
}
