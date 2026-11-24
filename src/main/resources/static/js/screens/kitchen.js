import { post, get } from "../api.js";
import { notify, notifyError, el, time, emptyState } from "../ui.js";
import { ticket } from "./ticket.js";

const REFRESH_INTERVAL = 10000;

export function kitchenScreen(area) {
    const toPrepareColumn = el("div", { cssClass: "rail-tickets" });
    const preparingColumn = el("div", { cssClass: "rail-tickets" });
    const toPrepareCount = el("span", { cssClass: "count" }, "0");
    const preparingCount = el("span", { cssClass: "count" }, "0");
    const updatedLabel = el("p", { cssClass: "muted" });

    area.replaceChildren(
        el("div", { cssClass: "screen-header" },
            el("div", {},
                el("h2", {}, "Kitchen"),
                updatedLabel)),
        el("div", { cssClass: "rail" },
            el("section", { cssClass: "rail-column" },
                el("h3", {}, "To prepare", toPrepareCount), toPrepareColumn),
            el("section", { cssClass: "rail-column" },
                el("h3", {}, "In preparation", preparingCount), preparingColumn)));

    async function load() {
        try {
            const queue = await get("/api/kitchen/queue");
            const paid = queue.filter((p) => p.status === "PAID");
            const preparing = queue.filter((p) => p.status === "PREPARING");
            toPrepareCount.textContent = paid.length;
            preparingCount.textContent = preparing.length;
            toPrepareColumn.replaceChildren(...(paid.length
                ? paid.map((p) => kitchenTicket(p, "Start preparing", "preparation"))
                : [emptyState("No orders waiting.")]));
            preparingColumn.replaceChildren(...(preparing.length
                ? preparing.map((p) => kitchenTicket(p, "Mark as ready", "ready"))
                : [emptyState("Nothing cooking right now.")]));
            updatedLabel.textContent = "Refreshes every 10 seconds. Last update at " + time(new Date()) + ".";
        } catch (error) {
            notifyError(error);
        }
    }

    function kitchenTicket(order, text, action) {
        return ticket(order, {
            showCustomer: true,
            actions: [
                el("p", { cssClass: "muted" }, "Paid at " + time(order.updatedAt)),
                el("button", {
                    cssClass: "button button-primary",
                    type: "button",
                    onclick: async (event) => {
                        event.target.disabled = true;
                        try {
                            await post(`/api/orders/${order.number}/${action}`);
                            notify(action === "ready" ? `Order #${order.number} ready.` : `Started preparing #${order.number}.`, "success");
                            load();
                        } catch (error) {
                            event.target.disabled = false;
                            notifyError(error);
                        }
                    }
                }, text)
            ]
        });
    }

    load();
    const timer = setInterval(load, REFRESH_INTERVAL);
    return () => clearInterval(timer);
}
