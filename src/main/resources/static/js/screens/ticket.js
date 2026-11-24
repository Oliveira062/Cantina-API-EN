import { money, el, badge } from "../ui.js";

export function ticket(order, options) {
    const config = options || {};
    const canRemove = config.onRemove && order.status === "CREATED";

    const items = order.items.map((item) => el("li", {},
        el("span", { cssClass: "qty" }, item.quantity + "x"),
        el("span", { cssClass: "name" }, item.product),
        el("span", { cssClass: "amount" }, money(item.subtotal)),
        canRemove
            ? el("button", {
                cssClass: "remove",
                type: "button",
                "aria-label": "Remove " + item.product + " from the order",
                onclick: () => config.onRemove(item)
            }, "×")
            : null));

    return el("article", { cssClass: "ticket", "aria-label": "Order number " + order.number },
        el("div", { cssClass: "ticket-top" },
            el("span", { cssClass: "ticket-number" }, el("small", {}, "#"), order.number),
            badge(order.status)),
        config.showCustomer ? el("p", { cssClass: "ticket-customer" }, order.customer) : null,
        items.length ? el("ul", { cssClass: "ticket-items" }, items) : el("p", { cssClass: "muted" }, "No items yet."),
        el("div", { cssClass: "ticket-total" },
            el("span", {}, "Total"),
            el("span", { cssClass: "amount" }, money(order.total))),
        config.actions && config.actions.length ? el("div", { cssClass: "ticket-actions" }, config.actions) : null);
}
