package com.example.sem4.exercise2.controller;

import com.example.sem4.exercise2.dto.BorrowItem;
import com.example.sem4.exercise2.dto.BorrowingForm;
import com.example.sem4.exercise2.dto.PaymentForm;
import com.example.sem4.exercise2.dto.ReturnForm;
import com.example.sem4.exercise2.dto.ReturnItem;
import com.example.sem4.exercise2.dto.ReturnLineForm;
import com.example.sem4.exercise2.dto.ReturnPreview;
import com.example.sem4.exercise2.dto.WaiverForm;
import com.example.sem4.exercise2.entity.Borrowing;
import com.example.sem4.exercise2.entity.BorrowingDetail;
import com.example.sem4.exercise2.entity.BorrowingStatus;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.service.BookService;
import com.example.sem4.exercise2.service.BorrowingService;
import com.example.sem4.exercise2.service.FinePolicyService;
import com.example.sem4.exercise2.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final MemberService memberService;
    private final BookService bookService;
    private final FinePolicyService finePolicyService;

    @GetMapping
    public String list(@RequestParam(required = false) BorrowingStatus status,
                       @RequestParam(required = false) Long memberId,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                       @RequestParam(defaultValue = "false") boolean overdueOnly,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("page", borrowingService.search(
                status, memberId, fromDate, toDate, overdueOnly,
                PageRequest.of(Math.max(page, 0), 8, Sort.by(Sort.Direction.DESC, "id"))));
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("statuses", BorrowingStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("memberId", memberId);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("overdueOnly", overdueOnly);
        return "borrowings/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            BorrowingForm form = new BorrowingForm();
            form.setDueDate(LocalDate.now().plusDays(14));
            form.getLines().add(new com.example.sem4.exercise2.dto.BorrowLineForm());
            model.addAttribute("form", form);
        }
        prepareBorrowForm(model);
        return "borrowings/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BorrowingForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareBorrowForm(model);
            return "borrowings/form";
        }
        try {
            List<BorrowItem> items = form.getLines().stream()
                    .map(line -> new BorrowItem(line.getBookId(), line.getQuantity()))
                    .toList();
            Borrowing borrowing = borrowingService.createBorrowing(
                    form.getMemberId(), LocalDate.now(), form.getDueDate(), items);
            redirectAttributes.addFlashAttribute("success", "Đã tạo phiếu mượn #" + borrowing.getId());
            return "redirect:/borrowings/" + borrowing.getId();
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            prepareBorrowForm(model);
            return "borrowings/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingService.getForView(id);
        model.addAttribute("borrowing", borrowing);
        if (!model.containsAttribute("waiverForm")) {
            model.addAttribute("waiverForm", new WaiverForm());
        }
        model.addAttribute("policy", finePolicyService.findActiveOrNull());
        return "borrowings/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowingService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Đã hủy phiếu và hoàn tồn kho");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/borrowings/" + id;
    }

    @GetMapping("/{id}/return")
    public String returnForm(@PathVariable Long id, Model model) {
        Borrowing borrowing = loadReturnPage(id, model);
        if (!model.containsAttribute("form")) {
            ReturnForm form = new ReturnForm();
            form.setReturnDate(LocalDate.now());
            for (BorrowingDetail detail : borrowing.getDetails()) {
                if (detail.getRemainingQuantity() > 0) {
                    form.getLines().add(new ReturnLineForm(detail.getId(), 0));
                }
            }
            model.addAttribute("form", form);
        }
        return "borrowings/return";
    }

    @PostMapping("/{id}/return")
    public String returnBooks(@PathVariable Long id,
                              @Valid @ModelAttribute("form") ReturnForm form,
                              BindingResult bindingResult,
                              @RequestParam(defaultValue = "confirm") String intent,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            loadReturnPage(id, model);
            return "borrowings/return";
        }
        List<ReturnItem> items = form.getLines().stream()
                .map(line -> new ReturnItem(line.getDetailId(), line.getQuantity() == null ? 0 : line.getQuantity()))
                .toList();
        try {
            if ("preview".equals(intent)) {
                ReturnPreview preview = borrowingService.previewReturn(id, form.getReturnDate(), items);
                model.addAttribute("preview", preview);
                loadReturnPage(id, model);
                return "borrowings/return";
            }
            Borrowing borrowing = borrowingService.returnBooks(id, form.getReturnDate(), items);
            redirectAttributes.addFlashAttribute("success", "Đã ghi nhận trả sách cho phiếu #" + borrowing.getId());
            return "redirect:/borrowings/" + id;
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            loadReturnPage(id, model);
            return "borrowings/return";
        }
    }

    @GetMapping("/{id}/payments/new")
    public String paymentForm(@PathVariable Long id, Model model) {
        model.addAttribute("borrowing", borrowingService.getForView(id));
        if (!model.containsAttribute("form")) {
            PaymentForm form = new PaymentForm();
            form.setPaymentDate(LocalDate.now());
            form.setMethod("Tiền mặt");
            model.addAttribute("form", form);
        }
        return "borrowings/payment";
    }

    @PostMapping("/{id}/payments")
    public String pay(@PathVariable Long id,
                      @Valid @ModelAttribute("form") PaymentForm form,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("borrowing", borrowingService.getForView(id));
            return "borrowings/payment";
        }
        try {
            borrowingService.payFine(id, form.getAmount(), form.getPaymentDate(), form.getMethod(), form.getNote());
            redirectAttributes.addFlashAttribute("success", "Đã ghi nhận thanh toán phí phạt");
            return "redirect:/borrowings/" + id;
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("borrowing", borrowingService.getForView(id));
            return "borrowings/payment";
        }
    }

    @PostMapping("/{id}/waivers")
    public String waive(@PathVariable Long id,
                        @Valid @ModelAttribute("waiverForm") WaiverForm form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Miễn giảm cần số tiền, lý do và người duyệt");
            return "redirect:/borrowings/" + id;
        }
        try {
            borrowingService.waiveFine(id, form.getAmount(), form.getReason(), form.getApprovedBy());
            redirectAttributes.addFlashAttribute("success", "Đã ghi nhận miễn giảm phí");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/borrowings/" + id;
    }

    private Borrowing loadReturnPage(Long id, Model model) {
        Borrowing borrowing = borrowingService.getForView(id);
        Map<Long, BorrowingDetail> detailById = new LinkedHashMap<>();
        for (BorrowingDetail detail : borrowing.getDetails()) {
            detailById.put(detail.getId(), detail);
        }
        model.addAttribute("borrowing", borrowing);
        model.addAttribute("detailById", detailById);
        model.addAttribute("policy", finePolicyService.findActiveOrNull());
        return borrowing;
    }

    private void prepareBorrowForm(Model model) {
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("books", bookService.search(null, null, PageRequest.of(0, 200, Sort.by("title"))).getContent());
    }
}
