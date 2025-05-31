// --- RATING LOGIC ---
const stars = document.querySelectorAll('#stars i');
let currentRating = 0;
const noteID = window.noteID || new URLSearchParams(window.location.search).get('noteID');

stars.forEach(star => {
    star.addEventListener('click', () => {
        currentRating = star.getAttribute('data-value');
        updateStars(currentRating);

        // Kirim rating ke backend
        fetch('/interface/rate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                moduleID: noteID,
                rating: currentRating
            })

        })
            .then(response => {
                if (!response.ok) throw new Error('Network error');
                return response.json();
            })
            .then(data => {
                console.log('Rating submitted:', data);
                loadAverageRating(noteID);
            })
            .catch(err => {
                console.error('Rating failed:', err);
                alert('Gagal memberi rating. Coba lagi.');
            });
    });

    star.addEventListener('mouseover', () => updateStars(star.getAttribute('data-value')));
    star.addEventListener('mouseout', () => updateStars(currentRating));
});

function updateStars(rating) {
    stars.forEach(star => {
        const value = star.getAttribute('data-value');
        star.classList.toggle('text-yellow-400', value <= rating);
        star.classList.toggle('text-gray-300', value > rating);
    });
}

async function loadAverageRating(noteID) {
    try {
        const res = await fetch(`/interface/rate/average?moduleID=${noteID}`);
        const data = await res.json();
        if (res.ok && data.rate !== undefined) {
            document.querySelector('.star > h2').textContent = `Overall Rating: ${data.rate.toFixed(2)}`;
            updateStars(Math.round(data.rate));
        } else {
            document.querySelector('.star > h2').textContent = `Overall Rating: N/A`;
        }
    } catch (e) {
        document.querySelector('.star > h2').textContent = `Overall Rating: Error`;
    }
}


document.addEventListener("DOMContentLoaded", () => {
    const noteID = window.noteID || new URLSearchParams(window.location.search).get("noteID");
    loadAverageRating(noteID);
});


// --- BOOKMARK LOGIC ---
async function toggleBookmark(noteID) {
    try {
        const button = document.getElementById("bookmark-button");
        const isBookmarked = button.classList.contains("bookmarked");

        const response = await fetch(`/interface/bookmark?moduleID=${noteID}`, {
            method: isBookmarked ? "DELETE" : "POST", // DELETE jika sudah di-bookmark
            credentials: 'include' // pastikan session dikirim
        });

        const result = await response.json();

        if (!response.ok) {
            alert("Gagal menyimpan bookmark. Silakan login atau coba lagi.");
            return;
        }

        // Toggle visual
        if (isBookmarked) {
            button.classList.remove("bookmarked");
            button.innerHTML = '<i class="far fa-bookmark"></i> Save';
        } else {
            button.classList.add("bookmarked");
            button.innerHTML = '<i class="fas fa-bookmark"></i> Saved';
        }

    } catch (error) {
        console.error("Error saat toggle bookmark:", error);
        alert("Terjadi kesalahan. Silakan coba lagi.");
    }
}

async function checkBookmarkStatus(noteID) {
    try {
        const response = await fetch(`/interface/bookmark/status?moduleID=${noteID}`, {
            method: "GET",
            credentials: 'include'
        });

        const result = await response.json();
        if (result.status === "success" && result.bookmarked === true) {
            const button = document.getElementById("bookmark-button");
            button.classList.add("bookmarked");
            button.innerHTML = '<i class="fas fa-bookmark"></i> Saved';
        }
    } catch (error) {
        console.warn("Tidak dapat memeriksa status bookmark:", error);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const noteID = window.noteID || new URLSearchParams(window.location.search).get("noteID");
    checkBookmarkStatus(noteID);
});
