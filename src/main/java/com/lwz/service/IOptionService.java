package com.lwz.service;


import com.lwz.model.Vo.OptionVo;

import java.util.List;
import java.util.Map;

/**
 * options的接口
 */
public interface IOptionService {

    void insertOption(OptionVo optionVo);

    void insertOption(String name, String value);

    /**
     * 查询所有系统设置信息
     */
    List<OptionVo> getOptions();

    /**
     * 保存一组配置
     *
     * @param options
     */
    void saveOptions(Map<String, String> options);

    OptionVo getOptionByName(String name);
}
