<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<section class="page-hero">
<div class="container">
        <div class="table-card card" >
            <div class="table-wrap">
            <h2>${roomInfo.room_name}</h2>
                            <table>
                                <thead>
                                    <tr>
                                        <th>
                                            구분
                                        </th>
                                        <th>
                                            금액
                                        </th>

                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>
                                            <strong>OTT 사용료</strong>
                                        </td>
                                        <td>
                                            <strong>${base_amount}</strong>
                                        </td>
                                        <td>
                                            <strong>+</strong>
                                        </td>   
                                    </tr>
                                    <tr>
                                        <td>
                                            <strong>수수료(3%)</strong>
                                        </td>
                                        <td>
                                            <strong>${fee_amount}</strong>
                                        </td>
                                        <td>
                                            <strong>+</strong>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <h3>최종 출금 금액</h3>
                                        </td>
                                        <td>
                                            <h3>${total_amount}</h3>
                                        </td>
                                        
                                    </tr>
                                </tbody>
                            </table>
                        </div>
            <table align="center" width="90%">
                <tr>
                    <th align="left" width="20%"><a href="${contextPath}/payment/paymenting.do" class="btn btn-primary full ott-main-btn">정산하기</a></th>
                    <th align="right" width="20%"><a href="${contextPath}/spendolive/ott/friends.do" class="btn btn-primary full ott-main-btn">취소하기</a></th>
                </tr>
            </table>
        </div>
</div>
</section>
