async function loadProfile() {

    const token = localStorage.getItem("token");

    const response = await fetch("/users/me", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        document.getElementById("profile")
            .textContent = "Please log in";

        return;
    }

    const user = await response.json();

    document.getElementById("profile").innerHTML = `
        <p>Username: ${user.username}</p>
        <p>Role: ${user.role}</p>
    `;
}