async function login() {

    const username = document.getElementById("loginUsername").value;

    const password = document.getElementById("loginPassword").value;

    const response = await fetch("/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username,
            password
        })
    });

    if (!response.ok) {
        document.getElementById("loginMessage")
            .textContent = "Invalid username or password";

        return;
    }

    const data = await response.json();

    localStorage.setItem("token", data.token);

    document.getElementById("loginMessage")
        .textContent = "Logged in! Redirecting...";

    setTimeout(() => {
        window.location.href = "/index.html";
    }, 500);
}

