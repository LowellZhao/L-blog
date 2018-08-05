package com.lwz.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwz.constant.WebConst;
import com.lwz.dao.ContentVoMapper;
import com.lwz.dao.MetaVoMapper;
import com.lwz.dto.Types;
import com.lwz.exception.TipException;
import com.lwz.model.Vo.ContentVo;
import com.lwz.model.Vo.ContentVoExample;
import com.lwz.service.IContentService;
import com.lwz.service.IMetaService;
import com.lwz.service.IRelationshipService;
import com.lwz.utils.DateKit;
import com.lwz.utils.TaleUtils;
import com.lwz.utils.Tools;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ContentServiceImpl implements IContentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Resource
    private ContentVoMapper contentDao;

    @Resource
    private MetaVoMapper metaDao;

    @Resource
    private IRelationshipService relationshipService;

    @Resource
    private IMetaService metasService;

    /**
     * 保存文章
     * @param contents
     */
    @Override
    @Transactional
    public String publish(ContentVo contents) {
        if (null == contents) {
            return "文章对象为空";
        }
        if (StringUtils.isBlank(contents.getTitle())) {
            return "文章标题不能为空";
        }
        if (StringUtils.isBlank(contents.getContent())) {
            return "文章内容不能为空";
        }
        int titleLength = contents.getTitle().length();
        if (titleLength > WebConst.MAX_TITLE_COUNT) {
            return "文章标题过长";
        }
        int contentLength = contents.getContent().length();
        if (contentLength > WebConst.MAX_TEXT_COUNT) {
            return "文章内容过长";
        }
        if (null == contents.getAuthorId()) {
            return "请登录后发布文章";
        }
        // 如果写了文章访问路径
        if (StringUtils.isNotBlank(contents.getSlug())) {
            if (contents.getSlug().length() < 5) {
                return "路径太短了";
            }
            // 判断文章访问路径
            if (!TaleUtils.isPath(contents.getSlug())) return "您输入的路径不合法";
            ContentVoExample contentVoExample = new ContentVoExample();
            // 文章访问地址唯一性校验
            contentVoExample.createCriteria().andTypeEqualTo(contents.getType()).andStatusEqualTo(contents.getSlug());
            long count = contentDao.countByExample(contentVoExample);
            if (count > 0) return "该路径已经存在，请重新输入";
        } else {
            contents.setSlug(null);
        }

        // 把含有emoji表情的字符串中的emoji表情转成相应的aliasString result = EmojiParser.parseToAliases(name);
        // 把取得的别名转成对应的emoji表情name = EmojiParser.parseToUnicode(name);
        // https://www.aliyun.com/jiaocheng/786784.html
        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        int time = DateKit.getCurrentUnixTime();
        contents.setCreated(time);
        contents.setModified(time);
        // 点击次数
        contents.setHits(0);
        // 评论数
        contents.setCommentsNum(0);

        // 标签
        String tags = contents.getTags();
        // 分类
        String categories = contents.getCategories();
        // 保存文章
        contentDao.insert(contents);
        // 保存后cid会自动存入
        Integer cid = contents.getCid();
        // 关联文章的标签和
        metasService.saveMetas(cid, tags, Types.TAG.getType());
        metasService.saveMetas(cid, categories, Types.CATEGORY.getType());
        return WebConst.SUCCESS_RESULT;
    }

    /**
     * 更新文章
     * @param contents
     */
    @Override
    @Transactional
    public String updateArticle(ContentVo contents) {
        if (null == contents) {
            return "文章对象为空";
        }
        if (StringUtils.isBlank(contents.getTitle())) {
            return "文章标题不能为空";
        }
        if (StringUtils.isBlank(contents.getContent())) {
            return "文章内容不能为空";
        }
        int titleLength = contents.getTitle().length();
        if (titleLength > WebConst.MAX_TITLE_COUNT) {
            return "文章标题过长";
        }
        int contentLength = contents.getContent().length();
        if (contentLength > WebConst.MAX_TEXT_COUNT) {
            return "文章内容过长";
        }
        if (null == contents.getAuthorId()) {
            return "请登录后发布文章";
        }
        // 设置内容缩略名
        if (StringUtils.isBlank(contents.getSlug())) {
            contents.setSlug(null);
        }
        int time = DateKit.getCurrentUnixTime();
        // 设置修改时间
        contents.setModified(time);
        // 得到文章id
        Integer cid = contents.getCid();

        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        // 修改文章
        contentDao.updateByPrimaryKeySelective(contents);
        // 删除文章的关联内容
        relationshipService.deleteById(cid, null);
        // 保存文章的关联内容
        metasService.saveMetas(cid, contents.getTags(), Types.TAG.getType());
        metasService.saveMetas(cid, contents.getCategories(), Types.CATEGORY.getType());

        return WebConst.SUCCESS_RESULT;
    }

    /**
     * 分页查询文章或页面
     * @param commentVoExample
     * @param page
     * @param limit
     */
    @Override
    public PageInfo<ContentVo> getArticlesWithpage(ContentVoExample commentVoExample, Integer page, Integer limit) {
        // 使用插件分页
        PageHelper.startPage(page, limit);
        // 查询
        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(commentVoExample);
        return new PageInfo<>(contentVos);
    }

    /**
     * 根据文章id删除
     * @param cid
     */
    @Override
    @Transactional
    public String deleteByCid(Integer cid) {
        ContentVo contents = this.getContents(cid + "");
        if (null != contents) {
            contentDao.deleteByPrimaryKey(cid);
            relationshipService.deleteById(cid, null);
            return WebConst.SUCCESS_RESULT;
        }
        return "数据为空";
    }

    /**
     * 根据id或slug获取文章
     *
     * @param id id
     * @return ContentVo
     */
    @Override
    public ContentVo getContents(String id) {
        if (StringUtils.isNotBlank(id)) {
            if (Tools.isNumber(id)) {
                ContentVo contentVo = contentDao.selectByPrimaryKey(Integer.valueOf(id));
                return contentVo;
            } else {
                ContentVoExample contentVoExample = new ContentVoExample();
                contentVoExample.createCriteria().andSlugEqualTo(id);
                List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
                if (contentVos.size() != 1) {
                    throw new TipException("query content by id and return is not one");
                }
                return contentVos.get(0);
            }
        }
        return null;
    }

    @Override
    public PageInfo<ContentVo> getContents(Integer p, Integer limit) {
        LOGGER.debug("Enter getContents method");
        ContentVoExample example = new ContentVoExample();
        example.setOrderByClause("created desc");
        example.createCriteria().andTypeEqualTo(Types.ARTICLE.getType()).andStatusEqualTo(Types.PUBLISH.getType());
        PageHelper.startPage(p, limit);
        List<ContentVo> data = contentDao.selectByExampleWithBLOBs(example);
        PageInfo<ContentVo> pageInfo = new PageInfo<>(data);
        LOGGER.debug("Exit getContents method");
        return pageInfo;
    }



    @Override
    public void updateContentByCid(ContentVo contentVo) {
        if (null != contentVo && null != contentVo.getCid()) {
            contentDao.updateByPrimaryKeySelective(contentVo);
        }
    }

    @Override
    public PageInfo<ContentVo> getArticles(Integer mid, int page, int limit) {
        int total = metaDao.countWithSql(mid);
        PageHelper.startPage(page, limit);
        List<ContentVo> list = contentDao.findByCatalog(mid);
        PageInfo<ContentVo> paginator = new PageInfo<>(list);
        paginator.setTotal(total);
        return paginator;
    }

    @Override
    public PageInfo<ContentVo> getArticles(String keyword, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        ContentVoExample contentVoExample = new ContentVoExample();
        ContentVoExample.Criteria criteria = contentVoExample.createCriteria();
        criteria.andTypeEqualTo(Types.ARTICLE.getType());
        criteria.andStatusEqualTo(Types.PUBLISH.getType());
        criteria.andTitleLike("%" + keyword + "%");
        contentVoExample.setOrderByClause("created desc");
        List<ContentVo> contentVos = contentDao.selectByExampleWithBLOBs(contentVoExample);
        return new PageInfo<>(contentVos);
    }

    @Override
    public void updateCategory(String ordinal, String newCatefory) {
        ContentVo contentVo = new ContentVo();
        contentVo.setCategories(newCatefory);
        ContentVoExample example = new ContentVoExample();
        example.createCriteria().andCategoriesEqualTo(ordinal);
        contentDao.updateByExampleSelective(contentVo, example);
    }


}
