package com.spring.batch.repository.pay;

import com.spring.batch.entity.pay.Pay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayRepository extends JpaRepository<Pay, Long> {

    List<Pay> findAllByAmountGreaterThanEqual(long amount);
}
