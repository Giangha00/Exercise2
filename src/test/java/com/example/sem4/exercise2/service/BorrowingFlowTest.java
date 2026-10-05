package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.dto.BorrowItem;
import com.example.sem4.exercise2.dto.ReturnItem;
import com.example.sem4.exercise2.entity.Book;
import com.example.sem4.exercise2.entity.Borrowing;
import com.example.sem4.exercise2.entity.BorrowingDetail;
import com.example.sem4.exercise2.entity.BorrowingStatus;
import com.example.sem4.exercise2.entity.Category;
import com.example.sem4.exercise2.entity.FinePolicy;
import com.example.sem4.exercise2.entity.Member;
import com.example.sem4.exercise2.exception.InsufficientStockException;
import com.example.sem4.exercise2.exception.InvalidPaymentException;
import com.example.sem4.exercise2.exception.InvalidReturnException;
import com.example.sem4.exercise2.exception.MemberNotEligibleException;
import com.example.sem4.exercise2.repository.BookRepository;
import com.example.sem4.exercise2.repository.BorrowingRepository;
import com.example.sem4.exercise2.repository.CategoryRepository;
import com.example.sem4.exercise2.repository.FinePolicyRepository;
import com.example.sem4.exercise2.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowingFlowTest {

    @Autowired
    private BorrowingService borrowingService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BorrowingRepository borrowingRepository;
    @Autowired
    private FinePolicyRepository finePolicyRepository;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void clean() {
        borrowingRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();
        finePolicyRepository.deleteAll();
    }

    @Test
    void borrowSeveralBooksDecreasesStock() {
        policy(0, "5000", null);
        Member member = member("M1");
        Book first = book("Sách A", "A-1", 5);
        Book second = book("Sách B", "B-1", 4);

        Borrowing borrowing = borrowingService.createBorrowing(
                member.getId(),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 10),
                List.of(new BorrowItem(first.getId(), 2), new BorrowItem(second.getId(), 1)));

        assertEquals(BorrowingStatus.BORROWING, borrowing.getStatus());
        assertEquals(2, borrowing.getDetails().size());
        assertEquals(3, stock(first.getId()));
        assertEquals(3, stock(second.getId()));
    }

    @Test
    void insufficientSecondBookRollsBackWholeSlip() {
        policy(0, "5000", null);
        Member member = member("M1");
        Book first = book("Sách A", "A-1", 5);
        Book second = book("Sách B", "B-1", 1);

        assertThrows(InsufficientStockException.class, () -> borrowingService.createBorrowing(
                member.getId(),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 10),
                List.of(new BorrowItem(first.getId(), 1), new BorrowItem(second.getId(), 2))));

        assertEquals(5, stock(first.getId()));
        assertEquals(1, stock(second.getId()));
        assertEquals(0, borrowingRepository.count());
    }

    @Test
    void returnAllOnTimeHasNoFine() {
        policy(0, "5000", null);
        Book book = book("Sách A", "A-1", 3);
        Borrowing borrowing = borrow(member("M1"), book, 1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));

        Borrowing returned = borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 10),
                List.of(new ReturnItem(detailId(borrowing), 1)));

        assertEquals(BorrowingStatus.RETURNED, returned.getStatus());
        assertEquals(0, returned.getTotalFineAmount().compareTo(BigDecimal.ZERO));
        assertEquals(3, stock(book.getId()));
    }

    @Test
    void partialOnTimeReturnKeepsSlipOpenAndRestocks() {
        policy(0, "5000", null);
        Book book = book("Sách A", "A-1", 4);
        Borrowing borrowing = borrow(member("M1"), book, 2, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));

        Borrowing returned = borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 8),
                List.of(new ReturnItem(detailId(borrowing), 1)));

        assertEquals(BorrowingStatus.PARTIALLY_RETURNED, returned.getStatus());
        assertEquals(1, returned.getDetails().get(0).getReturnedQuantity());
        assertEquals(0, returned.getTotalFineAmount().compareTo(BigDecimal.ZERO));
        assertEquals(3, stock(book.getId()));
    }

    @Test
    void threeDaysLateChargesQuantityReturned() {
        policy(0, "5000", null);
        Book book = book("Sách A", "A-1", 3);
        Borrowing borrowing = borrow(member("M1"), book, 1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));

        Borrowing returned = borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 13),
                List.of(new ReturnItem(detailId(borrowing), 1)));

        BorrowingDetail detail = returned.getDetails().get(0);
        assertEquals(3, detail.getLateDays());
        assertEquals(0, detail.getFineAmount().compareTo(new BigDecimal("15000")));
        assertEquals(BorrowingStatus.FINE_PENDING, returned.getStatus());
        assertEquals(0, returned.getUnpaidFineAmount().compareTo(new BigDecimal("15000")));
    }

    @Test
    void partialLateReturnChargesOnlyReturnedCopies() {
        policy(0, "5000", null);
        Book book = book("Sách A", "A-1", 4);
        Borrowing borrowing = borrow(member("M1"), book, 2, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));

        Borrowing returned = borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 13),
                List.of(new ReturnItem(detailId(borrowing), 1)));

        assertEquals(0, returned.getDetails().get(0).getFineAmount().compareTo(new BigDecimal("15000")));
        assertEquals(1, returned.getDetails().get(0).getRemainingQuantity());
        assertEquals(BorrowingStatus.OVERDUE, returned.getStatus());
        assertEquals(3, stock(book.getId()));
    }

    @Test
    void failedReturnRollsBackEarlierLinesInTheSameSlip() {
        policy(0, "5000", null);
        Member member = member("M1");
        Book first = book("Sách A", "A-1", 2);
        Book second = book("Sách B", "B-1", 2);
        Borrowing borrowing = borrowingService.createBorrowing(
                member.getId(),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 10),
                List.of(new BorrowItem(first.getId(), 1), new BorrowItem(second.getId(), 1)));
        Long firstDetail = borrowing.getDetails().get(0).getId();
        Long secondDetail = borrowing.getDetails().get(1).getId();

        assertThrows(InvalidReturnException.class, () -> borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 10),
                List.of(new ReturnItem(firstDetail, 1), new ReturnItem(secondDetail, 9))));

        Borrowing stored = borrowingRepository.findById(borrowing.getId()).orElseThrow();
        assertEquals(BorrowingStatus.BORROWING, stored.getStatus());
        assertEquals(1, stock(first.getId()));
        assertEquals(1, stock(second.getId()));
    }

    @Test
    void partialPaymentUpdatesBalancesAndRejectsOverpay() {
        policy(0, "5000", null);
        Book book = book("Sách A", "A-1", 2);
        Borrowing borrowing = borrow(member("M1"), book, 1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));
        borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 13),
                List.of(new ReturnItem(detailId(borrowing), 1)));

        Borrowing paid = borrowingService.payFine(
                borrowing.getId(), new BigDecimal("5000"), LocalDate.of(2026, 10, 14), "Tiền mặt", "lần 1");

        assertEquals(0, paid.getPaidFineAmount().compareTo(new BigDecimal("5000")));
        assertEquals(0, paid.getUnpaidFineAmount().compareTo(new BigDecimal("10000")));
        assertEquals(BorrowingStatus.FINE_PENDING, paid.getStatus());

        assertThrows(InvalidPaymentException.class, () -> borrowingService.payFine(
                borrowing.getId(), new BigDecimal("20000"), LocalDate.of(2026, 10, 14), "Tiền mặt", null));

        Borrowing still = borrowingRepository.findById(borrowing.getId()).orElseThrow();
        assertEquals(0, still.getPaidFineAmount().compareTo(new BigDecimal("5000")));
        assertEquals(0, still.getUnpaidFineAmount().compareTo(new BigDecimal("10000")));
    }

    @Test
    void memberWithUnpaidFineCannotBorrowAgain() {
        policy(0, "5000", null);
        Member member = member("M1");
        Book book = book("Sách A", "A-1", 3);
        Borrowing borrowing = borrow(member, book, 1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10));
        borrowingService.returnBooks(
                borrowing.getId(),
                LocalDate.of(2026, 10, 13),
                List.of(new ReturnItem(detailId(borrowing), 1)));

        assertThrows(MemberNotEligibleException.class, () -> borrowingService.createBorrowing(
                member.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                List.of(new BorrowItem(book.getId(), 1))));
        assertEquals(3, stock(book.getId()));
    }

    @Test
    void onlyOneConcurrentBorrowGetsTheLastCopy() throws Exception {
        policy(0, "5000", null);
        Book book = book("Cuốn cuối", "LAST", 1);
        Member first = member("M1");
        Member second = member("M2");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        List<String> failures = new ArrayList<>();

        try {
            Future<?> left = pool.submit(() -> borrowLastCopy(first.getId(), book.getId(), ready, start, success, failures));
            Future<?> right = pool.submit(() -> borrowLastCopy(second.getId(), book.getId(), ready, start, success, failures));
            ready.await();
            start.countDown();
            left.get();
            right.get();
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, success.get(), failures.toString());
        assertEquals(1, borrowingRepository.count());
        assertEquals(0, stock(book.getId()));
    }

    @Test
    void pagesRenderBorrowingFlow() throws Exception {
        policy(0, "5000", null);
        Member member = member("M9");
        Book book = book("Clean Code", "CC-1", 2);

        mockMvc.perform(get("/books")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Clean Code")));
        mockMvc.perform(get("/borrowings/new")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Tạo phiếu mượn")));
        mockMvc.perform(get("/fine-policies")).andExpect(status().isOk());
        mockMvc.perform(get("/reports/overdue")).andExpect(status().isOk());
        mockMvc.perform(post("/borrowings")
                        .param("memberId", member.getId().toString())
                        .param("dueDate", LocalDate.now().plusDays(7).toString())
                        .param("lines[0].bookId", book.getId().toString())
                        .param("lines[0].quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/borrowings/*"));
        mockMvc.perform(post("/borrowings").param("dueDate", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Chọn độc giả")));

        Long id = borrowingRepository.findAll().get(0).getId();
        mockMvc.perform(get("/borrowings/" + id)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Clean Code")));
        mockMvc.perform(get("/borrowings/" + id + "/return")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Xem phí phát sinh")));
        assertTrue(stock(book.getId()) == 1);
    }

    private void borrowLastCopy(Long memberId, Long bookId, CountDownLatch ready, CountDownLatch start,
                                AtomicInteger success, List<String> failures) {
        ready.countDown();
        try {
            start.await();
            borrowingService.createBorrowing(
                    memberId,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    List.of(new BorrowItem(bookId, 1)));
            success.incrementAndGet();
        } catch (RuntimeException ex) {
            synchronized (failures) {
                failures.add(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private Borrowing borrow(Member member, Book book, int quantity, LocalDate borrowed, LocalDate due) {
        return borrowingService.createBorrowing(
                member.getId(), borrowed, due, List.of(new BorrowItem(book.getId(), quantity)));
    }

    private Long detailId(Borrowing borrowing) {
        return borrowing.getDetails().get(0).getId();
    }

    private int stock(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow().getAvailableQuantity();
    }

    private Member member(String code) {
        Member member = new Member();
        member.setMemberCode(code);
        member.setFullName("Độc giả " + code);
        member.setEmail(code.toLowerCase() + "@example.com");
        member.setPhone("0900000000");
        return memberRepository.save(member);
    }

    private Book book(String title, String isbn, int quantity) {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() -> {
            Category created = new Category();
            created.setName("Thể loại");
            return categoryRepository.save(created);
        });
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Tác giả");
        book.setIsbn(isbn);
        book.setAvailableQuantity(quantity);
        book.setCategory(category);
        return bookRepository.save(book);
    }

    private void policy(int graceDays, String daily, String max) {
        FinePolicy policy = new FinePolicy();
        policy.setDailyFineAmount(new BigDecimal(daily));
        policy.setGraceDays(graceDays);
        policy.setMaxFineAmount(max == null ? null : new BigDecimal(max));
        policy.setActive(true);
        policy.setCreatedAt(LocalDate.now());
        finePolicyRepository.save(policy);
    }
}
