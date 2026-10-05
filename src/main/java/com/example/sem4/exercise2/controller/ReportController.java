package com.example.sem4.exercise2.controller;

import com.example.sem4.exercise2.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overdue")
    public String overdue(Model model) {
        model.addAttribute("rows", reportService.overdueReport());
        return "reports/overdue";
    }
}
