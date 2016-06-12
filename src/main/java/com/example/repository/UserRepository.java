package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Created by Kardash on 07.06.2016.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmailContaining(String email);//TODO shouldn't be through Containing

    void deleteAll();
}
