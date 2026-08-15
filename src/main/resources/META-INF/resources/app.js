async function loadProducts() {
    const statusElement = document.getElementById("product-status");

    const productListElement = document.getElementById("product-list");

    try {
        const response = await fetch("/products");

        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }

        const products = await response.json();

        statusElement.textContent = "";

        renderProducts(products, productListElement);

    } catch (error) {
        console.error(error);

        statusElement.textContent = "Unable to load products.";

        statusElement.className = "error-message";
    }
}

function renderProducts(products, container) {
    container.replaceChildren();

    if (products.length === 0) {
        const message = document.createElement("p");
        message.textContent = "No products are currently available.";
        container.appendChild(message);
        return;
    }

    for (const product of products) {
        const card = document.createElement("article");
        card.className = "product-card";

        const name = document.createElement("h3");
        name.textContent = product.name;

        const price = document.createElement("p");
        price.className = "product-price";
        price.textContent = formatPrice(product.priceInCents);

        const button = document.createElement("button");
        button.type = "button";
        button.className = "add-to-cart-button";
        button.textContent = "Add to Cart";
        button.onclick = () => addToCart(product.id);

        card.append(name, price, button);

        container.appendChild(card);
    }
}

async function addToCart(productId) {
    const token = localStorage.getItem("token");

    if (!token) {
        alert("Please log in first.");
        return;
    }

    const response = await fetch("/cart/items", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            productId: productId,
            quantity: 1
        })
    });

    if (!response.ok) {
        alert("Could not add item to cart.");
        return;
    }

    const cart = await response.json();

    console.log("Cart:", cart);

    updateCartCount(cart);
}


loadProducts();