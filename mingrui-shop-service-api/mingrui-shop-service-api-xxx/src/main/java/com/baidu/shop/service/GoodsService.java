package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获取spu信息")
    @GetMapping(value = "/goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "商品新增")
    @PostMapping(value = "/goods/save")
    Result<JSONObject> saveSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "通过spuId查询spudetail信息")
    @GetMapping(value = "/goods/getSpuDetailBySpuId")
    Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuId);

    @ApiOperation(value = "通过spuId查询sku信息")
    @GetMapping(value = "/goods/getSkusBySpuId")
    Result<List<SkuDTO>> getSkusBySpuId(Integer spuId);

    @ApiOperation(value = "商品修改")
    @PutMapping(value = "/goods/save")
    Result<JSONObject> updateSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "商品删除")
    @DeleteMapping(value = "/goods/deleteSpuId")
    Result<JSONObject> deleteSpu(Integer spuId);

    @ApiOperation(value = "上架下架")
    @PutMapping(value = "/goods/isSaleable")
    Result<JSONObject> isSaleable(@RequestBody SpuDTO spuDTO);

}
