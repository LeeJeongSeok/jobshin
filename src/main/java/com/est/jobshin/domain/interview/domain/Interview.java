package com.est.jobshin.domain.interview.domain;

import com.est.jobshin.domain.interviewDetail.domain.InterviewDetail;

import com.est.jobshin.domain.interviewDetail.util.Mode;
import com.est.jobshin.domain.user.domain.User;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "interviews")
@Entity
@Builder
public class Interview {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Enumerated(EnumType.STRING)
	private Mode mode;

	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InterviewDetail> interviewDetails = new ArrayList<>();

	private Interview(String title, LocalDateTime createdAt, User user, Mode mode) {
		this.title = title;
		this.createdAt = createdAt;
		this.user = user;
		this.mode = mode;
	}

	public static Interview createInterview(String title, LocalDateTime createdAt, User user, Mode mode) {
		return new Interview(title, createdAt, user, mode);
	}

	public void addInterviewDetails(InterviewDetail interview){
		interviewDetails.add(interview);
		interview.setInterview(this);
	}

	public void removeInterviewDetails(InterviewDetail interview){
		interviewDetails.clear();
		interview.setInterview(null);
	}
}
