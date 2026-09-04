package com.erp.config;

import com.erp.entity.JobTask;
import com.erp.entity.JobTaskLog;
import com.erp.mapper.JobTaskMapper;
import com.erp.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 定时任务调度器：每分钟扫描一次启用任务，cron 匹配则执行
 */
@Component
public class JobScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    private final JobTaskMapper jobTaskMapper;
    private final JobService jobService;

    public JobScheduler(JobTaskMapper jobTaskMapper, JobService jobService) {
        this.jobTaskMapper = jobTaskMapper;
        this.jobService = jobService;
    }

    @Scheduled(fixedDelay = 60000)
    public void scan() {
        List<JobTask> tasks = jobTaskMapper.selectEnabled();
        ZonedDateTime now = ZonedDateTime.now();
        for (JobTask t : tasks) {
            try {
                if (!CronExpression.isValidExpression(t.getCronExpr())) {
                    continue;
                }
                CronExpression cron = CronExpression.parse(t.getCronExpr());
                ZonedDateTime next = cron.next(now);
                // 下一执行时间落在当前这一分钟内则触发
                if (next == null || next.isAfter(now.plusMinutes(1))) {
                    continue;
                }
                runJob(t);
            } catch (Exception e) {
                log.error("定时任务调度异常: task={}", t.getJobCode(), e);
            }
        }
    }

    private void runJob(JobTask t) {
        LocalDateTime start = LocalDateTime.now();
        String result;
        String message;
        try {
            jobService.execute(t);
            result = "SUCCESS";
            message = "调度执行成功";
        } catch (Exception e) {
            result = "FAIL";
            message = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        LocalDateTime end = LocalDateTime.now();
        jobTaskMapper.updateLastRun(t.getId(), end);
        JobTaskLog logRec = new JobTaskLog();
        logRec.setJobId(t.getId());
        logRec.setStartTime(start);
        logRec.setEndTime(end);
        logRec.setResult(result);
        logRec.setMessage(message);
        logRec.setOperator("scheduler");
        logRec.setCreateBy("scheduler");
        jobTaskMapper.insertLog(logRec);
    }
}
