
/* ============================================================
   GLOBAL HELPERS
   ============================================================ */

function showToast(message, type = "success") {
    const toast = document.createElement("div");
    toast.className = `toast align-items-center text-bg-${type} border-0 position-fixed top-0 end-0 m-3`;
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
        </div>
    `;
    document.body.appendChild(toast);

    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    setTimeout(() => toast.remove(), 5000);
}

/* ============================================================
   LOAD TUTORIALS (AJAX)
   ============================================================ */
async function loadTutorials(courseId, page = 0) {
    const container = document.getElementById(`tutorials-container-${courseId}`);
    if (!container) return;

    container.innerHTML = `<div class="text-center text-muted">Loading...</div>`;

    try {
        const res = await fetch(`/dr/courses/${courseId}/tutorials?page=${page}`);
        const html = await res.text();
        container.innerHTML = html;
        attachPaginationListeners();
        attachTutorialClickEvents();
    } catch (e) {
        container.innerHTML = `<div class="text-danger">Failed to load tutorials.</div>`;
    }
}

/* ============================================================
   LOAD STUDENTS (AJAX)
   ============================================================ */
async function loadStudents(courseId, page = 0) {
    const container = document.getElementById(`students-container-${courseId}`);
    if (!container) return;

    container.innerHTML = `<div class="text-center text-muted">Loading...</div>`;

    try {
        const res = await fetch(`/dr/courses/${courseId}/students-fragment?page=${page}`);
        const html = await res.text();
        container.innerHTML = html;
        attachPaginationListeners();
    } catch (e) {
        container.innerHTML = `<div class="text-danger">Failed to load students.</div>`;
    }
}

/* ============================================================
   PAGINATION LISTENER
   ============================================================ */
function attachPaginationListeners() {
    document.querySelectorAll(".page-btn").forEach(btn => {
        btn.addEventListener("click", e => {
            e.preventDefault();
            const page = btn.dataset.page;
            const courseId = btn.dataset.containerId;

            if (btn.closest("#tutorials-container-" + courseId)) {
                loadTutorials(courseId, page);
            } else if (btn.closest("#students-container-" + courseId)) {
                loadStudents(courseId, page);
            }
        });
    });
}

/* ============================================================
   VIEW TUTORIAL DETAILS
   ============================================================ */
function attachTutorialClickEvents() {
    document.querySelectorAll(".tutorial-item").forEach(item => {
        item.addEventListener("click", async () => {
            const tutorialId = item.dataset.tutorialId;

            const res = await fetch(`/dr/tutorial/${tutorialId}/view`);
            const t = await res.json();

            // Reset viewer
            document.getElementById("viewerVideo").classList.add("d-none");
            document.getElementById("viewerPdf").classList.add("d-none");
            document.getElementById("viewerArticle").classList.add("d-none");
            document.getElementById("viewerQuiz").classList.add("d-none");

            document.getElementById("viewerTitle").innerText = t.title;

            if (t.type === "VIDEO") {
                const video = document.getElementById("viewerVideo");
                video.src = t.filePath;
                video.classList.remove("d-none");
            }
            if (t.type === "PDF") {
                const pdf = document.getElementById("viewerPdf");
                pdf.src = t.filePath;
                pdf.classList.remove("d-none");
            }
            if (t.type === "ARTICLE") {
                const art = document.getElementById("viewerArticle");
                art.innerHTML = t.articleContent;
                art.classList.remove("d-none");
            }
            if (t.type === "QUIZ") {
                const quiz = document.getElementById("viewerQuiz");
                quiz.innerText = JSON.stringify(t.quizQuestions, null, 2);
                quiz.classList.remove("d-none");
            }

            new bootstrap.Modal(document.getElementById("modalViewTutorial")).show();
        });
    });
}

/* ============================================================
   CREATE COURSE
   ============================================================ */
document.getElementById("btnCreateCourse")?.addEventListener("click", () => {
    new bootstrap.Modal(document.getElementById("modalCreateCourse")).show();
});
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

console.log({ csrfHeader, csrfToken });

document.getElementById("formCreateCourse")?.addEventListener("submit", async e => {
    e.preventDefault();
    const form = new FormData(e.target);

    const res = await fetch(`/dr/courses/create`, {
        method: "POST",
        body: form,
        headers: {
            [csrfHeader]: csrfToken
        }
    });

    if (res.redirected) {
        showToast("Course created");
        window.location = res.url;
    } else {
        showToast("Failed to create", "danger");
    }
});


/* ============================================================
   DELETE COURSE
   ============================================================ */
document.querySelectorAll(".btn-delete-course").forEach(btn => {
    btn.addEventListener("click", async () => {
        if (!confirm("Delete this course?")) return;

        const id = btn.dataset.courseId;

        const res = await fetch(`/dr/courses/delete/${id}`, { method: "POST" });
        if (res.redirected) {
            showToast("Course deleted");
            window.location = res.url;
        } else {
            showToast("Delete failed", "danger");
        }
    });
});

/* ============================================================
   ADD TUTORIAL
   ============================================================ */
document.querySelectorAll(".btn-add-tutorial").forEach(btn => {
    btn.addEventListener("click", () => {
        document.getElementById("tutorialCourseId").value = btn.dataset.courseId;

        // Reset fields
        document.getElementById("fileUploadSection").classList.add("d-none");
        document.getElementById("articleSection").classList.add("d-none");
        document.getElementById("quizSection").classList.add("d-none");

        new bootstrap.Modal(document.getElementById("modalAddTutorial")).show();
    });
});

document.getElementById("tutorialTypeSelect")?.addEventListener("change", () => {
    const type = document.getElementById("tutorialTypeSelect").value;

    document.getElementById("fileUploadSection").classList.add("d-none");
    document.getElementById("articleSection").classList.add("d-none");
    document.getElementById("quizSection").classList.add("d-none");

    if (["VIDEO", "PDF"].includes(type)) {
        document.getElementById("fileUploadSection").classList.remove("d-none");
    }
    if (type === "ARTICLE") {
        document.getElementById("articleSection").classList.remove("d-none");
    }
    if (type === "QUIZ") {
        document.getElementById("quizSection").classList.remove("d-none");
    }
});

/* QUIZ BUILDER ------------------------------------------ */

function addQuizQuestion() {
    const container = document.getElementById("quizQuestions");
    const index = container.children.length;

    container.insertAdjacentHTML("beforeend", `
        <div class="border rounded p-2 mb-2">
            <input class="form-control mb-2" name="questions" placeholder="Question">

            <input class="form-control mb-2" name="options"
                   placeholder="Options separated by comma">

            <input class="form-control mb-2" name="answers"
                   placeholder="Correct answer">
        </div>
    `);
}

document.getElementById("formAddTutorial")?.addEventListener("submit", async e => {
    e.preventDefault();

    const form = new FormData(e.target);
    const courseId = document.getElementById("tutorialCourseId").value;

    try {
        const res = await fetch(`/dr/courses/${courseId}/tutorials/add`, {
            method: "POST",
            body: form,
            headers: {
                [csrfHeader]: csrfToken
            }
        });

        if (res.redirected) {
            showToast("Tutorial added successfully!");
            window.location = res.url;
        } else {
            showToast("Failed to add tutorial", "danger");
        }

    } catch (err) {
        console.error(err);
        showToast("Server error while adding tutorial", "danger");
    }
});

/* ============================================================
   VIEW STUDENT ENROLLMENTS MODAL
   ============================================================ */
document.querySelectorAll(".btn-view-enrollments").forEach(btn => {
    btn.addEventListener("click", async () => {
        const courseId = btn.dataset.courseId;

        const modalBody = document.getElementById("enrollmentsContainer");
        modalBody.innerHTML = "Loading...";

        const res = await fetch(`/dr/courses/${courseId}/students-fragment`);
        const html = await res.text();
        modalBody.innerHTML = html;

        attachPaginationListeners();

        new bootstrap.Modal(document.getElementById("modalViewEnrollments")).show();
    });
});

/* ============================================================
   AUTO-LOAD ALL FRAGMENTS
   ============================================================ */
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[id^='tutorials-container-']").forEach(c => {
        const courseId = c.id.replace("tutorials-container-", "");
        loadTutorials(courseId);
    });

    document.querySelectorAll("[id^='students-container-']").forEach(c => {
        const courseId = c.id.replace("students-container-", "");
        loadStudents(courseId);
    });
});

let courseIdToDelete = null;

document.querySelectorAll('.btn-delete-course').forEach(btn => {
    btn.addEventListener('click', function() {
        courseIdToDelete = this.getAttribute('data-course-id');
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteCourseModal'));
        deleteModal.show();
    });
});

document.getElementById('confirmDeleteBtn').addEventListener('click', function() {
    if(courseIdToDelete) {
        // Perform deletion via AJAX or form submission
        // Example: redirecting to a delete endpoint
        window.location.href = `/dr/courses/delete/${courseIdToDelete}`;
    }
});
