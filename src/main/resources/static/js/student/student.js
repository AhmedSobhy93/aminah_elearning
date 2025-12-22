// document.addEventListener("DOMContentLoaded", () => {
//     const csrfToken = document.querySelector('meta[name="_csrf"]').content;
//
//     // Search courses
//     document.getElementById("courseSearch").addEventListener("input", e => {
//         const query = e.target.value.toLowerCase();
//         document.querySelectorAll("#coursesContainer .card").forEach(card => {
//             const title = card.querySelector(".card-title").textContent.toLowerCase();
//             card.style.display = title.includes(query) ? "block" : "none";
//         });
//     });
//
//     // Load sections dynamically
//     document.querySelectorAll(".btn-view-sections").forEach(btn => {
//         btn.addEventListener("click", async () => {
//             const courseId = btn.dataset.courseId;
//             const container = btn.closest(".card").querySelector(".section-container");
//             const html = await fetch(`/student/sections/${courseId}`).then(res => res.text());
//             container.innerHTML = html;
//
//             // Add tutorial buttons
//             container.querySelectorAll(".btn-view-tutorials").forEach(tutBtn => {
//                 tutBtn.addEventListener("click", async () => {
//                     const sectionId = tutBtn.dataset.sectionId;
//                     const tutContainer = tutBtn.closest(".card-body").querySelector(".tutorial-container");
//                     const html = await fetch(`/student/tutorials/${sectionId}`).then(res => res.text());
//                     tutContainer.innerHTML = html;
//
//                     // Tutorial modal buttons
//                     tutContainer.querySelectorAll(".btn-view-tutorial").forEach(vBtn => {
//                         vBtn.addEventListener("click", async () => {
//                             const tutorialId = vBtn.dataset.tutorialId;
//                             const modalHtml = await fetch(`/student/tutorial-modal/${tutorialId}`).then(res => res.text());
//                             document.getElementById("modalTutorialContainer").innerHTML = modalHtml;
//                             new bootstrap.Modal(document.getElementById("modalViewTutorial")).show();
//                         });
//                     });
//                 });
//             });
//         });
//     });
//
//     // Enroll in course
//     document.querySelectorAll(".btn-enroll").forEach(btn => {
//         btn.addEventListener("click", async () => {
//             const courseId = btn.dataset.courseId;
//             const res = await fetch(`/student/enroll/${courseId}`, {
//                 method: "POST",
//                 headers: {"X-CSRF-TOKEN": csrfToken}
//             });
//             if (res.ok) location.reload();
//         });
//     });
//
// });
//
// /* ============================================================
//    GLOBAL STUDENT FUNCTIONS (ALL PAGES)
//    ============================================================ */

/* ------------------------------
   QUIZ SUBMIT
   ------------------------------ */
// document.addEventListener("click", async (e) => {
//
//     if (!e.target.matches("#submitQuizBtn")) return;
//
//     const tutorialId = document.getElementById("tutorialId").value;
//     const form = new FormData(document.getElementById("quizForm"));
//
//     const answers = {};
//     [...form.entries()].forEach(([qid, ans]) => answers[qid] = parseInt(ans));
//
//     const res = await fetch(`/student/tutorial/${tutorialId}/quiz`, {
//         method: "POST",
//         headers: {"Content-Type": "application/json"},
//         body: JSON.stringify(answers)
//     });
//
//     const data = await res.json();
//
//     if (data.success) {
//         alert("Your Score: " + data.score);
//         location.reload();
//     }
// });

/* ------------------------------
   MARK COMPLETED
   ------------------------------ */
// document.addEventListener("click", async (e) => {
//
//     if (!e.target.matches("#markCompleteBtn")) return;
//
//     const tutorialId = document.getElementById("tutorialId").value;
//
//     const res = await fetch(`/student/tutorial/${tutorialId}/complete`, {
//         method: "POST"
//     });
//
//     const data = await res.json();
//
//     if (data.success) {
//         alert("Marked completed!");
//         location.reload();
//     }
// });

/* ------------------------------
   ACCORDION AUTO-SAVE PROGRESS (optional)
   ------------------------------ */
