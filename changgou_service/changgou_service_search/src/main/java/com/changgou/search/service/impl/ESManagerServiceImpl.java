package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.ESManagerMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ESManagerServiceImpl implements ESManagerService {

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private ESManagerMapper esManagerMapper;

    @Override
    public void createMappingAndIndex() {
        //创建索引
        template.createIndex(SkuInfo.class);
        //创建映射
        template.putMapping(SkuInfo.class);
    }

    @Override
    public void importAll() {
        List<Sku> skuList = skuFeign.findSkuListBySpuId("all");
        if (skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("当前没有查询到数据，无法导入索引库");
        }
        //转换为json字符串
        String jsonSkuList = JSON.toJSONString(skuList);
        //将json转换为skuInfo
        List<SkuInfo> skuInfoList = JSON.parseArray(jsonSkuList, SkuInfo.class);

        for (SkuInfo skuInfo : skuInfoList) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //导入索引库
        esManagerMapper.saveAll(skuInfoList);
    }

    @Override
    public void importDataBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        if (skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("当前没有查询到数据，无法导入索引库");
        }
        //转换为JSON字符串
        String jsonSkuList = JSON.toJSONString(skuList);
        //将JSON字符串转换为SkuInfo集合
        List<SkuInfo> skuInfoList = JSON.parseArray(jsonSkuList, SkuInfo.class);

        for (SkuInfo skuInfo : skuInfoList) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //导入索引库
        esManagerMapper.saveAll(skuInfoList);
    }

    @Override
    public void delDataBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        if (skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("当前没有查询到数据，无法从索引库删除");
        }
        for (Sku sku : skuList) {
            esManagerMapper.deleteById(Long.parseLong(sku.getId()));
        }
    }
}
