package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.utils.JSONUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "品牌接口")
public interface BrandService {

    @ApiOperation(value = "获取品牌信息")
    @GetMapping(value = "brand/list")
    Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO);

    @ApiOperation(value = "品牌新增")
    @PostMapping(value = "brand/save")
    Result<JSONObject> saveBrandInfo(@RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "品牌修改")
    @PutMapping(value = "brand/save")
    Result<JSONObject> editBrandInfo(@RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "品牌删除")
    @DeleteMapping(value = "brand/deleteId")
    Result<JSONObject> deleteBrandInfo(Integer id);


    @ApiOperation(value = "通过分类id获取品牌")
    @GetMapping(value = "brand/getBrandInfoByCategoryId")
    Result<JSONObject> getBrandInfoByCategoryId(Integer cid);


}
