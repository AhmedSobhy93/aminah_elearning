//
//
// document.addEventListener("DOMContentLoaded", () => {
//     // =======================
//     // CSRF SETUP
//     // =======================
//     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
//     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
//
//     // =======================
//     // COURSES
//     // =======================
//     const createCourseModal = new bootstrap.Modal(document.getElementById('modalCreateCourse'));
//
//     document.getElementById('btnCreateCourse')?.addEventListener('click', () => {
//         resetCourseForm();
//         createCourseModal.show();
//     });
//
//     document.querySelectorAll(".btnEditCourse").forEach(btn => {
//         btn.addEventListener("click", async () => {
//             const courseId = btn.dataset.courseId;
//             await loadCourseIntoForm(courseId);
//             createCourseModal.show();
//         });
//     });
//
//     document.getElementById("formCreateCourse")?.addEventListener("submit", async e => {
//         e.preventDefault();
//         const form = e.target;
//         const courseId = form.dataset.courseId;
//         const url = courseId ? `/dr/courses/edit/${courseId}` : `/dr/courses/create`;
//         const formData = new FormData(form);
//         formData.append("_csrf", csrfToken);
//
//         await fetch(url, {method: 'POST', body: formData});
//         createCourseModal.hide();
//         loadAllCourses();
//     });
//
//     async function loadCourseIntoForm(courseId) {
//         const res = await fetch(`/dr/courses/${courseId}/json`);
//         const course = await res.json();
//         const form = document.getElementById('formCreateCourse');
//         form.querySelector('input[name="title"]').value = course.title;
//         form.querySelector('input[name="courseName"]').value = course.courseName;
//         form.querySelector('input[name="price"]').value = course.price;
//         form.querySelector('select[name="published"]').value = course.published;
//         form.querySelector('input[name="videoUrl"]').value = course.videoUrl || "";
//         form.querySelector('textarea[name="description"]').value = course.description || "";
//         form.dataset.courseId = course.id;
//     }
//
//     function resetCourseForm() {
//         const form = document.getElementById('formCreateCourse');
//         form.reset();
//         delete form.dataset.courseId;
//     }
//
//     const deleteCourseModal = new bootstrap.Modal(document.getElementById("deleteCourseModal"));
//     let selectedCourseId = null;
//
//     document.querySelectorAll(".btn-delete-course").forEach(btn => {
//         btn.addEventListener("click", () => {
//             selectedCourseId = btn.dataset.courseId;
//             deleteCourseModal.show();
//         });
//     });
//
//     document.getElementById("confirmDeleteBtn")?.addEventListener("click", async () => {
//         if (!selectedCourseId) return;
//         await fetch(`/dr/courses/delete/${selectedCourseId}`, {
//             method: "POST", headers: {"Content-Type": "application/x-www-form-urlencoded"}, body: `_csrf=${csrfToken}`
//         });
//         deleteCourseModal.hide();
//         window.location.reload();
//     });
//
//     // =======================
//     // SECTIONS
//     // =======================
//     const sectionModal = new bootstrap.Modal(document.getElementById('modalSection'));
//
//     function attachSectionButtons() {
//         document.querySelectorAll('.btn-add-section').forEach(btn => {
//             btn.onclick = () => {
//                 document.getElementById('sectionCourseId').value = btn.dataset.courseId;
//                 document.getElementById('sectionTitle').value = '';
//                 document.getElementById('sectionId').value = '';
//                 sectionModal.show();
//             };
//         });
//
//         document.querySelectorAll('.btn-edit-section').forEach(btn => {
//             btn.onclick = () => {
//                 document.getElementById('sectionCourseId').value = btn.dataset.courseId;
//                 document.getElementById('sectionTitle').value = btn.dataset.sectionTitle;
//                 document.getElementById('sectionId').value = btn.dataset.sectionId;
//                 sectionModal.show();
//             };
//         });
//     }
//
//     const deleteSectionModal = new bootstrap.Modal(document.getElementById("deleteSectionModal"));
//     let selectedSectionId = null;
//
//     document.body.addEventListener("click", (e) => {
//         if (e.target.closest(".btn-delete-section")) {
//             const btn = e.target.closest(".btn-delete-section");
//             selectedSectionId = btn.dataset.sectionId;
//             deleteSectionModal.show();
//         }
//     });
//
//     document.getElementById("confirmSectionDeleteBtn")?.addEventListener("click", async () => {
//         if (!selectedSectionId) return;
//         await fetch(`/dr/sections/delete/${selectedSectionId}`, {
//             method: "POST", headers: {"Content-Type": "application/x-www-form-urlencoded"}, body: `_csrf=${csrfToken}`
//         });
//         deleteSectionModal.hide();
//         const currentCourseId = document.getElementById("sectionList")?.dataset.courseId;
//         if (currentCourseId) loadSections(currentCourseId, 1); else window.location.reload();
//     });
//
//     document.getElementById('btnSaveSection')?.addEventListener('click', () => {
//         document.getElementById('sectionForm').requestSubmit();
//     });
//
//     document.getElementById('sectionForm')?.addEventListener('submit', async e => {
//         e.preventDefault();
//         const courseId = document.getElementById('sectionCourseId').value;
//         const sectionId = document.getElementById('sectionId').value;
//         const title = document.getElementById('sectionTitle').value;
//         const description = document.getElementById('sectionDescription').value;
//
//         const url = sectionId ? `/dr/sections/edit/${sectionId}` : `/dr/courses/${courseId}/sections/add`;
//         const formData = new URLSearchParams();
//         formData.append('title', title);
//         formData.append('description', description);
//
//         await fetch(url, {
//             method: 'POST',
//             body: formData,
//             headers: {'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken}
//         });
//         sectionModal.hide();
//         loadSections(courseId);
//     });
//
//     window.loadSections=async function (courseId, page = 1) {
//         const container = document.getElementById(`sections-container-${courseId}`);
//         if (!container) return;
//         container.innerHTML = `<div class="text-center text-muted"><i class="fas fa-spinner fa-spin me-1"></i> Loading sections...</div>`;
//         const res = await fetch(`/dr/courses/${courseId}/sections-fragment?page=${page}`);
//         container.innerHTML = await res.text();
//
//         attachSectionButtons();
//         attachSectionPagination(courseId);
//
//         container.querySelectorAll('.tutorial-wrapper').forEach(wrapper => {
//             const sectionId = wrapper.id.replace('tutorials-container-', '');
//             loadTutorials(sectionId, 1);
//         });
//     }
//
//     function attachSectionPagination(courseId) {
//         document.querySelectorAll(`#sections-container-${courseId} .pagination a`).forEach(link => {
//             link.addEventListener('click', e => {
//                 e.preventDefault();
//                 const page = link.getAttribute("data-page");
//
//                 loadSections(courseId, page);
//             });
//         });
//     }
//
//
//     // =======================
//     // TUTORIALS
//     // =======================
// // ==========================
// // Global state
// // ==========================
//     const quizQuestions = [];
//     const modalAddTutorial = document.getElementById('modalAddTutorial');
//
// // ==========================
// // Delegated click listener
// // ==========================
//     document.addEventListener('click', async (e) => {
//         const btn = e.target.closest('button');
//         if (!btn) return;
//
//         const sectionId = btn.dataset.sectionId;
//         const tutorialId = btn.dataset.id;
//
//         // 1️⃣ Toggle Tutorials Collapse
//         if (btn.classList.contains('btn-toggle-tutorials')) {
//             const collapse = document.getElementById(`tutorials-collapse-${sectionId}`);
//             if (!collapse.classList.contains('show')) {
//                 const html = await fetch(`/dr/sections/${sectionId}/tutorials-fragment`).then(res => res.text());
//                 document.getElementById(`tutorials-container-${sectionId}`).innerHTML = html;
//                 new bootstrap.Collapse(collapse, {toggle: true});
//             } else {
//                 new bootstrap.Collapse(collapse, {toggle: true});
//             }
//         }
//
//         // 2️⃣ Add Tutorial
//         else if (btn.classList.contains('btn-add-tutorial')) {
//             document.getElementById('tutorialForm').reset();
//             document.getElementById('tutorialSectionId').value = sectionId;
//             showTutorialType('');
//             quizQuestions.length = 0;
//             renderQuizQuestions();
//             new bootstrap.Modal(modalAddTutorial).show();
//         }
//
//         // 3️⃣ Edit Tutorial
//         else if (btn.classList.contains('btn-edit-tutorial')) {
//             if (!tutorialId) return;
//             await loadTutorialIntoForm(tutorialId);
//             new bootstrap.Modal(modalAddTutorial).show();
//         }
//
//         // 4️⃣ Delete Tutorial
//         else if (btn.classList.contains('btn-delete-tutorial')) {
//             if (!tutorialId) return;
//             openDeleteTutorialModal(tutorialId);
//         }
//
//
//
//         // 5️⃣ Remove Quiz Question
//         else if (btn.classList.contains('btnRemoveQuestion')) {
//             const idx = parseInt(btn.dataset.index);
//             quizQuestions.splice(idx, 1);
//             renderQuizQuestions();
//         }
//         //  View Tutorial
//         else if (btn.classList.contains('btn-view-tutorial')) {
//             if (!tutorialId) return;
//             await openViewTutorialModal(tutorialId);
//         }
//     });
//
// // ==========================
// // Tutorial type change
// // ==========================
//     document.getElementById('tutorialTypeSelect')?.addEventListener('change', e => {
//         showTutorialType(e.target.value);
//     });
//
// // ==========================
// // Functions
// // ==========================
//     function showTutorialType(type) {
//         const fileInput = document.getElementById("tutorialFile");
//
//         const showFile = (type === "VIDEO" || type === "PDF");
//         document.getElementById("fileUploadSection").classList.toggle("d-none", !showFile);
//
//         if (!showFile) {
//             fileInput.value = ""; // 🔥 ensure file is removed
//         }
//
//         document.getElementById("articleSection").classList.toggle("d-none", type !== "ARTICLE");
//         document.getElementById("quizSection").classList.toggle("d-none", type !== "QUIZ");
//     }
//
//     function renderQuizQuestions() {
//         const container = document.getElementById('quizQuestionsContainer');
//         container.innerHTML = '';
//
//         quizQuestions.forEach((q, i) => {
//             const div = document.createElement('div');
//             div.classList.add('border', 'p-2', 'mb-2', 'rounded');
//
//             let optionInputs = q.options
//                 .map((opt, j) => `
//                 <div class="input-group mb-1">
//                     <input type="text" class="form-control optionInput"
//                            value="${opt}" placeholder="Option ${j + 1}"
//                            data-q="${i}" data-opt="${j}">
//                     <button class="btn btn-outline-danger btnRemoveOption"
//                             data-q="${i}" data-opt="${j}">
//                         ✕
//                     </button>
//                 </div>
//             `)
//                 .join("");
//
//             let correctOptions = q.options
//                 .map((_, j) => `
//                 <option value="${j}" ${q.correctOptionIndex === j ? "selected" : ""}>
//                     Option ${j + 1}
//                 </option>
//             `)
//                 .join("");
//
//             div.innerHTML = `
//             <div class="d-flex justify-content-between align-items-center mb-2">
//                 <strong>Question ${i + 1}</strong>
//                 <button type="button" class="btn btn-sm btn-danger btnRemoveQuestion" data-index="${i}">
//                     Remove Question
//                 </button>
//             </div>
//
//             <input type="text" class="form-control mb-2 questionInput"
//                    placeholder="Question text"
//                    value="${q.question}" data-q="${i}">
//
//             <div class="mt-2"><strong>Options</strong></div>
//             ${optionInputs}
//
//             <button class="btn btn-sm btn-secondary mb-2 btnAddOption" data-q="${i}">
//                 + Add Option
//             </button>
//
//             <label><strong>Correct Answer:</strong></label>
//             <select class="form-select correctSelect" data-q="${i}">
//                 ${correctOptions}
//             </select>
//         `;
//
//             container.appendChild(div);
//         });
//
//         attachQuizHandlers();
//     }
//
//     function attachQuizHandlers() {
//         const container = document.getElementById('quizQuestionsContainer');
//
//         // Edit question text
//         container.querySelectorAll(".questionInput").forEach(input => {
//             input.oninput = e => {
//                 const q = e.target.dataset.q;
//                 quizQuestions[q].question = e.target.value;
//             };
//         });
//
//         // Edit option text
//         container.querySelectorAll(".optionInput").forEach(input => {
//             input.oninput = e => {
//                 const q = e.target.dataset.q;
//                 const opt = e.target.dataset.opt;
//                 quizQuestions[q].options[opt] = e.target.value;
//             };
//         });
//
//         // Correct answer selector
//         container.querySelectorAll(".correctSelect").forEach(sel => {
//             sel.onchange = e => {
//                 const q = e.target.dataset.q;
//                 quizQuestions[q].correctOptionIndex = parseInt(e.target.value);
//             };
//         });
//
//         // Remove option
//         container.querySelectorAll(".btnRemoveOption").forEach(btn => {
//             btn.onclick = e => {
//                 const q = btn.dataset.q;
//                 const opt = btn.dataset.opt;
//
//                 quizQuestions[q].options.splice(opt, 1);
//
//                 // fix correct index if needed
//                 if (quizQuestions[q].correctOptionIndex >= quizQuestions[q].options.length) {
//                     quizQuestions[q].correctOptionIndex = 0;
//                 }
//
//                 renderQuizQuestions();
//             };
//         });
//
//         // Add option
//         container.querySelectorAll(".btnAddOption").forEach(btn => {
//             btn.onclick = e => {
//                 const q = btn.dataset.q;
//                 quizQuestions[q].options.push("");
//                 renderQuizQuestions();
//             };
//         });
//
//         // Remove whole question
//         container.querySelectorAll(".btnRemoveQuestion").forEach(btn => {
//             btn.onclick = e => {
//                 const index = btn.dataset.index;
//                 quizQuestions.splice(index, 1);
//                 renderQuizQuestions();
//             };
//         });
//     }
//     document.getElementById('btnAddQuizQuestion')?.addEventListener('click', () => {
//         quizQuestions.push({
//             question: "",
//             options: ["", ""],  // start with 2 options
//             correctOptionIndex: 0
//         });
//         renderQuizQuestions();
//     });
//
//
// // ==========================
// // Load Tutorials
// // ==========================
//     window.loadTutorials = async function (sectionId, page = 1) {
//         const container = document.getElementById(`tutorials-container-${sectionId}`);
//
//         if (!container) {
//             console.warn(`Container #tutorials-container-${sectionId} not found`);
//             return; // avoid crash
//         }
//
//         container.innerHTML = `
//         <div class="text-center text-muted">
//             <i class="fas fa-spinner fa-spin me-1"></i> Loading tutorials...
//         </div>`;
//
//         const html = await fetch(`/dr/sections/${sectionId}/tutorials-fragment?page=${page}`)
//             .then(res => res.text());
//
//         container.innerHTML = html;
//
//         container.querySelectorAll(".pagination a").forEach(a => {
//             a.addEventListener("click", e => {
//                 e.preventDefault();
//                 const p = parseInt(a.dataset.page);
//                 loadTutorials(sectionId, p);
//             });
//         });
//     };
//
//
// // ==========================
// // Load Tutorial Into Form
// // ==========================
//     async function loadTutorialIntoForm(tutorialId) {
//         const t = await fetch(`/dr/tutorial/${tutorialId}/json`).then(res => res.json());
//         const form = document.getElementById('tutorialForm');
//         form.querySelector('input[name="title"]').value = t.title || '';
//         form.querySelector('input[name="tutorialId"]').value = t.id;
//         form.querySelector('input[name="sectionId"]').value = t.sectionId || '';
//         form.querySelector('select[name="type"]').value = t.type || '';
//         showTutorialType(t.type);
//
//         if (t.type === 'QUIZ') {
//             quizQuestions.length = 0;
//             t.quizQuestions.forEach(q => quizQuestions.push(q));
//             renderQuizQuestions();
//         }
//     }
//
// // ==========================
// // Delete Tutorial Modal
// // ==========================
//     function openDeleteTutorialModal(id) {
//         document.getElementById("deleteTutorialId").value = id;
//         new bootstrap.Modal(document.getElementById("modalDeleteTutorial")).show();
//     }
//
// // ==========================
// // Submit Tutorial Form
// // ==========================
//
//     document.getElementById('tutorialForm')?.addEventListener('submit', async e => {
//         e.preventDefault();
//         const form = e.target;
//         const formData = new FormData(form);
//
//         if (!formData.has('_csrf')) formData.append('_csrf', csrfToken);
//
//         // ✅ Add quizQuestions if type is QUIZ
//         if (formData.get('type') === 'QUIZ' && quizQuestions.length > 0) {
//             document.getElementById('quizQuestionsJson').value = JSON.stringify(quizQuestions);
//         }
//
//
//         const tutorialId = formData.get('tutorialId');
//         const sectionId = formData.get('sectionId');
//         const url = `/dr/sections/${sectionId}/tutorials/add`;
//
//         const res = await fetch(url, {method: 'POST', body: formData});
//         if (res.ok) {
//             await loadTutorials(sectionId);
//             bootstrap.Modal.getInstance(modalAddTutorial).hide();
//             quizQuestions.length = 0;      // Clear questions after submit
//             renderQuizQuestions();          // Reset the quiz container
//         } else {
//             const data = await res.json();
//             alert(data.message || "Error");
//         }
//     });
//
//
// // ==========================
// // Delete Tutorial Form
// // ==========================
//     document.getElementById('deleteTutorialForm')?.addEventListener('submit', async e => {
//         e.preventDefault();
//         const id = document.getElementById('deleteTutorialId').value;
//         try {
//             const res = await fetch(`/dr/tutorial/${id}/delete-json`, {
//                 method: 'POST', headers: {
//                     'Content-Type': 'application/x-www-form-urlencoded', 'X-CSRF-TOKEN': csrfToken
//                 }, body: `id=${id}`
//             });
//             const data = await res.json();
//             if (data.success) {
//                 const sectionId = document.getElementById('tutorialSectionId')?.value;
//                 if (sectionId) await loadTutorials(sectionId);
//                 bootstrap.Modal.getInstance(document.getElementById('modalDeleteTutorial')).hide();
//             } else alert(data.message);
//         } catch (err) {
//             console.error(err);
//             alert('Error deleting tutorial.');
//         }
//     });
//
// // ==========================
// // View Tutorial Modal
// // ==========================
//     async function openViewTutorialModal(id) {
//         console.log("openViewTutorialModal", id);
//         try {
//             const tutorial = await fetch(`/dr/tutorial/${id}/json`).then(res => res.json());
//
//             document.getElementById('viewTutorialTitle').textContent = tutorial.title;
//
//             // Hide all sections
//             ['viewFileSection', 'viewArticleSection', 'viewQuizSection'].forEach(id =>
//                 document.getElementById(id).classList.add('d-none')
//             );
//
//             // ============================
//             // TYPE: VIDEO
//             // ============================
//             if (tutorial.type === 'VIDEO') {
//                 const container = document.getElementById('viewFileContainer');
//                 container.innerHTML = `
//                 <video class="w-100 rounded border" controls>
//                     <source src="${tutorial.filePath}" type="video/mp4">
//                     Your browser does not support HTML5 video.
//                 </video>`;
//                 document.getElementById('viewFileSection').classList.remove('d-none');
//             }
//
//                 // ============================
//                 // TYPE: PDF (Embedded Viewer)
//             // ============================
//             else if (tutorial.type === 'PDF') {
//                 const container = document.getElementById('viewFileContainer');
//                 container.innerHTML = `
//                 <embed src="${tutorial.filePath}"
//                        type="application/pdf"
//                        class="w-100 border rounded"
//                        style="height: 600px;">
//                 </embed>`;
//                 document.getElementById('viewFileSection').classList.remove('d-none');
//             }
//
//                 // ============================
//                 // TYPE: ARTICLE
//             // ============================
//             else if (tutorial.type === 'ARTICLE') {
//                 document.getElementById('viewArticleContent').innerHTML =
//                     `<div class="formatted-article">${tutorial.articleContent}</div>`;
//                 document.getElementById('viewArticleSection').classList.remove('d-none');
//             }
//
//                 // ============================
//                 // TYPE: QUIZ
//             // ============================
//             else if (tutorial.type === 'QUIZ') {
//                 const container = document.getElementById('viewQuizQuestionsContainer');
//                 container.innerHTML = '';
//
//                 if (!tutorial.quizQuestions || tutorial.quizQuestions.length === 0) {
//                     container.innerHTML = `
//             <div class="alert alert-warning text-center">No questions added.</div>
//         `;
//                     document.getElementById('viewQuizSection').classList.remove('d-none');
//                     return;
//                 }
//
//                 tutorial.quizQuestions.forEach((q, i) => {
//
//                     const optionsHtml = q.options
//                         .map((opt, idx) => `
//                 <li class="list-group-item d-flex justify-content-between align-items-center
//                     ${idx === q.correctOptionIndex ? 'list-group-item-success fw-bold border-success' : ''}">
//
//                     <span>
//                         <strong>${String.fromCharCode(65 + idx)}.</strong> ${opt}
//                     </span>
//
//                     ${idx === q.correctOptionIndex
//                             ? '<span class="badge bg-success">Correct</span>'
//                             : ''}
//                 </li>
//             `)
//                         .join('');
//
//                     const div = document.createElement('div');
//                     div.classList.add('mb-3', 'p-3', 'border', 'rounded', 'bg-light');
//
//                     div.innerHTML = `
//             <div class="fw-bold mb-2">
//                 Q${i + 1}. ${q.question}
//             </div>
//             <ul class="list-group">
//                 ${optionsHtml}
//             </ul>
//         `;
//
//                     container.appendChild(div);
//                 });
//
//                 document.getElementById('viewQuizSection').classList.remove('d-none');
//             }
//
//             // ============================
//             // SHOW MODAL
//             // ============================
//             const modalEl = document.getElementById('modalViewTutorial');
//             let modal = bootstrap.Modal.getInstance(modalEl);
//             if (!modal) modal = new bootstrap.Modal(modalEl);
//             modal.show();
//
//         } catch (err) {
//             console.error(err);
//             alert('Failed to load tutorial: ' + err.message);
//         }
//     }
//
//
//
//     // =======================
//     // STUDENTS
//     // =======================
//
//     function attachStudentPagination(courseId) {
//         document.querySelectorAll("#enrollmentsContainer .pagination a")
//             .forEach(a => {
//                 a.addEventListener("click", e => {
//                     e.preventDefault();
//                     const p = parseInt(a.dataset.page);
//                     loadStudents(courseId, p);
//                 });
//             });
//     }
//
//
//     //Modal view
//
//     window.loadStudents = async function (courseId, page = 0) {
//
//         const container = document.getElementById("enrollmentsContainer");
//         container.innerHTML = `
//         <div class="text-center text-muted">
//             <i class="fas fa-spinner fa-spin me-2"></i> Loading students...
//         </div>
//     `;
//
//         const html = await fetch(`/dr/courses/${courseId}/students-fragment?page=${page}`)
//             .then(res => res.text());
//
//         container.innerHTML = html;
//
//         attachStudentPagination(courseId);
//     };
//
//
//     window.showEnrollments = function (courseId) {
//         const modal = new bootstrap.Modal(document.getElementById("modalViewEnrollments"));
//         modal.show();
//
//         loadStudents(courseId, 0);
//     };
//
//
//
//     document.querySelectorAll(".btn-view-enrollments").forEach(btn => {
//         btn.addEventListener("click", async () => {
//             const courseId = btn.dataset.courseId;
//             const modalBody = document.getElementById("enrollmentsContainer");
//             modalBody.innerHTML = "Loading...";
//             const res = await fetch(`/dr/courses/${courseId}/students-fragment`);
//             modalBody.innerHTML = await res.text();
//             new bootstrap.Modal(document.getElementById("modalViewEnrollments")).show();
//         });
//     });
//
//     // =======================
//     // LOAD ALL COURSES INITIAL
//     // =======================
//     async function loadAllCourses() {
//         document.querySelectorAll('.course-card').forEach(card => {
//             const courseId = card.querySelector('.btn-add-section')?.dataset.courseId;
//             if (courseId) {
//                 loadSections(courseId);
//                 loadStudents(courseId);
//             }
//         });
//     }
//
//     loadAllCourses();
// });

