package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<JSONObject> getBrandInfoByCategoryId(Integer cid) {
        List<BrandEntity> brandInfoByCategoryId = brandMapper.getBrandInfoByCategoryId(cid);
        return this.setResultSuccess(brandInfoByCategoryId);
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteBrandInfo(Integer id) {
        brandMapper.deleteByPrimaryKey(id);

        this.deleteCategoryByBrand(id);

        return this.setResultSuccess();
    }


    @Transactional
    @Override
    public Result<JSONObject> editBrandInfo(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]), false).toCharArray()[0]);
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        this.deleteCategoryByBrand(brandEntity.getId());

        this.insertCategoryBrandList(brandDTO.getCategories(),brandEntity.getId());
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> saveBrandInfo(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO,BrandEntity.class);

        //处理品牌首字母
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);

        brandMapper.insertSelective(brandEntity);

        this.insertCategoryBrandList(brandDTO.getCategories(),brandEntity.getId());

        return this.setResultSuccess();
    }



    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //分页
        PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        if(!StringUtils.isEmpty(brandDTO.getSort())){
            //String order = "";
            //if (Boolean.valueOf(brandDTO.getOrder())){
            //    order = "desc";
            //}else{
            //    order = "asc";
            //}
            //PageHelper.orderBy(brandDTO.getSort()+ " " + (Boolean.valueOf(brandDTO.getOrder())? "desc" : "asc"));
            PageHelper.orderBy(brandDTO.getOrderBy());
        }

        Example example = new Example(BrandEntity.class);
        example.createCriteria().andLike("name","%"+brandDTO.getName()+"%");

        List<BrandEntity> brandEntities = brandMapper.selectByExample(example);
        PageInfo<BrandEntity> pageInfo  = new PageInfo<>(brandEntities);

        return this.setResultSuccess(pageInfo);
    }

    private void insertCategoryBrandList(String categories,Integer brandId){
        if(StringUtils.isEmpty(categories))throw new RuntimeException("分类信息不能为空");
        //判断分类集合中是否包含逗号","
        if(categories.contains(",")){
            categoryBrandMapper.insertList(
                    Arrays.asList(categories.split(","))
                            .stream()
                            .map(categoryIdStr -> new CategoryBrandEntity(Integer.valueOf(categoryIdStr)
                                    ,brandId))
                            .collect(Collectors.toList())
            );
        }else{
            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setBrandId(brandId);
            categoryBrandEntity.setCategoryId(Integer.valueOf(categories));

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }
    }

    private void deleteCategoryByBrand(Integer brandId){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",brandId);
        categoryBrandMapper.deleteByExample(example);
    }
}