//
// document.addEventListener("DOMContentLoaded", function () {
//     const tutorialModal = document.getElementById('tutorialModal');
//     const tutorialContent = document.getElementById('tutorialContent');
//
//     if (!tutorialModal) return;
//
//     // Use event delegation for multiple buttons
//     document.querySelectorAll('.view-tutorial-btn').forEach(button => {
//         button.addEventListener('click', function () {
//             const tutId = this.getAttribute('data-tut-id');
//
//             // Show loading message
//             tutorialContent.innerHTML = '<p>Loading...</p>';
//
//             // Fetch tutorial content
//             fetch('/student/tutorial/' + tutId)
//                 .then(res => res.text())
//                 .then(html => {
//                     tutorialContent.innerHTML = html;
//
//                     // Mark as read on the server
//                     fetch('/student/tutorial/' + tutId + '/mark-read', { method: 'POST' })
//                         .then(() => {
//                             // Update badge dynamically
//                             const card = button.closest('.tutorial-card');
//                             if (card) {
//                                 const badge = card.querySelector('.read-badge');
//                                 if (badge) {
//                                     badge.classList.remove('bg-secondary');
//                                     badge.classList.add('bg-success');
//                                     badge.innerHTML = '<i class="fa-solid fa-check-circle"></i> Read';
//                                 }
//                             }
//                         });
//                 });
//         });
//     });
// });
//
//
/////////////////////////
/////////////////////////////////////////////////
///////////////////////////
// document.addEventListener("DOMContentLoaded", () => {
//
//     let tutorialCompleted = false;
//     let currentTutorialId = null;
//
//     const modalEl = document.getElementById("tutorialModal");
//     const modalTitle = document.getElementById("tutorialModalTitle");
//     const modalBody = document.getElementById("tutorialContent");
//
//     const markCompletedOnce = (id) => {
//         if (tutorialCompleted) return;
//         tutorialCompleted = true;
//         console.log("Marking tutorial complete:", id);
//         if (typeof completeTutorial === "function") completeTutorial(id);
//     };
//
//     const resetModal = () => {
//         tutorialCompleted = false;
//         currentTutorialId = null;
//         if (modalTitle) modalTitle.textContent = "";
//         if (modalBody) {
//             modalBody.querySelectorAll("video").forEach(v => {
//                 v.pause();
//                 v.src = "";
//             });
//             ["#viewFileContainer", "#viewArticleContent", "#viewQuizQuestionsContainer", "#quizResult"].forEach(sel => {
//                 const el = modalBody.querySelector(sel);
//                 if (el) el.innerHTML = "";
//             });
//             modalBody.querySelectorAll("#viewFileSection, #viewArticleSection, #viewQuizSection").forEach(s => {
//                 s.classList.add("d-none");
//             });
//         }
//     };
//
//     document.querySelectorAll(".view-tutorial-btn").forEach(btn => {
//         btn.addEventListener("click", async () => {
//
//             resetModal();
//             currentTutorialId = btn.dataset.tutId || null;
//             if (modalTitle) modalTitle.textContent = btn.dataset.title || "Tutorial";
//
//             // Show loading spinner
//             const fileContainer = modalBody.querySelector("#viewFileContainer");
//             if (fileContainer) fileContainer.innerHTML = `
//                 <div class="text-center py-4">
//                     <i class="fa-solid fa-spinner fa-spin fa-2x text-primary"></i>
//                 </div>
//             `;
//
//             try {
//                 const res = await fetch(`/student/tutorial/${currentTutorialId}/json`);
//                 if (!res.ok) throw new Error("Failed to fetch tutorial");
//                 const t = await res.json();
//
//                 // === VIDEO ===
//                 if (t.type === "VIDEO") {
//                     const container = modalBody.querySelector("#viewFileContainer");
//                     if (container) container.innerHTML = `
//                         <video id="tutorialVideo" class="w-100" controls>
//                             <source src="${t.filePath}" type="video/mp4">
//                         </video>
//                     `;
//                     const section = modalBody.querySelector("#viewFileSection");
//                     if (section) section.classList.remove("d-none");
//
//                     const video = document.getElementById("tutorialVideo");
//                     if (video) video.addEventListener("ended", () => markCompletedOnce(t.id), { once: true });
//                 }
//
//                 // === PDF ===
//                 if (t.type === "PDF") {
//                     const container = modalBody.querySelector("#viewFileContainer");
//                     if (container) container.innerHTML = `<embed src="${t.filePath}" type="application/pdf" style="width:100%; height:70vh;">`;
//                     const section = modalBody.querySelector("#viewFileSection");
//                     if (section) section.classList.remove("d-none");
//
//                     // Mark complete after 3 seconds, but DO NOT close modal
//                     setTimeout(() => markCompletedOnce(t.id), 3000);
//                 }
//
//                 // === ARTICLE ===
//                 if (t.type === "ARTICLE") {
//                     const article = modalBody.querySelector("#viewArticleContent");
//                     if (article) article.innerHTML = t.articleContent;
//                     const section = modalBody.querySelector("#viewArticleSection");
//                     if (section) section.classList.remove("d-none");
//
//                     let completed = false;
//                     const markIfNotYet = () => {
//                         if (!completed) {
//                             completed = true;
//                             markCompletedOnce(t.id);
//                         }
//                     };
//
//                     if (article) {
//                         article.addEventListener("scroll", () => {
//                             if (article.scrollTop + article.clientHeight >= article.scrollHeight - 5) {
//                                 markIfNotYet();
//                             }
//                         });
//                     }
//
//                     // Also mark complete automatically after 8 seconds
//                     setTimeout(markIfNotYet, 8000);
//                 }
//
//                 // === QUIZ ===
//                 if (t.type === "QUIZ") {
//                     const quizContainer = modalBody.querySelector("#viewQuizQuestionsContainer");
//                     if (quizContainer) quizContainer.innerHTML = "";
//
//                     t.quizQuestions.forEach((q, i) => {
//                         const div = document.createElement("div");
//                         div.className = "mb-3 p-3 border rounded";
//                         div.innerHTML = `
//                             <strong>Q${i + 1}. ${q.question}</strong>
//                             <ul class="list-group mt-2">
//                                 ${q.options.map((opt, idx) =>
//                             `<li class="list-group-item">
//                                         <input type="radio" name="q${i}" value="${idx}"> ${String.fromCharCode(65 + idx)}. ${opt}
//                                     </li>`).join("")}
//                             </ul>
//                         `;
//                         quizContainer.appendChild(div);
//                     });
//
//                     const quizSection = modalBody.querySelector("#viewQuizSection");
//                     if (quizSection) quizSection.classList.remove("d-none");
//
//                     const submitBtn = modalBody.querySelector("#submitQuizBtn");
//                     const resultDiv = modalBody.querySelector("#quizResult");
//
//                     if (submitBtn && resultDiv) {
//                         submitBtn.onclick = () => {
//                             let score = 0;
//                             t.quizQuestions.forEach((q, i) => {
//                                 const selected = modalBody.querySelector(`input[name=q${i}]:checked`);
//                                 if (selected && parseInt(selected.value) === q.correctOptionIndex) score++;
//                             });
//                             const total = t.quizQuestions.length;
//                             if (score === total) {
//                                 resultDiv.innerHTML = `<div class="alert alert-success">All correct! Tutorial completed.</div>`;
//                                 markCompletedOnce(t.id);
//                             } else {
//                                 resultDiv.innerHTML = `<div class="alert alert-warning">Score ${score}/${total}. Try again!</div>`;
//                             }
//                         };
//                     }
//                 }
//
//                 // Show modal
//                 if (modalEl) bootstrap.Modal.getOrCreateInstance(modalEl).show();
//
//             } catch (err) {
//                 if (modalBody) modalBody.innerHTML = `<div class="alert alert-danger">Failed to load tutorial</div>`;
//                 console.error(err);
//             }
//
//         });
//     });
//
//     if (modalEl) modalEl.addEventListener("hidden.bs.modal", resetModal);
//
// });















