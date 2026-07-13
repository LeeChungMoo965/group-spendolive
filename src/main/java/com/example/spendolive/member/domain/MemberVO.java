package com.example.spendolive.member.domain;

import lombok.Data;

@Data
public class MemberVO {
	private int member_id;
	private int warning_count;
	private String email;
	private String password;
	private String id;
	private String member_name;
	private String nickname;
	private String phone;
	private String login_type;
	private String verify_type;
	private String role;
	private String status;
	private String created_at;
	private String blocked_until;
	private String update_at;
	private String last_login_at;
	private String card_status;
	private String account_status;
	private String warninged_at;
	
}

