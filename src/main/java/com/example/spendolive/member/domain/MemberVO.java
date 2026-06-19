package com.example.spendolive.member.domain;

import lombok.Setter;

import org.springframework.stereotype.Component;

import lombok.Getter;
@Getter
@Setter
@Component
public class MemberVO {
	private int member_id;
	private String email;
	private String password;
	private String id;
	private String member_name;
	private String nickname;
	private String phone;
	private String login_type;
	private String role;
	private String status;
	private String created_at;
	private String update_at;

	
}

