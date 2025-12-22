function animateProgress(bar, newValue) {
    let width = parseInt(bar.style.width);
    const interval = setInterval(() => {
        if (width >= newValue) clearInterval(interval);
        else {
            width++;
            bar.style.width = width + '%';
            bar.innerText = width + '%';
        }
    }, 10);
}
