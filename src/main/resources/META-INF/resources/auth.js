function getToken() {
    return localStorage.getItem("token");
}

const currencyFormatter = new Intl.NumberFormat("en-IE", {
    style: "currency",
    currency: "EUR"
});

function formatPrice(priceInCents) {
    return currencyFormatter.format(priceInCents / 100);
}

async function refreshAuthUI() {
    const authArea = document.getElementById("auth-area");

    if (!authArea) {
        return;
    }

    const token = getToken();

    if (!token) {
        renderLoggedOut(authArea);
        return;
    }

    try {
        const response = await fetch("/users/me", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            localStorage.removeItem("token");
            renderLoggedOut(authArea);
            return;
        }

        const user = await response.json();

        let balanceInCents = 0;

        const balanceResponse = await fetch("/users/me/balance", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (balanceResponse.ok) {
            const balance = await balanceResponse.json();
            balanceInCents = balance.balanceInCents;
        }

        renderLoggedIn(authArea, user.username, balanceInCents);

    } catch (error) {
        console.error(error);
        renderLoggedOut(authArea);
    }
}

function renderLoggedOut(container) {
    container.innerHTML = `
        <a class="auth-button" href="/register.html">Register</a>
        <a class="auth-button" href="/login.html">Login</a>
    `;
}

function renderLoggedIn(container, username, balanceInCents) {
    container.innerHTML = `
        <a class="auth-balance" href="/balance.html">${formatPrice(balanceInCents)}</a>
        <a class="auth-username" href="/profile.html">${username}</a>
        <a class="auth-button" href="/balance.html">Wallet</a>
        <button type="button" class="auth-button" onclick="logout()">Logout</button>
    `;
}

function logout() {
    localStorage.removeItem("token");
    window.location.href = "/index.html";
}

document.addEventListener("DOMContentLoaded", refreshAuthUI);



