package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.*;
import com.erp.mapper.*;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公共配置服务：车辆/会议室/系统参数/字典/编码规则/审批规则/审计查询
 */
@Service
public class SystemConfigService {

    private final VehicleMapper vehicleMapper;
    private final MeetingMapper meetingMapper;
    private final ParamMapper paramMapper;
    private final DictMapper dictMapper;
    private final CodeRuleMapper codeRuleMapper;
    private final AuditLogMapper auditLogMapper;

    public SystemConfigService(VehicleMapper vehicleMapper, MeetingMapper meetingMapper, ParamMapper paramMapper,
                               DictMapper dictMapper, CodeRuleMapper codeRuleMapper, AuditLogMapper auditLogMapper) {
        this.vehicleMapper = vehicleMapper;
        this.meetingMapper = meetingMapper;
        this.paramMapper = paramMapper;
        this.dictMapper = dictMapper;
        this.codeRuleMapper = codeRuleMapper;
        this.auditLogMapper = auditLogMapper;
    }

    // ============ 车辆 ============
    public List<SysVehicle> vehicles() { return vehicleMapper.selectAll(); }

    @Transactional
    public void saveVehicle(SysVehicle v) {
        if (v.getId() == null) {
            v.setCreateBy(UserContext.currentName());
            vehicleMapper.insert(v);
        } else {
            vehicleMapper.update(v);
        }
    }

    @Transactional
    public void deleteVehicle(Long id) { vehicleMapper.delete(id); }

    // ============ 会议室 ============
    public List<SysMeeting> meetings() { return meetingMapper.selectAll(); }

    @Transactional
    public void saveMeeting(SysMeeting m) {
        if (m.getId() == null) {
            m.setCreateBy(UserContext.currentName());
            meetingMapper.insert(m);
        } else {
            meetingMapper.update(m);
        }
    }

    @Transactional
    public void deleteMeeting(Long id) { meetingMapper.delete(id); }

    // ============ 系统参数 ============
    public List<SysParam> params() { return paramMapper.selectAll(); }

    @Transactional
    public void saveParam(SysParam p) {
        if (p.getId() == null) {
            p.setCreateBy(UserContext.currentName());
            paramMapper.insert(p);
        } else {
            paramMapper.update(p);
        }
    }

    @Transactional
    public void deleteParam(Long id) { paramMapper.delete(id); }

    // ============ 字典 ============
    public List<SysDict> dicts(String dictType) {
        return (dictType == null || dictType.isEmpty()) ? dictMapper.selectAll() : dictMapper.selectByType(dictType);
    }

    public List<String> dictTypes() { return dictMapper.selectTypes(); }

    @Transactional
    public void saveDict(SysDict d) {
        if (d.getId() == null) {
            d.setCreateBy(UserContext.currentName());
            dictMapper.insert(d);
        } else {
            dictMapper.update(d);
        }
    }

    @Transactional
    public void deleteDict(Long id) { dictMapper.delete(id); }

    // ============ 编码规则 ============
    public List<SysCodeRule> codeRules() { return codeRuleMapper.selectAll(); }

    @Transactional
    public void saveCodeRule(SysCodeRule r) {
        if (r.getId() == null) {
            r.setCreateBy(UserContext.currentName());
            codeRuleMapper.insert(r);
        } else {
            codeRuleMapper.update(r);
        }
    }

    @Transactional
    public void deleteCodeRule(Long id) { codeRuleMapper.delete(id); }

    // ============ 审批规则 ============
    public List<ApprovalRule> approvalRules() { return codeRuleMapper.selectApprovalAll(); }

    @Transactional
    public void saveApprovalRule(ApprovalRule r) {
        if (r.getId() == null) {
            r.setCreateBy(UserContext.currentName());
            codeRuleMapper.insertApproval(r);
        } else {
            codeRuleMapper.updateApproval(r);
        }
    }

    @Transactional
    public void deleteApprovalRule(Long id) { codeRuleMapper.deleteApproval(id); }

    // ============ 审计查询 ============
    public PageResult<SysAuditLog> auditPage(String operator, String action, String startTime, String endTime,
                                             int page, int pageSize) {
        long total = auditLogMapper.count(operator, action, startTime, endTime);
        List<SysAuditLog> list = auditLogMapper.page(operator, action, startTime, endTime, (page - 1) * pageSize, pageSize);
        return PageResult.of(total, list);
    }
}
