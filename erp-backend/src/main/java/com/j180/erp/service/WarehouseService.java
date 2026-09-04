package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j180.erp.common.Constants;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.dto.WarehouseForm;
import com.j180.erp.dto.WarehouseQuery;
import com.j180.erp.entity.Employee;
import com.j180.erp.entity.Warehouse;
import com.j180.erp.mapper.EmployeeMapper;
import com.j180.erp.mapper.EmployeeWarehouseMapper;
import com.j180.erp.mapper.WarehouseMapper;
import com.j180.erp.validator.WarehouseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 仓库信息服务
 */
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;
    private final EmployeeMapper employeeMapper;
    private final EmployeeWarehouseMapper employeeWarehouseMapper;
    private final WarehouseValidator warehouseValidator;

    public PageResult<Warehouse> page(WarehouseQuery query) {
        AssertUtil.notNull(query, "查询条件不能为空");
        query.validatePaging();
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Warehouse::getWhCode, keyword).or().like(Warehouse::getWhName, keyword));
        }
        wrapper.eq(query.getWhType() != null, Warehouse::getWhType, query.getWhType());
        wrapper.eq(query.getStatus() != null, Warehouse::getStatus, query.getStatus());
        wrapper.orderByAsc(Warehouse::getId);
        Page<Warehouse> page = warehouseMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page);
    }

    public List<Warehouse> listEnabled() {
        return warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getStatus, Constants.STATUS_ENABLED)
                .orderByAsc(Warehouse::getId));
    }

    public Warehouse getById(Long id) {
        return mustExist(id);
    }

    public void create(WarehouseForm form) {
        warehouseValidator.validate(form);
        AssertUtil.isNull(selectByCode(form.getWhCode()), "仓库编码已存在");
        AssertUtil.isNull(selectByName(form.getWhName()), "仓库名称已存在");
        checkManager(form.getManagerId());
        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(form, warehouse);
        if (warehouse.getStatus() == null) {
            warehouse.setStatus(Constants.STATUS_ENABLED);
        }
        warehouseMapper.insert(warehouse);
    }

    public void update(WarehouseForm form) {
        AssertUtil.notNull(form.getId(), "仓库ID不能为空");
        warehouseValidator.validate(form);
        Warehouse warehouse = mustExist(form.getId());
        AssertUtil.isFalse(selectByCode(form.getWhCode()) != null
                        && !selectByCode(form.getWhCode()).getId().equals(warehouse.getId()),
                "仓库编码已存在");
        AssertUtil.isFalse(selectByName(form.getWhName()) != null
                        && !selectByName(form.getWhName()).getId().equals(warehouse.getId()),
                "仓库名称已存在");
        checkManager(form.getManagerId());
        BeanUtils.copyProperties(form, warehouse, "id", "createTime", "updateTime");
        warehouseMapper.updateById(warehouse);
    }

    public void updateStatus(Long id, StatusForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        StatusEnum.check(form.getStatus());
        Warehouse warehouse = mustExist(id);
        warehouse.setStatus(form.getStatus());
        warehouseMapper.updateById(warehouse);
    }

    public void delete(Long id) {
        Warehouse warehouse = mustExist(id);
        long bindCount = employeeWarehouseMapper.countByWarehouseId(id);
        AssertUtil.isTrue(bindCount == 0, "仓库仍与员工存在数据权限绑定，不可删除");
        warehouseMapper.deleteById(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    private Warehouse mustExist(Long id) {
        Warehouse warehouse = warehouseMapper.selectById(id);
        AssertUtil.notNull(warehouse, "仓库不存在或已删除");
        return warehouse;
    }

    private Warehouse selectByCode(String code) {
        return warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWhCode, code).last("LIMIT 1"));
    }

    private Warehouse selectByName(String name) {
        return warehouseMapper.selectOne(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWhName, name).last("LIMIT 1"));
    }

    private void checkManager(Long managerId) {
        if (managerId == null) {
            return;
        }
        Employee employee = employeeMapper.selectById(managerId);
        AssertUtil.notNull(employee, "仓库负责人不存在");
        AssertUtil.isTrue(employee.getStatus() != Constants.EMP_LEAVED, "仓库负责人必须是未离职员工");
    }
}
