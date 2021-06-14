package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.sql.DatabaseMetaData;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param goods
     */
    @Override
    @Transactional
    public void add(Goods goods){
        // 获取spu
        Spu spu = goods.getSpu();
        // 设置分布式id
        spu.setId(String.valueOf(idWorker.nextId()));
        // 设置各种状态
        spu.setIsDelete("0");
        spu.setIsMarketable("0");
        spu.setStatus("0");
        // 将spu添加到数据库
        spuMapper.insertSelective(spu);
        // 将sku存入数据库
        saveSkuList(goods);
    }

    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    @Transactional
    public void delete(String id){
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断spu是否存在
        if (spu == null) {
            throw new RuntimeException("商品不存在！");
        }
        //判断spu是否处于删除状态
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("商品已删除！");
        }
        //判断spu是否处于下架状态
        if ("1".equals(spu.getIsMarketable())) {
            throw new RuntimeException("商品未下架！");
        }
        //设置商品状态
        spu.setIsDelete("1");
        spu.setStatus("0");
        //存入数据库
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    /***
     * 根据ID查询对应商品
     * @param id
     * @return
     */
    @Override
    public Goods findGoodsById(String id) {
        //创建Goods对象
        Goods goods = new Goods();
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        goods.setSpu(spu);
        //查询sku
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        List<Sku> skuList = skuMapper.selectByExample(example);
        goods.setSkuList(skuList);
        return goods;
    }

    /***
     * 修改商品信息
     * @param goods
     */
    @Override
    @Transactional
    public void update(Goods goods) {
        //更新spu
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKeySelective(spu);
        //更新skuList
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        skuMapper.deleteByExample(example); //删除原先的skuList
        saveSkuList(goods);
    }

    /***
     * 审核
     * @param id
     */
    @Override
    public void audit(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断是否存在
        if (spu == null) {
            throw new RuntimeException("商品不存在！");
        }
        //判断商品删除状态
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("商品已被删除！");
        }
        //设置商品审核状态并自动上架
        spu.setStatus("1");
        spu.setIsMarketable("1");
        //将修改内容存入数据
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 下架商品
     * @param id
     */
    @Override
    @Transactional
    public void pull(String id) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断商品是否存在
        if (spu == null) {
            throw new RuntimeException("商品不存在！");
        }
        //判断商品是否被删除
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("商品被删除！");
        }
        //修改商品上下架状态
        spu.setIsMarketable("0");
        //将修改信息存入数据库
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 上架商品
     * @param id
     */
    @Override
    @Transactional
    public void put(String id) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断商品是否存在
        if (spu == null) {
            throw new RuntimeException("商品不存在！");
        }
        //判断商品是否被删除
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("商品被删除！");
        }
        //判断商品是否经过审核
        if (!"1".equals(spu.getStatus())) {
            throw new RuntimeException("商品未审核！");
        }
        //修改商品上下架状态
        spu.setIsMarketable("1");
        //将修改信息存入数据库
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 恢复数据
     * @param id
     */
    @Override
    @Transactional
    public void restore(String id) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断商品是否存在
        if (spu == null) {
            throw new RuntimeException("商品不存在！");
        }
        //判断商品是否被删除
        if (!"1".equals(spu.getIsDelete())) {
            throw new RuntimeException("商品未删除！");
        }
        //修改商品状态
        spu.setIsDelete("0");
        spu.setStatus("0");
        //将修改信息存入数据库
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 物理删除
     * @param id
     */
    @Override
    @Transactional
    public void realDelete(String id) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断商品是否存在
        if (spu == null) {
            throw new RuntimeException("商品不存在！");
        }
        //检查是否删除的商品
        if (!spu.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品未删除！");
        }
        spuMapper.deleteByPrimaryKey(id);
    }

    /***
     * 存入sku
     * @param goods
     */
    private void saveSkuList(Goods goods) {
        //获取spu对象
        Spu spu = goods.getSpu();
        //当前日期
        Date date = new Date();
        //获取品牌对象
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        //获取分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //绑定品牌分类关系
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setCategoryId(spu.getCategory3Id());
        categoryBrand.setBrandId(spu.getBrandId());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count <= 0) {
            categoryBrandMapper.insertSelective(categoryBrand);
        }
        //获取sku集合对象
        List<Sku> skuList = goods.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            for (Sku sku : skuList) {
                //设置sku主键ID
                sku.setId(String.valueOf(idWorker.nextId()));
                //设置sku规格
                if (StringUtils.isEmpty(sku.getSpec())) {
                    sku.setSpec("{ }");
                }
                //设置sku名称(商品名称 + 规格)
                String name = sku.getName();
                //将规格json字符串转换成Map
                Map<String, String> map = JSON.parseObject(sku.getSpec(), Map.class);
                if (map != null && map.size() > 0) {
                    for (String value : map.values()) {
                        name += name + " " + value;
                    }
                }
                sku.setName(name);//名称
                sku.setSpuId(spu.getId());//设置spu的ID
                sku.setCreateTime(date);//创建日期
                sku.setUpdateTime(date);//修改日期
                sku.setCategoryId(category.getId());//商品分类ID
                sku.setCategoryName(category.getName());//商品分类名称
                sku.setBrandName(brand.getName());//品牌名称
                skuMapper.insertSelective(sku);//插入sku表数据
            }
        }
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
