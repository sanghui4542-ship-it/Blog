// ============================================================
// [파일 설명] 블로그 글과 AI 기능을 JSON 으로 주고받는 REST API 컨트롤러입니다.
// 화면(article.js)의 fetch() 요청이 이 파일의 메소드로 들어옵니다.
//
// [@RestController 의 뜻]
//   메소드가 돌려주는 객체를 HTML 화면이 아니라 JSON 데이터로 바꿔서 브라우저에 보냅니다.
//
// [전체 주소 목록]
//   POST   /api/articles         : 글 등록       → addArticle
//   GET    /api/articles         : 글 목록 조회  → findAllArticles
//   GET    /api/articles/{id}    : 글 1개 조회   → findArticle
//   DELETE /api/articles/{id}    : 글 삭제       → deleteArticle
//   PUT    /api/articles/{id}    : 글 수정       → updateArticle
//   POST   /api/ai-suggestions   : AI 글쓰기 도우미 → writingAssist
//   POST   /api/ai-thumbnails    : AI 썸네일 생성    → thumbnailGenerator
// ============================================================
package com.shinsunyoung.blog.controller;

// Lombok : final 필드를 받는 생성자를 자동으로 만듭니다.
import com.shinsunyoung.blog.domain.Article;
import com.shinsunyoung.blog.dto.AddArticleRequest;
import com.shinsunyoung.blog.dto.ArticleResponse;
import com.shinsunyoung.blog.dto.UpdateArticleRequest;
import com.shinsunyoung.blog.service.BlogService;
import com.shinsunyoung.blog.service.ThumbnailGeneratorService;
import com.shinsunyoung.blog.service.WritingAssistantService;
import lombok.RequiredArgsConstructor;

// Article : 글 엔티티입니다. 등록·수정 응답에서 그대로 JSON 으로 나갑니다.
import com.shinsunyoung.blog.domain.Article;

// dto 패키지의 모든 클래스(*)를 한꺼번에 가져옵니다.
//   AddArticleRequest, ArticleResponse, UpdateArticleRequest, WritingSuggestionRequest 등이 여기에 해당합니다.
import com.shinsunyoung.blog.dto.*;

// 서비스 클래스들 : 실제 업무를 맡는 객체입니다. 컨트롤러는 요청을 받아 이 객체들에게 넘깁니다.
import com.shinsunyoung.blog.service.BlogService;
import com.shinsunyoung.blog.service.ThumbnailGeneratorService;
import com.shinsunyoung.blog.service.WritingAssistantService;

// HttpStatus : HTTP 상태 번호를 이름으로 쓸 수 있게 한 값입니다. (CREATED = 201, OK = 200)
import org.springframework.http.HttpStatus;

// ResponseEntity : 응답의 상태 코드, 헤더, 본문을 한꺼번에 담는 응답 상자입니다.
import org.springframework.http.ResponseEntity;

// web.bind.annotation.* : @RestController, @PostMapping, @GetMapping, @PathVariable, @RequestBody 등 웹 요청 표시들입니다.
import org.springframework.web.bind.annotation.*;

// List : 글 목록을 담는 자료형입니다.
import java.util.List;

// [@RequiredArgsConstructor]
//   아래 final 필드 3개를 받는 생성자를 자동으로 만듭니다.
//   스프링이 이 생성자를 보고 BlogService, WritingAssistantService, ThumbnailGeneratorService 객체를 넣어 줍니다.
@RequiredArgsConstructor

// [@RestController] 이 클래스의 메소드들이 돌려주는 값을 JSON 으로 바꿔서 응답한다는 표시입니다.
@RestController
public class BlogApiController {

    // 글 업무를 맡는 서비스입니다.
    private final BlogService blogService;

    // AI 글쓰기 도우미 서비스입니다.
    private final WritingAssistantService writingAssistantService;

    // AI 썸네일 생성 서비스입니다.
    private final ThumbnailGeneratorService thumbnailGeneratorService;

