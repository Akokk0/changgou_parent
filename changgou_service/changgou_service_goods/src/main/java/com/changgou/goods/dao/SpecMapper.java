package com.changgou.goods.dao;

import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface SpecMapper extends Mapper<Spec> {

    /***
     * 根据分类查询规格参数
     * @return
     */
    @Select("select name, options from tb_spec where template_id in(select template_id from tb_category where name = #{categoryName})")
    List<Map> findSpecListByCategory(@Param("categoryName") String categoryName);

}
