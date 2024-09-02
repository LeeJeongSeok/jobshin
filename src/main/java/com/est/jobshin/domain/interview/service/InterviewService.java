package com.est.jobshin.domain.interview.service;

import com.est.jobshin.domain.interview.domain.Interview;
import com.est.jobshin.domain.interview.dto.InterviewDto;
import com.est.jobshin.domain.interview.repository.InterviewRepository;
import com.est.jobshin.domain.interviewDetail.domain.InterviewDetail;
import com.est.jobshin.domain.interviewDetail.dto.InterviewQuestion2;
import com.est.jobshin.domain.interviewDetail.dto.InterviewResultDetail;
import com.est.jobshin.domain.interviewDetail.service.InterviewDetailService;
import com.est.jobshin.domain.interviewDetail.util.Category;
import com.est.jobshin.domain.interviewDetail.util.Mode;
import com.est.jobshin.domain.user.domain.User;
import com.est.jobshin.domain.user.dto.UserResponse;
import com.est.jobshin.domain.user.repository.UserRepository;
import com.est.jobshin.global.security.model.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final InterviewDetailService interviewDetailService;
    private final UserRepository userRepository;

    @Transactional
    public InterviewDto getInterviewById(Long id) {
        return interviewRepository
                .findById(id)
                .map(InterviewDto::fromInterview)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));
    }

    @Transactional
    public Interview createPracticeInterview(Category category, HttpSession session) {
        User user = getCurrentUser();
        Interview interview = Interview.createInterview(
                null, LocalDateTime.now(), user, Mode.PRACTICE
        );

        Interview createdInterview = interviewRepository.save(interview);

//        interviewDetailService.createInterviewDetail(interview);
        interviewDetailService.practiceModeStarter(interview, category, user);

        session.setAttribute("questions", new ArrayList<>(interview.getInterviewDetails()));
        session.setAttribute("currentIndex", 0);

        session.setAttribute("interviewId", createdInterview.getId());

        return createdInterview;
    }

    @Transactional
    public Interview createRealInterview(HttpSession session) {
        User user = getCurrentUser();
        Interview interview = Interview.createInterview(
                null, LocalDateTime.now(), user, Mode.REAL
        );

        Interview createdInterview = interviewRepository.save(interview);

//        interviewDetailService.createInterviewDetail(interview);
        interviewDetailService.realModeStarter(interview, user);

        session.setAttribute("questions", new ArrayList<>(interview.getInterviewDetails()));
        session.setAttribute("currentIndex", 0);

        session.setAttribute("interviewId", createdInterview.getId());

        return createdInterview;
    }

    @Transactional
    public InterviewQuestion2 processAnswerAndGetNextQuestion(HttpSession session, InterviewQuestion2 interviewQuestion2) {
        InterviewQuestion2 nextQuestion = getNextQuestion2(session);

        CompletableFuture.runAsync(() -> {
            //interviewDetailService 에서 getAnswerByUser 메서드 비동기 실행
            interviewDetailService.getAnswerByUser(interviewQuestion2);
        });

        return nextQuestion;
    }

    public InterviewQuestion2 getNextQuestion2(HttpSession session) {
        List<InterviewDetail> questions = (List<InterviewDetail>) session.getAttribute("questions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        if (questions == null || currentIndex == null || currentIndex >= questions.size()) {
            return null;
        }

        InterviewDetail question = questions.get(currentIndex);
        session.setAttribute("currentIndex", currentIndex + 1);

        return InterviewQuestion2.from(question);
    }

    public List<InterviewResultDetail> finishInterview(HttpSession session) {
        return getInterviewDetails((Long) session.getAttribute("interviewId"));
    }

    public List<InterviewResultDetail> getInterviewDetails(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NoSuchElementException("Interview not found"));

        List<InterviewDetail> interviewDetails = interview.getInterviewDetails();

        return interviewDetails.stream()
                .map(InterviewResultDetail::from)
                .collect(Collectors.toList());
    }

    public void deleteInterviewsById(Long id) {
        interviewRepository.deleteById(id);
    }

    //현재 세션의 사용자 정보 가져오기
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new RuntimeException();
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}