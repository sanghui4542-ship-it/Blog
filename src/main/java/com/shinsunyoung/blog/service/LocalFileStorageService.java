// ============================================================
// 파일 경로
// src/main/java/me/shinsunyoung/springbootdeveloper/service/LocalFileStorageService.java
// ============================================================
//
// [이 클래스의 역할]
//
// FileStorageService 규격을 실제로 구현한 클래스입니다.
// 업로드된 파일을 서버 컴퓨터의 폴더(기본값: 프로젝트 실행 위치의 uploads 폴더)에 저장합니다.
//
// [호출하는 곳]
//
// 1. UploadController.uploadFile()                 : 사용자가 고른 이미지 파일을 저장할 때
// 2. ThumbnailGeneratorService.generateThumbnail() : AI 가 만든 썸네일을 저장할 때
//
// [수정 내용]
//
// 파일 저장이 실패하면, 이제 원래 오류(IOException)를 함께 넘기고 어떤 파일명인지 메시지에 넣습니다.
// 예전에는 메시지 없는 RuntimeException 을 던져서 원인을 알 수 없었습니다.

package com.shinsunyoung.blog.service;


// [1. 프로젝트 내부 import]
//
// UploadResponse : 저장 결과(저장된 파일 주소)를 담는 상자 (DTO)
import com.shinsunyoung.blog.dto.UploadResponse;


// [2. 스프링 관련 import]
//
// @Value   : application.yml 같은 설정 값을 변수에 넣어 줄 때 쓰는 표시입니다.
// @Service : 이 클래스를 스프링 객체로 등록합니다.
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


// [3. 자바 파일 처리 관련 import]
//
// IOException : 파일 읽기·쓰기 중 생기는 오류의 종류입니다.
// Files       : 폴더 만들기, 파일 쓰기 같은 작업을 해 주는 도구 클래스입니다.
// Path        : 폴더나 파일의 위치(경로)를 나타내는 객체입니다.
// Paths       : 문자열 경로를 Path 객체로 바꿔 주는 도구입니다.
// UUID        : 겹치지 않는 무작위 문자열을 만드는 클래스입니다.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


// [4. 클래스 선언]
// @Service
// - 스프링 객체로 등록합니다.
// implements FileStorageService
// - 저장 규격(store 메서드)을 구현합니다.
@Service
public class LocalFileStorageService implements FileStorageService {


    // [5. 저장 폴더 경로 필드]
    // - 파일을 저장할 절대 경로입니다. 생성자에서 한 번 정해지고 바뀌지 않습니다.
    private final Path uploadDir;


    // [6. 생성자 : 저장 폴더 경로 정하기]
    //
    // @Value("${app.upload-dir:uploads}")
    // - application.yml 에 app.upload-dir 값이 있으면 그 값을 넣습니다.
    // - 값이 없으면 콜론(:) 뒤의 기본값 "uploads" 를 넣습니다.
    public LocalFileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {

        // Paths.get(uploadDir) : 문자열 "uploads" 를 경로 객체로 바꿉니다.
        // .toAbsolutePath()    : 현재 실행 위치를 기준으로 전체 경로로 바꿉니다. (예: C:/project/uploads)
        // .normalize()         : "./", "../" 같은 중간 표현을 정리합니다.
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }


    // [7. store : 파일 저장]
    //
    // - 호출하는 곳 : UploadController.uploadFile(), ThumbnailGeneratorService.generateThumbnail()
    // - 매개변수 bytes    : 파일 내용 (바이트 배열)
    // - 매개변수 filename : 원래 파일 이름 (확장자를 얻는 데 씁니다)
    // - 반환값 : 웹에서 접근할 주소를 담은 UploadResponse
    @Override
    public UploadResponse store(byte[] bytes, String filename) {
        try {

            // [7-1] 저장 폴더가 없으면 만듭니다. 이미 있으면 아무 일도 하지 않습니다.
            Files.createDirectories(uploadDir);

            // [7-2] 원래 파일 이름에서 확장자(예: .png)를 꺼냅니다.
            // - 이름에 점(.)이 있으면 마지막 점부터 끝까지를 잘라냅니다.
            // - 점이 없으면 기본 확장자 ".png" 를 씁니다.
            String ext = filename.contains(".")
                    ? filename.substring(filename.lastIndexOf('.'))
                    : ".png";

            // [7-3] 무작위 고유 이름에 확장자를 붙여 새 파일 이름을 만듭니다. (예: 3f2a...-.png)
            // - 원래 이름을 그대로 쓰면 같은 이름의 파일이 덮어써지므로 UUID 를 씁니다.
            String saved = UUID.randomUUID() + ext;

            // [7-4] 저장 폴더 안에 새 이름으로 파일을 씁니다.
            // - uploadDir.resolve(saved) : 폴더 경로 뒤에 파일 이름을 붙인 전체 경로를 만듭니다.
            Files.write(uploadDir.resolve(saved), bytes);

            // [7-5] 웹 주소를 만들어 돌려줍니다. 예: /uploads/3f2a....png
            // - WebMvcConfig 가 /uploads/** 요청을 이 폴더로 연결해 주므로 이 주소로 이미지를 볼 수 있습니다.
            return new UploadResponse("/uploads/" + saved);

        } catch (IOException e) {

            // [7-6] 저장에 실패했을 때
            // - new RuntimeException(메시지, e) : 메시지와 원래 오류(e)를 함께 담아 던집니다.
            // - 메시지에 파일 이름을 넣어서, 로그를 보면 어떤 파일에서 실패했는지 바로 알 수 있습니다. [수정 내용]
            // - 원래 오류(e)가 함께 들어가므로, 로그에서 "디스크가 꽉 찼다" 같은 실제 원인도 확인할 수 있습니다.
            throw new RuntimeException("파일 저장에 실패했습니다. 파일명=" + filename, e);
        }
    }
}
