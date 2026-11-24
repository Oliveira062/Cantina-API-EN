const moneyFormat = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
const timeFormat = new Intl.DateTimeFormat("en-US", { hour: "2-digit", minute: "2-digit" });
const dateTimeFormat = new Intl.DateTimeFormat("en-US", {
    day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit"
});

export const STATUSES = {
    CREATED: "Created",
    AWAITING_PAYMENT: "Awaiting payment",
    PAID: "Paid",
    PREPARING: "Preparing",
    READY: "Ready",
    DELIVERED: "Delivered",
    CANCELED: "Canceled"
};

export const ROLES = {
    CUSTOMER: "Customer",
    CASHIER: "Cashier",
    KITCHEN: "Kitchen",
    ADMIN: "Administrator"
};

export function el(tag, attributes, ...children) {
    const element = document.createElement(tag);
    for (const [name, value] of Object.entries(attributes || {})) {
        if (value === undefined || value === null || value === false) {
            continue;
        }
        if (name.startsWith("on") && typeof value === "function") {
            element.addEventListener(name.substring(2), value);
        } else if (name === "cssClass") {
            element.className = value;
        } else if (value === true) {
            element.setAttribute(name, "");
        } else {
            element.setAttribute(name, value);
        }
    }
    for (const child of children.flat()) {
        if (child === undefined || child === null || child === false) {
            continue;
        }
        element.append(child instanceof Node ? child : document.createTextNode(String(child)));
    }
    return element;
}

export function money(value) {
    return moneyFormat.format(Number(value || 0));
}

export function time(instant) {
    return instant ? timeFormat.format(new Date(instant)) : "";
}

export function dateTime(instant) {
    return instant ? dateTimeFormat.format(new Date(instant)) : "";
}

export function badge(status) {
    return el("span", { cssClass: "badge badge-" + status }, STATUSES[status] || status);
}

export function notify(message, type) {
    const notice = el("div", { cssClass: "toast" + (type ? " toast-" + type : ""), role: "status" }, message);
    document.getElementById("toasts").append(notice);
    setTimeout(() => notice.remove(), type === "error" ? 6000 : 3500);
}

export function notifyError(error) {
    notify(error.message || "Something went wrong.", "error");
}

export function emptyState(text) {
    return el("div", { cssClass: "empty" }, text);
}

export function field(name, label, options) {
    const config = options || {};
    const id = "field-" + name;
    let input;
    if (config.options) {
        input = el("select", { id, name: name },
            config.options.map(([value, text]) => el("option", { value: value, selected: value === config.value }, text)));
    } else {
        input = el("input", {
            id,
            name: name,
            type: config.type || "text",
            value: config.value,
            required: config.required,
            min: config.min,
            max: config.max,
            step: config.step,
            autocomplete: config.autocomplete,
            placeholder: config.placeholderText
        });
    }
    if (config.type === "checkbox") {
        input.checked = Boolean(config.value);
        return el("div", { cssClass: "field field-checkbox" }, input, el("label", { for: id }, label));
    }
    return el("div", { cssClass: "field" },
        el("label", { for: id }, label),
        input,
        el("span", { cssClass: "field-error", "data-error": name }));
}

export function readForm(form) {
    const data = {};
    for (const input of form.querySelectorAll("input, select")) {
        if (!input.name) {
            continue;
        }
        if (input.type === "checkbox") {
            data[input.name] = input.checked;
        } else if (input.type === "number") {
            data[input.name] = input.value === "" ? null : Number(input.value);
        } else {
            data[input.name] = input.value.trim();
        }
    }
    return data;
}

export function showFieldErrors(form, error) {
    for (const marker of form.querySelectorAll("[data-error]")) {
        marker.textContent = "";
    }
    const fields = error.fields || {};
    let anyField = false;
    for (const [name, message] of Object.entries(fields)) {
        const marker = form.querySelector(`[data-error="${name}"]`);
        if (marker) {
            marker.textContent = message;
            anyField = true;
        }
    }
    if (!anyField || Object.keys(fields).length === 0) {
        notifyError(error);
    }
}

export function openForm(title, fields, buttonText, onSubmit, extra) {
    const dialog = document.getElementById("dialog");
    const submitButton = el("button", { cssClass: "button button-primary", type: "submit" }, buttonText);
    const form = el("form", { method: "dialog" },
        el("h3", {}, title),
        fields,
        extra,
        el("div", { cssClass: "dialog-actions" },
            el("button", { cssClass: "button", type: "button", onclick: () => dialog.close() }, "Back"),
            submitButton));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        submitButton.disabled = true;
        try {
            await onSubmit(readForm(form), form);
            dialog.close();
        } catch (error) {
            showFieldErrors(form, error);
        } finally {
            submitButton.disabled = false;
        }
    });

    dialog.replaceChildren(form);
    dialog.showModal();
    const first = form.querySelector("input, select");
    if (first) {
        first.focus();
    }
    return form;
}
