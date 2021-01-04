package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.utils.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByBrandId(Integer brandId) {
        List<CategoryEntity> list = categoryMapper.getCategoryByBrandId(brandId);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<JSONObject> saveCategory(CategoryEntity categoryEntity) {
        CategoryEntity parentCategoryEntity  = new CategoryEntity();
        parentCategoryEntity.setId(categoryEntity.getParentId());
        parentCategoryEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentCategoryEntity);

        categoryMapper.insertSelective(categoryEntity);
        return this.setResultSuccess();
    }


    @Override
    public Result<JSONObject> editCategoryByPid(CategoryEntity categoryEntity) {
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        return this.setResultSuccess();
    }


    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setParentId(pid);
        List<CategoryEntity> list = categoryMapper.select(categoryEntity);
        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteCategoryByPid(Integer id) {
        //校验id是否合法
        if (ObjectUtil.isNull(id) || id <= 0)return this.setResultError("id不合法");

        //根据id查询一条数据
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);

        if(ObjectUtil.isNull(categoryEntity))return this.setResultError("数据不存在");

        //判断当前节点是否为父节点
        if(categoryEntity.getIsParent() == 1)return this.setResultError("当前节点为父节点");

        //如果当前分类被品牌绑定的话不能被删除 --> 通过分类id查询中间表是否有数据 true : 当前分类不能被删除 false : 继续执行
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> categoryBrandList = categoryBrandMapper.selectByExample(example1);
        if(categoryBrandList.size() != 0) return this.setResultError("绑定了不能删除");

        //通过当前节点的父节点id 查询 当前节点(将要被删除的节点)的父节点下是否还有其他子节点
        //先new一个example的对象 然后把实体类放入这个对象中
        //where parentId = categoryEntity.getParentId();
        //对sql进行拼接 select * from 表名 where 1=1 and parentId = ?
        //将查出的数据装入list集合中
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> categoryList  = categoryMapper.selectByExample(example);

        //如果size <= 1 --> 如果当前节点被删除的话 当前节点的父节点下没有节点了 --> 将当前节点的父节点状态改为叶子节点
        if(categoryList.size() == 1){
            //new一个新的实体类 然后根据查询到的子节点的parentId 来修改父节点的状态
            //然后放入新new 的实体类中
            //根据id来调用update 方法 来修改
            CategoryEntity updateCategoryEntity = new CategoryEntity();
            updateCategoryEntity.setIsParent(0);
            updateCategoryEntity.setId(categoryEntity.getParentId());

            categoryMapper.updateByPrimaryKeySelective(updateCategoryEntity);
        }
        categoryMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }

}



