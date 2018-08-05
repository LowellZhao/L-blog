package com.lwz.controller.admin;

import com.github.pagehelper.PageInfo;
import com.lwz.controller.BaseController;
import com.lwz.model.Bo.RestResponseBo;
import com.lwz.model.Vo.CommentVo;
import com.lwz.model.Vo.CommentVoExample;
import com.lwz.model.Vo.UserVo;
import com.lwz.service.ICommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 评论管理
 */
@Controller
@RequestMapping("admin/comments")
public class CommentController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);

    @Resource
    private ICommentService commentsService;

    /**
     * 跳转到评论管理页面
     * @param page
     * @param limit
     */
    @GetMapping(value = "")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "15") int limit, HttpServletRequest request) {
        UserVo users = this.user(request);
        CommentVoExample commentVoExample = new CommentVoExample();
        // 根据自增编号降序
        commentVoExample.setOrderByClause("coid desc");
        // 不查询用户自己的评论
        commentVoExample.createCriteria().andAuthorIdNotEqualTo(users.getUid());
        // 查询评论
        PageInfo<CommentVo> commentsPaginator = commentsService.getCommentsWithPage(commentVoExample, page, limit);
        // 向前台传值
        request.setAttribute("comments", commentsPaginator);
        return "admin/comment_list";
    }

    /**
     * 删除一条评论
     * @param coid 自增id
     * @return
     */
    @PostMapping(value = "delete")
    @ResponseBody
    public RestResponseBo delete(@RequestParam Integer coid) {
        try {
            CommentVo comments = commentsService.getCommentById(coid);
            if (null == comments) {
                return RestResponseBo.fail("不存在该评论");
            }
            commentsService.delete(coid, comments.getCid());
        } catch (Exception e) {
            String msg = "评论删除失败";
            LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

    /**
     * 修改评论状态
     * @param coid      主键
     * @param status    状态
     */
    @PostMapping(value = "status")
    @ResponseBody
    public RestResponseBo delete(@RequestParam Integer coid, @RequestParam String status) {
        try {
            // 获取评论
            CommentVo comments = commentsService.getCommentById(coid);
            if (comments != null) {
                comments.setCoid(coid);
                comments.setStatus(status);
                // 修改评论信息
                commentsService.update(comments);
            } else {
                return RestResponseBo.fail("操作失败");
            }
        } catch (Exception e) {
            String msg = "操作失败";
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

}
