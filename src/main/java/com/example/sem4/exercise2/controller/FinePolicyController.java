package com.example.sem4.exercise2.controller;

import com.example.sem4.exercise2.dto.FinePolicyForm;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.service.FinePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fine-policies")
@RequiredArgsConstructor
public class FinePolicyController {

    private final FinePolicyService finePolicyService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("policies", finePolicyService.findAll());
        model.addAttribute("active", finePolicyService.findActiveOrNull());
        if (!model.containsAttribute("form")) {
            FinePolicyForm form = new FinePolicyForm();
            if (model.getAttribute("active") != null) {
                var active = finePolicyService.findActiveOrNull();
                form.setDailyFineAmount(active.getDailyFineAmount());
                form.setGraceDays(active.getGraceDays());
                form.setMaxFineAmount(active.getMaxFineAmount());
            }
            model.addAttribute("form", form);
        }
        return "fine-policies/list";
    }

    @PostMapping
    public String publish(@Valid @ModelAttribute("form") FinePolicyForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("policies", finePolicyService.findAll());
            model.addAttribute("active", finePolicyService.findActiveOrNull());
            return "fine-policies/list";
        }
        try {
            finePolicyService.publish(form);
            redirectAttributes.addFlashAttribute("success", "Đã áp dụng chính sách phí phạt mới");
            return "redirect:/fine-policies";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("policies", finePolicyService.findAll());
            model.addAttribute("active", finePolicyService.findActiveOrNull());
            return "fine-policies/list";
        }
    }
}
