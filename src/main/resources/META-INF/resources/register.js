async function register() {

    const username = document.getElementById("registerUsername").value;

    const password = document.getElementById("registerPassword").value;

    const response = await fetch("/auth/register", {
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
            .textContent = "Registration successful";
    } else {
        document.getElementById("registerMessage")
            .textContent = "Registration failed";
    }
}