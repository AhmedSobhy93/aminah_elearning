document.addEventListener("DOMContentLoaded", () => {
    console.log("Aminah E-Learning site loaded.");
});
document.addEventListener("DOMContentLoaded", function(){

    // Initialize Sortable
    document.querySelectorAll('.sortable').forEach(function(el){
        new Sortable(el, {
            animation: 150,
            onEnd: function(evt){
                let tutorialIds = Array.from(el.querySelectorAll('tr')).map(r => r.dataset.id);
                let courseId = el.dataset.courseId;
                fetch('/dr/tutorials/reorder', {
                    method: 'POST',
                    headers: {'Content-Type':'application/json'},
                    body: JSON.stringify(tutorialIds)
                }).then(res=>res.text()).then(data=>{
                    if(data==='ok') location.reload();
                    else alert('Error reordering tutorials');
                });
            }
        });
    });
});

// Functions
async function deleteCourse(id){
    if(!confirm('Delete this course?')) return;
    const res = await fetch(`/dr/courses/delete/${id}`, {method:'POST'});
    if(res.ok) location.reload(); else alert('Failed to delete course');
}

async function deleteTutorial(id){
    if(!confirm('Delete this tutorial?')) return;
    const res = await fetch(`/dr/tutorials/delete/${id}`, {method:'POST'});
    if(res.ok) location.reload(); else alert('Failed to delete tutorial');
}

function openEnrollments(courseId){
    window.location.href = `/dr/courses/${courseId}/enrollments`;
}

function editTutorial(id){
    const title = prompt("New Title:");
    if(title){
        const type = prompt("New Type (VIDEO, PDF, ARTICLE, QUIZ):");
        const form = new FormData();
        form.append('title', title);
        form.append('type', type);
        fetch(`/dr/tutorials/edit/${id}`, {method:'POST', body: form})
            .then(res=>res.text())
            .then(()=> location.reload())
            .catch(()=> alert("Error updating tutorial"));
    }
}

const typeSelect = document.getElementById('tutorialType');
const fileDiv = document.getElementById('fileInputDiv');
const articleDiv = document.getElementById('articleInputDiv');
const quizDiv = document.getElementById('quizInputDiv');

typeSelect.addEventListener('change', function() {
    const type = typeSelect.value;
    fileDiv.classList.add('d-none');
    articleDiv.classList.add('d-none');
    quizDiv.classList.add('d-none');

    if (type === 'VIDEO' || type === 'PDF') fileDiv.classList.remove('d-none');
    else if (type === 'ARTICLE') articleDiv.classList.remove('d-none');
    else if (type === 'QUIZ') quizDiv.classList.remove('d-none');
});

document.addEventListener('DOMContentLoaded', () => {
    const typeSelect = document.querySelector('select[name="type"]');
    const quizInputs = document.querySelectorAll('#quizInputDiv input');

    function toggleRequired(showQuiz) {
        quizInputs.forEach(input => {
            if (showQuiz) {
                input.setAttribute('required', true);
            } else {
                input.removeAttribute('required');
            }
        });
    }

    typeSelect.addEventListener('change', () => {
        const value = typeSelect.value;
        const quizDiv = document.getElementById('quizInputDiv');
        const articleDiv = document.getElementById('articleInputDiv');
        const fileDiv = document.getElementById('fileInputDiv');

        quizDiv.classList.toggle('d-none', value !== 'QUIZ');
        articleDiv.classList.toggle('d-none', value !== 'ARTICLE');
        fileDiv.classList.toggle('d-none', value !== 'VIDEO' && value !== 'PDF');

        toggleRequired(value === 'QUIZ');
    });

    toggleRequired(typeSelect.value === 'QUIZ'); // initialize
});


    document.addEventListener("DOMContentLoaded", () => {
    const typeSelect = document.getElementById("tutorialType");
    const fileDiv = document.getElementById("fileInputDiv");
    const articleDiv = document.getElementById("articleInputDiv");
    const quizDiv = document.getElementById("quizInputDiv");
    const quizContainer = document.getElementById("quizQuestionsContainer");
    const addQuestionBtn = document.getElementById("addQuestionBtn");

    typeSelect.addEventListener("change", () => {
    const type = typeSelect.value;
    fileDiv.classList.toggle("d-none", !(type === "VIDEO" || type === "PDF"));
    articleDiv.classList.toggle("d-none", type !== "ARTICLE");
    quizDiv.classList.toggle("d-none", type !== "QUIZ");
});

        addQuestionBtn.addEventListener("click", () => {
            const index = quizContainer.children.length + 1;
            const questionHTML = `
        <div class="card mt-2 p-3 border border-primary-subtle rounded">
            <div class="mb-2"><strong>Question ${index}</strong></div>
            <input type="text" name="questions[]" class="form-control mb-2" placeholder="Question" required>
            <input type="text" name="options[]" class="form-control mb-2" placeholder="Options (comma-separated)" required>
            <input type="text" name="answers[]" class="form-control" placeholder="Correct Answer" required>
        </div>`;
            quizContainer.insertAdjacentHTML("beforeend", questionHTML);
        });

    });
document.querySelector("form").addEventListener("submit", (e) => {
    if (typeSelect.value === "QUIZ" && quizContainer.children.length === 0) {
        e.preventDefault();
        alert("Please add at least one quiz question.");
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const typeSelect = document.querySelector('select[name="type"]');
    const fileInput = document.querySelector('input[name="file"]');
    const mp4Warning = document.getElementById("mp4Warning");

    typeSelect.addEventListener("change", () => {
        const value = typeSelect.value;

        if (value === "VIDEO") {
            fileInput.setAttribute("accept", "video/mp4");
            mp4Warning.classList.remove("d-none");
        } else {
            mp4Warning.classList.add("d-none");
        }
    });
});


