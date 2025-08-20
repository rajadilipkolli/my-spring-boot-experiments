package com.scheduler.quartz.web.controller;

import com.scheduler.quartz.model.response.ScheduleJob;
import com.scheduler.quartz.service.JobsService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final JobsService scheduleJobService;

    public IndexController(JobsService scheduleJobService) {
        this.scheduleJobService = scheduleJobService;
    }

    @GetMapping(path = {"/index", "/", "", "/index.html"})
    public String index(Model model) {
        List<ScheduleJob> jobList = scheduleJobService.getJobs();
        model.addAttribute("jobs", jobList);
        return "index";
    }
}
