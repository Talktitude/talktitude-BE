package edu.sookmyung.talktitude.memo.service;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.memo.dto.MemoResponse;
import edu.sookmyung.talktitude.memo.repository.MemoRepository;
import edu.sookmyung.talktitude.memo.dto.MemoRequest;
import edu.sookmyung.talktitude.report.model.Memo;
import edu.sookmyung.talktitude.report.model.MemoPhase;
import edu.sookmyung.talktitude.report.model.Report;
import edu.sookmyung.talktitude.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static edu.sookmyung.talktitude.memo.dto.MemoResponse.convertToMemoResponse;


@Service
@RequiredArgsConstructor
public class MemoService {

    private final ChatSessionRepository chatSessionRepository;
    private final MemoRepository memoRepository;
    private final ReportRepository reportRepository;

    //메모 생성 기능
    @Transactional
    public MemoResponse registerMemos(Long sessionId, MemoRequest memo, Member currentMember) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId).orElseThrow(()->new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));
        Memo savedMemo;
        //오른쪽 패널을 통해 상담 중 작성된 메모인 경우
        if(chatSession.getStatus() == Status.IN_PROGRESS) {

            //해당 세션 담당 Member만
            if(!chatSession.getMember().getLoginId().equals(currentMember.getLoginId())) {
                throw new BaseException(ErrorCode.CHAT_SESSION_ACCESS_DENIED);
            }
            //기존 메모가 있으면 업데이트, 없으면 생성
            Memo existingMemo = memoRepository.findDuringChatByChatSessionAndMemberAndMemoPhase(
                    chatSession, currentMember, MemoPhase.DURING_CHAT).orElse(null);

            if(existingMemo != null) {
                existingMemo.updateMemo(memo.memoText());
                savedMemo = existingMemo;
            }else{
                savedMemo = Memo.builder().member(currentMember).chatSession(chatSession).memoText(memo.memoText()).memoPhase(MemoPhase.DURING_CHAT).build();
            }
        }else{
            //리포트에서 댓글 형식으로 작성된 메모인 경우 -> 모든 Member
            savedMemo = Memo.builder().member(currentMember).chatSession(chatSession).memoText(memo.memoText()).memoPhase(MemoPhase.AFTER_CHAT).build();
        }
        memoRepository.save(savedMemo);
        return convertToMemoResponse(savedMemo);
    }

    //메모 삭제 기능
    @Transactional
    public void deleteMemo(Long memoId,Member currentMember) {
        Memo existingMemo = memoRepository.findById(memoId).orElseThrow(()->new BaseException(ErrorCode.MEMO_NOT_FOUND));
        //메모 소유자인지 확인
        if(!existingMemo.getMember().getLoginId().equals(currentMember.getLoginId())) {
            throw new BaseException(ErrorCode.MEMO_ACCESS_DENIED);
        }
        memoRepository.delete(existingMemo);
    }

    public List<MemoResponse> getAfterChatMemosForReport(Long sessionId) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));


        //상담 이후 작성된 메모 기준으로 조회
        return memoRepository.findByChatSessionAndMemoPhase(chatSession, MemoPhase.AFTER_CHAT)
                .stream()
                .map(MemoResponse::convertToMemoResponse)
                .collect(Collectors.toList());
    }

    public List<MemoResponse> getDuringChatMemosForReport(Long sessionId) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        //상담 중 작성된 메모 기준으로 조회
        return memoRepository.findByChatSessionAndMemoPhase(chatSession, MemoPhase.DURING_CHAT)
                .stream()
                .map(MemoResponse::convertToMemoResponse)
                .collect(Collectors.toList());
    }

    // 리포트에서 조회 가능한 전체 메모 -> 프론트에서는 타입별로 분류(memoPhase)
    public List<MemoResponse> getMemos(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));
        ChatSession chatSession = chatSessionRepository.findById(report.getChatSession().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        List<MemoResponse> reportMemoList = new ArrayList<>();
        reportMemoList.addAll(getAfterChatMemosForReport(chatSession.getId()));
        reportMemoList.addAll(getDuringChatMemosForReport(chatSession.getId()));

        return reportMemoList;
    }


    //메인화면 오른쪽 패널에서 확인 가능한 상담 메모
    @Transactional
    public List<MemoResponse> getDuringChatUserMemos(Long sessionId, Member currentMember) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId).orElseThrow(()->new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        //세션 멤버와 현재 로그인 사용자가 같은지 검증
        if(!chatSession.getMember().getLoginId().equals(currentMember.getLoginId())) {
            throw new BaseException(ErrorCode.CHAT_SESSION_ACCESS_DENIED);
        }

        //현재 로그인 사용자 + 상담 중 작성된 메모 기준으로 조회
        return memoRepository.findByChatSessionAndMemberAndMemoPhase(chatSession,currentMember, MemoPhase.DURING_CHAT)
                .stream()
                .map(MemoResponse::convertToMemoResponse)
                .collect(Collectors.toList());
    }

}
