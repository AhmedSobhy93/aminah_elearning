//
//     function handleTypeDisplay(type) {
//         document.getElementById('fileUploadSection').classList.toggle('d-none', type === 'ARTICLE' || type === 'QUIZ');
//         document.getElementById('articleSection').classList.toggle('d-none', type !== 'ARTICLE');
//         document.getElementById('quizSection').classList.toggle('d-none', type !== 'QUIZ');
//     }
//     // document.getElementById('tutorialTypeSelect').addEventListener('change', e => handleTypeDisplay(e.target.value));
// //
// //
// //     function renderViewModal(data) {
// //         document.getElementById('viewTutorialTitle').innerText = data.title;
// //
// //         document.getElementById('viewFileSection').classList.add('d-none');
// //         document.getElementById('viewArticleSection').classList.add('d-none');
// //         document.getElementById('viewQuizSection').classList.add('d-none');
// //
// //         if (data.type === 'VIDEO' || data.type === 'PDF') {
// //             const container = document.getElementById('viewFileContainer');
// //             container.innerHTML = '';
// //             if (data.filePath) {
// //                 if (data.type === 'VIDEO') {
// //                     const video = document.createElement('video');
// //                     video.src = data.filePath;
// //                     video.controls = true;
// //                     video.classList.add('w-100');
// //                     container.appendChild(video);
// //                 } else {
// //                     const link = document.createElement('a');
// //                     link.href = data.filePath;
// //                     link.target = '_blank';
// //                     link.innerText = 'Open PDF';
// //                     container.appendChild(link);
// //                 }
// //             }
// //             document.getElementById('viewFileSection').classList.remove('d-none');
// //         }
// //
// //         if (data.type === 'ARTICLE') {
// //             document.getElementById('viewArticleContent').innerHTML = data.articleContent || '<em>No content</em>';
// //             document.getElementById('viewArticleSection').classList.remove('d-none');
// //         }
// //
// //         if (data.type === 'QUIZ') {
// //             const container = document.getElementById('viewQuizQuestionsContainer');
// //             container.innerHTML = '';
// //             (data.quizQuestions || []).forEach((q, i) => {
// //                 const div = document.createElement('div');
// //                 div.classList.add('border', 'p-2', 'mb-2', 'rounded');
// //                 div.innerHTML = `<strong>Q${i+1}: ${q.question}</strong>
// //                              <ul>${q.options.map((opt, j) => `<li>${opt} ${q.correctOptionIndex === j ? '(Correct)' : ''}</li>`).join('')}</ul>`;
// //                 container.appendChild(div);
// //             });
// //             document.getElementById('viewQuizSection').classList.remove('d-none');
// //         }
// //     }
//
// //
// //     document.getElementById('tutorialForm').addEventListener('submit', (e) => {
// //         const hiddenInput = document.createElement('input');
// //         hiddenInput.type = 'hidden';
// //         hiddenInput.name = 'quizQuestionsJson';
// //         hiddenInput.value = JSON.stringify(quizQuestions);
// //         e.target.appendChild(hiddenInput);
// //     });
// //     async function loadTutorials(sectionId, page = 1) {
// //         const container = document.getElementById(`tutorials-container-${sectionId}`);
// //         if (!container) return;
// //
// //         container.innerHTML = `<div class="text-center text-muted">
// //         <i class="fas fa-spinner fa-spin me-1"></i> Loading tutorials...
// //     </div>`;
// //
// //         const res = await fetch(`/sections/${sectionId}/tutorials-fragment?page=${page}`);
// //         container.innerHTML = await res.text();
// //
// //         attachTutorialButtons(sectionId); // ✅ Attach buttons after content load
// //     }
// //
// //
// //     /* ====================================================
// //      *                       STUDENTS
// //      * ==================================================== */
// //     async function loadStudents(courseId, page = 0) {
// //         const container = document.getElementById(`students-container-${courseId}`);
// //         if (!container) return;
// //         container.innerHTML = `<div class="text-center text-muted">
// //             <i class="fas fa-spinner fa-spin me-1"></i> Loading students...
// //         </div>`;
// //         const res = await fetch(`/dr/courses/${courseId}/students-fragment?page=${page}`);
// //         container.innerHTML = await res.text();
// //         attachStudentPagination(courseId);
// //     }
// //
// //     function attachStudentPagination(courseId) {
// //         document.querySelectorAll(`#students-container-${courseId} .pagination a`).forEach(link => {
// //             link.addEventListener('click', (e) => {
// //                 e.preventDefault();
// //                 const page = new URL(link.href).searchParams.get('page') || 0;
// //                 loadStudents(courseId, page);
// //             });
// //         });
// //     }
// //
// //
// //     document.querySelectorAll(".btn-view-enrollments").forEach(btn => {
// //         btn.addEventListener("click", async () => {
// //             const courseId = btn.dataset.courseId;
// //
// //             const modalBody = document.getElementById("enrollmentsContainer");
// //             modalBody.innerHTML = "Loading...";
// //
// //             const res = await fetch(`/dr/courses/${courseId}/students-fragment`);
// //             const html = await res.text();
// //             modalBody.innerHTML = html;
// //
// //             // attachPaginationListeners();
// //
// //             new bootstrap.Modal(document.getElementById("modalViewEnrollments")).show();
// //         });
// //     });
// //
// //
// //     /* ====================================================
// //      *               INITIAL LOAD FOR ALL COURSES
// //      * ==================================================== */
// //     document.querySelectorAll('.course-card').forEach(card => {
// //         const courseId = card.querySelector('.btn-add-section')?.dataset.courseId;
// //         if (courseId) {
// //             loadSections(courseId);
// //             loadStudents(courseId);
// //         }
// //     });
// //
// // });
//
//
// document.addEventListener("DOMContentLoaded", () => {
//
//     /* ====================================================
//      *                     CSRF SETUP
//      * ==================================================== */
//     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
//     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
//
//     /* ====================================================
//      *                     COURSES
//      * ==================================================== */
//     const createCourseModal = new bootstrap.Modal(document.getElementById('modalCreateCourse'));
//
//     // Create / Edit course
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
//         await fetch(url, { method: 'POST', body: formData });
//         createCourseModal.hide();
//         loadAllCourses(); // reload courses dynamically
//     });
//
//     async function loadCourseIntoForm(courseId) {
//         const res = await fetch(`/dr/courses/${courseId}/json`);
//         const course = await res.json();
//
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
//     // Delete course
//
//     document.addEventListener("DOMContentLoaded", () => {
//
//         const deleteCourseModalEl = document.getElementById("deleteCourseModal");
//         const deleteCourseModal = new bootstrap.Modal(deleteCourseModalEl);
//
//         let selectedCourseId = null;
//
//         document.querySelectorAll(".btn-delete-course").forEach(btn => {
//             btn.addEventListener("click", () => {
//                 selectedCourseId = btn.dataset.courseId;
//                 deleteCourseModal.show();
//             });
//         });
//
//         document.getElementById("confirmDeleteBtn").addEventListener("click", async () => {
//             if (!selectedCourseId) return;
//
//             await fetch(`/dr/courses/delete/${selectedCourseId}`, {
//                 method: "POST",
//                 headers: {
//                     "Content-Type": "application/x-www-form-urlencoded"
//                 },
//                 body: "_csrf=" + document.querySelector('meta[name="_csrf"]').content
//             });
//
//             deleteCourseModal.hide();
//             window.location.reload();
//         });
//
//     });
//
//
//     /* ====================================================
//      *                   SECTIONS
//      * ==================================================== */
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
//             document.querySelectorAll('.btn-add-tutorial').forEach(btn => {
//                 btn.onclick = () => {
//                     document.getElementById('tutorialForm').reset();
//                     const sectionId = btn.dataset.sectionId;
//                     document.getElementById('tutorialSectionId').value = sectionId;
//                     document.getElementById('fileUploadSection').classList.add('d-none');
//                     document.getElementById('articleSection').classList.add('d-none');
//                     document.getElementById('quizSection').classList.add('d-none');
//                     new bootstrap.Modal(document.getElementById('modalAddTutorial')).show();
//                 };
//             });
//
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
//
//         // document.querySelectorAll('.btn-delete-section').forEach(btn => {
//         //     btn.onclick = async () => {
//         //         if (!confirm('Are you sure?')) return;
//         //         const sectionId = btn.dataset.sectionId;
//         //         await fetch(`/dr/sections/delete/${sectionId}`, { method: 'POST', headers: { [csrfHeader]: csrfToken } });
//         //         const courseId = btn.closest('.course-card').querySelector('.btn-add-section').dataset.courseId;
//         //         loadSections(courseId);
//         //     };
//         // });
//
//     }
//
//
//     document.addEventListener("DOMContentLoaded", () => {
//
//         const deleteSectionModalEl = document.getElementById("deleteSectionModal");
//         const deleteSectionModal = new bootstrap.Modal(deleteSectionModalEl);
//
//         let selectedSectionId = null;
//
//         // Event delegation for dynamically loaded elements
//         document.body.addEventListener("click", (e) => {
//
//             // DELETE section btn
//             if (e.target.closest(".btn-delete-section")) {
//                 const btn = e.target.closest(".btn-delete-section");
//                 selectedSectionId = btn.dataset.sectionId;
//                 deleteSectionModal.show();
//             }
//         });
//
//         // Confirm delete
//         document.getElementById("confirmSectionDeleteBtn").addEventListener("click", async () => {
//
//             if (!selectedSectionId) return;
//
//             const csrf = document.querySelector('meta[name="_csrf"]').content;
//
//             await fetch(`/dr/sections/delete/${selectedSectionId}`, {
//                 method: "POST",
//                 headers: { "Content-Type": "application/x-www-form-urlencoded" },
//                 body: `_csrf=${csrf}`
//             });
//
//             deleteSectionModal.hide();
//
//             // Reload sections only instead of whole page
//             if (typeof loadSections === "function") {
//                 // assume you keep current course/page in DOM
//                 const currentCourseId = document.getElementById("sectionList")?.dataset.courseId;
//                 if (currentCourseId) loadSections(currentCourseId, 1);
//             } else {
//                 window.location.reload();
//             }
//         });
//
//     });
//
//
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
//
//         const formData = new URLSearchParams();
//         formData.append('title', title);
//         formData.append('description', description);
//
//         await fetch(url, {
//             method: 'POST',
//             body: formData,
//             headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken }
//         });
//
//         sectionModal.hide();
//         loadSections(courseId);
//     });
//
//     async function loadSections(courseId) {
//         const container = document.getElementById(`sections-container-${courseId}`);
//         if (!container) return;
//
//         container.innerHTML = `<div class="text-center text-muted"><i class="fas fa-spinner fa-spin me-1"></i> Loading sections...</div>`;
//         const res = await fetch(`/dr/courses/${courseId}/sections-fragment`);
//         container.innerHTML = await res.text();
//
//         attachSectionButtons();
//
//         // Load tutorials for each section
//         container.querySelectorAll('.tutorial-wrapper').forEach(wrapper => {
//             const sectionId = wrapper.id.replace('tutorials-container-', '');
//             loadTutorials(sectionId,1);
//         });
//
//
//     }
//
//     /* ====================================================
//      *                   TUTORIALS
//      * ==================================================== */
//
//
//     async function loadTutorials(sectionId, page = 1) {
//         const container = document.getElementById(`tutorials-container-${sectionId}`);
//         if (!container) return;
//
//         container.innerHTML = `<div class="text-center text-muted"><i class="fas fa-spinner fa-spin me-1"></i> Loading tutorials...</div>`;
//         const res = await fetch(`/dr/sections/${sectionId}/tutorials-fragment?page=${page}`);
//
//         container.innerHTML = await res.text();
//
//         attachTutorialButtons(sectionId);
//     }
//
//
//
//     // function handleTypeDisplay(type) {
//     //     document.getElementById('fileUploadSection').classList.toggle('d-none', type === 'ARTICLE' || type === 'QUIZ');
//     //     document.getElementById('articleSection').classList.toggle('d-none', type !== 'ARTICLE');
//     //     document.getElementById('quizSection').classList.toggle('d-none', type !== 'QUIZ');
//     // }
//     // document.getElementById('tutorialTypeSelect')?.addEventListener('change', e => handleTypeDisplay(e.target.value));
//
//     function renderQuizQuestions() {
//         const container = document.getElementById('quizQuestionsContainer');
//         container.innerHTML = '';
//         quizQuestions.forEach((q, i) => {
//             const div = document.createElement('div');
//             div.classList.add('border', 'p-2', 'mb-2', 'rounded');
//             div.innerHTML = `
//                 <div class="d-flex justify-content-between align-items-center mb-1">
//                     <strong>Question ${i + 1}</strong>
//                     <button type="button" class="btn btn-sm btn-danger btnRemoveQuestion" data-index="${i}">Remove</button>
//                 </div>
//                 <input type="text" class="form-control mb-1" placeholder="Question text" value="${q.question}" data-index="${i}" data-field="question">
//                 ${q.options.map((opt, j) => `<input type="text" class="form-control mb-1" placeholder="Option ${j+1}" value="${opt}" data-index="${i}" data-option="${j}">`).join('')}
//                 <label>Correct Option:
//                     <select class="form-select mb-1" data-index="${i}" data-field="correctOptionIndex">
//                         ${q.options.map((_, j) => `<option value="${j}" ${q.correctOptionIndex === j ? 'selected' : ''}>Option ${j+1}</option>`).join('')}
//                     </select>
//                 </label>
//             `;
//             container.appendChild(div);
//         });
//
//         container.querySelectorAll('.btnRemoveQuestion').forEach(btn => {
//             btn.onclick = () => {
//                 const idx = btn.dataset.index;
//                 quizQuestions.splice(idx, 1);
//                 renderQuizQuestions();
//             };
//         });
//
//         container.querySelectorAll('input, select').forEach(input => {
//             input.oninput = e => {
//                 const idx = e.target.dataset.index;
//                 if (e.target.dataset.field === 'question') quizQuestions[idx].question = e.target.value;
//                 else if (e.target.dataset.field === 'correctOptionIndex') quizQuestions[idx].correctOptionIndex = parseInt(e.target.value);
//                 else if (e.target.dataset.option !== undefined) quizQuestions[idx].options[e.target.dataset.option] = e.target.value;
//             };
//         });
//     }
//
//     document.getElementById('btnAddQuizQuestion')?.addEventListener('click', () => {
//         quizQuestions.push({ question: '', options: ['', '', '', ''], correctOptionIndex: 0 });
//         renderQuizQuestions();
//     });
//     //
//     document.getElementById('tutorialForm')?.addEventListener('submit', e => {
//         const hiddenInput = document.createElement('input');
//         hiddenInput.type = 'hidden';
//         hiddenInput.name = 'quizQuestionsJson';
//         hiddenInput.value = JSON.stringify(quizQuestions);
//         e.target.appendChild(hiddenInput);
//     });
//     //
//     // function renderViewModal(data) {
//     //     document.getElementById('viewTutorialTitle').innerText = data.title;
//     //     document.getElementById('viewFileSection').classList.add('d-none');
//     //     document.getElementById('viewArticleSection').classList.add('d-none');
//     //     document.getElementById('viewQuizSection').classList.add('d-none');
//     //
//     //     if (data.type === 'VIDEO' || data.type === 'PDF') {
//     //         const container = document.getElementById('viewFileContainer');
//     //         container.innerHTML = '';
//     //         if (data.filePath) {
//     //             if (data.type === 'VIDEO') {
//     //                 const video = document.createElement('video');
//     //                 video.src = data.filePath;
//     //                 video.controls = true;
//     //                 video.classList.add('w-100');
//     //                 container.appendChild(video);
//     //             } else {
//     //                 const link = document.createElement('a');
//     //                 link.href = data.filePath;
//     //                 link.target = '_blank';
//     //                 link.innerText = 'Open PDF';
//     //                 container.appendChild(link);
//     //             }
//     //         }
//     //         document.getElementById('viewFileSection').classList.remove('d-none');
//     //     }
//     //
//     //     if (data.type === 'ARTICLE') {
//     //         document.getElementById('viewArticleContent').innerHTML = data.articleContent || '<em>No content</em>';
//     //         document.getElementById('viewArticleSection').classList.remove('d-none');
//     //     }
//     //
//     //     if (data.type === 'QUIZ') {
//     //         const container = document.getElementById('viewQuizQuestionsContainer');
//     //         container.innerHTML = '';
//     //         (data.quizQuestions || []).forEach((q, i) => {
//     //             const div = document.createElement('div');
//     //             div.classList.add('border', 'p-2', 'mb-2', 'rounded');
//     //             div.innerHTML = `<strong>Q${i+1}: ${q.question}</strong>
//     //                              <ul>${q.options.map((opt, j) => `<li>${opt} ${q.correctOptionIndex === j ? '(Correct)' : ''}</li>`).join('')}</ul>`;
//     //             container.appendChild(div);
//     //         });
//     //         document.getElementById('viewQuizSection').classList.remove('d-none');
//     //     }
//     // }
//
//
//
//     /*----------------------------------------------------
//       VIEW TUTORIAL
//   ----------------------------------------------------*/
//     // function openViewTutorialModal(id) {
//     //     fetch(`/dr/tutorial/${id}/json`)
//     //         .then(res => res.json())
//     //         .then(t => {
//     //             document.getElementById("viewTutorialTitle").innerText = t.title;
//     //
//     //             // Hide all sections
//     //             document.getElementById("viewFileSection").classList.add("d-none");
//     //             document.getElementById("viewArticleSection").classList.add("d-none");
//     //             document.getElementById("viewQuizSection").classList.add("d-none");
//     //
//     //             if (t.type === "VIDEO" || t.type === "PDF") {
//     //                 let container = document.getElementById("viewFileContainer");
//     //                 container.innerHTML = "";
//     //
//     //                 if (t.type === "VIDEO") {
//     //                     container.innerHTML = `
//     //                     <video controls class="w-100 rounded">
//     //                         <source src="${t.filePath}" type="video/mp4">
//     //                     </video>`;
//     //                 } else {
//     //                     container.innerHTML =
//     //                         `<iframe src="${t.filePath}" class="w-100" style="height: 500px;"></iframe>`;
//     //                 }
//     //
//     //                 document.getElementById("viewFileSection").classList.remove("d-none");
//     //             }
//     //
//     //             if (t.type === "ARTICLE") {
//     //                 document.getElementById("viewArticleContent").innerHTML = t.articleContent;
//     //                 document.getElementById("viewArticleSection").classList.remove("d-none");
//     //             }
//     //
//     //             if (t.type === "QUIZ") {
//     //                 let wrapper = document.getElementById("viewQuizQuestionsContainer");
//     //                 wrapper.innerHTML = "";
//     //
//     //                 t.quizQuestions.forEach((q, i) => {
//     //                     wrapper.innerHTML += `
//     //                     <div class="p-2 border rounded mb-2">
//     //                         <b>Q${i + 1}:</b> ${q.question}
//     //                     </div>`;
//     //                 });
//     //
//     //                 document.getElementById("viewQuizSection").classList.remove("d-none");
//     //             }
//     //
//     //             new bootstrap.Modal(document.getElementById("modalViewTutorial")).show();
//     //         });
//     // }
//
//     // /*----------------------------------------------------
//     //     EDIT TUTORIAL (uses same Add modal)
//     // ----------------------------------------------------*/
//     function openEditTutorialModal(id) {
//         fetch(`/dr/tutorial/${id}/json`)
//             .then(res => res.json())
//             .then(t => {
//
//                 // Fill fields
//                 document.getElementById("tutorialId").value = t.id;
//                 document.getElementById("tutorialTitle").value = t.title;
//                 document.getElementById("tutorialTypeSelect").value = t.type;
//
//                 // Show correct section based on type
//                 showTutorialType(t.type);
//
//                 if (t.type === "ARTICLE")
//                     document.getElementById("tutorialArticle").value = t.articleContent;
//
//                 if (t.type === "QUIZ") {
//                     let container = document.getElementById("quizQuestionsContainer");
//                     container.innerHTML = "";
//                     t.quizQuestions.forEach(q => {
//                         container.innerHTML += `
//                         <div class="border p-2 rounded mb-2">
//                             <input class="form-control mb-2" value="${q.question}">
//                         </div>`;
//                     });
//                 }
//
//                 // Set form to EDIT mode
//                 tutorialForm.action = `/dr/tutorial/${id}/edit`;
//
//                 new bootstrap.Modal(document.getElementById("modalAddTutorial")).show();
//             });
//     }
//
//     /*----------------------------------------------------
//         ADD TUTORIAL BUTTON
//     ----------------------------------------------------*/
//     function openAddTutorialModal(sectionId) {
//         document.getElementById("tutorialForm").reset();
//         document.getElementById("tutorialSectionId").value = sectionId;
//         document.getElementById("tutorialId").value = "";
//
//         // Set URL to ADD mode
//         tutorialForm.action = `/dr/sections/${sectionId}/tutorials/add`;
//
//         showTutorialType(null);
//
//         new bootstrap.Modal(modalAddTutorial).show();
//     }
//
//     /*----------------------------------------------------
//         DELETE TUTORIAL
//     ----------------------------------------------------*/
//
//
//     // /*----------------------------------------------------
//     //     Helper: Show correct fields based on type
//     // ----------------------------------------------------*/
//     function showTutorialType(type) {
//
//         document.getElementById("fileUploadSection").classList.add("d-none");
//         document.getElementById("articleSection").classList.add("d-none");
//         document.getElementById("quizSection").classList.add("d-none");
//
//         if (type === "VIDEO" || type === "PDF")
//             document.getElementById("fileUploadSection").classList.remove("d-none");
//
//         if (type === "ARTICLE")
//             document.getElementById("articleSection").classList.remove("d-none");
//
//         if (type === "QUIZ")
//             document.getElementById("quizSection").classList.remove("d-none");
//     }
//
//
//
//     async function loadTutorialIntoForm(sectionId) {
//         const res = await fetch(`/dr/tutorials/${courseId}/json`);
//         const tutorial = await res.json();
//
//         const form = document.getElementById('tutorialForm');
//         form.querySelector('input[name="title"]').value = tutorial.title;
//         form.querySelector('input[name="courseName"]').value = tutorial.courseName;
//         form.querySelector('input[name="status"]').value = tutorial.status;
//         form.querySelector('select[name="type"]').value = tutorial.type;
//         form.querySelector('input[name="videoUrl"]').value = tutorial.videoUrl || "";
//         form.dataset.tutorialId = tutorial.id;
//     }
//
//
//
//
// // =======================
// // LOAD TUTORIALS
// // =======================
// //     async function loadTutorials(sectionId, page = 1) {
// //         const container = document.getElementById(`tutorials-container-${sectionId}`);
// //         if (!container) return;
// //
// //         container.innerHTML = `<div class="text-center text-muted py-3">
// //         <i class="fas fa-spinner fa-spin me-1"></i> Loading tutorials...
// //     </div>`;
// //
// //         try {
// //             const res = await fetch(`/dr/sections/${sectionId}/tutorials-fragment?page=${page}`);
// //             if (!res.ok) throw new Error(`Failed to load tutorials: ${res.status}`);
// //             container.innerHTML = await res.text();
// //             attachTutorialButtons(sectionId);
// //         } catch (err) {
// //             container.innerHTML = `<div class="alert alert-danger text-center py-3">${err.message}</div>`;
// //             console.error(err);
// //         }
// //     }
//
// // =======================
// // ATTACH BUTTON HANDLERS
// // =======================
//     function attachTutorialButtons(sectionId) {
//         const container = document.getElementById(`tutorials-container-${sectionId}`);
//         if (!container) return;
//
//         // VIEW
//         container.querySelectorAll('.btn-view-tutorial').forEach(btn => {
//             btn.addEventListener('click', async () => {
//                 const id = btn.dataset.id;
//                 if (!id) return;
//                 await openViewTutorialModal(id);
//             });
//         });
//
//         // EDIT
//         container.querySelectorAll('.btn-edit-tutorial').forEach(btn => {
//             btn.addEventListener('click', async () => {
//                 const id = btn.dataset.id;
//                 if (!id) return;
//                 await  loadTutorialIntoForm(id)
//                 await openAddTutorialModal(id);
//             });
//         });
//
//         // DELETE
//         container.querySelectorAll('.btn-delete-tutorial').forEach(btn => {
//             btn.addEventListener('click', () => {
//                 const id = btn.dataset.id;
//                 if (!id) return;
//                 openDeleteTutorialModal(id);
//             });
//         });
//     }
//
// // =======================
// // VIEW MODAL
// // =======================
//     async function openViewTutorialModal(id) {
//         try {
//             const res = await fetch(`/dr/tutorial/${id}/json`);
//             if (!res.ok) throw new Error('Failed to fetch tutorial');
//             const t = await res.json();
//
//             document.getElementById('viewTutorialTitle').textContent = t.title || 'Untitled';
//
//             document.getElementById('viewFileSection').classList.toggle('d-none', !t.filePath);
//             document.getElementById('viewArticleSection').classList.toggle('d-none', !t.articleContent);
//             document.getElementById('viewQuizSection').classList.toggle('d-none', !t.quizQuestions || t.quizQuestions.length === 0);
//
//             if (t.filePath) {
//                 document.getElementById('viewFileContainer').innerHTML = `<a href="${t.filePath}" target="_blank">Download File</a>`;
//             }
//             if (t.articleContent) {
//                 document.getElementById('viewArticleContent').textContent = t.articleContent;
//             }
//             if (t.quizQuestions) {
//                 const container = document.getElementById('viewQuizQuestionsContainer');
//                 container.innerHTML = '';
//                 t.quizQuestions.forEach((q, i) => {
//                     const div = document.createElement('div');
//                     div.textContent = `${i+1}. ${q.question}`;
//                     container.appendChild(div);
//                 });
//             }
//
//             new bootstrap.Modal(document.getElementById('modalViewTutorial')).show();
//         } catch (err) {
//             console.error(err);
//             alert('Error loading tutorial.');
//         }
//     }
//
// // =======================
// // EDIT MODAL
// // =======================
// //     async function openEditTutorialModal(id) {
// //         try {
// //             const res = await fetch(`/dr/tutorial/${id}/json`);
// //             if (!res.ok) throw new Error('Failed to fetch tutorial');
// //             const t = await res.json();
// //
// //             document.getElementById('tutorialId').value = t.id || '';
// //             document.getElementById('tutorialTitle').value = t.title || '';
// //             document.getElementById('tutorialTypeSelect').value = t.type || '';
// //             document.getElementById('tutorialArticle').value = t.articleContent || '';
// //             document.getElementById('tutorialFile').value = '';
// //
// //             document.getElementById('fileUploadSection').classList.toggle('d-none', t.type === 'ARTICLE' || t.type === 'QUIZ');
// //             document.getElementById('articleSection').classList.toggle('d-none', t.type !== 'ARTICLE');
// //             document.getElementById('quizSection').classList.toggle('d-none', t.type !== 'QUIZ');
// //
// //             new bootstrap.Modal(document.getElementById('modalAddTutorial')).show();
// //         } catch (err) {
// //             console.error(err);
// //             alert('Error opening edit modal.');
// //         }
// //     }
//
// // =======================
// // DELETE MODAL
// // =======================
// //     function openDeleteTutorialModal(id) {
// //         const deleteInput = document.getElementById('deleteTutorialId');
// //         if (!deleteInput) return;
// //         deleteInput.value = id;
// //         new bootstrap.Modal(document.getElementById('modalDeleteTutorial')).show();
// //     }
//
// // =======================
// // ADD TUTORIAL BUTTON
// // =======================
// //     document.querySelectorAll('.btn-add-tutorial').forEach(btn => {
// //         btn.onclick = () => {
// //             document.getElementById('tutorialForm').reset();
// //             const sectionId = btn.dataset.sectionId;
// //             document.getElementById('tutorialSectionId').value = sectionId;
// //             document.getElementById('fileUploadSection').classList.add('d-none');
// //             document.getElementById('articleSection').classList.add('d-none');
// //             document.getElementById('quizSection').classList.add('d-none');
// //             new bootstrap.Modal(document.getElementById('modalAddTutorial')).show();
// //         };
// //     });
//
//
// // =======================
// // HANDLE ADD/EDIT FORM SUBMIT
// // =======================
//     document.getElementById('tutorialForm')?.addEventListener('submit', async e => {
//         e.preventDefault();
//         const form = e.target;
//         const formData = new FormData(form);
//
//         // Add CSRF token if not in form
//         if (!formData.has('_csrf')) {
//             formData.append('_csrf', csrfToken);
//         }
//
//         const tutorialId = formData.get('tutorialId');
//         const sectionId = formData.get('sectionId');
//
//         const url = tutorialId ? `/dr/tutorial/${tutorialId}/edit` : `/dr/sections/${sectionId}/tutorials/add`;
//
//         const res = await fetch(url, {
//             method: 'POST',
//             body: formData
//         });
//
//         if (res.ok) {
//             loadTutorials(sectionId);
//             bootstrap.Modal.getInstance(document.getElementById('modalAddTutorial')).hide();
//         } else {
//             const data = await res.json();
//             alert(data.message || "Error adding tutorial");
//         }
//     });
//
//
// // =======================
// // HANDLE DELETE FORM SUBMIT
// // =======================
//     document.addEventListener('DOMContentLoaded', () => {
//         const deleteForm = document.getElementById('deleteTutorialForm');
//         if (deleteForm) {
//             deleteForm.addEventListener('submit', async e => {
//                 e.preventDefault();
//                 const id = document.getElementById('deleteTutorialId').value;
//                 try {
//                     const res = await fetch(`/dr/tutorial/delete`, {
//                         method: 'POST',
//                         headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
//                         body: `id=${id}`
//                     });
//                     const data = await res.json();
//                     if (data.success) {
//                         const sectionId = document.getElementById('tutorialSectionId')?.value || 1;
//                         loadTutorials(sectionId);
//                         bootstrap.Modal.getInstance(document.getElementById('modalDeleteTutorial')).hide();
//                     } else {
//                         alert(data.message);
//                     }
//                 } catch (err) {
//                     console.error(err);
//                     alert('Error deleting tutorial.');
//                 }
//             });
//         }
//     });
//
//
//
//
//
//
//
//     /* ====================================================
//      *                   STUDENTS
//      * ==================================================== */
//
//         async function loadStudents(courseId, page = 0) {
//         const container = document.getElementById(`students-container-${courseId}`);
//         if (!container) return;
//         container.innerHTML = `<div class="text-center text-muted">
//             <i class="fas fa-spinner fa-spin me-1"></i> Loading students...
//         </div>`;
//         const res = await fetch(`/dr/courses/${courseId}/students-fragment?page=${page}`);
//         container.innerHTML = await res.text();
//         attachStudentPagination(courseId);
//     }
//
//     function attachStudentPagination(courseId) {
//         document.querySelectorAll(`#students-container-${courseId} .pagination a`).forEach(link => {
//             link.addEventListener('click', (e) => {
//                 e.preventDefault();
//                 const page = new URL(link.href).searchParams.get('page') || 0;
//                 loadStudents(courseId, page);
//             });
//         });
//     }
//
//
//     document.querySelectorAll(".btn-view-enrollments").forEach(btn => {
//         btn.addEventListener("click", async () => {
//             const courseId = btn.dataset.courseId;
//
//             const modalBody = document.getElementById("enrollmentsContainer");
//             modalBody.innerHTML = "Loading...";
//
//             const res = await fetch(`/dr/courses/${courseId}/students-fragment`);
//             const html = await res.text();
//             modalBody.innerHTML = html;
//
//             // attachPaginationListeners();
//
//             new bootstrap.Modal(document.getElementById("modalViewEnrollments")).show();
//         });
//     });
//
//
//
//     //     async function loadStudents(courseId, page = 0) {
//     //     const container = document.getElementById(`students-container-${courseId}`);
//     //     if (!container) return;
//     //     container.innerHTML = `<div class="text-center text-muted">
//     //         <i class="fas fa-spinner fa-spin me-1"></i> Loading students...
//     //     </div>`;
//     //     const res = await fetch(`/dr/courses/${courseId}/students-fragment?page=${page}`);
//     //     container.innerHTML = await res.text();
//     //     attachStudentPagination(courseId);
//     // }
//     //     function attachStudentPagination(courseId) {
//     //     document.querySelectorAll(`#students-container-${courseId} .pagination a`).forEach(link => {
//     //         link.addEventListener('click', (e) => {
//     //             e.preventDefault();
//     //             const page = new URL(link.href).searchParams.get('page') || 0;
//     //             loadStudents(courseId, page);
//     //         });
//     //     });
//     // }
//     /* ====================================================
//      *                   INITIAL LOAD
//      * ==================================================== */
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
//
// });
// function openDeleteTutorialModal(id) {
//     document.getElementById("deleteTutorialId").value = id;
//     document.getElementById("deleteTutorialForm").action = `/dr/tutorial/${id}/delete`;
//     new bootstrap.Modal(document.getElementById("modalDeleteTutorial")).show();
// }
//
// document.addEventListener("DOMContentLoaded", () => {
//
//     const deleteCourseModalEl = document.getElementById("deleteCourseModal");
//     const deleteCourseModal = new bootstrap.Modal(deleteCourseModalEl);
//
//     let selectedCourseId = null;
//
//     document.querySelectorAll(".btn-delete-course").forEach(btn => {
//         btn.addEventListener("click", () => {
//             selectedCourseId = btn.dataset.courseId;
//             deleteCourseModal.show();
//         });
//     });
//
//     document.getElementById("confirmDeleteBtn").addEventListener("click", async () => {
//         if (!selectedCourseId) return;
//
//         await fetch(`/dr/courses/delete/${selectedCourseId}`, {
//             method: "POST",
//             headers: {
//                 "Content-Type": "application/x-www-form-urlencoded"
//             },
//             body: "_csrf=" + document.querySelector('meta[name="_csrf"]').content
//         });
//
//         deleteCourseModal.hide();
//         window.location.reload();
//     });
//
// });
//
//
// /////////////////////////////
// /////////
//
//
//

