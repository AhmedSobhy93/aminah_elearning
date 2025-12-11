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
