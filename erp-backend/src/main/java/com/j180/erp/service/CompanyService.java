package com.j180.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j180.erp.common.BizException;
import com.j180.erp.common.Constants;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.enums.StatusEnum;
import com.j180.erp.common.util.AssertUtil;
import com.j180.erp.dto.CompanyForm;
import com.j180.erp.dto.CompanyQuery;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Company;
import com.j180.erp.mapper.CompanyMapper;
import com.j180.erp.validator.CompanyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 公司信息服务
 */
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyMapper companyMapper;
    private final CompanyValidator companyValidator;

    /**
     * 条件分页查询
     */
    public PageResult<Company> page(CompanyQuery query) {
        AssertUtil.notNull(query, "查询条件不能为空");
        query.validatePaging();
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Company::getCompanyCode, keyword)
                    .or().like(Company::getCompanyName, keyword)
                    .or().like(Company::getCreditCode, keyword));
        }
        wrapper.eq(query.getStatus() != null, Company::getStatus, query.getStatus());
        wrapper.orderByAsc(Company::getId);
        Page<Company> page = companyMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page);
    }

    /**
     * 全量启用公司（下拉选项）
     */
    public List<Company> listEnabled() {
        return companyMapper.selectList(new LambdaQueryWrapper<Company>()
                .eq(Company::getStatus, Constants.STATUS_ENABLED)
                .orderByAsc(Company::getId));
    }

    public Company getById(Long id) {
        return mustExist(id);
    }

    public void create(CompanyForm form) {
        companyValidator.validate(form);
        AssertUtil.isNull(selectByCode(form.getCompanyCode()), "公司编码已存在");
        AssertUtil.isNull(selectByName(form.getCompanyName()), "公司名称已存在");
        AssertUtil.isNull(selectByCreditCode(form.getCreditCode()), "统一社会信用代码已存在");
        Company company = new Company();
        BeanUtils.copyProperties(form, company);
        if (company.getStatus() == null) {
            company.setStatus(Constants.STATUS_ENABLED);
        }
        companyMapper.insert(company);
    }

    public void update(CompanyForm form) {
        AssertUtil.notNull(form.getId(), "公司ID不能为空");
        companyValidator.validate(form);
        Company company = mustExist(form.getId());
        AssertUtil.isFalse(selectByCode(form.getCompanyCode()) != null
                        && !selectByCode(form.getCompanyCode()).getId().equals(company.getId()),
                "公司编码已存在");
        AssertUtil.isFalse(selectByName(form.getCompanyName()) != null
                        && !selectByName(form.getCompanyName()).getId().equals(company.getId()),
                "公司名称已存在");
        AssertUtil.isFalse(selectByCreditCode(form.getCreditCode()) != null
                        && !selectByCreditCode(form.getCreditCode()).getId().equals(company.getId()),
                "统一社会信用代码已存在");
        BeanUtils.copyProperties(form, company, "id", "createTime", "updateTime");
        companyMapper.updateById(company);
    }

    public void updateStatus(Long id, StatusForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        StatusEnum.check(form.getStatus());
        Company company = mustExist(id);
        if (form.getStatus() == Constants.STATUS_DISABLED && company.getStatus() == Constants.STATUS_ENABLED) {
            long enabledCount = companyMapper.selectCount(new LambdaQueryWrapper<Company>()
                    .eq(Company::getStatus, Constants.STATUS_ENABLED));
            AssertUtil.isTrue(enabledCount > 1, "系统至少需要保留一条启用中的公司，无法停用");
        }
        company.setStatus(form.getStatus());
        companyMapper.updateById(company);
    }

    public void delete(Long id) {
        Company company = mustExist(id);
        AssertUtil.isTrue(company.getStatus() != Constants.STATUS_ENABLED,
                "启用中的公司不可删除，请先停用");
        companyMapper.deleteById(id);
    }

    public void deleteBatch(IdsForm form) {
        AssertUtil.notNull(form, "表单不能为空");
        AssertUtil.notEmpty(form.getIds(), "请选择要删除的数据");
        for (Long id : form.getIds()) {
            delete(id);
        }
    }

    private Company mustExist(Long id) {
        Company company = companyMapper.selectById(id);
        AssertUtil.notNull(company, "公司不存在或已删除");
        return company;
    }

    private Company selectByCode(String code) {
        return companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getCompanyCode, code).last("LIMIT 1"));
    }

    private Company selectByName(String name) {
        return companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getCompanyName, name).last("LIMIT 1"));
    }

    private Company selectByCreditCode(String creditCode) {
        return companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                .eq(Company::getCreditCode, creditCode).last("LIMIT 1"));
    }
}