document.addEventListener("DOMContentLoaded", () => {
    // =======================
    // CSRF SETUP
    // =======================
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // =======================
    // COURSES
    // =======================
    const createCourseModal = new bootstrap.Modal(document.getElementById('modalCreateCourse'));

    document.getElementById('btnCreateCourse')?.addEventListener('click', () => {
        resetCourseForm();
        createCourseModal.show();
    });

    document.querySelectorAll(".btnEditCourse").forEach(btn => {
        btn.addEventListener("click", async () => {
            const courseId = btn.dataset.courseId;
            await loadCourseIntoForm(courseId);
            createCourseModal.show();
        });
    });

    document.getElementById("formCreateCourse")?.addEventListener("submit", async e => {
        e.preventDefault();
        const form = e.target;
        const courseId = form.dataset.courseId;
        const url = courseId ? `/dr/courses/edit/${courseId}` : `/dr/courses/create`;
        const formData = new FormData(form);
        formData.append("_csrf", csrfToken);

        await fetch(url, { method: 'POST', body: formData });
        createCourseModal.hide();
        loadAllCourses();
    });

    async function loadCourseIntoForm(courseId) {
        const res = await fetch(`/dr/courses/${courseId}/json`);
        const course = await res.json();
        const form = document.getElementById('formCreateCourse');
        form.querySelector('input[name="title"]').value = course.title;
        form.querySelector('input[name="courseName"]').value = course.courseName;
        form.querySelector('input[name="price"]').value = course.price;
        form.querySelector('select[name="published"]').value = course.published;
        form.querySelector('input[name="videoUrl"]').value = course.videoUrl || "";
        form.querySelector('textarea[name="description"]').value = course.description || "";
        form.dataset.courseId = course.id;
    }

    function resetCourseForm() {
        const form = document.getElementById('formCreateCourse');
        form.reset();
        delete form.dataset.courseId;
    }

    const deleteCourseModal = new bootstrap.Modal(document.getElementById("deleteCourseModal"));
    let selectedCourseId = null;

    document.querySelectorAll(".btn-delete-course").forEach(btn => {
        btn.addEventListener("click", () => {
            selectedCourseId = btn.dataset.courseId;
            deleteCourseModal.show();
        });
    });

    document.getElementById("confirmDeleteBtn")?.addEventListener("click", async () => {
        if (!selectedCourseId) return;
        await fetch(`/dr/courses/delete/${selectedCourseId}`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `_csrf=${csrfToken}`
        });
        deleteCourseModal.hide();
        window.location.reload();
    });

    // =======================
    // SECTIONS
    // =======================
    const sectionModal = new bootstrap.Modal(document.getElementById('modalSection'));

    function attachSectionButtons() {
        document.querySelectorAll('.btn-add-section').forEach(btn => {
            btn.onclick = () => {
                document.getElementById('sectionCourseId').value = btn.dataset.courseId;
                document.getElementById('sectionTitle').value = '';
                document.getElementById('sectionId').value = '';
                sectionModal.show();
            };
        });

        document.querySelectorAll('.btn-edit-section').forEach(btn => {
            btn.onclick = () => {
                document.getElementById('sectionCourseId').value = btn.dataset.courseId;
                document.getElementById('sectionTitle').value = btn.dataset.sectionTitle;
                document.getElementById('sectionId').value = btn.dataset.sectionId;
                sectionModal.show();
            };
        });
    }

    const deleteSectionModal = new bootstrap.Modal(document.getElementById("deleteSectionModal"));
    let selectedSectionId = null;

    document.body.addEventListener("click", (e) => {
        if (e.target.closest(".btn-delete-section")) {
            const btn = e.target.closest(".btn-delete-section");
            selectedSectionId = btn.dataset.sectionId;
            deleteSectionModal.show();
        }
    });

    document.getElementById("confirmSectionDeleteBtn")?.addEventListener("click", async () => {
        if (!selectedSectionId) return;
        await fetch(`/dr/sections/delete/${selectedSectionId}`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `_csrf=${csrfToken}`
        });
        deleteSectionModal.hide();
        const currentCourseId = document.getElementById("sectionList")?.dataset.courseId;
        if (currentCourseId) loadSections(currentCourseId, 1);
        else window.location.reload();
    });

    document.getElementById('btnSaveSection')?.addEventListener('click', () => {
        document.getElementById('sectionForm').requestSubmit();
    });

    document.getElementById('sectionForm')?.addEventListener('submit', async e => {
        e.preventDefault();
        const courseId = document.getElementById('sectionCourseId').value;
        const sectionId = document.getElementById('sectionId').value;
        const title = document.getElementById('sectionTitle').value;
        const description = document.getElementById('sectionDescription').value;

        const url = sectionId ? `/dr/sections/edit/${sectionId}` : `/dr/courses/${courseId}/sections/add`;
        const formData = new URLSearchParams();
        formData.append('title', title);
        formData.append('description', description);

        await fetch(url, { method: 'POST', body: formData, headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken } });
        sectionModal.hide();
        loadSections(courseId);
    });

    async function loadSections(courseId, page = 1) {
        const container = document.getElementById(`sections-container-${courseId}`);
        if (!container) return;
        container.innerHTML = `<div class="text-center text-muted"><i class="fas fa-spinner fa-spin me-1"></i> Loading sections...</div>`;
        const res = await fetch(`/dr/courses/${courseId}/sections-fragment?page=${page}`);
        container.innerHTML = await res.text();

        attachSectionButtons();
        attachSectionPagination(courseId);

        container.querySelectorAll('.tutorial-wrapper').forEach(wrapper => {
            const sectionId = wrapper.id.replace('tutorials-container-', '');
            loadTutorials(sectionId, 1);
        });
    }

    function attachSectionPagination(courseId) {
        document.querySelectorAll(`#sections-container-${courseId} .pagination a`).forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                const page = new URL(link.href).searchParams.get('page') || 1;
                loadSections(courseId, page);
            });
        });
    }


    // =======================
    // TUTORIALS
    // =======================