document.addEventListener("DOMContentLoaded", () => {

    let tutorialCompleted = false;
    let currentTutorialId = null;

    const offcanvasEl = document.getElementById("tutorialOffcanvas");
    const offcanvasTitle = document.getElementById("offcanvasLabel");
    const offcanvasBody = document.getElementById("offcanvasContent");

    const offcanvas = new bootstrap.Offcanvas(offcanvasEl);

    const markCompletedOnce = (id) => {
        if (tutorialCompleted) return;
        tutorialCompleted = true;
        console.log("Tutorial completed:", id);
        if (typeof completeTutorial === "function") completeTutorial(id);
    };

    const resetOffcanvas = () => {
        tutorialCompleted = false;
        currentTutorialId = null;
        offcanvasTitle.textContent = "";
        ["#viewFileContainer","#viewArticleContent","#viewQuizQuestionsContainer","#quizResult"].forEach(sel => {
            const el = offcanvasBody.querySelector(sel);
            if (el) el.innerHTML = "";
        });
        ["#viewFileSection","#viewArticleSection","#viewQuizSection"].forEach(sel => {
            offcanvasBody.querySelector(sel).classList.add("d-none");
        });
    };

    document.querySelectorAll(".view-tutorial-btn").forEach(btn => {
        btn.addEventListener("click", async e => {
            e.preventDefault();
            resetOffcanvas();

            currentTutorialId = btn.dataset.tutId;
            offcanvasTitle.textContent = btn.closest(".tutorial-card").querySelector("b").textContent;

            const fileContainer = offcanvasBody.querySelector("#viewFileContainer");
            fileContainer.innerHTML = `<div class="text-center py-4">
                <i class="fa-solid fa-spinner fa-spin fa-2x text-primary"></i>
            </div>`;

            try {
                const res = await fetch(`/student/tutorial/${currentTutorialId}/json`);
                if (!res.ok) throw new Error("Failed to fetch tutorial");
                const t = await res.json();

                // VIDEO
                if (t.type === "VIDEO") {
                    const container = offcanvasBody.querySelector("#viewFileContainer");
                    container.innerHTML = `<video id="tutorialVideo" class="w-100" controls>
                        <source src="${t.filePath}" type="video/mp4">
                    </video>`;
                    offcanvasBody.querySelector("#viewFileSection").classList.remove("d-none");

                    const video = document.getElementById("tutorialVideo");
                    video.addEventListener("ended", () => markCompletedOnce(t.id), { once: true });
                }

                // PDF
                if (t.type === "PDF") {
                    const container = offcanvasBody.querySelector("#viewFileContainer");
                    container.innerHTML = `<embed src="${t.filePath}" type="application/pdf" style="width:100%; height:60vh;">`;
                    offcanvasBody.querySelector("#viewFileSection").classList.remove("d-none");
                    // auto-complete after 3s
                    setTimeout(() => markCompletedOnce(t.id), 3000);
                }

                // ARTICLE
                if (t.type === "ARTICLE") {
                    const article = offcanvasBody.querySelector("#viewArticleContent");
                    article.innerHTML = t.articleContent;
                    offcanvasBody.querySelector("#viewArticleSection").classList.remove("d-none");

                    const markIfEnd = () => markCompletedOnce(t.id);
                    article.addEventListener("scroll", () => {
                        if (article.scrollTop + article.clientHeight >= article.scrollHeight - 5) markIfEnd();
                    });
                    // optional: auto-complete after timeout
                    setTimeout(markIfEnd, 8000);
                }

                // QUIZ
                if (t.type === "QUIZ") {
                    const quizContainer = offcanvasBody.querySelector("#viewQuizQuestionsContainer");
                    quizContainer.innerHTML = "";
                    t.quizQuestions.forEach((q,i) => {
                        const div = document.createElement("div");
                        div.className = "mb-3 p-3 border rounded";
                        div.innerHTML = `<strong>Q${i+1}. ${q.question}</strong>
                            <ul class="list-group mt-2">
                            ${q.options.map((opt,idx) => `<li class="list-group-item">
                                <input type="radio" name="q${i}" value="${idx}"> ${String.fromCharCode(65+idx)}. ${opt}
                            </li>`).join('')}
                            </ul>
                            <div class="quiz-feedback mt-1"></div>`;
                        quizContainer.appendChild(div);
                    });
                    offcanvasBody.querySelector("#viewQuizSection").classList.remove("d-none");

                    const submitBtn = offcanvasBody.querySelector("#submitQuizBtn");
                    const resultDiv = offcanvasBody.querySelector("#quizResult");
                    const retryBtn = offcanvasBody.querySelector("#retryBtn");

                    submitBtn.onclick = () => {
                        let score = 0;
                        t.quizQuestions.forEach((q,i) => {
                            const sel = offcanvasBody.querySelector(`input[name=q${i}]:checked`);
                            if (sel && parseInt(sel.value) === q.correctOptionIndex) score++;
                        });
                        const total = t.quizQuestions.length;
                        if (score === total) {
                            resultDiv.innerHTML = `<div class="alert alert-success">All correct! Tutorial completed.</div>`;
                            markCompletedOnce(t.id);
                        } else {
                            resultDiv.innerHTML = `<div class="alert alert-warning">Score ${score}/${total}. Try again!</div>`;
                            retryBtn.classList.remove("d-none");
                        }
                    };

                    retryBtn.onclick = () => {
                        offcanvasBody.querySelector("#quizForm").reset();
                        offcanvasBody.querySelectorAll(".quiz-feedback").forEach(f=>f.innerHTML='');
                        resultDiv.innerHTML = '';
                        retryBtn.classList.add("d-none");
                    };
                }

                offcanvas.show();

            } catch (err) {
                offcanvasBody.innerHTML = `<div class="alert alert-danger">Failed to load tutorial</div>`;
                console.error(err);
            }
        });
    });

    offcanvasEl.addEventListener("hidden.bs.offcanvas", resetOffcanvas);

});












