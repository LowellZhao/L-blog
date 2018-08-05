package com.lwz.service.impl;

import com.lwz.constant.WebConst;
import com.lwz.dao.MetaVoMapper;
import com.lwz.dto.MetaDto;
import com.lwz.dto.Types;
import com.lwz.exception.TipException;
import com.lwz.model.Vo.ContentVo;
import com.lwz.model.Vo.MetaVo;
import com.lwz.model.Vo.MetaVoExample;
import com.lwz.model.Vo.RelationshipVoKey;
import com.lwz.service.IContentService;
import com.lwz.service.IMetaService;
import com.lwz.service.IRelationshipService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签、分类
 */
@Service
public class MetaServiceImpl implements IMetaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaServiceImpl.class);

    @Resource
    private MetaVoMapper metaDao;

    @Resource
    private IRelationshipService relationshipService;

    @Resource
    private IContentService contentService;

    @Override
    public MetaDto getMeta(String type, String name) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {
            return metaDao.selectDtoByNameAndType(name, type);
        }
        return null;
    }

    @Override
    public Integer countMeta(Integer mid) {
        return metaDao.countWithSql(mid);
    }

    /**
     * 根据类型查询项目列表
     * @param types
     */
    @Override
    public List<MetaVo> getMetas(String types) {
        if (StringUtils.isNotBlank(types)) {
            MetaVoExample metaVoExample = new MetaVoExample();
            metaVoExample.setOrderByClause("sort desc, mid desc");
            metaVoExample.createCriteria().andTypeEqualTo(types);
            return metaDao.selectByExample(metaVoExample);
        }
        return null;
    }

    /**
     * 根据类型查询项目列表，带项目下面的文章数
     */
    @Override
    public List<MetaDto> getMetaList(String type, String orderby, int limit) {
        if (StringUtils.isNotBlank(type)) {
            if (StringUtils.isBlank(orderby)) {
                // 根据数量降序，关联id降序
                orderby = "count desc, a.mid desc";
            }
            if (limit < 1 || limit > WebConst.MAX_POSTS) {
                limit = 10;
            }
            Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("type", type);
            paraMap.put("order", orderby);
            paraMap.put("limit", limit);
            return metaDao.selectFromSql(paraMap);
        }
        return null;
    }

    /**
     * 删除项目
     * @param mid
     */
    @Override
    @Transactional
    public void delete(int mid) {
        // 根据主键查询meta
        MetaVo metas = metaDao.selectByPrimaryKey(mid);
        if (null != metas) {
            String type = metas.getType();
            String name = metas.getName();
            // 删除meta
            metaDao.deleteByPrimaryKey(mid);
            // 根据mid查询关联
            List<RelationshipVoKey> rlist = relationshipService.getRelationshipById(null, mid);
            if (null != rlist) {
                for (RelationshipVoKey r : rlist) {
                    // 根据cid查询文章
                    ContentVo contents = contentService.getContents(String.valueOf(r.getCid()));
                    if (null != contents) {
                        ContentVo temp = new ContentVo();
                        temp.setCid(r.getCid());
                        // 如果是类型
                        if (type.equals(Types.CATEGORY.getType())) {
                            // 去除文章中的该类型
                            temp.setCategories(reMeta(name, contents.getCategories()));
                        }
                        // 如果是标签
                        if (type.equals(Types.TAG.getType())) {
                            // 去除文章中的该标签
                            temp.setTags(reMeta(name, contents.getTags()));
                        }
                        // 更新文章信息
                        contentService.updateContentByCid(temp);
                    }
                }
            }
            // 删除关联信息中的mid相关的信息
            relationshipService.deleteById(null, mid);
        }
    }

    /**
     * 保存项目
     * @param type
     * @param name
     * @param mid
     */
    @Override
    @Transactional
    public void saveMeta(String type, String name, Integer mid) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(name)) {
            MetaVoExample metaVoExample = new MetaVoExample();
            metaVoExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
            // 查询是否有相同值
            List<MetaVo> metaVos = metaDao.selectByExample(metaVoExample);
            MetaVo metas;
            if (metaVos.size() != 0) {
                throw new TipException("已经存在该项");
            } else {
                metas = new MetaVo();
                metas.setName(name);
                // 已经存在id，更新项目
                if (null != mid) {
                    MetaVo original = metaDao.selectByPrimaryKey(mid);
                    metas.setMid(mid);
                    // 更新项目信息
                    metaDao.updateByPrimaryKeySelective(metas);
                    // 更新原有文章的categories（分类名称）
                    contentService.updateCategory(original.getName(), name);
                } else {
                    // 不存在id,新增项目
                    metas.setType(type);
                    metaDao.insertSelective(metas);
                }
            }
        }
    }

    /**
     * 保存多个项目
     * @param cid   文章id
     * @param names 标签
     * @param type  分类
     */
    @Override
    @Transactional
    public void saveMetas(Integer cid, String names, String type) {
        if (null == cid) {
            throw new TipException("项目关联id不能为空");
        }
        if (StringUtils.isNotBlank(names) && StringUtils.isNotBlank(type)) {
            String[] nameArr = StringUtils.split(names, ",");
            for (String name : nameArr) {
                this.saveOrUpdate(cid, name, type);
            }
        }
    }

    /**
     * 保存或者修改
     * @param cid
     * @param name
     * @param type
     */
    private void saveOrUpdate(Integer cid, String name, String type) {
        MetaVoExample metaVoExample = new MetaVoExample();
        metaVoExample.createCriteria().andTypeEqualTo(type).andNameEqualTo(name);
        List<MetaVo> metaVos = metaDao.selectByExample(metaVoExample);

        int mid;
        MetaVo metas;
        if (metaVos.size() == 1) {
            metas = metaVos.get(0);
            mid = metas.getMid();
        } else if (metaVos.size() > 1) {
            throw new TipException("查询到多条数据");
        } else {
            metas = new MetaVo();
            metas.setSlug(name);
            metas.setName(name);
            metas.setType(type);
            metaDao.insertSelective(metas);
            mid = metas.getMid();
        }
        if (mid != 0) {
            Long count = relationshipService.countById(cid, mid);
            if (count == 0) {
                RelationshipVoKey relationships = new RelationshipVoKey();
                relationships.setCid(cid);
                relationships.setMid(mid);
                relationshipService.insertVo(relationships);
            }
        }
    }

    /**
     * 去除文章的分类中 name字段
     * @param name
     * @param metas
     * @return
     */
    private String reMeta(String name, String metas) {
        String[] ms = StringUtils.split(metas, ",");
        StringBuilder sbuf = new StringBuilder();
        for (String m : ms) {
            if (!name.equals(m)) {
                sbuf.append(",").append(m);
            }
        }
        if (sbuf.length() > 0) {
            // 去除第一个 ,
            return sbuf.substring(1);
        }
        return "";
    }

    @Override
    @Transactional
    public void saveMeta(MetaVo metas) {
        if (null != metas) {
            metaDao.insertSelective(metas);
        }
    }

    @Override
    @Transactional
    public void update(MetaVo metas) {
        if (null != metas && null != metas.getMid()) {
            metaDao.updateByPrimaryKeySelective(metas);
        }
    }
}
