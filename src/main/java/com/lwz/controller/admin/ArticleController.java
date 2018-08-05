package com.lwz.controller.admin;

import com.github.pagehelper.PageInfo;
import com.lwz.constant.WebConst;
import com.lwz.controller.BaseController;
import com.lwz.dto.LogActions;
import com.lwz.dto.Types;
import com.lwz.exception.TipException;
import com.lwz.model.Bo.RestResponseBo;
import com.lwz.model.Vo.ContentVo;
import com.lwz.model.Vo.ContentVoExample;
import com.lwz.model.Vo.MetaVo;
import com.lwz.model.Vo.UserVo;
import com.lwz.service.IContentService;
import com.lwz.service.ILogService;
import com.lwz.service.IMetaService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文章管理
 */
@Controller
@RequestMapping("/admin/article")
@Transactional(rollbackFor = TipException.class)
public class ArticleController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleController.class);

    @Resource
    private IContentService contentsService;

    @Resource
    private IMetaService metasService;

    @Resource
    private ILogService logService;

    /**
     * 跳转到文章管理页面
     * @param page
     * @param limit
     */
    @GetMapping(value = "")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "10") int limit, HttpServletRequest request) {
        ContentVoExample contentVoExample = new ContentVoExample();
        // 根据创建时间降序排列
        contentVoExample.setOrderByClause("created desc");
        // 查询post（文章）
        contentVoExample.createCriteria().andTypeEqualTo(Types.ARTICLE.getType());
        // 分页查询文章
        PageInfo<ContentVo> contentsPaginator = contentsService.getArticlesWithpage(contentVoExample, page, limit);
        // 保存查询结果
        request.setAttribute("articles", contentsPaginator);
        return "admin/article_list";
    }

    /**
     * 跳转到发布文章页面
     * @param request
     * @return
     */
    @GetMapping(value = "/publish")
    public String newArticle(HttpServletRequest request) {
        // 获取category类型的数据  文章分类
        List<MetaVo> categories = metasService.getMetas(Types.CATEGORY.getType());
        // 保存结果
        request.setAttribute("categories", categories);
        return "admin/article_edit";
    }

    /**
     * 发布文章
     * @param contents
     */
    @PostMapping(value = "/publish")
    @ResponseBody
    public RestResponseBo publishArticle(ContentVo contents, HttpServletRequest request) {
        UserVo users = this.user(request);
        // 获取session中的登陆用户id
        contents.setAuthorId(users.getUid());
        // 设置类型为post（文章）
        contents.setType(Types.ARTICLE.getType());
        if (StringUtils.isBlank(contents.getCategories())) {
            contents.setCategories("默认分类");
        }
        // 保存文章
        String result = contentsService.publish(contents);
        if (!WebConst.SUCCESS_RESULT.equals(result)) {
            return RestResponseBo.fail(result);
        }
        return RestResponseBo.ok();
    }

    /**
     * 根据文章id跳转到修改文章页面
     * @param cid
     * @param request
     * @return
     */
    @GetMapping(value = "/{cid}")
    public String editArticle(@PathVariable String cid, HttpServletRequest request) {
        // 根据id获取文章类容
        ContentVo contents = contentsService.getContents(cid);
        // 保存文章内容
        request.setAttribute("contents", contents);
        // 获取文章类型
        List<MetaVo> categories = metasService.getMetas(Types.CATEGORY.getType());
        // 保存文章类型
        request.setAttribute("categories", categories);
        request.setAttribute("active", "article");
        return "admin/article_edit";
    }

    /**
     * 修改文章内容
     * @param contents
     */
    @PostMapping(value = "/modify")
    @ResponseBody
    public RestResponseBo modifyArticle(ContentVo contents, HttpServletRequest request) {
        // 获取当前用户信息
        UserVo users = this.user(request);
        // 给当前文章作者赋值
        contents.setAuthorId(users.getUid());
        // 设置类型为post
        contents.setType(Types.ARTICLE.getType());
        // 更新文章
        String result = contentsService.updateArticle(contents);
        if (!WebConst.SUCCESS_RESULT.equals(result)) {
            return RestResponseBo.fail(result);
        }
        return RestResponseBo.ok();
    }

    /**
     * 根据id删除文章
     * @param cid
     * @param request
     * @return
     */
    @RequestMapping(value = "/delete")
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
