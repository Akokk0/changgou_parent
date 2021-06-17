package com.changgou.search.service;

public interface ESManagerService {

    void createMappingAndIndex();

    void importAll();

    void importDataBySpuId(String spuId);

    void delDataBySpuId(String spuId);

}
