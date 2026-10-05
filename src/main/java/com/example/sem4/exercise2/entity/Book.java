package com.example.sem4.exercise2.entity;

import com.example.sem4.exercise2.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, unique = true, length = 50)
    private String isbn;

    @Column(nullable = false)
    private Integer availableQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Optimistic locking: hai giao dịch cùng sửa một sách thì giao dịch ghi sau bị từ chối.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    public void decreaseAvailable(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mượn phải lớn hơn 0");
        }
        int current = availableQuantity == null ? 0 : availableQuantity;
        if (current < quantity) {
            throw new InsufficientStockException(
                    "Sách \"" + title + "\" chỉ còn " + current + " cuốn, không đủ để mượn " + quantity
                            + ". Toàn bộ phiếu được hoàn tác.");
        }
        this.availableQuantity = current - quantity;
    }

    public void increaseAvailable(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng hoàn trả phải lớn hơn 0");
        }
        int current = availableQuantity == null ? 0 : availableQuantity;
        this.availableQuantity = current + quantity;
    }
}