// ==========================
// Global state
// ==========================
    const quizQuestions = [];
    const modalAddTutorial = document.getElementById('modalAddTutorial');

// ==========================
// Delegated click listener
// ==========================
    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('button');
        if (!btn) return;

        const sectionId = btn.dataset.sectionId;
        const tutorialId = btn.dataset.id;

        // 1️⃣ Toggle Tutorials Collapse
        if (btn.classList.contains('btn-toggle-tutorials')) {
            const collapse = document.getElementById(`tutorials-collapse-${sectionId}`);
            if (!collapse.classList.contains('show')) {
                const html = await fetch(`/dr/sections/${sectionId}/tutorials-fragment`).then(res => res.text());
                document.getElementById(`tutorials-container-${sectionId}`).innerHTML = html;
                new bootstrap.Collapse(collapse, { toggle: true });
            } else {
                new bootstrap.Collapse(collapse, { toggle: true });
            }
        }

        // 2️⃣ Add Tutorial
        else if (btn.classList.contains('btn-add-tutorial')) {
            document.getElementById('tutorialForm').reset();
            document.getElementById('tutorialSectionId').value = sectionId;
            showTutorialType('');
            quizQuestions.length = 0;
            renderQuizQuestions();
            new bootstrap.Modal(modalAddTutorial).show();
        }

        // 3️⃣ Edit Tutorial
        else if (btn.classList.contains('btn-edit-tutorial')) {
            if (!tutorialId) return;
            await loadTutorialIntoForm(tutorialId);
            new bootstrap.Modal(modalAddTutorial).show();
        }

        // 4️⃣ Delete Tutorial
        else if (btn.classList.contains('btn-delete-tutorial')) {
            if (!tutorialId) return;
            openDeleteTutorialModal(tutorialId);
        }



        // 5️⃣ Remove Quiz Question
        else if (btn.classList.contains('btnRemoveQuestion')) {
            const idx = parseInt(btn.dataset.index);
            quizQuestions.splice(idx, 1);
            renderQuizQuestions();
        }
        //  View Tutorial
        else if (btn.classList.contains('btn-view-tutorial')) {
            if (!tutorialId) return;
            await openViewTutorialModal(tutorialId);
        }
    });

