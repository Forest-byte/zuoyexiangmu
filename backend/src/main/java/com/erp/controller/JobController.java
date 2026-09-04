package com.erp.controller;

import com.erp.annotation.RequirePermission;
import com.erp.common.PageResult;
import com.erp.common.Result;
import com.erp.entity.JobTask;
import com.erp.entity.JobTaskLog;
import com.erp.service.JobService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定时任务接口：任务 CRUD / 手动执行 / 日志
 */
@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/tasks")
    @RequirePermission("system:job:list")
    public Result<List<JobTask>> tasks() { return Result.ok(jobService.tasks()); }

    @PostMapping("/tasks")
    @RequirePermission("system:job:add")
    public Result<JobTask> saveTask(@RequestBody JobTask t) { return Result.ok(jobService.saveTask(t)); }

    @PostMapping("/tasks/{id}/toggle")
    @RequirePermission("system:job:edit")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer enabled = Boolean.TRUE.equals(body.get("enabled")) ? 1 : 0;
        jobService.toggleTask(id, enabled);
        return Result.ok(null);
    }

    @DeleteMapping("/tasks/{id}")
    @RequirePermission("system:job:del")
    public Result<Void> delete(@PathVariable Long id) { jobService.deleteTask(id); return Result.ok(null); }

    @PostMapping("/tasks/{id}/run")
    @RequirePermission("system:job:edit")
    public Result<Map<String, Object>> run(@PathVariable Long id) { return Result.ok(jobService.run(id)); }

    @GetMapping("/logs")
    @RequirePermission("system:joblog:list")
    public Result<PageResult<JobTaskLog>> logs(@RequestParam(required = false) Long jobId,
                                               @RequestParam(required = false) String result,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(jobService.logPage(jobId, result, page, pageSize));
    }
}
