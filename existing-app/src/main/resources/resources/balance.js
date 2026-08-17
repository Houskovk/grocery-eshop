async function loadBalance() {

    const token = localStorage.getItem("token");

    const balanceElement = document.getElementById("balance-amount");

    if (!token) {
        window.location.href = "/login.html";
        return;
    }

    const response = await fetch("/users/me/balance", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        localStorage.removeItem("token");
        window.location.href = "/login.html";
        return;
    }

    const data = await response.json();

    balanceElement.textContent = formatPrice(data.balanceInCents);
}

async function addFunds() {

    const token = localStorage.getItem("token");

    const amountInput = document.getElementById("addAmount");

    const messageElement = document.getElementById("balanceMessage");

    const amountInEuros = parseFloat(amountInput.value);

    if (isNaN(amountInEuros) || amountInEuros <= 0) {
        messageElement.textContent = "Please enter a valid amount.";
        messageElement.className = "error-message";
        return;
    }

    const amountInCents = Math.round(amountInEuros * 100);

    const response = await fetch("/users/me/balance/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            amountInCents
        })
    });

    if (!response.ok) {
        messageElement.textContent = "Unable to add funds.";
        messageElement.className = "error-message";
        return;
    }

    const data = await response.json();

    document.getElementById("balance-amount").textContent = formatPrice(data.balanceInCents);

    messageElement.textContent = "Funds added successfully!";
    messageElement.className = "success-message";

    amountInput.value = "";

    await refreshAuthUI();
}

document.addEventListener("DOMContentLoaded", loadBalance);

