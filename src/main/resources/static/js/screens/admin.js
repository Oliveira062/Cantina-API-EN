import { patch, post, get, del, put } from "../api.js";
import { openForm, notify, notifyError, field, money, el, readForm, showFieldErrors, ROLES, STATUSES, emptyState } from "../ui.js";

const LOW_STOCK = 5;

export async function dashboardScreen(area) {
    area.replaceChildren(el("p", { cssClass: "muted" }, "Loading..."));
    try {
        const [orders, products] = await Promise.all([get("/api/orders"), get("/api/products")]);
        const todayText = new Date().toDateString();
        const today = orders.filter((p) => new Date(p.createdAt).toDateString() === todayText);
        const revenue = today
            .filter((p) => p.payment && p.status !== "CANCELED")
            .reduce((sum, p) => sum + Number(p.total), 0);
        const runningLow = products.filter((p) => p.active && p.stockQuantity <= LOW_STOCK);

        const counters = Object.entries(STATUSES).map(([status, name]) => el("div", { cssClass: "counter" },
            el("strong", {}, orders.filter((p) => p.status === status).length),
            el("span", {}, name)));

        area.replaceChildren(
            el("div", { cssClass: "screen-header" },
                el("div", {},
                    el("h2", {}, "Dashboard"),
                    el("p", { cssClass: "muted" }, "Overview of orders and stock."))),
            el("div", { cssClass: "counters" },
                el("div", { cssClass: "counter highlight" },
                    el("strong", { cssClass: "amount" }, money(revenue)),
                    el("span", {}, `Received today in ${today.length} order(s)`)),
                counters),
            el("section", { cssClass: "block" },
                el("h3", {}, "Low stock"),
                runningLow.length
                    ? el("table", { cssClass: "table" },
                        el("tbody", {}, runningLow.map((p) => el("tr", {},
                            el("td", {}, p.name),
                            el("td", { cssClass: "amount" }, p.stockQuantity === 0 ? "Sold out" : p.stockQuantity + " left")))))
                    : emptyState("All products have more than " + LOW_STOCK + " units.")));
    } catch (error) {
        notifyError(error);
    }
}

export async function productsScreen(area) {
    const table = el("div");

    area.replaceChildren(
        el("div", { cssClass: "screen-header" },
            el("div", {},
                el("h2", {}, "Products"),
                el("p", { cssClass: "muted" }, "Deactivated products leave the menu but stay in past orders.")),
            el("button", { cssClass: "button button-primary", type: "button", onclick: newProduct }, "Add product")),
        table);

    async function load() {
        try {
            render(await get("/api/products"));
        } catch (error) {
            notifyError(error);
        }
    }

    function render(products) {
        table.replaceChildren(el("table", { cssClass: "table" },
            el("thead", {}, el("tr", {},
                el("th", {}, "Product"),
                el("th", {}, "Price"),
                el("th", {}, "Stock"),
                el("th", { cssClass: "hide-mobile" }, "Status"),
                el("th", {}))),
            el("tbody", {}, products.map((p) => el("tr", {},
                el("td", {}, el("strong", {}, p.name), el("div", { cssClass: "muted" }, p.description)),
                el("td", { cssClass: "amount" }, money(p.price)),
                el("td", { cssClass: "amount" }, p.stockQuantity),
                el("td", { cssClass: "hide-mobile" }, p.active ? "On the menu" : "Deactivated"),
                el("td", {}, el("div", { cssClass: "actions" },
                    el("button", { cssClass: "button button-small", type: "button", onclick: () => changePrice(p) }, "Price"),
                    el("button", { cssClass: "button button-small", type: "button", onclick: () => restock(p) }, "Restock"),
                    p.active
                        ? el("button", { cssClass: "button button-small button-danger", type: "button", onclick: () => activate(p, false) }, "Deactivate")
                        : el("button", { cssClass: "button button-small", type: "button", onclick: () => activate(p, true) }, "Reactivate"))))))));
    }

    function newProduct() {
        openForm("Add product", [
            field("name", "Name", { required: true }),
            field("description", "Description"),
            field("price", "Price ($)", { type: "number", step: "0.01", min: "0.01", required: true }),
            field("initialQuantity", "Initial quantity", { type: "number", min: 0, value: 0, required: true })
        ], "Add", async (data) => {
            await post("/api/products", data);
            notify("Product added.", "success");
            load();
        });
    }

    function changePrice(product) {
        openForm("Price of " + product.name, [
            field("price", "New price ($)", { type: "number", step: "0.01", min: "0.01", value: product.price, required: true })
        ], "Save price", async (data) => {
            await patch(`/api/products/${product.code}/price`, data);
            notify("Price saved.", "success");
            load();
        });
    }

    function restock(product) {
        openForm("Restock " + product.name, [
            field("quantity", `How many units arrived? (current: ${product.stockQuantity})`, { type: "number", min: 1, required: true })
        ], "Restock", async (data) => {
            await post(`/api/products/${product.code}/stock`, data);
            notify("Stock updated.", "success");
            load();
        });
    }

    async function activate(product, active) {
        try {
            if (active) {
                await post(`/api/products/${product.code}/reactivate`);
            } else {
                await del(`/api/products/${product.code}`);
            }
            notify(active ? "Product back on the menu." : "Product deactivated.");
            load();
        } catch (error) {
            notifyError(error);
        }
    }

    load();
}

