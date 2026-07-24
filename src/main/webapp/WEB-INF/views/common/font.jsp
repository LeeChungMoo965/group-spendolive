<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>

<button id="fontToggle" class="font-toggle2">간편</button>

<div id="fontPanel" class="font-panel">
  <div class="font-header">
    <button type="button" id="btn-font-down" class="btn btn-primary mini" title="글자 크기 축소">▼</button>
    <button type="button" id="btn-font-up" class="btn btn-primary mini" title="글자 크기 확대">▲</button> 
    <button type="button" id="fontClose" class="chatbot-close">×</button>

  </div>
      <!-- 폰트 선택 드롭다운 추가 -->
    <select id="fontSelect" class="font-select-box" title="폰트 선택">
 <option value="system">기본 폰트</option>
      <option value="jua">주아체 (Jua)</option>
      <option value="sans-serif">고딕 (Sans-Serif)</option>
      <option value="serif">명조 (Serif)</option>
      <option value="monospace">고정폭 (Monospace)</option>
    </select>
</div>