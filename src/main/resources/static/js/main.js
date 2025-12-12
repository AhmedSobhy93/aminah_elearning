document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('input[name="keyword"]');
    if(searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            if(e.key === "Enter") {
                this.form.submit();
            }
        });
    }
});
document.addEventListener("DOMContentLoaded", () => {

    const helpToggle = document.getElementById("helpToggle");
    const helpPanel  = document.getElementById("floatingHelpPanel");

    helpToggle.addEventListener("click", (e) => {
        e.stopPropagation();
        helpPanel.style.display = (helpPanel.style.display === "block") ? "none" : "block";
    });

    // Close when clicking outside
    document.addEventListener("click", (e) => {
        if (!helpPanel.contains(e.target) && e.target !== helpToggle) {
            helpPanel.style.display = "none";
        }
    });
});

