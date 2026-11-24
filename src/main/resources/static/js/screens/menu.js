import { post, get, del } from "../api.js";
import { openForm, notify, notifyError, field, money, el, emptyState } from "../ui.js";
import { ticket } from "./ticket.js";

export async function menuScreen(area, user, customer) {
    const isCounter = Boolean(customer);
    const state = { products: [], order: null };
    const listArea = el("div");
    const ticketArea = el("div", { cssClass: "sticky-column" });

    const columns = el("div", { cssClass: "two-columns" }, listArea, ticketArea);
    if (isCounter) {
        area.replaceChildren(columns);
    } else {
        area.replaceChildren(
            el("div", { cssClass: "screen-header" },
                el("div", {},
                    el("h2", {}, "Menu"),
                    el("p", { cssClass: "muted" }, "Pick the quantity and add it. You pay at the end and follow the preparation in My orders."))),
            columns);
    }

    async function load() {
        try {
            const [products, created, awaiting] = await Promise.all([
                get("/api/products"),
                get("/api/orders?status=CREATED"),
                get("/api/orders?status=AWAITING_PAYMENT")
            ]);
            state.products = products;
            const openOrders = created.concat(awaiting).filter((p) => !isCounter || p.customerId === customer.id);
            state.order = openOrders.length ? openOrders[0] : null;
            renderList();
            renderTicket();
        } catch (error) {
            notifyError(error);
        }
    }

    function renderList() {
        if (!state.products.length) {
            listArea.replaceChildren(emptyState("The menu is empty right now."));
            return;
        }
        listArea.replaceChildren(el("div", { cssClass: "menu-list" }, state.products.map(productRow)));
    }

    function productRow(product) {
        const quantity = el("input", {
            type: "number", min: 1, max: Math.max(1, Math.min(50, product.stockQuantity)), value: 1,
            "aria-label": "Quantity of " + product.name
        });
        const canBuy = product.available && (!state.order || state.order.status === "CREATED");
        return el("div", { cssClass: "menu-item" + (product.available ? "" : " sold-out") },
            el("div", {},
                el("h3", {}, product.name),
                el("p", { cssClass: "muted" },
                    product.description,
                    product.available ? ` · ${product.stockQuantity} available` : " · Sold out")),
            el("span", { cssClass: "price amount" }, money(product.price)),
            product.available
                ? el("div", { cssClass: "quantity" },
                    quantity,
                    el("button", {
                        cssClass: "button button-small",
                        type: "button",
                        disabled: !canBuy,
                        onclick: () => add(product, Number(quantity.value))
                    }, "Add"))
                : el("span", { cssClass: "muted" }, "Sold out"));
    }

    async function add(product, quantity) {
        try {
            if (!state.order) {
                state.order = await post("/api/orders", isCounter ? { customerId: customer.id } : undefined);
            }
            state.order = await post(`/api/orders/${state.order.number}/items`,
                { productCode: product.code, quantity });
            notify(`${quantity}x ${product.name} added to the order.`);
            renderTicket();
        } catch (error) {
            notifyError(error);
        }
    }

    async function removeFromOrder(item) {
        try {
            state.order = await del(`/api/orders/${state.order.number}/items/${item.productCode}`);
            renderTicket();
        } catch (error) {
            notifyError(error);
        }
    }

    async function pay(type, amountReceived) {
        const response = await post(`/api/orders/${state.order.number}/payment`, { type, amountReceived });
        notify(response.message, "success");
        const paidOrder = response.order;
        state.order = null;
        await load();
        ticketArea.replaceChildren(
            ticket(paidOrder, { showCustomer: isCounter }),
            el("p", { cssClass: "muted note" },
                isCounter
                    ? "Payment registered. The order is already in the kitchen queue."
                    : "Payment approved. You will get a notice when it is ready."));
    }

    function receiveCash() {
        const total = Number(state.order.total);
        const change = el("p", { cssClass: "change" }, "Change: " + money(0));
        const form = openForm("Receive cash",
            [field("amountReceived", `Amount received (total ${money(total)})`, { type: "number", step: "0.01", min: total, required: true })],
            "Confirm payment",
            (data) => pay("CASH", data.amountReceived),
            change);
        form.querySelector("input").addEventListener("input", (event) => {
            const received = Number(event.target.value || 0);
            change.textContent = received >= total ? "Change: " + money(received - total) : "Missing " + money(total - received);
        });
    }

    async function cancel() {
        if (!confirm(`Cancel order #${state.order.number}?`)) {
            return;
        }
        try {
            await post(`/api/orders/${state.order.number}/cancel`);
            notify("Order canceled.");
            state.order = null;
            load();
        } catch (error) {
            notifyError(error);
        }
    }

    function renderTicket() {
        renderList();
        ticketArea.classList.toggle("has-order", Boolean(state.order));
        if (!state.order) {
            ticketArea.replaceChildren(el("article", { cssClass: "ticket" },
                el("div", { cssClass: "ticket-empty" },
                    el("h3", {}, "Your order shows up here"),
                    el("p", { cssClass: "muted" }, "Add an item from the menu to start."))));
            return;
        }
        const hasItems = state.order.items.length > 0;
        const payWithHandling = (type) => pay(type).catch(notifyError);
        const actions = [
            el("div", { cssClass: "row" },
                el("button", { cssClass: "button button-primary", type: "button", disabled: !hasItems, onclick: () => payWithHandling("PIX") }, "Pay with PIX"),
                el("button", { cssClass: "button button-primary", type: "button", disabled: !hasItems, onclick: () => payWithHandling("CARD") }, "Pay by card")),
            isCounter || user.role === "ADMIN"
                ? el("button", { cssClass: "button", type: "button", disabled: !hasItems, onclick: receiveCash }, "Receive cash")
                : null,
            el("button", { cssClass: "button button-danger", type: "button", onclick: cancel }, "Cancel order")
        ].filter(Boolean);
        ticketArea.replaceChildren(ticket(state.order, { onRemove: removeFromOrder, actions, showCustomer: isCounter }));
    }

    await load();
}