/* ============================================================================
   DR DASHBOARD – Unified JS Controller
   Handles: Courses, Sections, Tutorials, Quizzes, Students
   Author: ChatGPT refactor (optimized for delegation & DR principles)
============================================================================ */

document.addEventListener("DOMContentLoaded", () => {

    /* ------------------------------------------------------------------------
       CSRF
    ------------------------------------------------------------------------ */
    const csrf = {
        token: document.querySelector('meta[name="_csrf"]').content,
        header: document.querySelector('meta[name="_csrf_header"]').content
    };

    const addCsrf = (headers = {}) => ({
        ...headers,
        [csrf.header]: csrf.token
    });

    /* ------------------------------------------------------------------------
       Bootstrap modals
    ------------------------------------------------------------------------ */
    const modals = {
        createCourse: new bootstrap.Modal("#modalCreateCourse"),
        deleteCourse: new bootstrap.Modal("#deleteCourseModal"),
        section: new bootstrap.Modal("#modalSection"),
        deleteSection: new bootstrap.Modal("#deleteSectionModal"),
        addTutorial: new bootstrap.Modal("#modalAddTutorial"),
        deleteTutorial: new bootstrap.Modal("#modalDeleteTutorial"),
        viewTutorial: new bootstrap.Modal("#modalViewTutorial"),
        enrollments: new bootstrap.Modal("#modalViewEnrollments")
    };

    /* ------------------------------------------------------------------------
       Helpers
    ------------------------------------------------------------------------ */
    const fetchHTML = (url) => fetch(url).then(r => r.text());
    const fetchJSON = (url) => fetch(url).then(r => r.json());

    const post = (url, body) => {
        const headers = {};
        headers[csrf.header] = csrf.token;

        return fetch(url, {
            method: "POST",
            body,
            headers // NO Content-Type here
        });
    };


    const postJSON = (url, data) =>
        fetch(url, {
            method: "POST",
            headers: addCsrf({ "Content-Type": "application/json" }),
            body: JSON.stringify(data)
        });

    /* =========================================================================
       COURSES
       ========================================================================= */

    // Open create course modal
    document.getElementById("btnCreateCourse")?.addEventListener("click", () => {
        const form = document.getElementById("formCreateCourse");
        form.reset();
        delete form.dataset.courseId;
        modals.createCourse.show();
    });

    // Submit create/edit course
    document.getElementById("formCreateCourse")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const form = e.target;
        const courseId = form.dataset.courseId;
        const url = courseId ? `/dr/courses/edit/${courseId}` : `/dr/courses/create`;
        const fd = new FormData(form);
        fd.append("_csrf", csrf.token);

        await fetch(url, { method: "POST", body: fd });
        modals.createCourse.hide();
        location.reload();
    });

    // Delegated - Edit course
    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".btnEditCourse");
        if (!btn) return;

        const courseId = btn.dataset.courseId;
        const course = await fetchJSON(`/dr/courses/${courseId}/json`);

        const form = document.getElementById("formCreateCourse");
        form.dataset.courseId = course.id;
        form.title.value = course.title;
        form.courseName.value = course.courseName;
        form.price.value = course.price;
        form.published.value = course.published;
        form.videoUrl.value = course.videoUrl ?? "";
        form.description.value = course.description ?? "";

        modals.createCourse.show();
    });

    // Delegated - delete course
    let deleteCourseId = null;
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-delete-course");
        if (!btn) return;
        deleteCourseId = btn.dataset.courseId;
        modals.deleteCourse.show();
    });

    document.getElementById("confirmDeleteBtn")?.addEventListener("click", async () => {
        if (!deleteCourseId) return;
        await post(`/dr/courses/delete/${deleteCourseId}`, `_csrf=${csrf.token}`);
        location.reload();
    });

    /* =========================================================================
       SECTIONS
       ========================================================================= */

    // Delegated add/edit section
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-add-section, .btn-edit-section");
        if (!btn) return;

        const form = document.getElementById("sectionForm");
        form.reset();

        form.sectionCourseId.value = btn.dataset.courseId;
        form.sectionId.value = btn.dataset.sectionId ?? "";
        form.sectionTitle.value = btn.dataset.sectionTitle ?? "";
        form.sectionDescription.value = btn.dataset.sectionDescription ?? "";

        modals.section.show();
    });

    // Save section
    document.getElementById("sectionForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const f = e.target;

        const url = f.sectionId.value
            ? `/dr/sections/edit/${f.sectionId.value}`
            : `/dr/courses/${f.sectionCourseId.value}/sections/add`;

        await post(url, new URLSearchParams({
            title: f.sectionTitle.value,
            description: f.sectionDescription.value
        }));

        modals.section.hide();
        loadSections(f.sectionCourseId.value);
    });

    // Delete section
    let deleteSectionId = null;
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-delete-section");
        if (!btn) return;
        deleteSectionId = btn.dataset.sectionId;
        modals.deleteSection.show();
    });

    document.getElementById("confirmSectionDeleteBtn")?.addEventListener("click", async () => {
        await post(`/dr/sections/delete/${deleteSectionId}`, `_csrf=${csrf.token}`);
        modals.deleteSection.hide();
        location.reload();
    });

    // Load sections (recalled after edits)
    window.loadSections = async (courseId, page = 1) => {
        const box = document.getElementById(`sections-container-${courseId}`);
        if (!box) return;

        box.innerHTML = `<div class="text-muted text-center"><i class="fas fa-spinner fa-spin"></i> Loading…</div>`;
        box.innerHTML = await fetchHTML(`/dr/courses/${courseId}/sections-fragment?page=${page}`);

        // reattach pagination
        box.querySelectorAll(".pagination a").forEach(a =>
            a.onclick = (ev) => {
                ev.preventDefault();
                loadSections(courseId, a.dataset.page);
            }
        );
    };

    /* =========================================================================
       TUTORIALS + QUIZ BUILDER
       ========================================================================= */
    const quizQuestions = [];

    // Toggle tutorials
    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".btn-toggle-tutorials");
        if (!btn) return;

        const secId = btn.dataset.sectionId;
        const collapse = document.getElementById(`tutorials-collapse-${secId}`);

        if (!collapse.classList.contains("show")) {
            await loadTutorials(secId);
        }
        new bootstrap.Collapse(collapse, { toggle: true });
    });


    // Add tutorial
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-add-tutorial");
        if (!btn) return;

        const form = document.getElementById("tutorialForm");
        form.reset();

        document.getElementById("tutorialSectionId").value = btn.dataset.sectionId;
        document.getElementById("tutorialId").value = "";
        document.getElementById("tutorialTypeSelect").value = "";

        quizQuestions.length = 0;
        renderQuizQuestions();

        modals.addTutorial.show();
    });

