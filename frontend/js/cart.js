async function loadCart() {

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/login.html";
        return;
    }

    const response = await fetch(`${API.order}/cart`, {
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

    const response = await fetch(`${API.order}/cart/items/${productId}`, {
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

    const response = await fetch(`${API.order}/cart/items/${productId}`, {
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

    const response = await fetch(`${API.order}/cart`, {
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

async function checkout() {

    const token = localStorage.getItem("token");

    const response = await fetch(`${API.order}/checkout`, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        const error = await response.text();
        alert("Checkout failed: " + error);
        return;
    }

    const result = await response.json();

    alert(
        `Purchase successful!\n` +
        `Total: ${formatPrice(result.totalInCents)}`
    );

    await loadCart();
    await loadOrders();
    await refreshAuthUI();
}

async function loadOrders() {

    const token = localStorage.getItem("token");

    const container = document.getElementById("orders");

    const response = await fetch(`${API.order}/orders`, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!response.ok) {
        console.error("Could not load orders");
        container.innerHTML = "<p>Could not load order history.</p>";
        return;
    }

    const orders = await response.json();

    renderOrders(orders, container);
}

function renderOrders(orders, container) {

    container.innerHTML = "";

    if (orders.length === 0) {
        container.innerHTML = "<p>You have no past orders.</p>";
        return;
    }

    for (const order of orders) {
        const element = document.createElement("div");
        element.className = "order-card";

        const itemsHtml = order.items
            .map(item => `
                <li>
                    ${item.productName}
                    &times; ${item.quantity}
                    &mdash; ${formatPrice(item.subtotalInCents)}
                </li>
            `)
            .join("");

        element.innerHTML = `
            <h3>Order ${order.id}</h3>
            <p>${new Date(order.createdAt).toLocaleString()}</p>

            <ul>
                ${itemsHtml}
            </ul>

            <strong>
                Total: ${formatPrice(order.totalInCents)}
            </strong>
        `;

        container.appendChild(element);
    }
}

document.getElementById("checkout-button").addEventListener("click", checkout);

loadCart();
loadOrders();

