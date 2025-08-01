package edu.sookmyung.talktitude.memo.repository;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.report.model.Memo;
import edu.sookmyung.talktitude.report.model.MemoPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByChatSessionAndMemoPhase(ChatSession chatSession, MemoPhase memoPhase);
    Memo findByChatSessionAndMemberAndMemoPhase(ChatSession chatSession, Member currentMember, MemoPhase memoPhase);
    Memo findByChatSession(ChatSession chatSession);
}

