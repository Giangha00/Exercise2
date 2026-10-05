package com.example.sem4.exercise2.service;

import com.example.sem4.exercise2.dto.BookForm;
import com.example.sem4.exercise2.entity.Book;
import com.example.sem4.exercise2.entity.Category;
import com.example.sem4.exercise2.exception.BusinessException;
import com.example.sem4.exercise2.repository.BookRepository;
import com.example.sem4.exercise2.repository.CategoryRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, Long categoryId, Pageable pageable) {
        return bookRepository.findAll(filter(keyword, categoryId), pageable);
    }

    @Transactional(readOnly = true)
    public Book get(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sách"));
    }

    @Transactional(rollbackFor = Exception.class)
    public Book save(BookForm form) {
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy thể loại"));
        String isbn = form.getIsbn().trim();
        Book book;
        if (form.getId() == null) {
            if (bookRepository.existsByIsbn(isbn)) {
                throw new BusinessException("ISBN đã tồn tại");
            }
            book = new Book();
        } else {
            book = get(form.getId());
            if (bookRepository.existsByIsbnAndIdNot(isbn, book.getId())) {
                throw new BusinessException("ISBN đã tồn tại");
            }
        }
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor().trim());
        book.setIsbn(isbn);
        book.setAvailableQuantity(form.getAvailableQuantity());
        book.setCategory(category);
        return bookRepository.save(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public Category addCategory(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Tên thể loại không được để trống");
        }
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BusinessException("Thể loại đã tồn tại");
        }
        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description == null ? null : description.trim());
        return categoryRepository.save(category);
    }

    private Specification<Book> filter(String keyword, Long categoryId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like),
                        cb.like(cb.lower(root.get("isbn")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
