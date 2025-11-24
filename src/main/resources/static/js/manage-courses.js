document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".tutorial-form").forEach(form => {
        const typeSelect = form.querySelector(".tutorial-type");
        const fileGroup = form.querySelector(".file-group");
        const articleGroup = form.querySelector(".article-group");
        const quizGroup = form.querySelector(".quiz-group");
        const quizContainer = form.querySelector(".quiz-container");
        const addQuizBtn = form.querySelector(".add-quiz-question");

        const updateInputs = () => {
            const type = typeSelect.value;
            fileGroup.style.display = (type === "VIDEO" || type === "PDF") ? "block" : "none";
            articleGroup.style.display = (type === "ARTICLE") ? "block" : "none";
            quizGroup.style.display = (type === "QUIZ") ? "block" : "none";
        };

        typeSelect.addEventListener("change", updateInputs);
        updateInputs();

        addQuizBtn.addEventListener("click", e => {
            e.preventDefault();
            const idx = quizContainer.children.length;
            const questionDiv = document.createElement("div");
            questionDiv.classList.add("quiz-question");
            questionDiv.innerHTML = `
                <input type="text" name="questions" class="form-control mb-1" placeholder="Question ${idx+1}" required>
                <input type="text" name="options" class="form-control mb-1" placeholder="Options (comma-separated)" required>
                <input type="text" name="answers" class="form-control mb-1" placeholder="Answer" required>
            `;
            quizContainer.appendChild(questionDiv);
        });

        form.addEventListener("submit", e => {
            const type = typeSelect.value;
            if ((type === "VIDEO" || type === "PDF") && !fileGroup.querySelector("input").files.length) {
                e.preventDefault(); alert("Please select a file.");
            }
            if (type === "ARTICLE" && !articleGroup.querySelector("textarea").value.trim()) {
                e.preventDefault(); alert("Please enter article content.");
            }
            if (type === "QUIZ" && quizContainer.children.length === 0) {
                e.preventDefault(); alert("Please add at least one quiz question.");
            }
        });
    });
});

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".tutorial-form").forEach(form => {
        const typeSelect = form.querySelector(".tutorial-type");
        const fileGroup = form.querySelector(".file-group");
        const articleGroup = form.querySelector(".article-group");
        const quizGroup = form.querySelector(".quiz-group");
        const quizContainer = form.querySelector(".quiz-container");
        const addQuizBtn = form.querySelector(".add-quiz-question");

        const updateInputs = () => {
            const type = typeSelect.value;
            fileGroup.style.display = (type === "VIDEO" || type === "PDF") ? "block" : "none";
            articleGroup.style.display = (type === "ARTICLE") ? "block" : "none";
            quizGroup.style.display = (type === "QUIZ") ? "block" : "none";
        };

        typeSelect.addEventListener("change", updateInputs);
        updateInputs();

        addQuizBtn.addEventListener("click", e => {
            e.preventDefault();
            const idx = quizContainer.children.length;
            const questionDiv = document.createElement("div");
            questionDiv.classList.add("quiz-question");
            questionDiv.innerHTML = `
                <input type="text" name="questions" class="form-control mb-1" placeholder="Question ${idx+1}" required>
                <input type="text" name="options" class="form-control mb-1" placeholder="Options (comma-separated)" required>
                <input type="text" name="answers" class="form-control mb-1" placeholder="Answer" required>
            `;
            quizContainer.appendChild(questionDiv);
        });
    });
});
