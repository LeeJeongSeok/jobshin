package com.est.jobshin.domain.user.domain;

import com.est.jobshin.domain.interview.domain.Interview;
import com.est.jobshin.domain.posts.domain.Post;
import com.est.jobshin.domain.user.util.BaseEntity;
import com.est.jobshin.domain.user.util.Language;
import com.est.jobshin.domain.user.util.Level;
import com.est.jobshin.domain.user.util.Position;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "email", unique = true)
	private String email;


	@Column(name = "password")
	private String password;

	@Column(name = "username")
	private String username;

	@Column(name = "language")
	@Enumerated(EnumType.STRING)
	private Language language;

	@Column(name = "level")
	@Enumerated(EnumType.STRING)
	@Default
	private Level level = Level.LV2;

	@Column(name = "position")
	@Enumerated(EnumType.STRING)
	private Position position;
}
