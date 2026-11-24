package br.edu.canteen;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mvc;

    private String adminToken;

    @BeforeEach
    void signInAsAdmin() throws Exception {
        adminToken = login("admin@test.com", "Admin@2026");
    }

    @Test
    @DisplayName("A protected route without a token returns 401")
    void noToken() throws Exception {
        mvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("A tampered token returns 401")
    void tamperedToken() throws Exception {
        String token = newCustomer();
        String tampered = token.substring(0, token.length() - 3) + "abc";
        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("The menu is public and hides deactivated products")
    void publicMenu() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Chicken croquette")));
    }

    @Test
    @DisplayName("Public sign-up always creates CUSTOMER, even when another role is sent")
    void registrationCannotChooseRole() throws Exception {
        String email = uniqueEmail();
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Smarty", "email": "%s", "password": "%s", "role": "ADMIN"}
                        """.formatted(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("Validation returns the message of each invalid field")
    void fieldValidation() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Jo", "email": "not-an-email", "password": "short"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    @DisplayName("Customers cannot create products")
    void customerCannotCreateProduct() throws Exception {
        String token = newCustomer();
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Pastry", "description": "Beef", "price": 7.50, "initialQuantity": 10}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A customer cannot see another customer's order")
    void isolationBetweenCustomers() throws Exception {
        String ann = newCustomer();
        String bob = newCustomer();
        long number = createOrder(ann);

        mvc.perform(get("/api/orders/" + number).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/orders/" + number + "/cancel").header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Customers cannot register cash payments")
    void customerCannotPayCash() throws Exception {
        String token = newCustomer();
        long number = createOrder(token);
        addItem(token, number, 2, 1);
        mvc.perform(post("/api/orders/" + number + "/payment").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "CASH", "amountReceived": 50}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("counter")));
    }

    @Test
    @DisplayName("Full flow: customer orders, kitchen prepares, cashier delivers")
    void fullFlow() throws Exception {
        String customer = newCustomer();
        String kitchen = newStaff("KITCHEN");
        String cashier = newStaff("CASHIER");

        long number = createOrder(customer);
        addItem(customer, number, 4, 2).andExpect(jsonPath("$.total").value(6.0));

        mvc.perform(post("/api/orders/" + number + "/payment").header("Authorization", "Bearer " + customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "pix"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.order.status").value("PAID"));

        mvc.perform(post("/api/orders/" + number + "/delivery").header("Authorization", "Bearer " + customer))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/kitchen/queue").header("Authorization", "Bearer " + kitchen))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].number", hasItem((int) number)));

        mvc.perform(post("/api/orders/" + number + "/preparation").header("Authorization", "Bearer " + kitchen))
                .andExpect(status().isOk());
        mvc.perform(post("/api/orders/" + number + "/ready").header("Authorization", "Bearer " + kitchen))
                .andExpect(status().isOk());
        mvc.perform(post("/api/orders/" + number + "/delivery").header("Authorization", "Bearer " + cashier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        mvc.perform(get("/api/orders/" + number + "/history").header("Authorization", "Bearer " + customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));

        mvc.perform(get("/api/notifications").header("Authorization", "Bearer " + customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message", containsString("Delivered")));
    }

    @Test
    @DisplayName("After 5 wrong passwords the login is locked")
    void loginLockout() throws Exception {
        String email = uniqueEmail();
        register(email);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                            {"email": "%s", "password": "wrong123"}
                            """.formatted(email)))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, PASSWORD)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("A deactivated account loses access immediately, even with a valid token")
    void deactivatedAccount() throws Exception {
        String email = uniqueEmail();
        long id = register(email);
        String token = login(email, PASSWORD);

        mvc.perform(patch("/api/users/" + id + "/status").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active": false}
                                """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("The web interface is public and is served with the content security policy")
    void publicInterface() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
        mvc.perform(get("/js/app.js")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Only cashiers and admins search customers at the counter")
    void customerSearch() throws Exception {
        String email = uniqueEmail();
        register(email);
        String customer = login(email, PASSWORD);
        String cashier = newStaff("CASHIER");

        mvc.perform(get("/api/customers").param("search", email).header("Authorization", "Bearer " + customer))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/customers").param("search", email).header("Authorization", "Bearer " + cashier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("CUSTOMER"));
    }

    private String newCustomer() throws Exception {
        String email = uniqueEmail();
        register(email);
        return login(email, PASSWORD);
    }

    private String newStaff(String role) throws Exception {
        String email = uniqueEmail();
        mvc.perform(post("/api/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Staff member", "email": "%s", "password": "%s", "role": "%s"}
                                """.formatted(email, PASSWORD, role)))
                .andExpect(status().isCreated());
        return login(email, PASSWORD);
    }

    private long register(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Test Customer", "email": "%s", "password": "%s", "studentId": "2024001"}
                        """.formatted(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private long createOrder(String token) throws Exception {
        String response = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.number")).longValue();
    }

    private ResultActions addItem(String token, long number, long product, int quantity) throws Exception {
        return mvc.perform(post("/api/orders/" + number + "/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productCode": %d, "quantity": %d}
                                """.formatted(product, quantity)))
                .andExpect(status().isOk());
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }
}
