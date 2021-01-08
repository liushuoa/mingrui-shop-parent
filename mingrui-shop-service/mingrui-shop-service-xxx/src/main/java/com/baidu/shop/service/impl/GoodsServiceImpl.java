package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.dto.SpuDetailDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GoodsServiceImpl  extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    @Transactional
    @Override
    public Result<JSONObject> isSaleable(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        if (ObjectUtil.isNotNull(spuEntity.getSaleable()) && spuEntity.getSaleable() < 2){
            if(spuEntity.getSaleable() == 1){
                spuEntity.setSaleable(0);
            }else{
                spuEntity.setSaleable(1);
            }
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess();
        }
        return this.setResultError("失败");
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteSpu(Integer spuId) {
        //删除spu表中的数据
        spuMapper.deleteByPrimaryKey(spuId);
        //删除spuDetail表中的数据
        spuDetailMapper.deleteByPrimaryKey(spuId);
        this.deleteSkuAndStock(spuId);
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> updateSpu(SpuDTO spuDTO) {
        final Date date = new Date();
        //修改spu里的信息
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(),SpuDetailEntity.class));

        this.deleteSkuAndStock(spuDTO.getId());
        //sku stock 新增方法
        this.saveSkuAndStock(spuDTO,spuEntity.getId(),date);
        return this.setResultSuccess();
    }

    private void deleteSkuAndStock(Integer spuId){
        //通过spuId删除sku的数据
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);

        List<Long> collect = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());
        skuMapper.deleteByIdList(collect);
        stockMapper.deleteByIdList(collect);
    }

    private void saveSkuAndStock(SpuDTO spuDTO,Integer spuId,Date date){
        List<SkuDTO> skus = spuDTO.getSkus();
        skus.stream().forEach(skuDTO -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);
            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }

    @Override
    public Result<List<SkuDTO>> getSkusBySpuId(Integer spuId) {
        List<SkuDTO> list = skuMapper.getSkusAndStockBySpuId(spuId);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveSpu(SpuDTO spuDTO) {
        final Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);
        //新增spuDetail
        SpuDetailDTO spuDetail = spuDTO.getSpuDetail();
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDetail, SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuEntity.getId());
        spuDetailMapper.insertSelective(spuDetailEntity);
        //新增sku
        List<SkuDTO> skus = spuDTO.getSkus();
        skus.stream().forEach(skuDTO -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuEntity.getId());
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);
            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
        return this.setResultSuccess();
    }

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {
        if(!StringUtils.isEmpty(spuDTO.getSort()) && !StringUtils.isEmpty(spuDTO.getOrder()))
            PageHelper.orderBy(spuDTO.getOrderBy());

        if(ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() < 2)
            criteria.andEqualTo("saleable",spuDTO.getSaleable());
        if(!StringUtils.isEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%"+spuDTO.getTitle()+"%");

        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);

        List<SpuDTO> spuDTOList = spuEntities.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity ,SpuDTO.class);

            /*CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(spuEntity.getCid1());
            CategoryEntity categoryEntity2 = categoryMapper.selectByPrimaryKey(spuEntity.getCid2());
            CategoryEntity categoryEntity3 = categoryMapper.selectByPrimaryKey(spuEntity.getCid3());
            spuDTO1.setCategoryName(categoryEntity.getName() + "/" + categoryEntity2.getName() + "/" + categoryEntity3.getName());*/
            List<Integer> integers = Arrays.asList(spuEntity.getCid1(), spuEntity.getCid2(), spuEntity.getCid3());
            List<CategoryEntity> categoryEntities = categoryMapper.selectByIdList(integers);
            String collect = categoryEntities.stream().map(categoryEntity -> categoryEntity.getName()).collect(Collectors.joining("/"));
            spuDTO1.setCategoryName(collect);
            /*String categoryName = "";
            List<String> strings = new ArrayList<>();
            strings.set(0,"");
            categoryEntities.stream().forEach(categoryEntity -> {
                strings.set(0,strings.get(0)+categoryEntity.getName()+"/");
            });
            categoryName = strings.get(0).substring(0,strings.get(0).length());*/


            //品牌名称 brandId
            //spuEntity.getBrandId()
            BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuEntity.getBrandId());
            spuDTO1.setBrandName(brandEntity.getName());
            return spuDTO1;
        }).collect(Collectors.toList());

        PageInfo<SpuEntity> spuEntityPageInfo = new PageInfo<>(spuEntities);
        return this.setResult(HTTPStatus.OK,spuEntityPageInfo.getTotal()+"",spuDTOList);
    }


}