// Edit tutorial
    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".btn-edit-tutorial");
        if (!btn) return;

        const t = await fetchJSON(`/dr/tutorial/${btn.dataset.id}/json`);
        const form = document.getElementById("tutorialForm");

        document.getElementById("tutorialTitle").value = t.title;
        document.getElementById("tutorialSectionId").value = t.sectionId;
        document.getElementById("tutorialId").value = t.id;
        document.getElementById("tutorialTypeSelect").value = t.type;

        showTutorialType(t.type);

        if (t.type === "QUIZ") {
            quizQuestions.length = 0;
            t.quizQuestions.forEach(q => quizQuestions.push(q));
            renderQuizQuestions();
        }

        modals.addTutorial.show();
    });


    // Save tutorial
    document.getElementById("tutorialForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const fd = new FormData(e.target);

        const sectionId = document.getElementById("tutorialSectionId").value;
        const tutorialId = document.getElementById("tutorialId").value;
        const form = document.getElementById("tutorialForm");

        const type = fd.get("type");

        if (type === "QUIZ") {
            fd.set("quizQuestionsJson", JSON.stringify(quizQuestions));
        }

        try {
            const resp = await fetch(`/dr/sections/${sectionId}/tutorials/add`, {
                method: "POST",
                body: fd,
                headers: {
                    [csrf.header]: csrf.token
                }
            }).then(r => r.json());

            if (resp.success) {
                modals.addTutorial.hide();
                loadTutorials(sectionId);
            } else {
                alert("Error saving tutorial: " + resp.message);
            }
        } catch (err) {
            console.error(err);
            alert("Failed to save tutorial");
        }
    });


    // Delete tutorial
    document.addEventListener("click", (e) => {
        const btn = e.target.closest(".btn-delete-tutorial");
        if (!btn) return;

        document.getElementById("deleteTutorialId").value = btn.dataset.id;
        modals.deleteTutorial.show();
    });

    document.getElementById("deleteTutorialForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const id = deleteTutorialId.value;
        await post(`/dr/tutorial/${id}/delete-json`, `id=${id}`);
        modals.deleteTutorial.hide();
        location.reload();
    });

    // Load tutorials
    window.loadTutorials = async (sectionId, page = 1) => {
        const box = document.getElementById(`tutorials-container-${sectionId}`);
        if (!box) return;

        box.innerHTML = `<div class="text-center"><i class="fas fa-spinner fa-spin"></i></div>`;
        box.innerHTML = await fetchHTML(`/dr/sections/${sectionId}/tutorials-fragment?page=${page}`);

        box.querySelectorAll(".pagination a").forEach(a =>
            a.onclick = (ev) => {
                ev.preventDefault();
                loadTutorials(sectionId, a.dataset.page);
            }
        );
    };

    /* ------------------------------------------------------------------------
       QUIZ BUILDER
    ------------------------------------------------------------------------ */
    const renderQuizQuestions = () => {
        const container = document.getElementById("quizQuestionsContainer");
        container.innerHTML = "";

        quizQuestions.forEach((q, i) => {
            const card = document.createElement("div");
            card.className = "border rounded p-2 mb-2";

            const opts = q.options.map((opt, idx) => `
                <div class="input-group mb-1">
                    <input type="text" 
                           class="form-control opt" 
                           data-q="${i}" data-idx="${idx}" value="${opt}">
                    <button class="btn btn-danger btn-del-opt" data-q="${i}" data-idx="${idx}">✕</button>
                </div>`).join("");

            card.innerHTML = `
                <div class="d-flex justify-content-between">
                    <strong>Question ${i + 1}</strong>
                    <button class="btn btn-sm btn-danger btn-del-q" data-index="${i}">Remove</button>
                </div>
                <input class="form-control qtext mt-2" data-q="${i}" value="${q.question}">
                <div class="mt-2"><strong>Options:</strong></div>
                ${opts}
                <button class="btn btn-sm btn-secondary btn-add-opt" data-q="${i}">+ Option</button>
                <label class="mt-2">Correct Answer</label>
                <select class="form-select correct" data-q="${i}">
                    ${q.options.map((_, idx) =>
                `<option value="${idx}" ${idx === q.correctOptionIndex ? "selected" : ""}>
                            Option ${idx + 1}
                        </option>`).join("")}
                </select>
            `;

            container.appendChild(card);
        });
    };

    // Quiz input events (delegated)
    document.addEventListener("input", (e) => {
        if (e.target.classList.contains("qtext")) {
            quizQuestions[e.target.dataset.q].question = e.target.value;
        }
        if (e.target.classList.contains("opt")) {
            quizQuestions[e.target.dataset.q].options[e.target.dataset.idx] = e.target.value;
        }
    });

    document.addEventListener("change", (e) => {
        if (e.target.classList.contains("correct")) {
            quizQuestions[e.target.dataset.q].correctOptionIndex = Number(e.target.value);
        }
    });

    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("btn-del-q")) {
            quizQuestions.splice(e.target.dataset.index, 1);
            renderQuizQuestions();
        }
        if (e.target.classList.contains("btn-add-opt")) {
            quizQuestions[e.target.dataset.q].options.push("");
            renderQuizQuestions();
        }
        if (e.target.classList.contains("btn-del-opt")) {
            const q = e.target.dataset.q;
            const i = e.target.dataset.idx;
            quizQuestions[q].options.splice(i, 1);
            renderQuizQuestions();
        }
    });

    document.getElementById("btnAddQuizQuestion")?.addEventListener("click", () => {
        quizQuestions.push({
            question: "",
            options: ["", ""],
            correctOptionIndex: 0
        });
        renderQuizQuestions();
    });

    /* =========================================================================
       VIEW TUTORIAL
       ========================================================================= */

    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".btn-view-tutorial");
        if (!btn) return;

        const t = await fetchJSON(`/dr/tutorial/${btn.dataset.id}/json`);

        // Reset visibility
        ["viewFileSection", "viewArticleSection", "viewQuizSection"].forEach(id =>
            document.getElementById(id).classList.add("d-none")
        );

        if (t.type === "VIDEO" || t.type === "PDF") {
            const box = document.getElementById("viewFileContainer");
            box.innerHTML = t.type === "VIDEO"
                ? `<video class="w-100" controls><source src="${t.filePath}" type="video/mp4"></video>`
                : `<embed src="${t.filePath}" class="w-100" style="height:600px">`;
            document.getElementById("viewFileSection").classList.remove("d-none");
        }

        if (t.type === "ARTICLE") {
            document.getElementById("viewArticleContent").innerHTML = t.articleContent;
            document.getElementById("viewArticleSection").classList.remove("d-none");
        }

        if (t.type === "QUIZ") {
            const box = document.getElementById("viewQuizQuestionsContainer");
            box.innerHTML = "";

            t.quizQuestions.forEach((q, i) => {
                const div = document.createElement("div");
                div.className = "mb-3 p-3 border rounded";

                div.innerHTML = `
                    <strong>Q${i + 1}. ${q.question}</strong>
                    <ul class="list-group mt-2">
                        ${q.options.map((opt, idx) =>
                    `<li class="list-group-item ${idx === q.correctOptionIndex ? 'list-group-item-success' : ''}">
                                ${String.fromCharCode(65 + idx)}. ${opt}
                            </li>`
                ).join("")}
                    </ul>
                `;

                box.appendChild(div);
            });

            document.getElementById("viewQuizSection").classList.remove("d-none");
        }

        modals.viewTutorial.show();
    });

    /* =========================================================================
       STUDENTS
       ========================================================================= */

    window.loadStudents = async (courseId, page = 0) => {
        const container = document.getElementById("enrollmentsContainer");
        container.innerHTML = `<div class="text-center"><i class="fas fa-spinner fa-spin"></i></div>`;
        container.innerHTML = await fetchHTML(`/dr/courses/${courseId}/students-fragment?page=${page}`);

        container.querySelectorAll(".pagination a").forEach(a =>
            a.onclick = (ev) => {
                ev.preventDefault();
                loadStudents(courseId, a.dataset.page);
            }
        );
    };

    window.showEnrollments = (courseId) => {
        modals.enrollments.show();
        loadStudents(courseId);
    };

    /* =========================================================================
       INIT – LOAD SECTIONS & STUDENTS FOR ALL COURSES
       ========================================================================= */
    document.querySelectorAll(".course-card").forEach(card => {
        const courseId = card.querySelector(".btn-add-section")?.dataset.courseId;
        if (courseId) {
            loadSections(courseId);
        }
    });


    function waitForElement(id, callback) {
        const el = document.getElementById(id);
        if (el) return callback(el);

        const observer = new MutationObserver(() => {
            const el = document.getElementById(id);
            if (el) {
                observer.disconnect();
                callback(el);
            }
        });

        observer.observe(document.body, {childList: true, subtree: true});
    }
    waitForElement("tutorialTypeSelect", el => {
        el.addEventListener("change", e => {
            showTutorialType(e.target.value);
        });
    });

    function showTutorialType(type) {

        const fileUpload = document.getElementById("fileUploadSection");
        const article = document.getElementById("articleSection");
        const quiz = document.getElementById("quizSection");

        if (!fileUpload || !article || !quiz) {
            console.warn("Tutorial type UI elements not loaded yet.");
            return;
        }

        // Hide all first
        fileUpload.classList.add("d-none");
        article.classList.add("d-none");
        quiz.classList.add("d-none");

        // Show based on type
        if (type === "VIDEO" || type === "PDF") {
            fileUpload.classList.remove("d-none");
        }
        else if (type === "ARTICLE") {
            article.classList.remove("d-none");
        }
        else if (type === "QUIZ") {
            quiz.classList.remove("d-none");
        }
    }


});
