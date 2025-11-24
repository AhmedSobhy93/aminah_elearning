document.addEventListener("DOMContentLoaded", () => {
    console.log("Manage Courses JS Loaded");

    // -----------------------------
    // HANDLE TUTORIAL TYPE SWITCHING
    // -----------------------------
    document.querySelectorAll("select[name='type']").forEach(select => {
        select.addEventListener("change", (e) => {
            const row = e.target.closest("form");

            const fileDiv = row.querySelector("#fileInputDiv");
            const articleDiv = row.querySelector("#articleInputDiv");
            const quizDiv = row.querySelector("#quizInputDiv");

            const type = e.target.value;

            // Reset first
            fileDiv.classList.add("d-none");
            articleDiv.classList.add("d-none");
            quizDiv.classList.add("d-none");

            if (type === "VIDEO" || type === "PDF") {
                fileDiv.classList.remove("d-none");
            } else if (type === "ARTICLE") {
                articleDiv.classList.remove("d-none");
            } else if (type === "QUIZ") {
                quizDiv.classList.remove("d-none");
            }
        });
    });

    // ---------------------------------
    // DYNAMIC QUIZ QUESTIONS PER COURSE
    // ---------------------------------
    document.querySelectorAll("#addQuestionBtn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const quizContainer = e.target.closest("form").querySelector("#quizQuestionsContainer");

            const questionIndex = quizContainer.children.length;

            const block = document.createElement("div");
            block.className = "border rounded p-2 mb-2";
            block.innerHTML = `
                <label class="fw-bold">Question ${questionIndex + 1}</label>
                <input type="text" name="questions[${questionIndex}].question"
                       class="form-control mb-2" placeholder="Question" required>

                <label>Options (comma separated)</label>
                <input type="text" name="questions[${questionIndex}].options"
                       class="form-control mb-2" placeholder="Option1, Option2, Option3" required>

                <label>Correct Answer</label>
                <input type="text" name="questions[${questionIndex}].answer"
                       class="form-control mb-2" placeholder="Correct Answer" required>

                <button type="button" class="btn btn-sm btn-danger removeQuestionBtn">
                    Remove
                </button>
            `;

            quizContainer.appendChild(block);

            // Remove button event
            block.querySelector(".removeQuestionBtn").addEventListener("click", () => {
                block.remove();
            });
        });
    });

    // -----------------------------
    // FIX BOOTSTRAP MODAL RELOAD BUG
    // -----------------------------
    document.querySelectorAll(".modal").forEach(modal => {
        modal.addEventListener("hidden.bs.modal", () => {
            const iframes = modal.querySelectorAll("iframe, video");
            iframes.forEach(el => {
                el.src = el.src; // reload content
            });
        });
    });

});