export function counterScreen(area, user) {
    const result = el("div", { cssClass: "search-results" });
    const search = el("input", { type: "search", placeholder: "Name, email or student ID", "aria-label": "Search customer" });
    const orderArea = el("div");

    async function find(event) {
        event.preventDefault();
        try {
            const customers = await get("/api/customers?search=" + encodeURIComponent(search.value.trim()));
            result.replaceChildren(...(customers.length
                ? customers.map((customer) => el("button", { type: "button", onclick: () => choose(customer) },
                    el("strong", {}, customer.name),
                    el("span", { cssClass: "muted" }, "  " + customer.email + (customer.studentId ? " · student ID " + customer.studentId : ""))))
                : [emptyState("No customer found. Ask the person to create an account on their phone.")]));
        } catch (error) {
            notifyError(error);
        }
    }

    function choose(customer) {
        result.replaceChildren();
        form.hidden = true;
        orderArea.replaceChildren(
            el("div", { cssClass: "chosen-customer" },
                el("span", {}, "Order for ", el("strong", {}, customer.name)),
                el("button", { cssClass: "button button-small", type: "button", onclick: change }, "Change customer")),
            el("div"));
        menuScreen(orderArea.lastChild, user, customer);
    }

    function change() {
        orderArea.replaceChildren();
        form.hidden = false;
        search.value = "";
        search.focus();
    }

    const form = el("form", { cssClass: "customer-search", onsubmit: find },
        search,
        el("button", { cssClass: "button", type: "submit" }, "Search"));

    area.replaceChildren(
        el("div", { cssClass: "screen-header" },
            el("div", {},
                el("h2", {}, "Counter"),
                el("p", { cssClass: "muted" }, "Find the customer, build the order and register the payment."))),
        form,
        result,
        orderArea);
    search.focus();
}
