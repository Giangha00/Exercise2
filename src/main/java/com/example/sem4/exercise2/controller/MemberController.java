package com.example.sem4.exercise2.controller;

import com.example.sem4.exercise2.dto.MemberForm;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.service.MemberService;
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
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.findAll());
        return "members/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new MemberForm());
        return "members/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") MemberForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "members/form";
        }
        try {
            memberService.save(form);
            redirectAttributes.addFlashAttribute("success", "Đã lưu độc giả");
            return "redirect:/members";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "members/form";
        }
    }
}
