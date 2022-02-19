package com.smartcontactmanager.dao;

import com.smartcontactmanager.model.Contact;
import com.smartcontactmanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    @Query("from Contact as c where c.user.id = :userId")
    // pageable-> current per page
    // and contact per page - 5
    public Page<Contact> findContactsByUserId(@Param("userId") int userId, Pageable pageable);

    // search
    public List<Contact> findByNameContainingAndUser(String keywords, User user);
}
