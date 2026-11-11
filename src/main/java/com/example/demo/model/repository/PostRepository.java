package com.example.demo.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.Post;
import com.example.demo.model.domain.Member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository // 게시글 리포지토리 : save, findAll, findById, delete 는 이미 있다
public interface PostRepository extends JpaRepository<Post, Long> {

    // [연습②] 내가 쓴 글 : 작성자(author)의 username 으로, 번호 큰 순서(최신 글)로
    List<Post> findByAuthor_UsernameOrderByIdDesc(String username);

    // [10주차] 단계 2 : 제목 또는 내용에 검색어가 들어간 글 (JPQL)
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByAuthor(Member author); // 이 회원이 쓴 글이 있는지

}