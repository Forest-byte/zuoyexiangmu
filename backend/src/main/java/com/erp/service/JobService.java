package com.erp.service;

import com.erp.common.BusinessException;
import com.erp.common.PageResult;
import com.erp.entity.JobTask;
import com.erp.entity.JobTaskLog;
import com.erp.mapper.JobTaskMapper;
import com.erp.mapper.StockMapper;
import com.erp.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 定时任务服务：任务 CRUD / 手动执行 / 日志查询
 */
@Service
public class JobService {

    private final JobTaskMapper jobTaskMapper;
    private final ReportService reportService;
    private final StockMapper stockMapper;

    public JobService(JobTaskMapper jobTaskMapper, ReportService reportService, StockMapper stockMapper) {
        this.jobTaskMapper = jobTaskMapper;
        this.reportService = reportService;
        this.stockMapper = stockMapper;
    }

    public List<JobTask> tasks() { return jobTaskMapper.selectAll(); }

    @Transactional
    public JobTask saveTask(JobTask t) {
        if (t.getId() == null) {
            t.setCreateBy(UserContext.currentName());
            jobTaskMapper.insert(t);
        } else {
            jobTaskMapper.update(t);
        }
        return t;
    }

    @Transactional
    public void toggleTask(Long id, Integer enabled) {
        jobTaskMapper.updateEnabled(id, enabled);
    }

    @Transactional
    public void deleteTask(Long id) { jobTaskMapper.delete(id); }

    /** 手动执行一次任务 */
    public Map<String, Object> run(Long id) {
        JobTask t = jobTaskMapper.findById(id);
        if (t == null) throw new BusinessException("任务不存在");
        LocalDateTime start = LocalDateTime.now();
        String result;
        String message;
        try {
            execute(t);
            result = "SUCCESS";
            message = "手动执行成功";
        } catch (Exception e) {
            result = "FAIL";
            message = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        LocalDateTime end = LocalDateTime.now();
        jobTaskMapper.updateLastRun(id, end);
        JobTaskLog log = new JobTaskLog();
        log.setJobId(id);
        log.setStartTime(start);
        log.setEndTime(end);
        log.setResult(result);
        log.setMessage(message);
        log.setOperator(UserContext.currentName());
        log.setCreateBy(UserContext.currentName());
        jobTaskMapper.insertLog(log);
        return Map.of("result", result, "message", message);
    }

    /** 任务执行体（按 job_code 分发） */
    public void execute(JobTask t) {
        String code = t.getJobCode();
        switch (code) {
            case "STOCK_SNAPSHOT" -> reportService.saveSnapshot("RPT_STOCK");
            case "LOW_STOCK_ALERT" -> {
                int lowCount = stockMapper.selectLowStock().size();
                if (lowCount > 0) {
                    throw new BusinessException("低库存商品 " + lowCount + " 种，请及时补货");
                }
            }
            case "ARC_DUE_REMIND" -> {
                // 应收到期提醒：存在未结清应收即提示
                int count = stockMapper.selectLowStock().size(); // 占位，实际由报表统计
            }
            case "SALE_DAILY_REPORT" -> reportService.saveSnapshot("RPT_SALE");
            case "PURCHASE_DAILY_REPORT" -> reportService.saveSnapshot("RPT_PURCHASE");
            case "FINANCE_DAILY_REPORT" -> reportService.saveSnapshot("RPT_FINANCE");
            case "DATA_BACKUP_REMIND" -> { /* 提示人工备份 */ }
            default -> { /* 未知任务 */ }
        }
    }

    public PageResult<JobTaskLog> logPage(Long jobId, String result, int page, int pageSize) {
        return PageResult.of(jobTaskMapper.countLogs(jobId, result),
                jobTaskMapper.pageLogs(jobId, result, (page - 1) * pageSize, pageSize));
    }
}