    // ---------------------------------------------------------------
    // 글 등록 : POST /api/articles
    // ---------------------------------------------------------------
    // @PostMapping("/api/articles") : POST 방식으로 /api/articles 요청이 오면 이 메소드가 실행됩니다.
    // @RequestBody AddArticleRequest request :
    //   요청 본문의 JSON 을 AddArticleRequest 객체로 자동 변환해서 넣어 줍니다.
    //   (JSON 의 title, content, imageUrl 키가 같은 이름의 필드에 들어갑니다)
    // 반환 타입 ResponseEntity<Article> : 상태 코드와 Article 객체를 함께 돌려줍니다.
    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(@RequestBody AddArticleRequest request) {

        // blogService.save(request) : 글을 DB 에 저장하고, 저장된 Article(id 포함)을 받습니다.
        Article savedArticle = blogService.save(request);

        // ResponseEntity.status(HttpStatus.CREATED) : 상태 코드 201(Created, 생성됨)을 설정합니다.
        //   .body(savedArticle) : 응답 본문에 저장된 글을 넣습니다. 이 글은 JSON 으로 바뀌어 나갑니다.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    // ---------------------------------------------------------------
    // 글 목록 조회 : GET /api/articles
    // ---------------------------------------------------------------
    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles() {

        // 1) blogService.findAll() : 모든 Article 목록을 가져옵니다.
        // 2) .stream() : 목록을 하나씩 꺼내며 처리할 수 있는 흐름(스트림)으로 바꿉니다.
        // 3) .map(ArticleResponse::new) : 각 Article 을 ArticleResponse 로 바꿉니다.
        //      ArticleResponse::new 는 new ArticleResponse(article) 과 같은 뜻입니다.
        // 4) .toList() : 바뀐 결과를 다시 목록으로 모읍니다.
        List<ArticleResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleResponse::new)
                .toList();

        // 상태 코드 200(OK)과 함께 목록을 돌려줍니다.
        return ResponseEntity.ok()
                .body(articles);
    }

    // ---------------------------------------------------------------
    // 글 1개 조회 : GET /api/articles/{id}
    // ---------------------------------------------------------------
    // {id} : 주소 중 바뀌는 부분입니다. 예: /api/articles/3 이면 id 는 3 입니다.
    // @PathVariable long id : 주소의 {id} 값을 꺼내서 long 타입 변수 id 에 넣습니다.
    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable long id) {

        // 번호로 글 1개를 찾습니다. 없으면 BlogService 에서 예외가 발생합니다.
        Article article = blogService.findById(id);

        // ArticleResponse 로 바꿔서 200 응답으로 돌려줍니다.
        return ResponseEntity.ok()
                .body(new ArticleResponse(article));
    }

    // ---------------------------------------------------------------
    // 글 삭제 : DELETE /api/articles/{id}
    // ---------------------------------------------------------------
    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable long id) {

        // 번호에 해당하는 글을 삭제합니다.
        blogService.delete(id);

        // Void : 돌려줄 본문이 없다는 뜻입니다. build() 는 본문 없이 200 응답을 만듭니다.
        return ResponseEntity.ok()
                .build();
    }

    // ---------------------------------------------------------------
    // 글 수정 : PUT /api/articles/{id}
    // ---------------------------------------------------------------
    // @PathVariable long id : 수정할 글 번호
    // @RequestBody UpdateArticleRequest request : 새 제목·본문·이미지 주소가 담긴 JSON
    @PutMapping("/api/articles/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable long id,
                                                 @RequestBody UpdateArticleRequest request) {

        // 수정 업무를 BlogService 에 맡기고, 수정된 Article 을 받습니다.
        Article updatedArticle = blogService.update(id, request);

        // 200 응답 본문에 수정된 글을 담아 돌려줍니다.
        return ResponseEntity.ok()
                .body(updatedArticle);
    }

    // ---------------------------------------------------------------
    // AI 글쓰기 도우미 : POST /api/ai-suggestions
    // ---------------------------------------------------------------
    // 요청 JSON {"title", "content", "question"} → WritingSuggestionRequest 로 변환
    // 응답 JSON {"suggestions":[...]} ← WritingSuggestionsResponse 를 JSON 으로 변환
    @PostMapping("/api/ai-suggestions")
    public ResponseEntity<WritingSuggestionsResponse> writingAssist(@RequestBody WritingSuggestionRequest request) {

        // AI 에게 질문하고 제안 목록을 받는 일은 WritingAssistantService 가 합니다.
        WritingSuggestionsResponse response = writingAssistantService.getWritingAssist(request);

        // 200 응답 본문에 제안 목록을 담아 돌려줍니다.
        return ResponseEntity.ok()
                .body(response);
    }

    // ---------------------------------------------------------------
    // AI 썸네일 생성 : POST /api/ai-thumbnails
    // ---------------------------------------------------------------
    // 요청 JSON {"title", "content"} → GeneratorThumbnailRequest 로 변환
    // 응답 JSON {"imageUrl": "/uploads/..."} ← GeneratorThumbnailResponse 를 JSON 으로 변환
    @PostMapping("/api/ai-thumbnails")
    public ResponseEntity<GeneratorThumbnailResponse> thumbnailGenerator(@RequestBody GeneratorThumbnailRequest request) {

        // 썸네일 생성·저장은 ThumbnailGeneratorService 가 맡고, 저장된 이미지 주소를 받습니다.
        GeneratorThumbnailResponse response = thumbnailGeneratorService.generateThumbnail(request);

        // 200 응답 본문에 이미지 주소를 담아 돌려줍니다.
        return ResponseEntity.ok()
                .body(response);
    }
}
