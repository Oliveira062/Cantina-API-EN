import { clearToken, get, whenSessionLost, savedToken } from "./api.js";
import { el, ROLES } from "./ui.js";
import { signInScreen } from "./screens/sign-in.js";
import { counterScreen, menuScreen } from "./screens/menu.js";
import { ordersScreen } from "./screens/orders.js";
import { kitchenScreen } from "./screens/kitchen.js";
import { canteenScreen, teamScreen, dashboardScreen, productsScreen } from "./screens/admin.js";
import { notificationBell } from "./screens/notifications.js";

const root = document.getElementById("app");

const TABS = {
    menu: { title: "Menu", open: (area, user) => menuScreen(area, user) },
    "my-orders": { title: "My orders", open: ordersScreen },
    counter: { title: "Counter", open: counterScreen },
    orders: { title: "Orders", open: ordersScreen },
    kitchen: { title: "Kitchen", open: kitchenScreen },
    dashboard: { title: "Dashboard", open: dashboardScreen },
    products: { title: "Products", open: productsScreen },
    team: { title: "Team", open: teamScreen },
    canteen: { title: "Canteen", open: (area) => canteenScreen(area, updateCanteen) }
};

const TABS_BY_ROLE = {
    CUSTOMER: ["menu", "my-orders"],
    CASHIER: ["counter", "orders"],
    KITCHEN: ["kitchen", "orders"],
    ADMIN: ["dashboard", "orders", "kitchen", "counter", "products", "team", "canteen"]
};

let canteen = null;
let cleanupScreen = null;
let bell = null;
let canteenBadge = null;

async function start() {
    whenSessionLost(() => signOut("Your session ended. Sign in again to continue."));
    try {
        canteen = await get("/api/canteen");
    } catch (error) {
        canteen = null;
    }
    if (!savedToken()) {
        showSignIn();
        return;
    }
    try {
        buildApp(await get("/api/auth/me"));
    } catch (error) {
        showSignIn();
    }
}

function showSignIn(notice) {
    signInScreen(root, canteen, buildApp, notice);
}

function signOut(notice) {
    clearToken();
    if (cleanupScreen) {
        cleanupScreen();
        cleanupScreen = null;
    }
    if (bell) {
        bell.stop();
        bell = null;
    }
    history.replaceState(null, "", "/");
    showSignIn(notice);
}

function updateCanteen(next) {
    canteen = next;
    if (canteenBadge) {
        canteenBadge.replaceWith(createCanteenBadge());
    }
}

function createCanteenBadge() {
    canteenBadge = canteen
        ? el("span", { cssClass: "canteen-status " + (canteen.openNow ? "open" : "closed") },
            canteen.openNow ? "Open now" : "Closed now")
        : el("span");
    return canteenBadge;
}

function buildApp(user) {
    const tabs = TABS_BY_ROLE[user.role] || [];
    const area = el("main", { cssClass: "content", id: "content", tabindex: "-1" });
    const tabButtons = tabs.map((key) => el("button", {
        cssClass: "tab",
        type: "button",
        role: "tab",
        "data-tab": key,
        onclick: () => openTab(key)
    }, TABS[key].title));

    bell = notificationBell();

    root.replaceChildren(
        el("header", { cssClass: "topbar" },
            el("div", { cssClass: "topbar-row" },
                el("span", { cssClass: "topbar-name" }, canteen ? canteen.name : "Canteen"),
                createCanteenBadge(),
                el("div", { cssClass: "topbar-user" },
                    el("span", { cssClass: "user-name" }, `${user.name} · ${ROLES[user.role]}`),
                    bell.button,
                    el("button", { cssClass: "button button-small", type: "button", onclick: () => signOut() }, "Sign out"))),
            el("nav", { cssClass: "tabs", role: "tablist", "aria-label": "Sections" }, tabButtons)),
        area);

    function openTab(key) {
        if (!tabs.includes(key)) {
            key = tabs[0];
        }
        if (cleanupScreen) {
            cleanupScreen();
            cleanupScreen = null;
        }
        for (const button of tabButtons) {
            button.setAttribute("aria-selected", String(button.dataset.tab === key));
        }
        history.replaceState(null, "", "#" + key);
        const result = TABS[key].open(area, user);
        if (typeof result === "function") {
            cleanupScreen = result;
        }
    }

    openTab(location.hash.substring(1));
}

start();
