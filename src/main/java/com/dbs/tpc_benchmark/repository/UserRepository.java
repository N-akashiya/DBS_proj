package com.dbs.tpc_benchmark.repository;

import com.dbs.tpc_benchmark.typings.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);
    List<User> findByStatus(String status);
    List<User> findByRole(String role);
}