
//삭제기능

//삭제 버튼 영역 <a>태그를 선택해서 저장
const deleteButton = document.getElementById('delete-btn');

//삭제 버튼 영역 <a>태그를 선택했으면?
if(deleteButton){

    deleteButton.addEventListener('click', event => {

        //<input type="hidden">에 설정된 삭제할 글 id 얻기
        let id = document.getElementById('article-id').value;

        //비동기 통신 방법인 fetch 함수를 이용하여 컨트롤러에 삭제 요청
        fetch(`/api/articles/${id}`,{method: 'DELETE'})
            //삭제 요청에 성공 했을 때 화살표 함수가 실행되어 '삭제가 완료되었습니다' 메세지 창 보여주고 글목록 조회 재요청.
            .then(() => { alert('삭제가 완료 되었습니다.');  location.replace('/articles') });

    });

}

//수정기능

//1. id='modify-btn' 인 <수정> <button>를 선택해서 저장
const modifyButton = document.getElementById('modify-btn');

//2. <수정><button>을 선택해 왔다면? click 이벤트를 등록해서 click 했을때 컨트롤러로 수정요청 하자
if(modifyButton){
        modifyButton.addEventListener('click', event =>{

            //수정 요청할 URL을 브라우저에 작성된 http://localhost:8080/articles?id=2준비
            let params = new URLSearchParams(location.search);

            //수정 요청할 글 id 2 준비
            let id = params.get('id');

            //수정 요청! BlogApiController 컨트롤러에 작성된 @PutMapping 어노테이션이 붙은 메소드에 수정요청!
            fetch(`/api/articles/${id}`,
                {method : 'PUT',
                     headers : {"Content-Type":"application/json"},
                     body :  JSON.stringify({title: document.getElementById('title').value,
                                                  content:document.getElementById('content').value })
                    }
                )
                //수정 요청에 성공했을때...사용자의 브라우저에 '수정이 완료 되었습니다.' 메세지 창 보여주고
                //수정 된 글 상세 화면 보여주기 위해 상세 화면 재요청!
                .then(()=>{
                    alert('수정이 완료 되었습니다.');
                    location.replace(`/articles/${id}`);
                });


        });
}

//새로운 글 등록 요청 기능

//1. id='create-btn' 인 <등록> <button>를 선택해서 저장
const createButton = document.getElementById('create-btn');

//2. <등록><button>을 선택해 왔다면? click 이벤트를 등록해서 click 했을때 컨트롤러로 새글 등록 요청 하자
if(createButton){
    createButton.addEventListener('click', event =>{

        //등록 요청! BlogApiController 컨트롤러에 작성된 @PostMapping 어노테이션이 붙은 메소드에 새글 등록요청!
        fetch(`/api/articles`,
            {method : 'POST',
                headers : {"Content-Type":"application/json"},
                body :  JSON.stringify({title: document.getElementById('title').value,
                                             content:document.getElementById('content').value})
                }
            )
            //새 글 등록 요청에 성공했을때...사용자의 브라우저에 '등록이 완료 되었습니다.' 메세지 창 보여주고
            //새 글 등록에 성공했는지 확인 하기 위해 글목록 조회를 재요청한다.
            .then(()=>{
                alert('새글 등록이 완료 되었습니다.');
                location.replace(`/articles`);
            });
    });
}

// [8장 · 글쓰기 도우미] "AI 도움받기" 버튼을 누르면 모달 창(글쓰기 도우미)을 엽니다.
// 창을 열 때마다 이전 제안 영역과 질문 칸을 비워서 깨끗한 상태로 보여 줍니다.
const aiAssistButton = document.getElementById('ai-assist-btn');

if (aiAssistButton) {
    aiAssistButton.addEventListener('click', event => {

        // $('#aiAssistModal').modal('show')는 부트스트랩(jQuery) 기능으로 모달 창을 띄웁니다.
        $('#aiAssistModal').modal('show');

        document.getElementById('ai-suggestion').style.display = 'none';
        document.getElementById('ai-question').value = '';
    });
}

