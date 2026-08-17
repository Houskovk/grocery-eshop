async function register() {

    const username = document.getElementById("registerUsername").value;

    const password = document.getElementById("registerPassword").value;

    const response = await fetch(`${API.user}/auth/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username,
            password
        })
    });

    if (response.ok) {
        document.getElementById("registerMessage")
            .textContent = "Registration successful! Redirecting to login...";

        setTimeout(() => {
            window.location.href = "/login.html";
        }, 1000);
    } else {
        document.getElementById("registerMessage")
            .textContent = "Registration failed";
    }
}
