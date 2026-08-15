async function loadProfile() {

    const token = localStorage.getItem("token");

    const profileElement = document.getElementById("profile");

    if (!token) {
        window.location.href = "/login.html";
        return;
    }

    const response = await fetch("/users/me", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        localStorage.removeItem("token");
        window.location.href = "/login.html";
        return;
    }

    const user = await response.json();

    profileElement.innerHTML = `
        <p>Username: ${user.username}</p>
        <p>Role: ${user.role}</p>
    `;
}

document.addEventListener("DOMContentLoaded", loadProfile);