// [8장 · 글쓰기 도우미] 모달 안의 "제안 받기" 버튼: 지금 쓰는 제목·본문·질문을 모아 AI에게 보냅니다.
const getSuggestionButton = document.getElementById('get-suggestion-btn');

if (getSuggestionButton) {
    getSuggestionButton.addEventListener('click', event => {

        const title = document.getElementById('title').value;
        const content = document.getElementById('content').value;
        const question = document.getElementById('ai-question').value;

        // 질문이 비어 있으면 AI가 무엇을 도와줄지 알 수 없으므로 막습니다.
        if (!question.trim()) {
            alert('고민되는 내용을 입력해주세요.');
            return;
        }

        // 로딩 표시
        document.getElementById('ai-loading').style.display = 'block';
        document.getElementById('ai-suggestion').style.display = 'none';

        // 서버(WritingSuggestionRequest record)의 필드 이름(title, content, question)과 똑같은 키로 JSON을 만듭니다.
        const body = JSON.stringify({
            title: title,
            content: content,
            question: question
        });

        // POST /api/ai-suggestions 제안 요청-> BlogApiController ->  WritingAssistantService가 AI에게 물어본 결과를 받습니다.
        fetch('/api/ai-suggestions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: body
        })
            .then(response => {
                return response.json();
            })
            .then(data => {
                // 응답이 도착했으니 로딩 문구를 숨깁니다.
                document.getElementById('ai-loading').style.display = 'none';

                const suggestionContent = document.getElementById('ai-suggestion-content');

                // 제안 목록(data.suggestions)을 클릭 가능한 목록 HTML로 만듭니다.
                let html = '';
                if (data.suggestions && data.suggestions.length > 0) {
                    html += '<ul class="list-group">';
                    data.suggestions.forEach((suggestion, index) => {
                        // data-suggestion: 클릭했을 때 본문에 넣을 글을 담아 둡니다. 큰따옴표는 &quot;로 바꿔 깨짐을 막습니다.
                        html += `<li class="list-group-item suggestion-item" style="cursor: pointer;" data-suggestion="${suggestion.replace(/"/g, '&quot;')}" title="클릭하면 본문에 추가됩니다">
                                ${suggestion}
                                <small class="text-muted float-right">클릭하여 추가</small>
                                </li>`;
                    });
                    html += '</ul>';
                }

                suggestionContent.innerHTML = html;
                document.getElementById('ai-suggestion').style.display = 'block';
            })
    });
}

// [8장 · 글쓰기 도우미] 제안 목록 중 하나를 클릭하면 본문 끝에 그 문장을 붙이고 모달을 닫습니다.
// 이벤트 위임: 목록 항목이 나중에 만들어지므로 부모(suggestionContent)에 클릭 이벤트를 한 번만 답니다.
const suggestionContent = document.getElementById('ai-suggestion-content');
if (suggestionContent) {
    suggestionContent.addEventListener('click', function(e) {
        // 클릭한 곳에서 가장 가까운 제안 항목(.suggestion-item)을 찾습니다.
        const suggestionItem = e.target.closest('.suggestion-item');
        if (suggestionItem) {
            const suggestion = suggestionItem.getAttribute('data-suggestion');
            const contentTextarea = document.getElementById('content');

            // 본문 끝에 줄바꿈 두 번을 넣고 제안을 붙입니다. (이미 줄바꿈으로 끝나면 줄바꿈을 더 넣지 않습니다)
            const currentContent = contentTextarea.value;
            const separator = currentContent && !currentContent.endsWith('\n') ? '\n\n' : '';
            contentTextarea.value = currentContent + separator + suggestion;

            $('#aiAssistModal').modal('hide');

            contentTextarea.focus();
        }
    });
}
















