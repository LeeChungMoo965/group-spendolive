package com.example.spendolive.member.domain;

import lombok.Setter;
import lombok.Getter;
@Getter
@Setter
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
	private String open_bank_user_seq_no;
	private String open_bank_token;
	private String fintech_use_num;
	private String bank_code;
	private String account_num;
	
}

