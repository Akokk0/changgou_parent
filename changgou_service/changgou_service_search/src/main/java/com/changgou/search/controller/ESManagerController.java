package com.changgou.search.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manage")
public class ESManagerController {

    @Autowired
    private ESManagerService esManagerService;

    @GetMapping("/create")
    public Result create() {
        esManagerService.createMappingAndIndex();
        return new Result(true, StatusCode.OK, "创建索引库结构成功！");
    }

    @GetMapping("/import/{id}")
    public Result importAll(@PathVariable("id") String id) {
        esManagerService.importDataBySpuId(id);
        return new Result(true, StatusCode.OK, "导入全部数据成功！");
    }
}
