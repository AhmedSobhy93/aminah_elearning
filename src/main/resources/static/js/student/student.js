document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    // Search courses
    document.getElementById("courseSearch").addEventListener("input", e => {
        const query = e.target.value.toLowerCase();
        document.querySelectorAll("#coursesContainer .card").forEach(card => {
            const title = card.querySelector(".card-title").textContent.toLowerCase();
            card.style.display = title.includes(query) ? "block" : "none";
        });
    });

    // Load sections dynamically
    document.querySelectorAll(".btn-view-sections").forEach(btn => {
        btn.addEventListener("click", async () => {
            const courseId = btn.dataset.courseId;
            const container = btn.closest(".card").querySelector(".section-container");
            const html = await fetch(`/student/sections/${courseId}`).then(res => res.text());
            container.innerHTML = html;

            // Add tutorial buttons
            container.querySelectorAll(".btn-view-tutorials").forEach(tutBtn => {
                tutBtn.addEventListener("click", async () => {
                    const sectionId = tutBtn.dataset.sectionId;
                    const tutContainer = tutBtn.closest(".card-body").querySelector(".tutorial-container");
                    const html = await fetch(`/student/tutorials/${sectionId}`).then(res => res.text());
                    tutContainer.innerHTML = html;

                    // Tutorial modal buttons
                    tutContainer.querySelectorAll(".btn-view-tutorial").forEach(vBtn => {
                        vBtn.addEventListener("click", async () => {
                            const tutorialId = vBtn.dataset.tutorialId;
                            const modalHtml = await fetch(`/student/tutorial-modal/${tutorialId}`).then(res => res.text());
                            document.getElementById("modalTutorialContainer").innerHTML = modalHtml;
                            new bootstrap.Modal(document.getElementById("modalViewTutorial")).show();
                        });
                    });
                });
            });
        });
    });

    // Enroll in course
    document.querySelectorAll(".btn-enroll").forEach(btn => {
        btn.addEventListener("click", async () => {
            const courseId = btn.dataset.courseId;
            const res = await fetch(`/student/enroll/${courseId}`, {
                method: "POST",
                headers: {"X-CSRF-TOKEN": csrfToken}
            });
            if (res.ok) location.reload();
        });
    });
});
