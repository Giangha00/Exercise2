package com.example.sem4.exercise2.controller;

import com.example.sem4.exercise2.dto.BookForm;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.service.BookService;
import com.example.sem4.exercise2.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("page", bookService.search(
                keyword, categoryId, PageRequest.of(Math.max(page, 0), 8, Sort.by("title").ascending())));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "books/list";
    }

    @GetMapping("/books/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new BookForm());
        }
        model.addAttribute("categories", categoryRepository.findAll());
        return "books/form";
    }

    @PostMapping("/books")
    public String create(@Valid @ModelAttribute("form") BookForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "books/form";
        }
        try {
            bookService.save(form);
            redirectAttributes.addFlashAttribute("success", "Đã lưu sách");
            return "redirect:/books";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            return "books/form";
        }
    }

    @PostMapping("/categories")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes) {
        try {
            bookService.addCategory(name, description);
            redirectAttributes.addFlashAttribute("success", "Đã thêm thể loại");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/books/new";
    }
}