///////////////////
// document.addEventListener("DOMContentLoaded", () => {
//
//     let tutorialCompleted = false;
//     let currentTutorialId = null;
//
//     const offcanvasEl = document.getElementById("tutorialOffcanvas");
//     const offcanvasTitle = document.getElementById("offcanvasLabel");
//     const offcanvasBody = document.getElementById("offcanvasContent");
//
//     const markCompletedOnce = (id) => {
//         if (tutorialCompleted) return;
//         tutorialCompleted = true;
//         console.log("Marking tutorial complete:", id);
//         if (typeof completeTutorial === "function") completeTutorial(id);
//     };
//
//     const resetOffcanvas = () => {
//         tutorialCompleted = false;
//         currentTutorialId = null;
//         if (offcanvasTitle) offcanvasTitle.textContent = "";
//         if (offcanvasBody) {
//             offcanvasBody.querySelectorAll("video").forEach(v => {
//                 v.pause();
//                 v.src = "";
//             });
//             ["#viewFileContainer", "#viewArticleContent", "#viewQuizQuestionsContainer", "#quizResult"].forEach(sel => {
//                 const el = offcanvasBody.querySelector(sel);
//                 if (el) el.innerHTML = "";
//             });
//             offcanvasBody.querySelectorAll("#viewFileSection, #viewArticleSection, #viewQuizSection").forEach(s => {
//                 s.classList.add("d-none");
//             });
//         }
//     };
//
//     const offcanvas = new bootstrap.Offcanvas(offcanvasEl);
//
//     document.querySelectorAll(".view-tutorial-btn").forEach(btn => {
//         btn.addEventListener("click", async () => {
//             resetOffcanvas();
//             currentTutorialId = btn.dataset.tutId || null;
//             if (offcanvasTitle) offcanvasTitle.textContent = btn.closest(".tutorial-card").querySelector("b").textContent;
//
//             const fileContainer = offcanvasBody.querySelector("#viewFileContainer");
//             if (fileContainer) fileContainer.innerHTML = `<div class="text-center py-4">
//                 <i class="fa-solid fa-spinner fa-spin fa-2x text-primary"></i>
//             </div>`;
//
//             try {
//                 const res = await fetch(`/student/tutorial/${currentTutorialId}/json`);
//                 if (!res.ok) throw new Error("Failed to fetch tutorial");
//                 const t = await res.json();
//
//                 // VIDEO
//                 if (t.type === "VIDEO") {
//                     const container = offcanvasBody.querySelector("#viewFileContainer");
//                     container.innerHTML = `<video id="tutorialVideo" class="w-100" controls>
//                         <source src="${t.filePath}" type="video/mp4">
//                     </video>`;
//                     offcanvasBody.querySelector("#viewFileSection").classList.remove("d-none");
//
//                     const video = document.getElementById("tutorialVideo");
//                     video.addEventListener("ended", () => markCompletedOnce(t.id), { once: true });
//                 }
//
//                 // PDF
//                 if (t.type === "PDF") {
//                     const container = offcanvasBody.querySelector("#viewFileContainer");
//                     container.innerHTML = `<embed src="${t.filePath}" type="application/pdf" style="width:100%; height:60vh;">`;
//                     offcanvasBody.querySelector("#viewFileSection").classList.remove("d-none");
//                     setTimeout(() => markCompletedOnce(t.id), 3000);
//                 }
//
//                 // ARTICLE
//                 if (t.type === "ARTICLE") {
//                     const article = offcanvasBody.querySelector("#viewArticleContent");
//                     article.innerHTML = t.articleContent;
//                     offcanvasBody.querySelector("#viewArticleSection").classList.remove("d-none");
//
//                     let completed = false;
//                     const markIfNotYet = () => { if (!completed) { completed = true; markCompletedOnce(t.id); } };
//                     article.addEventListener("scroll", () => {
//                         if (article.scrollTop + article.clientHeight >= article.scrollHeight - 5) markIfNotYet();
//                     });
//                     setTimeout(markIfNotYet, 8000);
//                 }
//
//                 // QUIZ
//                 if (t.type === "QUIZ") {
//                     const quizContainer = offcanvasBody.querySelector("#viewQuizQuestionsContainer");
//                     quizContainer.innerHTML = "";
//                     t.quizQuestions.forEach((q, i) => {
//                         const div = document.createElement("div");
//                         div.className = "mb-3 p-3 border rounded";
//                         div.innerHTML = `<strong>Q${i+1}. ${q.question}</strong>
//                             <ul class="list-group mt-2">
//                             ${q.options.map((opt, idx) => `<li class="list-group-item">
//                                 <input type="radio" name="q${i}" value="${idx}"> ${String.fromCharCode(65+idx)}. ${opt}
//                             </li>`).join("")}
//                             </ul>`;
//                         quizContainer.appendChild(div);
//                     });
//                     offcanvasBody.querySelector("#viewQuizSection").classList.remove("d-none");
//
//                     const submitBtn = offcanvasBody.querySelector("#submitQuizBtn");
//                     const resultDiv = offcanvasBody.querySelector("#quizResult");
//                     submitBtn.onclick = () => {
//                         let score = 0;
//                         t.quizQuestions.forEach((q,i) => {
//                             const sel = offcanvasBody.querySelector(`input[name=q${i}]:checked`);
//                             if (sel && parseInt(sel.value) === q.correctOptionIndex) score++;
//                         });
//                         const total = t.quizQuestions.length;
//                         if (score === total) {
//                             resultDiv.innerHTML = `<div class="alert alert-success">All correct! Tutorial completed.</div>`;
//                             markCompletedOnce(t.id);
//                         } else {
//                             resultDiv.innerHTML = `<div class="alert alert-warning">Score ${score}/${total}. Try again!</div>`;
//                         }
//                     };
//                 }
//
//                 offcanvas.show();
//
//             } catch (err) {
//                 offcanvasBody.innerHTML = `<div class="alert alert-danger">Failed to load tutorial</div>`;
//                 console.error(err);
//             }
//         });
//     });
//
//     offcanvasEl.addEventListener("hidden.bs.offcanvas", resetOffcanvas);
// });
//
// /* =========================
//    MARK TUTORIAL COMPLETED
//    ========================= */
//
// async function completeTutorial(tutorialId) {
//
//     console.log("Tutorial completed:", tutorialId);
//
//     const csrf = getCsrf();
//
//     const res = await fetch(`/student/tutorial/${tutorialId}/complete`, {
//         method: "POST",
//         headers: {
//             [csrf.header]: csrf.token,
//             "Content-Type": "application/json"
//         }
//     });
//
//     const data = await res.json();
//
//     if (data.success) {
//         location.reload(); // UI refresh (safe & simple)
//     }
// }
//
// function getCsrf() {
//     return {
//         token: document.querySelector('meta[name="_csrf"]').content,
//         header: document.querySelector('meta[name="_csrf_header"]').content
//     };
// }
//
// /* =========================
//    QUIZ FUNCTIONS (UNCHANGED)
//    ========================= */
//
// window.submitQuiz = function (tutorialId) {
//
//     const payload = {};
//
//     document.querySelectorAll('#quizForm input:checked').forEach(input => {
//         const qid = input.name.replace('q_', '');
//         payload[qid] = parseInt(input.value);
//     });
//
//     const csrf = getCsrf();
//
//     fetch(`/student/tutorial/${tutorialId}/quiz`, {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/json',
//             [csrf.header]: csrf.token
//         },
//         body: JSON.stringify(payload)
//     })
//
//         .then(r => r.json())
//         .then(res => {
//
//             document.querySelectorAll('.quiz-feedback').forEach(f => f.innerHTML = '');
//
//             res.results.forEach(r => {
//                 const block = document.querySelector(`[data-qid="${r.questionId}"]`);
//                 const feedback = block.querySelector('.quiz-feedback');
//
//                 feedback.innerHTML = r.correct
//                     ? `<span class="text-success">
//                          <i class="fa-solid fa-check-circle"></i> Correct
//                        </span>`
//                     : `<span class="text-danger">
//                          <i class="fa-solid fa-xmark-circle"></i>
//                          Correct answer: <b>${r.correctAnswer}</b>
//                        </span>`;
//             });
//
//             document.getElementById('quizResult').innerHTML = `
//                 <div class="alert ${res.passed ? 'alert-success' : 'alert-danger'}">
//                     <i class="fa-solid ${res.passed ? 'fa-trophy' : 'fa-circle-xmark'} me-2"></i>
//                     Score: ${res.score} / ${res.total}
//                 </div>`;
//
//             if (res.passed) {
//                 setTimeout(openNextTutorial, 1200);
//             } else {
//                 document.getElementById('retryBtn').classList.remove('d-none');
//             }
//         });
// };
//
// window.retryQuiz = function () {
//     document.getElementById('quizForm').reset();
//     document.querySelectorAll('.quiz-feedback').forEach(f => f.innerHTML = '');
//     document.getElementById('quizResult').innerHTML = '';
//     document.getElementById('retryBtn').classList.add('d-none');
// };
//
// window.openNextTutorial = function () {
//
//     const current = document.querySelector('.view-tutorial-btn[data-active="true"]');
//     if (!current) return;
//
//     const next = current.closest('.tutorial-card')
//         ?.nextElementSibling
//         ?.querySelector('.view-tutorial-btn');
//
//     if (next) {
//         bootstrap.Modal.getInstance(
//             document.getElementById('tutorialModal')
//         ).hide();
//
//         setTimeout(() => next.click(), 500);
//     }
//     const article = document.getElementById("articleContainer");
//
//     if (article) {
//         article.addEventListener("scroll", () => {
//             const reachedEnd =
//                 article.scrollTop + article.clientHeight >= article.scrollHeight - 10;
//
//             if (reachedEnd) {
//                 completeTutorial(article.dataset.tutorialId);
//             }
//         });
//     }
//
//
//
// };
//
// // // PDF auto-complete
// // const pdfContainer = document.querySelector('.pdf-container');
// // if (pdfContainer) {
// //     pdfContainer.addEventListener('scroll', () => {
// //         const endReached =
// //             pdfContainer.scrollTop + pdfContainer.clientHeight >=
// //             pdfContainer.scrollHeight - 10;
// //         if (endReached) {
// //             completeTutorial(pdfContainer.dataset.tutorialId);
// //         }
// //     });
// // }
//
// // Article auto-complete
// const article = document.getElementById('articleContainer');
// if (article) {
//     article.addEventListener('scroll', () => {
//         const endReached =
//             article.scrollTop + article.clientHeight >=
//             article.scrollHeight - 10;
//         if (endReached) {
//             completeTutorial(article.dataset.tutorialId);
//         }
//     });
// }
//
