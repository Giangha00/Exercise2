package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.dto.BorrowItem;
import com.example.sem4.exercise2.dto.ReturnItem;
import com.example.sem4.exercise2.dto.ReturnPreview;
import com.example.sem4.exercise2.entity.Book;
import com.example.sem4.exercise2.entity.Borrowing;
import com.example.sem4.exercise2.entity.BorrowingDetail;
import com.example.sem4.exercise2.entity.BorrowingStatus;
import com.example.sem4.exercise2.entity.FinePayment;
import com.example.sem4.exercise2.entity.FinePolicy;
import com.example.sem4.exercise2.entity.FineWaiver;
import com.example.sem4.exercise2.entity.Member;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.exception.InsufficientStockException;
import com.example.sem4.exercise2.exception.InvalidPaymentException;
import com.example.sem4.exercise2.exception.InvalidReturnException;
import com.example.sem4.exercise2.exception.MemberNotEligibleException;
import com.example.sem4.exercise2.repository.BookRepository;
import com.example.sem4.exercise2.repository.BorrowingDetailRepository;
import com.example.sem4.exercise2.repository.BorrowingRepository;
import com.example.sem4.exercise2.repository.MemberRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private static final int MAX_OUTSTANDING_COPIES = 5;

    private final BorrowingRepository borrowingRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FinePolicyService finePolicyService;
    private final FineCalculator fineCalculator;

    /**
     * Trừ tồn kho và tạo phiếu trong cùng một transaction.
     * Một dòng không hợp lệ hoặc xung đột @Version làm RuntimeException,
     * Spring hoàn tác mọi thay đổi tồn kho đã flush trong giao dịch này.
     */
    @Transactional(rollbackFor = Exception.class)
    public Borrowing createBorrowing(Long memberId, LocalDate borrowedDate, LocalDate dueDate, List<BorrowItem> items) {
        if (borrowedDate == null || dueDate == null) {
            throw new BusinessException("Thiếu ngày mượn hoặc hạn trả");
        }
        if (dueDate.isBefore(borrowedDate)) {
            throw new BusinessException("Hạn trả không được trước ngày mượn");
        }
        List<BorrowItem> lines = normalizeBorrowItems(items);

        refreshOverdue(LocalDate.now());
        Member member = memberRepository.lockById(memberId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy độc giả"));
        if (borrowingRepository.countBlocking(member.getId()) > 0) {
            throw new MemberNotEligibleException(
                    "Độc giả đang có phiếu quá hạn hoặc còn phí phạt chưa thanh toán, chưa được tạo phiếu mới");
        }
        long outstanding = borrowingDetailRepository.sumOutstandingCopies(member.getId());
        int requested = lines.stream().mapToInt(BorrowItem::quantity).sum();
        if (outstanding + requested > MAX_OUTSTANDING_COPIES) {
            throw new MemberNotEligibleException(
                    "Mỗi độc giả chỉ được giữ tối đa " + MAX_OUTSTANDING_COPIES
                            + " cuốn chưa trả. Đang giữ " + outstanding + " cuốn.");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setMember(member);
        borrowing.setBorrowedDate(borrowedDate);
        borrowing.setDueDate(dueDate);

        for (BorrowItem item : lines) {
            Book book = bookRepository.findById(item.bookId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy sách " + item.bookId()));
            try {
                book.decreaseAvailable(item.quantity());
                bookRepository.saveAndFlush(book);
            } catch (OptimisticLockingFailureException ex) {
                throw new InsufficientStockException(
                        "Sách \"" + book.getTitle() + "\" vừa được mượn đồng thời. Toàn bộ phiếu được hoàn tác.");
            }
            BorrowingDetail detail = new BorrowingDetail();
            detail.setBook(book);
            detail.setQuantity(item.quantity());
            detail.setReturnedQuantity(0);
            detail.setLateDays(0);
            detail.setFineAmount(BigDecimal.ZERO);
            borrowing.addDetail(detail);
        }

        borrowing.refreshStatus(LocalDate.now());
        return borrowingRepository.save(borrowing);
    }

    @Transactional(readOnly = true)
    public ReturnPreview previewReturn(Long borrowingId, LocalDate returnDate, List<ReturnItem> items) {
        Borrowing borrowing = getRequired(borrowingId);
        LocalDate actualReturnDate = returnDate == null ? LocalDate.now() : returnDate;
        validateReturnDate(borrowing, actualReturnDate);
        if (!borrowing.canReturn()) {
            throw new InvalidReturnException("Không thể trả sách với phiếu trạng thái " + borrowing.getStatus().getLabel());
        }
        FinePolicy policy = finePolicyService.requireActive();
        List<ReturnPreview.Line> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean any = false;
        for (ReturnItem item : safeReturnItems(items)) {
            if (item.quantity() <= 0) {
                continue;
            }
            any = true;
            BorrowingDetail detail = findDetail(borrowing, item.detailId());
            ensureReturnQuantity(detail, item.quantity());
            FineCalculator.FineQuote quote = fineCalculator.quote(
                    borrowing.getDueDate(), actualReturnDate, item.quantity(), detail.getFineAmount(), policy);
            lines.add(new ReturnPreview.Line(detail.getBook().getTitle(), item.quantity(), quote.lateDays(), quote.amount()));
            total = total.add(quote.amount());
        }
        if (!any) {
            throw new InvalidReturnException("Nhập số lượng trả lớn hơn 0 cho ít nhất một đầu sách");
        }
        return new ReturnPreview(total, lines);
    }

    /**
     * Cập nhật số đã trả, tồn kho và phí phạt trong cùng một transaction.
     * Lỗi ở bước tính phí hoặc tồn kho sẽ hoàn tác toàn bộ lần trả.
     */
    @Transactional(rollbackFor = Exception.class)
    public Borrowing returnBooks(Long borrowingId, LocalDate returnDate, List<ReturnItem> items) {
        Borrowing borrowing = getRequired(borrowingId);
        LocalDate actualReturnDate = returnDate == null ? LocalDate.now() : returnDate;
        validateReturnDate(borrowing, actualReturnDate);
        if (!borrowing.canReturn()) {
            throw new InvalidReturnException("Không thể trả sách với phiếu trạng thái " + borrowing.getStatus().getLabel());
        }
        FinePolicy policy = finePolicyService.requireActive();
        boolean any = false;
        for (ReturnItem item : safeReturnItems(items)) {
            if (item.quantity() <= 0) {
                continue;
            }
            any = true;
            BorrowingDetail detail = findDetail(borrowing, item.detailId());
            ensureReturnQuantity(detail, item.quantity());
            FineCalculator.FineQuote quote = fineCalculator.quote(
                    borrowing.getDueDate(), actualReturnDate, item.quantity(), detail.getFineAmount(), policy);
            detail.setReturnedQuantity(detail.getReturnedQuantity() + item.quantity());
            detail.setFineAmount(detail.getFineAmount().add(quote.amount()));
            detail.setLateDays(quote.lateDays());
            detail.setLastReturnedDate(actualReturnDate);
            Book book = detail.getBook();
            try {
                book.increaseAvailable(item.quantity());
                bookRepository.saveAndFlush(book);
            } catch (OptimisticLockingFailureException ex) {
                throw new BusinessException("Tồn kho sách \"" + book.getTitle() + "\" vừa thay đổi. Lần trả này được hoàn tác.");
            }
        }
        if (!any) {
            throw new InvalidReturnException("Nhập số lượng trả lớn hơn 0 cho ít nhất một đầu sách");
        }
        borrowing.refreshStatus(actualReturnDate);
        return borrowingRepository.save(borrowing);
    }

    @Transactional(rollbackFor = Exception.class)
    public Borrowing payFine(Long borrowingId, BigDecimal amount, LocalDate paymentDate, String method, String note) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException("Số tiền thanh toán phải lớn hơn 0");
        }
        if (method == null || method.isBlank()) {
            throw new InvalidPaymentException("Chọn hình thức thanh toán");
        }
        Borrowing borrowing = getRequired(borrowingId);
        if (borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new InvalidPaymentException("Không thể thanh toán phiếu đã hủy");
        }
        if (amount.compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new InvalidPaymentException(
                    "Không được thanh toán vượt quá số phí còn phải thu (" + borrowing.getUnpaidFineAmount().toPlainString() + " đ)");
        }
        FinePayment payment = new FinePayment();
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate == null ? LocalDate.now() : paymentDate);
        payment.setMethod(method.trim());
        payment.setNote(note == null ? null : note.trim());
        borrowing.addPayment(payment);
        borrowing.refreshStatus(LocalDate.now());
        return borrowingRepository.save(borrowing);
    }

    @Transactional(rollbackFor = Exception.class)
    public Borrowing waiveFine(Long borrowingId, BigDecimal amount, String reason, String approvedBy) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException("Số tiền miễn giảm phải lớn hơn 0");
        }
        if (reason == null || reason.isBlank() || approvedBy == null || approvedBy.isBlank()) {
            throw new InvalidPaymentException("Miễn giảm phí cần lý do và người duyệt");
        }
        Borrowing borrowing = getRequired(borrowingId);
        if (borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new InvalidPaymentException("Không thể miễn giảm phiếu đã hủy");
        }
        if (amount.compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new InvalidPaymentException("Số tiền miễn giảm vượt quá phí còn phải thu");
        }
        FineWaiver waiver = new FineWaiver();
        waiver.setAmount(amount);
        waiver.setReason(reason.trim());
        waiver.setApprovedBy(approvedBy.trim());
        waiver.setApprovedDate(LocalDate.now());
        borrowing.addWaiver(waiver);
        borrowing.refreshStatus(LocalDate.now());
        return borrowingRepository.save(borrowing);
    }

    @Transactional(rollbackFor = Exception.class)
    public Borrowing cancel(Long borrowingId) {
        Borrowing borrowing = getRequired(borrowingId);
        if (!borrowing.canCancel()) {
            throw new BusinessException("Chỉ được hủy phiếu khi chưa trả sách và chưa phát sinh phí");
        }
        for (BorrowingDetail detail : borrowing.getDetails()) {
            Book book = detail.getBook();
            book.increaseAvailable(detail.getQuantity());
            bookRepository.saveAndFlush(book);
        }
        borrowing.setStatus(BorrowingStatus.CANCELLED);
        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing getForView(Long id) {
        refreshOverdue(LocalDate.now());
        Borrowing borrowing = getRequired(id);
        borrowing.getMember().getFullName();
        borrowing.getDetails().forEach(detail -> detail.getBook().getTitle());
        borrowing.getPayments().size();
        borrowing.getWaivers().size();
        return borrowing;
    }

    @Transactional
    public Page<Borrowing> search(BorrowingStatus status, Long memberId, LocalDate fromDate, LocalDate toDate,
                                  boolean overdueOnly, Pageable pageable) {
        refreshOverdue(LocalDate.now());
        return borrowingRepository.findAll(filter(status, memberId, fromDate, toDate, overdueOnly), pageable);
    }

    @Transactional
    public void refreshOverdue(LocalDate today) {
        List<Borrowing> stale = borrowingRepository.findByStatusInAndDueDateBefore(
                List.of(BorrowingStatus.BORROWING, BorrowingStatus.PARTIALLY_RETURNED), today);
        for (Borrowing borrowing : stale) {
            borrowing.setStatus(BorrowingStatus.OVERDUE);
        }
        if (!stale.isEmpty()) {
            borrowingRepository.flush();
        }
    }

    private Borrowing getRequired(Long id) {
        return borrowingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn"));
    }

    private List<BorrowItem> normalizeBorrowItems(List<BorrowItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Phiếu mượn phải có ít nhất một đầu sách");
        }
        Set<Long> seen = new HashSet<>();
        List<BorrowItem> lines = new ArrayList<>();
        for (BorrowItem item : items) {
            if (item == null || item.bookId() == null) {
                throw new BusinessException("Mỗi dòng mượn phải chọn sách");
            }
            if (item.quantity() <= 0) {
                throw new BusinessException("Số lượng mượn phải lớn hơn 0");
            }
            if (!seen.add(item.bookId())) {
                throw new BusinessException("Không được có hai dòng trùng một đầu sách trong cùng phiếu");
            }
            lines.add(item);
        }
        return lines;
    }

    private List<ReturnItem> safeReturnItems(List<ReturnItem> items) {
        return items == null ? List.of() : items;
    }

    private void validateReturnDate(Borrowing borrowing, LocalDate returnDate) {
        if (returnDate.isBefore(borrowing.getBorrowedDate())) {
            throw new InvalidReturnException("Ngày trả không được trước ngày mượn");
        }
    }

    private BorrowingDetail findDetail(Borrowing borrowing, Long detailId) {
        return borrowing.getDetails().stream()
                .filter(detail -> detail.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new InvalidReturnException("Dòng sách không thuộc phiếu này"));
    }

    private void ensureReturnQuantity(BorrowingDetail detail, int quantity) {
        if (quantity > detail.getRemainingQuantity()) {
            throw new InvalidReturnException(
                    "Không được trả nhiều hơn số đang mượn của \"" + detail.getBook().getTitle()
                            + "\" (còn " + detail.getRemainingQuantity() + " cuốn)");
        }
    }

    private Specification<Borrowing> filter(BorrowingStatus status, Long memberId, LocalDate fromDate,
                                            LocalDate toDate, boolean overdueOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (memberId != null) {
                predicates.add(cb.equal(root.get("member").get("id"), memberId));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("borrowedDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("borrowedDate"), toDate));
            }
            if (overdueOnly) {
                predicates.add(cb.equal(root.get("status"), BorrowingStatus.OVERDUE));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
