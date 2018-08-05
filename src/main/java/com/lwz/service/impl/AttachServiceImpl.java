package com.lwz.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwz.dao.AttachVoMapper;
import com.lwz.model.Vo.AttachVo;
import com.lwz.model.Vo.AttachVoExample;
import com.lwz.service.IAttachService;
import com.lwz.utils.DateKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 附件业务处理
 */
@Service
public class AttachServiceImpl implements IAttachService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachServiceImpl.class);

    @Resource
    private AttachVoMapper attachDao;

    /**
     * 分页查询附件
     * @param page
     * @param limit
     * @return
     */
    @Override
    public PageInfo<AttachVo> getAttachs(Integer page, Integer limit) {
        // Mybatis 分页插件
        PageHelper.startPage(page, limit);
        AttachVoExample attachVoExample = new AttachVoExample();
        // 主键降序查询
        attachVoExample.setOrderByClause("id desc");
        // 查询附件
        List<AttachVo> attachVos = attachDao.selectByExample(attachVoExample);
        return new PageInfo<>(attachVos);
    }

    @Override
    public AttachVo selectById(Integer id) {
        if(null != id){
            return attachDao.selectByPrimaryKey(id);
        }
        return null;
    }

    /**
     * 保存附件
     *
     * @param fname
     * @param fkey
     * @param ftype
     * @param author
     */
    @Override
    @Transactional
    public void save(String fname, String fkey, String ftype, Integer author) {
        AttachVo attach = new AttachVo();
        attach.setFname(fname);
        attach.setAuthorId(author);
        attach.setFkey(fkey);
        attach.setFtype(ftype);
        attach.setCreated(DateKit.getCurrentUnixTime());
        attachDao.insertSelective(attach);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (null != id) {
            attachDao.deleteByPrimaryKey( id);
        }
    }
}
