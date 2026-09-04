package com.j180.erp.controller;

import com.j180.erp.audit.Audit;
import com.j180.erp.common.PageResult;
import com.j180.erp.common.Result;
import com.j180.erp.dto.CompanyForm;
import com.j180.erp.dto.CompanyQuery;
import com.j180.erp.dto.IdsForm;
import com.j180.erp.dto.StatusForm;
import com.j180.erp.entity.Company;
import com.j180.erp.security.RequiresPermission;
import com.j180.erp.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 公司信息接口
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/page")
    @RequiresPermission("P_COMPANY")
    public Result<PageResult<Company>> page(CompanyQuery query) {
        return Result.ok(companyService.page(query));
    }

    @GetMapping("/list")
    @RequiresPermission("P_COMPANY")
    public Result<List<Company>> list() {
        return Result.ok(companyService.listEnabled());
    }

    @GetMapping("/{id}")
    @RequiresPermission("P_COMPANY")
    public Result<Company> detail(@PathVariable Long id) {
        return Result.ok(companyService.getById(id));
    }

    @PostMapping
    @RequiresPermission("B_COMPANY_ADD")
    @Audit(module = "公司管理", action = "新增公司", targetType = "sys_company")
    public Result<Void> create(@RequestBody CompanyForm form) {
        companyService.create(form);
        return Result.ok();
    }

    @PutMapping
    @RequiresPermission("B_COMPANY_EDIT")
    @Audit(module = "公司管理", action = "编辑公司", targetType = "sys_company")
    public Result<Void> update(@RequestBody CompanyForm form) {
        companyService.update(form);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("B_COMPANY_STATUS")
    @Audit(module = "公司管理", action = "停用启用公司", targetType = "sys_company")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusForm form) {
        companyService.updateStatus(id, form);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("B_COMPANY_DELETE")
    @Audit(module = "公司管理", action = "删除公司", targetType = "sys_company")
    public Result<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @RequiresPermission("B_COMPANY_DELETE")
    @Audit(module = "公司管理", action = "批量删除公司", targetType = "sys_company")
    public Result<Void> deleteBatch(@RequestBody IdsForm form) {
        companyService.deleteBatch(form);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequiresPermission("B_COMPANY_EXPORT")
    public ResponseEntity<byte[]> export() {
        List<Company> list = companyService.listEnabled();
        StringBuilder sb = new StringBuilder("\uFEFF公司编码,公司名称,统一社会信用代码,法定代表人,注册地址,联系电话,邮箱,状态\n");
        for (Company c : list) {
            sb.append(csv(c.getCompanyCode())).append(',')
                    .append(csv(c.getCompanyName())).append(',')
                    .append(csv(c.getCreditCode())).append(',')
                    .append(csv(c.getLegalPerson())).append(',')
                    .append(csv(c.getAddress())).append(',')
                    .append(csv(c.getPhone())).append(',')
                    .append(csv(c.getEmail())).append(',')
                    .append(c.getStatus() == 1 ? "启用" : "停用")
                    .append('\n');
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "companies.csv");
        return new ResponseEntity<>(sb.toString().getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    private String csv(String s) {
        if (s == null) {
            return "";
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
