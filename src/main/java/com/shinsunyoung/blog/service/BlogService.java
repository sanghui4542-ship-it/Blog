// ============================================================
// 파일 경로
// src/main/java/me/shinsunyoung/springbootdeveloper/service/BlogService.java
// ============================================================
//
// [이 클래스의 역할]
//
// 블로그 글(Article)에 관한 업무를 처리하는 서비스 클래스입니다.
// 컨트롤러는 요청을 받아 이 클래스에 일을 맡기고, 이 클래스는 리포지토리를 통해 DB 작업을 합니다.
//
// [호출하는 곳]
//
// 1. BlogApiController  : save, findAll, findById, delete, update 를 호출합니다. (JSON API)
// 2. BlogViewController : findAll, findById 를 호출합니다. (화면)
//
// [수정 내용]
//
// 글을 찾지 못하면 이제 ArticleNotFoundException(404)을 던집니다.
// 예전에는 IllegalArgumentException(500)을 던졌습니다.

package com.shinsunyoung.blog.service;


// [1. Lombok 관련 import]
//
// @RequiredArgsConstructor 를 쓰기 위한 import 입니다.
// final 필드(blogRepository)를 받는 생성자를 자동으로 만들어 줍니다.
import com.shinsunyoung.blog.domain.Article;
import com.shinsunyoung.blog.dto.AddArticleRequest;
import com.shinsunyoung.blog.exception.ArticleNotFoundException;
import com.shinsunyoung.blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;


// [2. 프로젝트 내부 import]
//
// Article                  : 글 엔티티입니다. 반환 타입으로 씁니다.
// AddArticleRequest        : 글 등록 요청 상자 (DTO)
// UpdateArticleRequest     : 글 수정 요청 상자 (DTO)
// BlogRepository           : article 테이블에 접근하는 리포지토리입니다.
// ArticleNotFoundException : 글이 없을 때 던지는 404 전용 예외입니다.
import com.shinsunyoung.blog.domain.Article;
import com.shinsunyoung.blog.dto.AddArticleRequest;
import com.shinsunyoung.blog.dto.UpdateArticleRequest;
import com.shinsunyoung.blog.exception.ArticleNotFoundException;
import com.shinsunyoung.blog.repository.BlogRepository;


// [3. 스프링 관련 import]
//
// @Service        : 업무 처리 담당 객체로 스프링에 등록합니다.
// @Transactional  : 메서드 안의 DB 작업을 하나의 묶음(트랜잭션)으로 처리합니다.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// [4. 자바 기본 자료형 import]
//
// List : 여러 글을 담는 목록입니다.
import java.util.List;


// [5. 클래스 위의 어노테이션]

// @RequiredArgsConstructor
// - 아래 final 필드 blogRepository 를 받는 생성자를 자동으로 만듭니다.
// - 스프링이 이 생성자를 보고 BlogRepository 객체를 넣어 줍니다. (생성자 주입)
@RequiredArgsConstructor

// @Service
// - 업무 처리 담당 객체로 스프링에 등록합니다.
@Service
public class BlogService {


    // [6. 리포지토리 필드]
    // - article 테이블 작업을 맡는 객체입니다. 스프링이 생성자로 넣어 줍니다.
    private final BlogRepository blogRepository;


    // [7. save : 새 글 저장]
    //
    // - 호출하는 곳 : BlogApiController.addArticle()
    // - 매개변수 request : 브라우저가 보낸 제목, 본문, 이미지 주소 상자입니다.
    // - 반환값 : 저장된 Article 객체 (id 번호가 채워져 있습니다)
    public Article save(AddArticleRequest request) {

        // request.toEntity() : 요청 상자의 값을 Article 객체로 바꿉니다.
        // blogRepository.save(...) : 그 객체를 article 테이블에 INSERT 하고, 저장된 객체를 돌려줍니다.
        return blogRepository.save(request.toEntity());
    }


    // [8. findAll : 모든 글 목록 조회]
    //
    // - 호출하는 곳 : BlogApiController.findAllArticles(), BlogViewController.getArticles()
    public List<Article> findAll() {

        // blogRepository.findAll() : article 테이블의 모든 행을 SELECT 해서 목록으로 돌려줍니다.
        return blogRepository.findAll();
    }


    // [9. findById : 번호로 글 1개 조회]
    //
    // - 호출하는 곳 : BlogApiController.findArticle(), BlogViewController.getArticle(), BlogViewController.newArticle()
    // - 매개변수 id : 찾을 글 번호
    // - 글이 없으면 ArticleNotFoundException 이 던져지고, 응답은 404 가 됩니다. [수정 내용]
    public Article findById(long id) {

        // [9-1] blogRepository.findById(id)
        // - 번호로 찾은 결과를 Optional 상자에 담아 돌려줍니다. (있으면 Article, 없으면 빈 상자)
        return blogRepository.findById(id)

                // [9-2] orElseThrow(...)
                // - 상자가 비어 있으면 괄호 안의 예외를 던집니다.
                // - 상자에 글이 있으면 그 Article 을 꺼내서 return 으로 넘깁니다.
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }


    // [10. delete : 글 삭제]
    //
    // - 호출하는 곳 : BlogApiController.deleteArticle()
    // - 삭제 전에 글이 있는지 먼저 확인합니다. [수정 내용]
    //   예전에는 없는 번호를 지우면 Spring Data 가 던지는 예외 때문에 500 오류가 났습니다.
    public void delete(long id) {

        // [10-1] existsById(id) : 이 번호의 글이 DB 에 있으면 true, 없으면 false 를 돌려줍니다.
        // !existsById(id) : "없다"면 (true 가 되어) 아래 if 문 안으로 들어갑니다.
        if (!blogRepository.existsById(id)) {

            // [10-2] 글이 없으면 404 전용 예외를 던지고 메서드를 끝냅니다. (아래 삭제 줄은 실행되지 않습니다)
            throw new ArticleNotFoundException(id);
        }

        // [10-3] 글이 있을 때만 여기까지 옵니다. article 테이블에서 이 번호의 행을 DELETE 합니다.
        blogRepository.deleteById(id);
    }


    // [11. update : 글 수정]
    //
    // - 호출하는 곳 : BlogApiController.updateArticle()
    // - @Transactional 이 붙어 있으므로, 객체 값만 바꿔도 메서드가 끝날 때 DB 에 UPDATE 가 저장됩니다.
    //   (save()를 따로 부르지 않아도 되는 이유이며, 이를 '변경 감지'라고 부릅니다)
    @Transactional
    public Article update(long id, UpdateArticleRequest request) {

        // [11-1] 수정할 글을 DB 에서 찾습니다.
        // - 글이 없으면 ArticleNotFoundException(404)이 던져져서 아래 줄은 실행되지 않습니다. [수정 내용]
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        // [11-2] 찾은 글의 제목, 본문, 이미지 주소를 요청 값으로 바꿉니다.
        // - Article.update() 메서드가 필드 값을 바꿉니다.
        article.update(request.getTitle(), request.getContent(), request.getImageUrl());

        // [11-3] 바뀐 글 객체를 호출한 곳(BlogApiController.updateArticle)으로 돌려줍니다.
        return article;
    }
}
