package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.Goods;
import com.erp.entity.GoodsCategory;
import com.erp.entity.GoodsUnit;
import com.erp.mapper.GoodsCategoryMapper;
import com.erp.mapper.GoodsMapper;
import com.erp.mapper.GoodsUnitMapper;
import com.erp.mapper.StockMapper;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品档案服务：商品/分类/计量单位
 */
@Service
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsCategoryMapper categoryMapper;
    private final GoodsUnitMapper unitMapper;
    private final StockMapper stockMapper;

    public GoodsService(GoodsMapper goodsMapper, GoodsCategoryMapper categoryMapper, GoodsUnitMapper unitMapper, StockMapper stockMapper) {
        this.goodsMapper = goodsMapper;
        this.categoryMapper = categoryMapper;
        this.unitMapper = unitMapper;
        this.stockMapper = stockMapper;
    }

    // ============ 商品 ============
    public PageResult<Goods> goodsPage(String keyword, Long categoryId, String status, int page, int pageSize) {
        return PageResult.of(goodsMapper.count(keyword, categoryId, status),
                goodsMapper.page(keyword, categoryId, status, (page - 1) * pageSize, pageSize));
    }

    public List<Goods> goodsAll() { return goodsMapper.selectAll(); }

    public List<Goods> goodsOnSale() { return goodsMapper.selectOnSale(); }

    public Goods goodsDetail(Long id) {
        Goods g = goodsMapper.findById(id);
        if (g != null) {
            g.setStockQty(stockMapper.sumQty(id));
        }
        return g;
    }

    @Transactional
    public Goods saveGoods(Goods g) {
        if (g.getId() == null) {
            if (g.getCode() == null || g.getCode().isEmpty()) {
                Long maxId = goodsMapper.maxId();
                g.setCode(String.format("GD%03d", (maxId == null ? 0 : maxId) + 1));
            }
            g.setCreateBy(UserContext.currentName());
            goodsMapper.insert(g);
        } else {
            goodsMapper.update(g);
        }
        return g;
    }

    @Transactional
    public void deleteGoods(Long id) {
        goodsMapper.delete(id);
    }

    @Transactional
    public void updateLimits(Long id, java.math.BigDecimal low, java.math.BigDecimal high) {
        goodsMapper.updateLimits(id, low, high);
    }

    // ============ 分类 ============
    public List<GoodsCategory> categoryTree() {
        List<GoodsCategory> all = categoryMapper.selectAll();
        Map<Long, List<GoodsCategory>> byParent = new LinkedHashMap<>();
        for (GoodsCategory c : all) byParent.computeIfAbsent(c.getParentId() == null ? 0L : c.getParentId(), k -> new ArrayList<>()).add(c);
        List<GoodsCategory> roots = new ArrayList<>();
        for (GoodsCategory c : byParent.getOrDefault(0L, new ArrayList<>())) {
            fillCat(c, byParent);
            roots.add(c);
        }
        return roots;
    }

    private void fillCat(GoodsCategory p, Map<Long, List<GoodsCategory>> byParent) {
        List<GoodsCategory> children = byParent.getOrDefault(p.getId(), new ArrayList<>());
        for (GoodsCategory c : children) fillCat(c, byParent);
        p.setChildren(children);
    }

    @Transactional
    public void saveCategory(GoodsCategory c) {
        if (c.getId() == null) {
            c.setCreateBy(UserContext.currentName());
            categoryMapper.insert(c);
        } else {
            categoryMapper.update(c);
        }
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (categoryMapper.countChildren(id) > 0) throw new BusinessException("存在子分类，禁止删除");
        if (categoryMapper.countGoodsRef(id) > 0) throw new BusinessException("分类下存在商品，禁止删除");
        categoryMapper.delete(id);
    }

    // ============ 单位 ============
    public List<GoodsUnit> units() { return unitMapper.selectAll(); }

    @Transactional
    public void saveUnit(GoodsUnit u) {
        if (u.getId() == null) {
            u.setCreateBy(UserContext.currentName());
            unitMapper.insert(u);
        } else {
            unitMapper.update(u);
        }
    }

    @Transactional
    public void deleteUnit(Long id) {
        if (unitMapper.countGoodsRef(id) > 0) throw new BusinessException("单位已被商品使用，禁止删除");
        unitMapper.delete(id);
    }
}
