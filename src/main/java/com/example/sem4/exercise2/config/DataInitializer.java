package com.example.sem4.exercise2.config;

import com.example.sem4.exercise2.entity.Book;
import com.example.sem4.exercise2.entity.Category;
import com.example.sem4.exercise2.entity.FinePolicy;
import com.example.sem4.exercise2.entity.Member;
import com.example.sem4.exercise2.repository.BookRepository;
import com.example.sem4.exercise2.repository.CategoryRepository;
import com.example.sem4.exercise2.repository.FinePolicyRepository;
import com.example.sem4.exercise2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FinePolicyRepository finePolicyRepository;

    @Override
    public void run(String... args) {
        if (finePolicyRepository.count() == 0) {
            FinePolicy policy = new FinePolicy();
            policy.setDailyFineAmount(new BigDecimal("5000"));
            policy.setGraceDays(1);
            policy.setMaxFineAmount(new BigDecimal("150000"));
            policy.setActive(true);
            policy.setCreatedAt(LocalDate.now());
            finePolicyRepository.save(policy);
        }
        if (categoryRepository.count() > 0) {
            return;
        }
        Category literature = category("Văn học", "Tiểu thuyết và truyện");
        Category science = category("Khoa học", "Khoa học thường thức");
        Category technology = category("Công nghệ thông tin", "Lập trình và phần mềm");

        book("Số đỏ", "Vũ Trọng Phụng", "978-0001", 5, literature);
        book("Dế Mèn phiêu lưu ký", "Tô Hoài", "978-0002", 4, literature);
        book("Nhà giả kim", "Paulo Coelho", "978-0003", 1, literature);
        book("Lược sử thời gian", "Stephen Hawking", "978-0004", 3, science);
        book("Clean Code", "Robert C. Martin", "978-0005", 3, technology);
        book("Spring in Action", "Craig Walls", "978-0006", 2, technology);

        member("M001", "Nguyễn Văn An", "an@example.com", "0901000001");
        member("M002", "Trần Thị Bình", "binh@example.com", "0901000002");
        member("M003", "Lê Hoàng Cường", "cuong@example.com", "0901000003");
    }

    private Category category(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private void book(String title, String author, String isbn, int quantity, Category category) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setAvailableQuantity(quantity);
        book.setCategory(category);
        bookRepository.save(book);
    }

    private void member(String code, String name, String email, String phone) {
        Member member = new Member();
        member.setMemberCode(code);
        member.setFullName(name);
        member.setEmail(email);
        member.setPhone(phone);
        memberRepository.save(member);
    }
}
