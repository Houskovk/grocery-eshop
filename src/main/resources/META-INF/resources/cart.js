async function loadCart() {

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/login.html";
        return;
    }

    const response = await fetch("/cart", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        alert("Could not load cart.");
        return;
    }

    const cart = await response.json();

    renderCart(cart);
}

function renderCart(cart) {

    const container = document.getElementById("cart-items");

    container.innerHTML = "";

    if (cart.items.length === 0) {

        container.innerHTML = "<p>Your cart is empty.</p>";

    } else {

        cart.items.forEach(item => {

            container.innerHTML += `
                <div class="cart-item">
                    <strong>${item.name}</strong>

                    <span>${formatPrice(item.price)}</span>

                    <input
                        type="number"
                        min="1"
                        value="${item.quantity}"
                        onchange="updateQuantity('${item.productId}', this.value)"
                    >

                    <span>${formatPrice(item.lineTotal)}</span>

                    <button onclick="removeItem('${item.productId}')">
                        Remove
                    </button>
                </div>
            `;
        });
    }

    document.getElementById("cart-total").textContent = formatPrice(cart.total);

    updateCartCount(cart);
}

async function updateQuantity(productId, quantity) {

    const token = localStorage.getItem("token");

    const response = await fetch(`/cart/items/${productId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            quantity: Number(quantity)
        })
    });

    if (!response.ok) {
        alert("Could not update quantity.");
        await loadCart();
        return;
    }

    const cart = await response.json();

    renderCart(cart);
}

async function removeItem(productId) {

    const token = localStorage.getItem("token");

    const response = await fetch(`/cart/items/${productId}`, {
        method: "DELETE",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        alert("Could not remove item.");
        return;
    }

    const cart = await response.json();

    renderCart(cart);
}

async function clearCart() {

    const token = localStorage.getItem("token");

    const response = await fetch("/cart", {
        method: "DELETE",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        alert("Could not clear cart.");
        return;
    }

    const cart = await response.json();

    renderCart(cart);
}

loadCart();

