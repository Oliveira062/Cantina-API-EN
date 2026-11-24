import { patch, get } from "../api.js";
import { notifyError, dateTime, el, emptyState } from "../ui.js";

const INTERVAL = 20000;

export function notificationBell() {
    const counterBadge = el("span", { cssClass: "bell-count", hidden: true });
    const button = el("button", { cssClass: "button button-small bell", type: "button", "aria-label": "Notifications" }, "Notices", counterBadge);
    let list = [];
    let panel = null;

    async function refresh() {
        try {
            list = await get("/api/notifications");
            const unread = list.filter((n) => !n.read).length;
            counterBadge.textContent = unread > 9 ? "9+" : String(unread);
            counterBadge.hidden = unread === 0;
            if (panel) {
                renderPanel();
            }
        } catch (error) {
            if (error.status !== 401) {
                notifyError(error);
            }
        }
    }

    function renderPanel() {
        const items = list.map((n) => el("li", {
            cssClass: "notification" + (n.read ? "" : " unread"),
            tabindex: "0",
            onclick: () => markRead(n)
        }, el("p", {}, n.message), el("p", { cssClass: "muted" }, dateTime(n.createdAt))));
        panel.replaceChildren(
            el("header", {},
                el("h3", {}, "Notices"),
                el("button", { cssClass: "button button-small", type: "button", onclick: close }, "Close")),
            items.length ? el("ul", {}, items) : el("div", { cssClass: "inner-space" }, emptyState("No notices yet.")));
    }

    async function markRead(notification) {
        if (notification.read) {
            return;
        }
        try {
            await patch(`/api/notifications/${notification.id}/read`);
            refresh();
        } catch (error) {
            notifyError(error);
        }
    }

    function open() {
        panel = el("aside", { cssClass: "notifications-panel", "aria-label": "Notices" });
        document.body.append(panel);
        renderPanel();
        document.addEventListener("keydown", closeOnEscape);
    }

    function close() {
        if (panel) {
            panel.remove();
            panel = null;
        }
        document.removeEventListener("keydown", closeOnEscape);
        button.focus();
    }

    function closeOnEscape(event) {
        if (event.key === "Escape") {
            close();
        }
    }

    button.addEventListener("click", () => (panel ? close() : open()));
    refresh();
    const timer = setInterval(refresh, INTERVAL);

    return { button, stop: () => { clearInterval(timer); close(); } };
}
