<div id="memberStatusOverlay"
                    class="status-overlay"
                    role="dialog"
                    aria-modal="true"
                    aria-labelledby="memberStatusTitle"
                    aria-describedby="memberStatusMessage"
                    hidden>
                    <div class="status-box">
                    
                    <div id="memberStatusSpinner"
                            class="status-spinner"
                            aria-hidden="true"></div>

                        <div id="memberStatusIcon"
                            class="status-icon"
                            aria-hidden="true"
                            hidden></div>
                            <h3 id="memberStatusTitle">중복 확인중 입니다.</h3>
                        <p id="memberStatusMessage">
                            창을 닫거나 새로고침하지 말아주세요.
                        </p>
                    <div id="memberStatusActions"
                            class="status-actions"
                            hidden>
                            <button type="button"
                                    id="memberStatusCloseButton"
                                    class="btn btn-outline">
                                확인
                            </button>
                            <button type="button"
                                    id="memberStatusActionButton"
                                    class="btn btn-primary"
                                    hidden>
                                이동하기
                            </button>
                        </div>
                    </div>
                </div>