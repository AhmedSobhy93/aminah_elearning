document.addEventListener("DOMContentLoaded", function () {

    loadAllTutorialsAndStudents();

    document.getElementById("formCreateCourse").addEventListener("submit", async function (e) {
        e.preventDefault();
        let formData = new FormData(this);
        await fetch(this.action, { method: "POST", body: formData });
        location.reload();
    });

    document.getElementById("tutorialType").addEventListener("change", handleTutorialTypeChange);
});

function loadAllTutorialsAndStudents() {
    document.querySelectorAll("[id^='tutorials-container-']").forEach(box => {
        const courseId = box.id.split("-")[2];
        loadTutorials(courseId, 0);
        loadStudents(courseId, 0);
    });
}

async function loadTutorials(courseId, page) {
    const url = `/dr/courses/${courseId}/tutorials?page=${page}`;
    const fragment = await (await fetch(url)).text();
    document.getElementById("tutorials-container-" + courseId).innerHTML = fragment;
}

async function loadStudents(courseId, page) {
    const url = `/dr/courses/${courseId}/students-fragment?page=${page}`;
    const fragment = await (await fetch(url)).text();
    document.getElementById("students-container-" + courseId).innerHTML = fragment;
}

function deleteCourse(id) {
    if (!confirm("Are you sure?")) return;
    fetch(`/dr/courses/delete/${id}`, { method: "POST" }).then(() => location.reload());
}

function openTutorialModal(courseId) {
    document.querySelector("#formAddTutorial input[name='courseId']").value = courseId;
    new bootstrap.Modal(document.getElementById("modalAddTutorial")).show();
}

function handleTutorialTypeChange() {
    let type = this.value;
    document.getElementById("fileUploadSection").classList.add("d-none");
    document.getElementById("articleSection").classList.add("d-none");
    document.getElementById("quizSection").classList.add("d-none");

    if (type === "VIDEO" || type === "PDF") document.getElementById("fileUploadSection").classList.remove("d-none");
    if (type === "ARTICLE") document.getElementById("articleSection").classList.remove("d-none");
    if (type === "QUIZ") document.getElementById("quizSection").classList.remove("d-none");
}

function addQuizQuestion() {
    const container = document.getElementById("quizQuestions");
    container.insertAdjacentHTML("beforeend", `
        <div class="card p-3 mb-2">
            <input class="form-control mb-2" name="questions" placeholder="Question" required>
            <input class="form-control mb-2" name="options" placeholder="Options (comma separated)" required>
            <input class="form-control" name="answers" placeholder="Correct Answer" required>
        </div>
    `);
}

async function loadTutorials(courseId, page = 0) {
    const url = `/dr/courses/${courseId}/tutorials?page=${page}&size=2`; // 2 per page
    const fragment = await (await fetch(url)).text();
    document.getElementById("tutorials-container-" + courseId).innerHTML = fragment;
}

async function loadStudents(courseId, page = 0) {
    const url = `/dr/courses/${courseId}/students-fragment?page=${page}&size=5`; // 5 per page
    const fragment = await (await fetch(url)).text();
    document.getElementById("students-container-" + courseId).innerHTML = fragment;
}
function viewTutorial(tutorial) {
    const modalTitle = document.getElementById("modalViewTitle");
    const modalBody = document.getElementById("modalViewBody");

    modalTitle.textContent = tutorial.title;

    modalBody.innerHTML = ''; // reset
    if(tutorial.type === 'VIDEO' || tutorial.type === 'PDF'){
        modalBody.innerHTML = `<a href="${tutorial.url}" target="_blank">Open ${tutorial.type}</a>`;
    } else if(tutorial.type === 'ARTICLE'){
        modalBody.innerHTML = `<div>${tutorial.content}</div>`;
    } else if(tutorial.type === 'QUIZ'){
        modalBody.innerHTML = `<ul>${tutorial.questions.map(q => `<li>${q}</li>`).join('')}</ul>`;
    }

    new bootstrap.Modal(document.getElementById("modalViewContent")).show();
}
function editCourse(courseId) {
    // fetch course data
    fetch(`/dr/courses/${courseId}/edit`).then(res => res.json()).then(course => {
        const form = document.getElementById("formCreateCourse");
        form.title.value = course.title;
        form.courseName.value = course.courseName;
        form.description.value = course.description;
        form.price.value = course.price;
        form.published.checked = course.published;
        form.videoUrl.value = course.videoUrl;
        form.action = `/dr/courses/${courseId}/update`;
        new bootstrap.Modal(document.getElementById("modalCreateCourse")).show();
    });
}

function editTutorial(tutorialId) {
    fetch(`/dr/tutorials/${tutorialId}/edit`).then(res => res.json()).then(tutorial => {
        const form = document.getElementById("formAddTutorial");
        form.title.value = tutorial.title;
        form.type.value = tutorial.type;
        handleTutorialTypeChange.call(form.type);
        // fill file/article/quiz if needed
        form.action = `/dr/tutorials/${tutorialId}/update`;
        new bootstrap.Modal(document.getElementById("modalAddTutorial")).show();
    });
}