export async function teamScreen(area, user) {
    const table = el("div");

    area.replaceChildren(
        el("div", { cssClass: "screen-header" },
            el("div", {},
                el("h2", {}, "Team and accounts"),
                el("p", { cssClass: "muted" }, "Customers create their own account. Staff are added here.")),
            el("button", { cssClass: "button button-primary", type: "button", onclick: newStaff }, "Add staff member")),
        table);

    async function load() {
        try {
            render(await get("/api/users"));
        } catch (error) {
            notifyError(error);
        }
    }

    function render(users) {
        table.replaceChildren(el("table", { cssClass: "table" },
            el("thead", {}, el("tr", {},
                el("th", {}, "Name"),
                el("th", { cssClass: "hide-mobile" }, "Email"),
                el("th", {}, "Role"),
                el("th", {}))),
            el("tbody", {}, users.map((u) => el("tr", {},
                el("td", {}, el("strong", {}, u.name), u.active ? null : el("div", { cssClass: "muted" }, "Account deactivated")),
                el("td", { cssClass: "hide-mobile" }, u.email),
                el("td", {}, ROLES[u.role]),
                el("td", {}, u.id === user.id ? el("span", { cssClass: "muted" }, "You") : el("div", { cssClass: "actions" },
                    el("button", {
                        cssClass: "button button-small" + (u.active ? " button-danger" : ""),
                        type: "button",
                        onclick: () => toggleStatus(u)
                    }, u.active ? "Deactivate" : "Reactivate"))))))));
    }

    function newStaff() {
        openForm("Add staff member", [
            field("name", "Name", { required: true }),
            field("email", "Email", { type: "email", required: true }),
            field("role", "Role", { options: [["CASHIER", "Cashier"], ["KITCHEN", "Kitchen"], ["ADMIN", "Administrator"]] }),
            field("password", "Initial password", { type: "password", autocomplete: "new-password", required: true })
        ], "Add", async (data) => {
            await post("/api/users", data);
            notify("Staff member added.", "success");
            load();
        });
    }

    async function toggleStatus(target) {
        if (target.active && !confirm(`Deactivate ${target.name}'s account? They lose access immediately.`)) {
            return;
        }
        try {
            await patch(`/api/users/${target.id}/status`, { active: !target.active });
            notify(target.active ? "Account deactivated." : "Account reactivated.");
            load();
        } catch (error) {
            notifyError(error);
        }
    }

    load();
}

export async function canteenScreen(area, onSave) {
    let config;
    try {
        config = await get("/api/canteen");
    } catch (error) {
        notifyError(error);
        return;
    }
    const form = el("form", { cssClass: "wide-form", novalidate: true },
        field("name", "Canteen name", { value: config.name, required: true }),
        field("opensAt", "Opens at", { type: "time", value: config.opensAt.substring(0, 5), required: true }),
        field("closesAt", "Closes at", { type: "time", value: config.closesAt.substring(0, 5), required: true }),
        field("serviceFee", "Service fee (%)", { type: "number", step: "0.5", min: 0, max: 30, value: Number(config.serviceFee) * 100 }),
        field("enforceHours", "Refuse orders outside opening hours", { type: "checkbox", value: config.enforceHours }),
        el("button", { cssClass: "button button-primary", type: "submit" }, "Save changes"));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = readForm(form);
        data.serviceFee = Number(data.serviceFee || 0) / 100;
        try {
            const saved = await put("/api/canteen", data);
            notify("Changes saved.", "success");
            onSave(saved);
        } catch (error) {
            showFieldErrors(form, error);
        }
    });

    area.replaceChildren(
        el("div", { cssClass: "screen-header" },
            el("div", {},
                el("h2", {}, "Canteen"),
                el("p", { cssClass: "muted" }, "Hours, service fee and operating rules."))),
        form);
}
