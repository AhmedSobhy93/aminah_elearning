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
