import { post, get } from "../api.js";
import { openForm, notify, notifyError, field, dateTime, money, el, time, badge, STATUSES, emptyState } from "../ui.js";

const COUNTER_STAFF = ["CASHIER", "ADMIN"];
const KITCHEN_STAFF = ["KITCHEN", "ADMIN"];

export function ordersScreen(area, user) {
    const isCustomer = user.role === "CUSTOMER";
    let filter = "";
    let expanded = null;
    const filters = el("div", { cssClass: "filters", role: "group", "aria-label": "Filter by status" });
    const list = el("div");

    area.replaceChildren(
        el("div", { cssClass: "screen-header" },
            el("div", {},
                el("h2", {}, isCustomer ? "My orders" : "Orders"),
                el("p", { cssClass: "muted" }, isCustomer
                    ? "Tap an order to see its items and every status change."
                    : "Tap an order to see its details and the actions available to your role.")),
            el("button", { cssClass: "button", type: "button", onclick: () => load() }, "Refresh")),
        filters,
        el("div", { cssClass: "spacer" }),
        list);

    function renderFilters() {
        const options = [["", "All"]].concat(Object.entries(STATUSES));
        filters.replaceChildren(...options.map(([value, text]) => el("button", {
            cssClass: "filter",
            type: "button",
            "aria-pressed": String(filter === value),
            onclick: () => {
                filter = value;
                renderFilters();
                load();
            }
        }, text)));
    }

    async function load() {
        try {
            const orders = await get("/api/orders" + (filter ? "?status=" + filter : ""));
            render(orders);
        } catch (error) {
            notifyError(error);
        }
    }

    function render(orders) {
        if (!orders.length) {
            list.replaceChildren(emptyState(isCustomer
                ? "You have not placed any orders yet. Pick something from the menu."
                : "No orders with this status."));
            return;
        }
        const body = el("tbody");
        for (const order of orders) {
            const row = el("tr", { cssClass: "order-row", tabindex: "0", "aria-expanded": String(expanded === order.number) },
                el("td", {}, el("strong", {}, "#" + order.number)),
                isCustomer ? null : el("td", {}, order.customer),
                el("td", {}, badge(order.status)),
                el("td", { cssClass: "amount" }, money(order.total)),
                el("td", { cssClass: "muted hide-mobile" }, dateTime(order.createdAt)));
            const toggleRow = () => {
                expanded = expanded === order.number ? null : order.number;
                render(orders);
            };
            row.addEventListener("click", toggleRow);
            row.addEventListener("keydown", (event) => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    toggleRow();
                }
            });
            body.append(row);
            if (expanded === order.number) {
                const cell = el("td", { colspan: isCustomer ? 4 : 5 }, el("p", { cssClass: "muted" }, "Loading..."));
                body.append(el("tr", { cssClass: "order-detail" }, cell));
                renderDetail(order, cell);
            }
        }
        list.replaceChildren(el("table", { cssClass: "table" },
            el("thead", {}, el("tr", {},
                el("th", {}, "Order"),
                isCustomer ? null : el("th", {}, "Customer"),
                el("th", {}, "Status"),
                el("th", {}, "Total"),
                el("th", { cssClass: "hide-mobile" }, "Opened at"))),
            body));
    }

    async function renderDetail(order, cell) {
        let history = [];
        try {
            history = await get(`/api/orders/${order.number}/history`);
        } catch (error) {
            notifyError(error);
        }
        cell.replaceChildren(el("div", { cssClass: "detail-grid" },
            el("div", {},
                el("h3", {}, "Items"),
                el("ul", { cssClass: "ticket-items" }, order.items.map((item) => el("li", {},
                    el("span", { cssClass: "qty" }, item.quantity + "x"),
                    el("span", { cssClass: "name" }, item.product),
                    el("span", { cssClass: "amount" }, money(item.subtotal))))),
                order.payment
                    ? el("p", { cssClass: "muted" }, `Paid with ${paymentName(order.payment)}` +
                        (Number(order.change) > 0 ? `, change of ${money(order.change)}` : ""))
                    : null,
                el("div", { cssClass: "table-actions" }, orderActions(order))),
            el("div", {},
                el("h3", {}, "History"),
                el("ol", { cssClass: "timeline" }, history.map((entry) => el("li", {},
                    el("strong", {}, STATUSES[entry.to]),
                    el("p", { cssClass: "muted" }, `${time(entry.recordedAt)} · ${entry.actor}`)))))));
    }

    function orderActions(order) {
        const role = user.role;
        const actions = [];
        const awaiting = order.status === "CREATED" || order.status === "AWAITING_PAYMENT";

        if (awaiting && COUNTER_STAFF.includes(role) && order.items.length) {
            actions.push(button("Register payment", "button-primary", () => registerPayment(order)));
        }
        if (order.status === "PAID" && KITCHEN_STAFF.includes(role)) {
            actions.push(button("Start preparing", "button-primary", () => advance(order, "preparation", "Preparation started.")));
        }
        if (order.status === "PREPARING" && KITCHEN_STAFF.includes(role)) {
            actions.push(button("Mark as ready", "button-primary", () => advance(order, "ready", "Order ready.")));
        }
        if (order.status === "READY" && COUNTER_STAFF.includes(role)) {
            actions.push(button("Confirm delivery", "button-primary", () => advance(order, "delivery", "Delivery confirmed.")));
        }
        const finished = order.status === "DELIVERED" || order.status === "CANCELED";
        const canCancel = role === "CUSTOMER" ? awaiting : COUNTER_STAFF.includes(role) && !finished;
        if (canCancel) {
            actions.push(button("Cancel order", "button-danger", () => cancel(order)));
        }
        return actions;
    }

    function button(text, cssClass, action) {
        return el("button", { cssClass: "button button-small " + cssClass, type: "button", onclick: action }, text);
    }

    async function advance(order, action, message) {
        try {
            await post(`/api/orders/${order.number}/${action}`);
            notify(message, "success");
            load();
        } catch (error) {
            notifyError(error);
        }
    }

    async function cancel(order) {
        if (!confirm(`Cancel order #${order.number}?`)) {
            return;
        }
        try {
            const response = await post(`/api/orders/${order.number}/cancel`);
            notify(response.stockReturned ? "Order canceled and stock returned." : "Order canceled.");
            load();
        } catch (error) {
            notifyError(error);
        }
    }

    function registerPayment(order) {
        openForm(`Payment for order #${order.number}`,
            [
                field("type", "Payment method", { options: [["PIX", "PIX"], ["CARD", "Card"], ["CASH", "Cash"]] }),
                field("amountReceived", `Amount received, cash only (total ${money(order.total)})`,
                    { type: "number", step: "0.01", min: 0 })
            ],
            "Confirm payment",
            async (data) => {
                const response = await post(`/api/orders/${order.number}/payment`, data);
                notify(response.message, "success");
                load();
            });
    }

    renderFilters();
    load();
}

function paymentName(type) {
    return { PIX: "PIX", CARD: "card", CASH: "cash" }[type] || type;
}
