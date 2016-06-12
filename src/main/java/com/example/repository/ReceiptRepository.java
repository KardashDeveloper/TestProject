package com.example.repository;

import com.example.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kardash on 09.06.2016.
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
