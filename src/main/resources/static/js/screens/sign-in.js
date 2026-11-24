import { post, saveToken } from "../api.js";
import { field, el, readForm, showFieldErrors } from "../ui.js";

export function signInScreen(root, canteen, onSignIn, notice) {
    const side = el("div", { cssClass: "sign-in-form" });
    const hoursText = canteen
        ? `Open from ${canteen.opensAt.substring(0, 5)} to ${canteen.closesAt.substring(0, 5)}. Order on your phone and pick it up at the counter when the kitchen calls.`
        : "Order on your phone and pick it up at the counter when the kitchen calls.";

    root.replaceChildren(el("main", { cssClass: "sign-in" },
        el("section", { cssClass: "sign-in-wall" },
            el("p", { cssClass: "brand" }, canteen ? canteen.name : "University Canteen"),
            el("h1", {}, "Your snack without the queue at break time"),
            el("p", { cssClass: "hours" }, hoursText)),
        side));

    showLogin(side, onSignIn, notice);
}

function showLogin(side, onSignIn, notice) {
    const form = el("form", { novalidate: true },
        el("h2", {}, "Sign in"),
        el("p", { cssClass: "muted" }, notice || "Use the email and password of your canteen account."),
        field("email", "Email", { type: "email", autocomplete: "username", required: true }),
        field("password", "Password", { type: "password", autocomplete: "current-password", required: true }),
        el("button", { cssClass: "button button-primary button-wide", type: "submit" }, "Sign in"),
        el("p", { cssClass: "switch-link muted" }, "No account yet? ",
            el("button", { cssClass: "link", type: "button", onclick: () => showRegister(side, onSignIn) }, "Create account")));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const button = form.querySelector("[type=submit]");
        button.disabled = true;
        try {
            const response = await post("/api/auth/login", readForm(form));
            saveToken(response.token);
            onSignIn(response.user);
        } catch (error) {
            showFieldErrors(form, error);
        } finally {
            button.disabled = false;
        }
    });

    side.replaceChildren(form);
    form.querySelector("input").focus();
}

function showRegister(side, onSignIn) {
    const form = el("form", { novalidate: true },
        el("h2", {}, "Create account"),
        el("p", { cssClass: "muted" }, "The password needs at least 8 characters, with letters and numbers."),
        field("name", "Name", { autocomplete: "name", required: true }),
        field("email", "Email", { type: "email", autocomplete: "username", required: true }),
        field("studentId", "Student ID (optional)", { placeholderText: "Digits only" }),
        field("password", "Password", { type: "password", autocomplete: "new-password", required: true }),
        el("button", { cssClass: "button button-primary button-wide", type: "submit" }, "Create account"),
        el("p", { cssClass: "switch-link muted" }, "Already have an account? ",
            el("button", { cssClass: "link", type: "button", onclick: () => showLogin(side, onSignIn) }, "Sign in")));

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const button = form.querySelector("[type=submit]");
        button.disabled = true;
        const data = readForm(form);
        try {
            await post("/api/auth/register", data);
            const response = await post("/api/auth/login", { email: data.email, password: data.password });
            saveToken(response.token);
            onSignIn(response.user);
        } catch (error) {
            showFieldErrors(form, error);
        } finally {
            button.disabled = false;
        }
    });

    side.replaceChildren(form);
    form.querySelector("input").focus();
}