// ==========================
// Tutorial type change
// ==========================
    document.getElementById('tutorialTypeSelect')?.addEventListener('change', e => {
        showTutorialType(e.target.value);
    });

// ==========================
// Functions
// ==========================
    function showTutorialType(type) {
        document.getElementById("fileUploadSection").classList.toggle("d-none", !(type === "VIDEO" || type === "PDF"));
        document.getElementById("articleSection").classList.toggle("d-none", type !== "ARTICLE");
        document.getElementById("quizSection").classList.toggle("d-none", type !== "QUIZ");
    }

    function renderQuizQuestions() {
        const container = document.getElementById('quizQuestionsContainer');
        container.innerHTML = '';

        quizQuestions.forEach((q, i) => {
            const div = document.createElement('div');
            div.classList.add('border', 'p-2', 'mb-2', 'rounded');
            div.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-1">
                <strong>Question ${i + 1}</strong>
                <button type="button" class="btn btn-sm btn-danger btnRemoveQuestion" data-index="${i}">Remove</button>
            </div>
            <input type="text" class="form-control mb-1" placeholder="Question text" value="${q.question}" data-index="${i}" data-field="question">
            ${q.options.map((opt, j) => `<input type="text" class="form-control mb-1" placeholder="Option ${j+1}" value="${opt}" data-index="${i}" data-option="${j}">`).join('')}
            <label>Correct Option:
                <select class="form-select mb-1" data-index="${i}" data-field="correctOptionIndex">
                    ${q.options.map((_, j) => `<option value="${j}" ${q.correctOptionIndex === j ? 'selected' : ''}>Option ${j+1}</option>`).join('')}
                </select>
            </label>
        `;
            container.appendChild(div);
        });

        // Input handling
        container.querySelectorAll('input, select').forEach(input => {
            input.oninput = e => {
                const idx = parseInt(e.target.dataset.index);
                if (e.target.dataset.field === 'question') quizQuestions[idx].question = e.target.value;
                else if (e.target.dataset.field === 'correctOptionIndex') quizQuestions[idx].correctOptionIndex = parseInt(e.target.value);
                else if (e.target.dataset.option !== undefined) quizQuestions[idx].options[e.target.dataset.option] = e.target.value;
            };
        });
    }

// Add new quiz question
    document.getElementById('btnAddQuizQuestion')?.addEventListener('click', () => {
        quizQuestions.push({ question: '', options: ['', '', '', ''], correctOptionIndex: 0 });
        renderQuizQuestions();
    });

// ==========================
// Load Tutorials
// ==========================
    async function loadTutorials(sectionId, page = 1) {
        const container = document.getElementById(`tutorials-container-${sectionId}`);
        if (!container) return;
        container.innerHTML = `<div class="text-center text-muted"><i class="fas fa-spinner fa-spin me-1"></i> Loading tutorials...</div>`;
        const html = await fetch(`/dr/sections/${sectionId}/tutorials-fragment?page=${page}`).then(res => res.text());
        container.innerHTML = html;
    }

// ==========================
// Load Tutorial Into Form
// ==========================
    async function loadTutorialIntoForm(tutorialId) {
        const t = await fetch(`/dr/tutorial/${tutorialId}/json`).then(res => res.json());
        const form = document.getElementById('tutorialForm');
        form.querySelector('input[name="title"]').value = t.title || '';
        form.querySelector('input[name="tutorialId"]').value = t.id;
        form.querySelector('input[name="sectionId"]').value = t.sectionId || '';
        form.querySelector('select[name="type"]').value = t.type || '';
        showTutorialType(t.type);

        if (t.type === 'QUIZ') {
            quizQuestions.length = 0;
            t.quizQuestions.forEach(q => quizQuestions.push(q));
            renderQuizQuestions();
        }
    }

// ==========================
// Delete Tutorial Modal
// ==========================
    function openDeleteTutorialModal(id) {
        document.getElementById("deleteTutorialId").value = id;
        new bootstrap.Modal(document.getElementById("modalDeleteTutorial")).show();
    }

// ==========================
// Submit Tutorial Form
// ==========================
    document.getElementById('tutorialForm')?.addEventListener('submit', async e => {
        e.preventDefault();
        const form = e.target;
        const formData = new FormData(form);

        if (!formData.has('_csrf')) formData.append('_csrf', csrfToken);

        // ✅ Add quizQuestions if type is QUIZ
        if (formData.get('type') === 'QUIZ') {
            formData.append('quizQuestions', JSON.stringify(quizQuestions));
        }

        const tutorialId = formData.get('tutorialId');
        const sectionId = formData.get('sectionId');
        const url = tutorialId ? `/dr/tutorial/${tutorialId}/edit` : `/dr/sections/${sectionId}/tutorials/add`;

        const res = await fetch(url, { method: 'POST', body: formData });
        if (res.ok) {
            await loadTutorials(sectionId);
            bootstrap.Modal.getInstance(modalAddTutorial).hide();
            quizQuestions.length = 0;      // Clear questions after submit
            renderQuizQuestions();          // Reset the quiz container
        } else {
            const data = await res.json();
            alert(data.message || "Error");
        }
    });


// ==========================
// Delete Tutorial Form
// ==========================
    document.getElementById('deleteTutorialForm')?.addEventListener('submit', async e => {
        e.preventDefault();
        const id = document.getElementById('deleteTutorialId').value;
        try {
            const res = await fetch(`/dr/tutorial/${id}/delete-json`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: `id=${id}`
            });
            const data = await res.json();
            if (data.success) {
                const sectionId = document.getElementById('tutorialSectionId')?.value;
                if(sectionId) await loadTutorials(sectionId);
                bootstrap.Modal.getInstance(document.getElementById('modalDeleteTutorial')).hide();
            } else alert(data.message);
        } catch (err) {
            console.error(err);
            alert('Error deleting tutorial.');
        }
    });

// ==========================
// View Tutorial Modal
// ==========================
    async function openViewTutorialModal(id) {
        console.log("openViewTutorialModal", id);
        try {
            const tutorial = await fetch(`/dr/tutorial/${id}/json`).then(res => res.json());

            document.getElementById('viewTutorialTitle').textContent = tutorial.title;

            // Hide all sections
            ['viewFileSection','viewArticleSection','viewQuizSection'].forEach(id => document.getElementById(id).classList.add('d-none'));

            if (tutorial.type === 'VIDEO' || tutorial.type === 'PDF') {
                const container = document.getElementById('viewFileContainer');
                container.innerHTML = tutorial.type === 'VIDEO'
                    ? `<video class="w-100" controls><source src="${tutorial.filePath}" type="video/mp4"></video>`
                    : `<a href="${tutorial.filePath}" target="_blank">Download PDF</a>`;
                document.getElementById('viewFileSection').classList.remove('d-none');
            } else if (tutorial.type === 'ARTICLE') {
                document.getElementById('viewArticleContent').innerHTML = tutorial.articleContent;
                document.getElementById('viewArticleSection').classList.remove('d-none');
            } else if (tutorial.type === 'QUIZ') {
                const container = document.getElementById('viewQuizQuestionsContainer');
                container.innerHTML = '';
                tutorial.quizQuestions.forEach((q,i) => {
                    const div = document.createElement('div');
                    div.classList.add('mb-3','p-2','border','rounded');
                    div.innerHTML = `<strong>Q${i+1}: ${q.question}</strong><br>Options: ${q.options.join(', ')}<br>Answer: ${q.answer}`;
                    container.appendChild(div);
                });
                document.getElementById('viewQuizSection').classList.remove('d-none');
            }

            const modalEl = document.getElementById('modalViewTutorial');
            let modal = bootstrap.Modal.getInstance(modalEl);
            if (!modal) modal = new bootstrap.Modal(modalEl);
            modal.show();

        } catch (err) {
            console.error(err);
            alert('Failed to load tutorial: ' + err.message);
        }
    }



    // =======================
    // STUDENTS
    // =======================
    async function loadStudents(courseId, page = 0) {
        const container = document.getElementById(`students-container-${courseId}`);
        if (!container) return;
        container.innerHTML = `<div class="text-center text-muted"><i class="fas fa-spinner fa-spin me-1"></i> Loading students...</div>`;
        const res = await fetch(`/dr/courses/${courseId}/students-fragment?page=${page}`);
        container.innerHTML = await res.text();
        attachStudentPagination(courseId);
    }

    function attachStudentPagination(courseId) {
        document.querySelectorAll(`#students-container-${courseId} .pagination a`).forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                const page = new URL(link.href).searchParams.get('page') || 0;
                loadStudents(courseId, page);
            });
        });
    }

    document.querySelectorAll(".btn-view-enrollments").forEach(btn => {
        btn.addEventListener("click", async () => {
            const courseId = btn.dataset.courseId;
            const modalBody = document.getElementById("enrollmentsContainer");
            modalBody.innerHTML = "Loading...";
            const res = await fetch(`/dr/courses/${courseId}/students-fragment`);
            modalBody.innerHTML = await res.text();
            new bootstrap.Modal(document.getElementById("modalViewEnrollments")).show();
        });
    });

    // =======================
    // LOAD ALL COURSES INITIAL
    // =======================
    async function loadAllCourses() {
        document.querySelectorAll('.course-card').forEach(card => {
            const courseId = card.querySelector('.btn-add-section')?.dataset.courseId;
            if (courseId) { loadSections(courseId); loadStudents(courseId); }
        });
    }
    loadAllCourses();
});
